package io.github.alaugks.spring.messagesource.xliff.catalog;

import io.github.alaugks.spring.messagesource.xliff.catalog.xliff.XliffDocument;
import io.github.alaugks.spring.messagesource.xliff.catalog.xliff.XliffVersion12;
import io.github.alaugks.spring.messagesource.xliff.catalog.xliff.XliffVersion2;
import io.github.alaugks.spring.messagesource.xliff.catalog.xliff.XliffVersionInterface;
import io.github.alaugks.spring.messagesource.xliff.exception.SaxErrorHandler;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceRuntimeException;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceSAXParseFatalErrorException;
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

public final class CatalogBuilder {

    private final ResourcesLoader resourceLoader;
    private Catalog catalog;
    private final List<String> translationUnitIdentifiers;
    Set<XliffVersionInterface> supportedVersions = Set.of(
            new XliffVersion12(),
            new XliffVersion2()
    );

    public CatalogBuilder(Builder builder) {
        this.resourceLoader = builder.resourceLoader;
        this.translationUnitIdentifiers = builder.translationUnitIdentifiers;
    }

    public static Builder builder(ResourcesLoader resourceLoader) {
        return new Builder(resourceLoader);
    }

    public static final class Builder {

        private final ResourcesLoader resourceLoader;
        private List<String> translationUnitIdentifiers;

        private Builder(ResourcesLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
        }

        public Builder translationUnitIdentifiersOrdering(List<String> translationUnitIdentifiers) {
            if (translationUnitIdentifiers != null) {
                this.translationUnitIdentifiers = translationUnitIdentifiers;
            }
            return this;
        }

        public CatalogBuilder build() {
            return new CatalogBuilder(this);
        }

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

    public XliffVersionInterface getReader(String version) {
        for (XliffVersionInterface xliffClass : this.supportedVersions) {
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
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            documentBuilder.setErrorHandler(new SaxErrorHandler());
            Document document;
            try {
                document = documentBuilder.parse(translationFile.getInputStream());
            } catch (SAXException e) {
                throw new XliffMessageSourceSAXParseFatalErrorException(e);
            }

            Element root = document.getDocumentElement();
            XliffDocument xliffDocument = new XliffDocument(root);

            if (!xliffDocument.isXliffDocument()) {
                continue;
            }

            String xliffVersion = xliffDocument.getXliffVersion();

            XliffVersionInterface xliffReader = this.getReader(xliffVersion);
            if (xliffReader != null) {
                if (this.translationUnitIdentifiers != null) {
                    xliffReader.setUnitIdentifiersOrdering(this.translationUnitIdentifiers);
                }
                xliffReader.read(this.catalog, xliffDocument, translationFile.getDomain(), translationFile.getLocale());
            } else {
                throw new XliffMessageSourceVersionSupportException(
                        String.format("XLIFF version \"%s\" not supported.", xliffVersion)
                );
            }
        }
    }
}
