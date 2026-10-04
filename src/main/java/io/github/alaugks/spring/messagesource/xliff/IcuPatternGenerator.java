// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceRuntimeException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Generates an ICU MessageFormat pattern from an XLIFF 2.2 unit annotated with
 * the Plural, Gender, and Select (PGS) Module.
 *
 * <p>The unit's single {@code pgs:switch} ({@code plural}, {@code gender} or
 * {@code select}) becomes an ICU argument; the unit's {@code <segment>}s are the
 * cases, matched via {@code pgs:case}. Within a case, literal text is
 * ICU-escaped and each {@code <ph disp="..."/>} placeholder becomes an ICU
 * argument {@code {disp}}.
 *
 * <p>Example: a {@code plural:count} switch with cases {@code 0}, {@code 1}
 * and {@code other} yields
 * {@code {count, plural, =0 {…} =1 {…} other {…}}}.
 */
class IcuPatternGenerator {

	private static final String PGS_NS = "urn:oasis:names:tc:xliff:pgs:1.0";

	private static final String OTHER = "other";

	private static final List<String> PLURAL_KEYWORDS = List.of("zero", "one", "two", "few", "many", OTHER);

	private static final List<String> ICU_TYPES = List.of("plural", "select");

	/** Argument names and select keys: letters, digits and underscore only, so they cannot alter the ICU structure. */
	private static final Pattern ICU_IDENTIFIER = Pattern.compile("[\\p{L}\\p{N}_]+");

	private static final Pattern EXACT_NUMBER = Pattern.compile("\\d+");

	/**
	 * Generates the ICU pattern for a PGS-annotated unit.
	 *
	 * @param unit      the {@code <unit>} element.
	 * @param pgsSwitch the unit's {@code pgs:switch} value.
	 * @return the ICU pattern, or {@code ""} when the unit has no segment.
	 * @throws XliffMessageSourceRuntimeException when the switch is malformed,
	 *         its type, a variable name, a case or a placeholder cannot be
	 *         expressed as valid ICU syntax, a segment has no or a duplicate
	 *         {@code pgs:case}, or the mandatory {@code other} case is
	 *         missing.
	 */
	String generate(Element unit, String pgsSwitch) {
		List<Element> segments = this.segments(unit);

		if (segments.isEmpty()) {
			return "";
		}

		return this.build(unit, this.parseSwitch(unitName(unit), pgsSwitch), segments);
	}

	/** Parses a single pgs:switch value (a type:variable token), mapping the PGS type to its ICU counterpart; fails when malformed. */
	private PgsSwitch parseSwitch(String unitName, String pgsSwitch) {
		String pgsSwitchValue = pgsSwitch.trim();
		int colon = pgsSwitchValue.indexOf(':');

		if (colon > 0 && colon < pgsSwitchValue.length() - 1) {
			return new PgsSwitch(icuType(unitName, pgsSwitchValue.substring(0, colon)), pgsSwitchValue.substring(colon + 1));
		}

		throw invalid(unitName, "malformed pgs:switch '" + pgsSwitch + "', expected type:variable");
	}

	/** Maps a PGS switch type to its ICU type (gender becomes select); rejects unsupported types. */
	private static String icuType(String unitName, String pgsType) {
		String icuType = "gender".equals(pgsType) ? "select" : pgsType;

		if (!ICU_TYPES.contains(icuType)) {
			throw invalid(unitName, "unsupported pgs:switch type '" + pgsType + "'");
		}

		return icuType;
	}

	/** Collects the unit's <segment> child elements in document order. */
	private List<Element> segments(Element unit) {
		List<Element> segments = new ArrayList<>();
		Node child = unit.getFirstChild();

		while (child != null) {
			if (child.getNodeType() == Node.ELEMENT_NODE && "segment".equals(XliffDocument.elementName(child))) {
				segments.add((Element) child);
			}
			child = child.getNextSibling();
		}

		return segments;
	}

	/** Builds the ICU pattern for the switch, emitting one case branch per segment. */
	private String build(Element unit, PgsSwitch pgs, List<Element> segments) {
		String unitName = unitName(unit);
		requireIdentifier(unitName, "pgs:switch variable", pgs.variable());
		Map<String, Element> cases = this.segmentsByCase(unitName, segments);
		StringBuilder icu = new StringBuilder();
		icu.append('{').append(pgs.variable()).append(", ").append(pgs.icuType()).append(',');

		for (Map.Entry<String, Element> branch : cases.entrySet()) {
			String body = this.caseText(unitName, branch.getValue());
			icu.append(' ').append(formatCase(unitName, pgs.icuType(), branch.getKey())).append(" {").append(body).append('}');
		}

		if (!cases.containsKey(OTHER)) {
			throw invalid(unitName, "missing mandatory 'other' case");
		}

		return icu.append('}').toString();
	}

	/** Maps each pgs:case value to its segment, preserving document order; a duplicate case is rejected. */
	private Map<String, Element> segmentsByCase(String unitName, List<Element> segments) {
		Map<String, Element> byCase = new LinkedHashMap<>();

		for (Element segment : segments) {
			String caseValue = this.caseValue(unitName, segment);
			if (byCase.putIfAbsent(caseValue, segment) != null) {
				throw invalid(unitName, "duplicate pgs:case '" + caseValue + "'");
			}
		}

		return byCase;
	}

	/** Returns the segment's pgs:case value; a missing value is rejected. */
	private String caseValue(String unitName, Element segment) {
		String raw = segment.getAttributeNS(PGS_NS, "case").trim();

		if (raw.isEmpty()) {
			throw invalid(unitName, "segment without pgs:case");
		}

		return raw;
	}

	/** Formats a case key for ICU: numeric plural cases become exact matches (=0); CLDR plural keywords and all select cases are emitted as-is. */
	private static String formatCase(String unitName, String icuType, String caseValue) {
		if ("plural".equals(icuType)) {
			if (PLURAL_KEYWORDS.contains(caseValue)) {
				return caseValue;
			}
			if (!EXACT_NUMBER.matcher(caseValue).matches()) {
				throw invalid(unitName, "invalid plural pgs:case '" + caseValue + "'");
			}
			return "=" + caseValue;
		}

		requireIdentifier(unitName, "select pgs:case", caseValue);

		return caseValue;
	}

	/** Builds the ICU sub-message for a segment from its <target> (falling back to <source>). */
	private String caseText(String unitName, Element segment) {
		Element content = XliffDocument.firstChildElement(segment, XliffDocument.TARGET);

		if (content == null) {
			content = XliffDocument.firstChildElement(segment, XliffDocument.SOURCE);
		}

		if (content == null) {
			return "";
		}

		StringBuilder text = new StringBuilder();
		this.appendText(unitName, content, text);

		return text.toString().trim();
	}

	/** Walks a node's children, ICU-escaping text and converting <ph disp> to ICU arguments. */
	private void appendText(String unitName, Node node, StringBuilder out) {
		Node child = node.getFirstChild();

		while (child != null) {
			switch (child.getNodeType()) {
				case Node.TEXT_NODE, Node.CDATA_SECTION_NODE -> out.append(escape(child.getNodeValue()));
				case Node.ELEMENT_NODE -> this.appendElement(unitName, (Element) child, out);
				default -> {
					// comments and other node types contribute nothing
				}
			}
			child = child.getNextSibling();
		}
	}

	/** Converts a <ph disp> placeholder to an ICU argument; recurses into any other element. */
	private void appendElement(String unitName, Element element, StringBuilder out) {
		if ("ph".equals(XliffDocument.elementName(element))) {
			String disp = element.getAttribute("disp");
			if (!disp.isEmpty()) {
				requireIdentifier(unitName, "<ph disp>", disp);
				out.append('{').append(disp).append('}');
			}
		} else {
			this.appendText(unitName, element, out);
		}
	}

	/** Escapes ICU MessageFormat metacharacters in literal text: apostrophes are doubled and runs containing { } # are wrapped in apostrophes. */
	private static String escape(String text) {
		StringBuilder out = new StringBuilder(text.length());
		boolean quoted = false;

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '\'') {
				quoted = closeQuote(out, quoted);
				out.append("''");
			} else if (c == '{' || c == '}' || c == '#') {
				quoted = openQuote(out, quoted);
				out.append(c);
			} else {
				quoted = closeQuote(out, quoted);
				out.append(c);
			}
		}
		closeQuote(out, quoted);

		return out.toString();
	}

	/** Opens an ICU quote run if not already open; returns the new quoted state. */
	private static boolean openQuote(StringBuilder out, boolean quoted) {
		if (!quoted) {
			out.append('\'');
		}

		return true;
	}

	/** Closes an ICU quote run if open; returns the new quoted state. */
	private static boolean closeQuote(StringBuilder out, boolean quoted) {
		if (quoted) {
			out.append('\'');
		}

		return false;
	}

	/** Rejects values that are not a plain ICU identifier. */
	private static void requireIdentifier(String unitName, String what, String value) {
		if (!ICU_IDENTIFIER.matcher(value).matches()) {
			throw invalid(unitName, "invalid " + what + " '" + value + "'");
		}
	}

	/** Retrieves the name of a unit element. If the "name" attribute is empty. Fallback to the value of the "id" attribute.*/
	private static String unitName(Element unit) {
		String name = unit.getAttribute("name");
		return name.isEmpty() ? unit.getAttribute("id") : name;
	}

	/**
	 * Creates and returns a new {@link XliffMessageSourceRuntimeException} with a detailed error message.
	 * The error message specifies the unit name and the reason for the failure.
	 */
	private static XliffMessageSourceRuntimeException invalid(String unitName, String reason) {
		return new XliffMessageSourceRuntimeException("Cannot generate ICU pattern for unit '" + unitName + "': " + reason);
	}

	/** A PGS switch resolved to its ICU type and the ICU argument variable name. */
	private record PgsSwitch(String icuType, String variable) {

	}
}
