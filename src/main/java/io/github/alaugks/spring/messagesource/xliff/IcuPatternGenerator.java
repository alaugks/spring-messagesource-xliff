// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * <p>Example: a {@code plural:file_count} switch with cases {@code 0}, {@code 1}
 * and {@code other} yields
 * {@code {file_count, plural, =0 {…} =1 {…} other {…}}}.
 */
final class IcuPatternGenerator {

	private static final String PGS_NS = "urn:oasis:names:tc:xliff:pgs:1.0";

	private static final String OTHER = "other";

	private static final Set<String> PLURAL_KEYWORDS = Set.of("zero", "one", "two", "few", "many", OTHER);

	/**
	 * Generates the ICU pattern for a PGS-annotated unit.
	 *
	 * @param unit      the {@code <unit>} element.
	 * @param pgsSwitch the unit's {@code pgs:switch} value.
	 * @return the ICU pattern, or {@code ""} when no switch or segment can be
	 *         read.
	 */
	String generate(Element unit, String pgsSwitch) {
		PgsSwitch pgs = this.parseSwitch(pgsSwitch);
		List<Element> segments = this.segments(unit);
		if (pgs == null || segments.isEmpty()) {
			return "";
		}
		return this.build(pgs, segments);
	}

	/** Parses a single pgs:switch value (a type:variable token), mapping the PGS type to its ICU counterpart; returns null when malformed. */
	private PgsSwitch parseSwitch(String pgsSwitch) {
		String pgsSwitchValue = pgsSwitch.trim();
		int colon = pgsSwitchValue.indexOf(':');
		if (colon > 0 && colon < pgsSwitchValue.length() - 1) {
			return new PgsSwitch(icuType(pgsSwitchValue.substring(0, colon)), pgsSwitchValue.substring(colon + 1));
		}
		return null;
	}

	/** Maps a PGS switch type to its ICU type (gender becomes select). */
	private static String icuType(String pgsType) {
		return "gender".equals(pgsType) ? "select" : pgsType;
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
	private String build(PgsSwitch pgs, List<Element> segments) {
		StringBuilder icu = new StringBuilder();
		icu.append('{').append(pgs.variable()).append(", ").append(pgs.icuType()).append(',');
		for (Map.Entry<String, Element> branch : this.segmentsByCase(segments).entrySet()) {
			String body = this.caseText(branch.getValue());
			icu.append(' ').append(formatCase(pgs.icuType(), branch.getKey())).append(" {").append(body).append('}');
		}
		return icu.append('}').toString();
	}

	/** Maps each pgs:case value to its segment, preserving document order; the first segment wins on duplicate cases. */
	private Map<String, Element> segmentsByCase(List<Element> segments) {
		Map<String, Element> byCase = new LinkedHashMap<>();
		for (Element segment : segments) {
			byCase.putIfAbsent(this.caseValue(segment), segment);
		}
		return byCase;
	}

	/** Returns the segment's pgs:case value, defaulting to other when absent. */
	private String caseValue(Element segment) {
		String raw = segment.getAttributeNS(PGS_NS, "case").trim();
		return raw.isEmpty() ? OTHER : raw;
	}

	/** Formats a case key for ICU: numeric plural cases become exact matches (=0); CLDR plural keywords and all select cases are emitted as-is. */
	private static String formatCase(String icuType, String caseValue) {
		if ("plural".equals(icuType) && !PLURAL_KEYWORDS.contains(caseValue)) {
			return "=" + caseValue;
		}
		return caseValue;
	}

	/** Builds the ICU sub-message for a segment from its <target> (falling back to <source>). */
	private String caseText(Element segment) {
		Element content = XliffDocument.firstChildElement(segment, XliffDocument.TARGET);
		if (content == null) {
			content = XliffDocument.firstChildElement(segment, XliffDocument.SOURCE);
		}
		if (content == null) {
			return "";
		}
		StringBuilder text = new StringBuilder();
		this.appendText(content, text);
		return text.toString().trim();
	}

	/** Walks a node's children, ICU-escaping text and converting <ph disp> to ICU arguments. */
	private void appendText(Node node, StringBuilder out) {
		Node child = node.getFirstChild();
		while (child != null) {
			switch (child.getNodeType()) {
				case Node.TEXT_NODE, Node.CDATA_SECTION_NODE -> out.append(escape(child.getNodeValue()));
				case Node.ELEMENT_NODE -> this.appendElement((Element) child, out);
				default -> {
					// comments and other node types contribute nothing
				}
			}
			child = child.getNextSibling();
		}
	}

	/** Converts a <ph disp> placeholder to an ICU argument; recurses into any other element. */
	private void appendElement(Element element, StringBuilder out) {
		if ("ph".equals(XliffDocument.elementName(element))) {
			String disp = element.getAttribute("disp");
			if (!disp.isEmpty()) {
				out.append('{').append(disp).append('}');
			}
		}
		else {
			this.appendText(element, out);
		}
	}

	/** Escapes ICU MessageFormat metacharacters in literal text: apostrophes are doubled and runs containing { } # | are wrapped in apostrophes. */
	private static String escape(String text) {
		StringBuilder out = new StringBuilder(text.length());
		boolean quoted = false;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '\'') {
				quoted = closeQuote(out, quoted);
				out.append("''");
			}
			else if (c == '{' || c == '}' || c == '#' || c == '|') {
				quoted = openQuote(out, quoted);
				out.append(c);
			}
			else {
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

	/** A PGS switch resolved to its ICU type and the ICU argument variable name. */
	private record PgsSwitch(String icuType, String variable) {
	}
}
