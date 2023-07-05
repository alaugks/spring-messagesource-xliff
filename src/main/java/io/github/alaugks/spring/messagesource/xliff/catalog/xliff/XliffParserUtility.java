package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import org.w3c.dom.CharacterData;
import org.w3c.dom.*;

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
        Node sourceElement = getFirstChild(
                translationNodeElement.getElementsByTagName(elementName)
        );
        return getCharacterDataFromElement(sourceElement);
    }

    public static String getSource(Element translationNodeElement) {
        return getElementValue(translationNodeElement, "source");
    }

    public static String getTargetValue(Element translationNodeElement) {
        return getElementValue(translationNodeElement, "target");
    }

    public static NodeList getTranslationNodes(Document document, String nodeName) {
        return document.getElementsByTagName(nodeName);
    }

    public static String getCode(String id, String resname, String sourceValue) {
        //  resname|name -> id -> sourceValue

        if (resname != null) {
            return resname;
        }

        if (id != null) {
            return id;
        }

        return sourceValue;
    }

    public static String getResname(Node translationNode, String name) {
        return getAttributeValue(translationNode, name);
    }

    public static String getId(Node translationNode) {
        return getAttributeValue(translationNode, "id");
    }
}
