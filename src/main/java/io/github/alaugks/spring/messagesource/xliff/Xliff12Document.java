// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Reads translation units from a parsed XLIFF 1.2 document.
 *
 * <p>For each {@code <trans-unit>}: the key is the first non-empty value of
 * {@code resname}, then {@code id}; the value is the {@code <target>} text and
 * falls back to the {@code <source>} text when no target is present.
 * Units that have neither a {@code resname} nor an {@code id} are skipped.
 *
 * <p>The value is the element's text content (any embedded markup, e.g. HTML in
 * a CDATA section, is taken verbatim; XLIFF inline elements are not
 * interpreted). It is trimmed unless the element's effective {@code xml:space}
 * is {@code "preserve"}.
 */
public class Xliff12Document extends XliffDocument implements XliffDocumentInterface {

	/**
	 * Creates a reader for the given XLIFF 1.2 root element.
	 *
	 * @param root the root element of the parsed XLIFF document.
	 */
	public Xliff12Document(Element root) {
		super(root);
	}

	/**
	 * Creates a reader for the given parsed XLIFF 1.2 document.
	 *
	 * @param document the parsed XLIFF document.
	 */
	public Xliff12Document(Document document) {
		super(document);
	}

	/**
	 * Extracts the translation units from the XLIFF 1.2 document.
	 *
	 * @return ordered map of key to translated text; empty if the document is not an XLIFF document.
	 */
	public Map<String, String> getUnits() {
		Map<String, String> transUnits = new LinkedHashMap<>();
		if (this.isXliffDocument()) {
			NodeList nodeList = this.root.getElementsByTagName("trans-unit");
			for (int i = 0; i < nodeList.getLength(); i++) {
				this.addTransUnit((Element) nodeList.item(i), transUnits);
			}
		}
		return transUnits;
	}

	/**
	 * Adds the trans-unit's key and value to the map, skipping it when it has no key.
	 */
	private void addTransUnit(Element transUnit, Map<String, String> transUnits) {
		String key = this.firstNonEmpty(
				transUnit.getAttribute("resname"),
				transUnit.getAttribute("id")
		);
		if (key.isEmpty()) {
			return;
		}

		Element target = firstChildElement(transUnit, TARGET);
		Element valueElement = target != null
				? target
				: firstChildElement(transUnit, SOURCE);
		transUnits.put(key, this.value(valueElement));
	}

	  private Locale targetLanguage() {
		Element file = firstChildElement(this.root, "file");
		if (file == null) {
			return null;
		}
		String targetLanguage = file.getAttribute("target-language");
		return targetLanguage.isEmpty() ? null : Locale.forLanguageTag(targetLanguage);
	}
}

