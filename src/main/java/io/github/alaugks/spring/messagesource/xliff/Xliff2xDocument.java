// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Reads translation units from a parsed XLIFF 2.0/2.1/2.2 document.
 *
 * <p>For each {@code <unit>}: the key is the first non-empty value of
 * {@code name}, then {@code id}; the value reconstructs the unit's translated
 * text by concatenating, in document order, the {@code <target>} text (falling
 * back to {@code <source>}) of each {@code <segment>} and {@code <ignorable>}
 * — as XLIFF prescribes for segmented content (Segmentation, Section 4.8).
 * Units that have neither a {@code name} nor an {@code id} are skipped.
 *
 * <p>A unit carrying the XLIFF 2.2 Plural, Gender, and Select (PGS) Module
 * (a {@code pgs:switch} attribute) is instead converted to an ICU MessageFormat
 * pattern by {@link IcuPatternGenerator}; that pattern becomes the unit's value.
 *
 * <p>Each element's text content is taken verbatim (embedded markup, e.g. HTML
 * in a CDATA section, is preserved; XLIFF inline elements are not interpreted).
 * The concatenated value is trimmed unless the unit, or one of its elements,
 * declares an effective {@code xml:space} of {@code "preserve"}.
 */
public class Xliff2xDocument extends XliffDocument implements XliffDocumentInterface {

	/** Namespace of the XLIFF 2.2 Plural, Gender, and Select (PGS) Module. */
	private static final String PGS_NS = "urn:oasis:names:tc:xliff:pgs:1.0";

	private final IcuPatternGenerator icuPatternGenerator = new IcuPatternGenerator();

	/**
	 * Creates a reader for the given XLIFF 2.0/2.1/2.2 root element.
	 *
	 * @param root the root element of the parsed XLIFF document.
	 */
	public Xliff2xDocument(Element root) {
		super(root);
	}

	/**
	 * Creates a reader for the given parsed XLIFF 2.0/2.1/2.2 document.
	 *
	 * @param document the parsed XLIFF document.
	 */
	public Xliff2xDocument(Document document) {
		super(document);
	}

	/**
	 * Extracts the translation units from the XLIFF 2.0/2.1/2.2 document.
	 *
	 * @return ordered map of key to translated text; empty if the document is
	 *         not an XLIFF document.
	 */
	@Override
	public Map<String, String> getUnits() {
		Map<String, String> transUnits = new LinkedHashMap<>();
		if (this.isXliffDocument()) {
			NodeList units = this.root.getElementsByTagName("unit");
			for (int i = 0; i < units.getLength(); i++) {
				this.addUnit((Element) units.item(i), transUnits);
			}
		}
		return transUnits;
	}

	/**
	 * Adds the unit's key and value to the map, skipping it when it has no key
	 * or no segments. PGS-annotated units are converted to an ICU pattern.
	 */
	private void addUnit(Element unit, Map<String, String> transUnits) {
		String key = this.firstNonEmpty(unit.getAttribute("name"), unit.getAttribute("id"));
		if (key.isEmpty()) {
			return;
		}

		String pgsSwitch = unit.getAttributeNS(PGS_NS, "switch");
		if (!pgsSwitch.isEmpty()) {
			String icu = this.icuPatternGenerator.generate(unit, pgsSwitch);
			if (!icu.isEmpty()) {
				transUnits.put(key, icu);
			}
			return;
		}

		List<Element> elements = this.getSegmentNodes(unit);
		if (elements.isEmpty()) {
			return;
		}

		String assembled = this.concatSegments(elements);
		transUnits.put(key, this.isPreservesWhitespace(elements) ? assembled : assembled.trim());
	}

	/**
	 * Returns whether any element's source or target declares
	 * xml:space="preserve".
	 */
	private boolean isPreservesWhitespace(List<Element> elements) {
		for (Element element : elements) {
			if (this.isPreserveSpace(firstChildElement(element, TARGET))
				|| this.isPreserveSpace(firstChildElement(element, SOURCE))
			) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Collects the unit's <segment> and <ignorable> child elements in document
	 * order.
	 */
	private List<Element> getSegmentNodes(Element unit) {
		List<Element> segments = new ArrayList<>();
		NodeList nodes = unit.getChildNodes();

		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && this.isSegmentOrIgnorable(node)) {
				segments.add((Element) node);
			}
		}

		return segments;
	}

	/**
	 * Concatenates each segment's target text (falling back to source) and each
	 * ignorable's source text in document order.
	 */
	private String concatSegments(List<Element> elements) {
		StringBuilder value = new StringBuilder();
		for (Element element : elements) {
			if (this.isSegment(element)) {
				Element target = firstChildElement(element, TARGET);
				value.append(this.rawValue(target != null ? target : firstChildElement(element, SOURCE)));
			}
			else {
				value.append(this.rawValue(firstChildElement(element, SOURCE)));
			}
		}
		return value.toString();
	}

	/**
	 * Returns whether the node is a <segment> element.
	 */
	private boolean isSegment(Node node) {
		return "segment".equals(elementName(node));
	}

	/**
	 * Returns whether the node is a <segment> or <ignorable> element.
	 */
	private boolean isSegmentOrIgnorable(Node node) {
		return this.isSegment(node) || "ignorable".equals(elementName(node));
	}
}