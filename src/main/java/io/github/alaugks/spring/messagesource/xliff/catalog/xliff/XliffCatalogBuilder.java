package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogBuilderInterface;
import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogInterface;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceRuntimeException;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceVersionSupportException;
import io.github.alaugks.spring.messagesource.xliff.ressources.ResourcesLoader;
import io.github.alaugks.spring.messagesource.xliff.ressources.ResourcesLoaderInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XliffCatalogBuilder implements CatalogBuilderInterface {

    private CatalogInterface catalog;
    private List<String> translationUnitIdentifiers;

    @Override
    public CatalogInterface createCatalog(ResourcesLoaderInterface resourceLoader, CatalogInterface catalog) {
        try {
            this.catalog = catalog;
            this.readFile(resourceLoader.getResourcesInputStream());
            return this.catalog;
        } catch (ParserConfigurationException | IOException e) {
            throw new XliffMessageSourceRuntimeException(e);
        }
    }

    public void setTranslationUnitIdentifiersOrdering(List<String> translationUnitIdentifiers) {
        this.translationUnitIdentifiers = translationUnitIdentifiers;
    }

    private void readFile(ArrayList<ResourcesLoader.Dto> translationFiles) throws ParserConfigurationException, IOException {

        XliffReader xliffReader = new XliffReader();

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

            String version = XliffParserUtility.getAttributeValue(
                    root.getAttributes().getNamedItem("version")
            );

            XliffInterface xliffInterface = xliffReader.getReader(version);
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
