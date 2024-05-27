package io.github.alaugks.spring.messagesource.xliff;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class XliffDocument {

    private final Element root;
    private List<String> transUnitIdentifiers;
    private NodeList nodeList;

    public XliffDocument(Element root) {
        this.root = root;
    }

    public XliffDocument(Document document) {
        this.root = document.getDocumentElement();
    }

    public Set<TransUnit> getTransUnits(String transUnitName, List<String> transUnitIdentifiers) {
        this.nodeList = this.root.getElementsByTagName(transUnitName);
        this.transUnitIdentifiers = transUnitIdentifiers;
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
            Arrays.stream(this.transUnitIdentifiers.toArray())
                .map(value -> this.getAttributeValue(
                    node.getAttributes().getNamedItem(value.toString())
                ))
                .filter(Objects::nonNull)
                .findFirst().ifPresent(code -> transUnits.add(
                    new TransUnit(
                        code,
                        getCharacterDataFromElement(
                            node.getElementsByTagName("target").item(0).getFirstChild()
                        )
                    )
                ));
        }

        return transUnits;
    }

    private String getCharacterDataFromElement(Node child) {
        if (child.getNextSibling() != null) {
            return child.getNextSibling().getTextContent().trim();
        }
        return child.getNodeValue().trim();
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
