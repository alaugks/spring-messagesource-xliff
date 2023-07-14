package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import org.w3c.dom.CharacterData;
import org.w3c.dom.*;

import java.util.List;

final class XliffParserUtility {

    private XliffParserUtility() {
        throw new IllegalStateException("XliffParserUtility class");
    }

    static Node getFirstChild(NodeList nodeList) {
        return nodeList.item(0).getFirstChild();
    }

    public static String getAttributeValue(Node node) {
        if (node != null) {
            return node.getNodeValue();
        }
        return null;
    }

    private static String getCharacterDataFromElement(Node child) {
        if (child instanceof CharacterData) {
            if (child.getNextSibling() != null) {
                return child.getNextSibling().getTextContent().trim();
            }
            return ((CharacterData) child).getData().trim();
        }
        return null;
    }

    public static String getAttributeValue(Node translationNode, String attributeName) {
        return getAttributeValue(
                translationNode.getAttributes().getNamedItem(attributeName)
        );
    }

    public static String getElementValue(Element translationNodeElement, String elementName) {
        return getCharacterDataFromElement(
                getFirstChild(
                        translationNodeElement.getElementsByTagName(elementName)
                )
        );
    }

    public static String getTargetValue(Element translationNodeElement) {
        return getElementValue(translationNodeElement, "target");
    }

    public static NodeList getTranslationUnits(Document document, String nodeName) {
        return document.getElementsByTagName(nodeName);
    }

    public static String getCode(
            Element translationUnit,
            List<String> translationUnitIdentifiers
    ) {
        if (translationUnitIdentifiers != null) {
            for (String name : translationUnitIdentifiers) {
                String value = getAttributeValue(translationUnit, name);
                if (value != null) {
                    return value;
                }
            }
        }

        return null;
    }
}
