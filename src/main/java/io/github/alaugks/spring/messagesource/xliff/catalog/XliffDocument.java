package io.github.alaugks.spring.messagesource.xliff.catalog;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class XliffDocument {

    private final Element root;
    private List<String> identifiers;
    private NodeList nodeList;

    public XliffDocument(Element root) {
        this.root = root;
    }

    public XliffDocument(Document document) {
        this.root = document.getDocumentElement();
    }

    public Set<TransUnit> getTransUnits(String transUnitName, List<String> translationUnitIdentifiers) {
        this.nodeList = this.root.getElementsByTagName(transUnitName);
        this.identifiers = translationUnitIdentifiers;
        return this.getNodes();
    }

    public boolean isXliffDocument() {
        // Simple test: Filter if root element <xliff>
        return root.getNodeName().equals("xliff");
    }

    public String getXliffVersion() {
        return this.getAttributeValue(
            root.getAttributes().getNamedItem("version")
        );
    }

    private Set<TransUnit> getNodes() {
        Set<TransUnit> transUnits = new HashSet<>();

        for (int item = 0; item < nodeList.getLength(); item++) {
            Element node = (Element) nodeList.item(item);

            String code = this.getCode(node);

            if (code != null) {
                transUnits.add(
                    new TransUnit(
                        code,
                        getCharacterDataFromElement(
                            node.getElementsByTagName("target").item(0).getFirstChild()
                        )
                    )
                );
            }
        }

        return transUnits;
    }

    private String getCode(Element translationUnit) {
        if (this.identifiers != null) {
            for (String name : this.identifiers) {
                String value = this.getAttributeValue(translationUnit, name);
                if (value != null) {
                    return value;
                }
            }
        }

        return null;
    }

    private String getCharacterDataFromElement(Node child) {
        if (child instanceof CharacterData node) {
            if (child.getNextSibling() != null) {
                return child.getNextSibling().getTextContent().trim();
            }
            return node.getData().trim();
        }
        return null;
    }

    private String getAttributeValue(Node translationNode, String attributeName) {
        return this.getAttributeValue(
            translationNode.getAttributes().getNamedItem(attributeName)
        );
    }

    private String getAttributeValue(Node node) {
        if (node != null) {
            return node.getNodeValue();
        }
        return null;
    }

    public record TransUnit(String code, String value) {

    }

}
