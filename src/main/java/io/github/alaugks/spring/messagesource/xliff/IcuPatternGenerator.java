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
 * <p>Each {@code pgs:switch} ({@code plural}, {@code gender} or {@code select})
 * becomes an ICU argument; the unit's {@code <segment>}s are the cases, matched
 * via {@code pgs:case}. Multiple switches are nested in declaration order.
 * Within a case, literal text is ICU-escaped and each {@code <ph disp="..."/>}
 * placeholder becomes an ICU argument {@code {disp}}.
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
		List<PgsSwitch> switches = this.parseSwitches(pgsSwitch);
		List<Element> segments = this.segments(unit);
		if (switches.isEmpty() || segments.isEmpty()) {
			return "";
		}
		return this.build(switches, 0, segments);
	}

	/** Parses a pgs:switch value (whitespace-separated type:variable tokens), mapping each PGS type to its ICU counterpart. */
	private List<PgsSwitch> parseSwitches(String pgsSwitch) {
		List<PgsSwitch> switches = new ArrayList<>();
		for (String token : pgsSwitch.trim().split("\\s+")) {
			int colon = token.indexOf(':');
			if (colon > 0 && colon < token.length() - 1) {
				switches.add(new PgsSwitch(icuType(token.substring(0, colon)), token.substring(colon + 1)));
			}
		}
		return switches;
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

	/** Recursively builds the ICU pattern for the switch at the given level, nesting the remaining switches inside each case branch. */
	private String build(List<PgsSwitch> switches, int level, List<Element> segments) {
		PgsSwitch current = switches.get(level);
		boolean last = level + 1 == switches.size();

		StringBuilder icu = new StringBuilder();
		icu.append('{').append(current.variable()).append(", ").append(current.icuType()).append(',');
		for (Map.Entry<String, List<Element>> branch : this.groupByCase(segments, level).entrySet()) {
			String body = last
				? this.caseText(branch.getValue().get(0))
				: this.build(switches, level + 1, branch.getValue());
			icu.append(' ').append(formatCase(current.icuType(), branch.getKey())).append(" {").append(body).append('}');
		}
		return icu.append('}').toString();
	}

	/** Groups the segments by their pgs:case value at the given level, preserving document order. */
	private Map<String, List<Element>> groupByCase(List<Element> segments, int level) {
		Map<String, List<Element>> byCase = new LinkedHashMap<>();
		for (Element segment : segments) {
			byCase.computeIfAbsent(this.caseValueAt(segment, level), key -> new ArrayList<>()).add(segment);
		}
		return byCase;
	}

	/** Returns the segment's pgs:case value for the switch at the given level. */
	private String caseValueAt(Element segment, int level) {
		String raw = segment.getAttributeNS(PGS_NS, "case").trim();
		if (raw.isEmpty()) {
			return OTHER;
		}
		String[] cases = raw.split("\\s+");
		return level < cases.length ? cases[level] : OTHER;
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
