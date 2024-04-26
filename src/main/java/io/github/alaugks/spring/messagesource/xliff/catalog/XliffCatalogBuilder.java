package io.github.alaugks.spring.messagesource.xliff.catalog;

import io.github.alaugks.spring.messagesource.xliff.catalog.XliffVersionInterface.XliffIdentifierInterface;
import io.github.alaugks.spring.messagesource.xliff.exception.SaxErrorHandler;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceRuntimeException;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceSAXParseFatalErrorException;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceVersionSupportException;
import io.github.alaugks.spring.messagesource.xliff.records.Translation;
import io.github.alaugks.spring.messagesource.xliff.records.TranslationFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public final class XliffCatalogBuilder {

    private final List<TranslationFile> translationFiles;
    private final String defaultDomain;
    private final Locale defaultLocale;
    private final List<XliffVersionInterface.XliffIdentifierInterface> transUnitIdentifier;
    private final List<Translation> translations;
    List<XliffVersionInterface> supportedVersions = List.of(
        new XliffVersion12(),
        new XliffVersion2()
    );

    public XliffCatalogBuilder(
        List<TranslationFile> translationFiles,
        String defaultDomain,
        Locale defaultLocale,
        List<XliffIdentifierInterface> transUnitIdentifier
    ) {
        this.translationFiles = translationFiles;
        this.defaultDomain = defaultDomain;
        this.defaultLocale = defaultLocale;
        this.transUnitIdentifier = transUnitIdentifier;
        this.translations = new ArrayList<>();
    }

    public BaseCatalog getBaseCatalog() {
        try {
            // Default Domain
            Assert.notNull(this.defaultDomain, "Default domain is null");
            Assert.isTrue(!this.defaultDomain.trim().isEmpty(), "Default domain is empty");

            // Default Locale
            Assert.notNull(this.defaultLocale, "Default locale is null");
            Assert.isTrue(!this.defaultLocale.toString().trim().isEmpty(), "Default locale is empty");

            this.readFiles(translationFiles);
            return new BaseCatalog(translations, this.defaultLocale, this.defaultDomain).build();
        } catch (ParserConfigurationException | IOException e) {
            throw new XliffMessageSourceSAXParseFatalErrorException(e);
        }
    }

    private void readFiles(List<TranslationFile> translationTranslationFiles)
        throws ParserConfigurationException, IOException {

        for (TranslationFile translationFile : translationTranslationFiles) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            documentBuilder.setErrorHandler(new SaxErrorHandler());
            Document document;
            try {
                document = documentBuilder.parse(translationFile.inputStream());
            } catch (SAXException e) {
                throw new XliffMessageSourceRuntimeException(e);
            }

            Element root = document.getDocumentElement();
            XliffDocument xliffDocument = new XliffDocument(root);

            if (!xliffDocument.isXliffDocument()) {
                continue;
            }

            String xliffVersion = xliffDocument.getXliffVersion();

            XliffVersionInterface xliffReader = this.supportedVersions
                .stream()
                .filter(o -> o.support(xliffVersion))
                .findFirst()
                .orElse(null);

            if (xliffReader != null) {
                xliffReader.setTransUnitIdentifier(this.transUnitIdentifier);
                xliffReader.read(this.translations, xliffDocument, translationFile.domain(), translationFile.locale());
            } else {
                throw new XliffMessageSourceVersionSupportException(
                    String.format("XLIFF version \"%s\" not supported.", xliffVersion)
                );
            }
        }
    }
}
