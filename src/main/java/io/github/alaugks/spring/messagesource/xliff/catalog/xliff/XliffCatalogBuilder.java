package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.Catalog;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceRuntimeException;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceVersionSupportException;
import io.github.alaugks.spring.messagesource.xliff.ressources.ResourcesLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public final class XliffCatalogBuilder {

    private final ResourcesLoader resourceLoader;
    private Catalog catalog;
    private List<String> translationUnitIdentifiers;
    Set<XliffInterface> supportedVersions = Set.of(
            new XliffVersion12(),
            new XliffVersion2()
    );

    public XliffCatalogBuilder(ResourcesLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public Catalog createCatalog(Catalog catalog) {
        try {
            this.catalog = catalog;
            this.readFile(resourceLoader.getTranslationFiles());
            return this.catalog;
        } catch (ParserConfigurationException | IOException e) {
            throw new XliffMessageSourceRuntimeException(e);
        }
    }

    public void setTranslationUnitIdentifiersOrdering(List<String> translationUnitIdentifiers) {
        if(null != translationUnitIdentifiers) {
            this.translationUnitIdentifiers = translationUnitIdentifiers;
        }
    }

    public XliffInterface getReader(String version) {
        for (XliffInterface xliffClass : this.supportedVersions) {
            if (xliffClass.support(version)) {
                return xliffClass;
            }
        }
        return null;
    }

    private void readFile(List<ResourcesLoader.Dto> translationFiles) throws ParserConfigurationException, IOException {

        for (ResourcesLoader.Dto translationFile : translationFiles) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document;
            try {
                document = builder.parse(translationFile.getInputStream());
            } catch (SAXException e) {
                throw new XliffMessageSourceRuntimeException(e);
            }

            Element root = document.getDocumentElement();

            // Simple test: Filter if root element not <xliff>
            if (!root.getNodeName().equals("xliff")) {
                continue;
            }

            String version = XliffReader.getAttributeValue(
                    root.getAttributes().getNamedItem("version")
            );

            XliffInterface xliffInterface = this.getReader(version);
            if (xliffInterface != null) {
                if (this.translationUnitIdentifiers != null) {
                    xliffInterface.setTranslationUnitIdentifiersOrdering(this.translationUnitIdentifiers);
                }
                xliffInterface.read(this.catalog, document, translationFile.getDomain(), translationFile.getLocale());
            } else {
                throw new XliffMessageSourceVersionSupportException(String.format("XLIFF version \"%s\" not supported.", version));
            }
        }
    }
}
