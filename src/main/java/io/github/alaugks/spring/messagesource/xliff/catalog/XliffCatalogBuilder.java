package io.github.alaugks.spring.messagesource.xliff.catalog;

import io.github.alaugks.spring.messagesource.xliff.exception.SaxErrorHandler;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceRuntimeException;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceSAXParseException.FatalError;
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
    private final List<Translation> translations;
    List<XliffVersionInterface> supportedVersions = List.of(
        new XliffVersion12(),
        new XliffVersion2()
    );

    public XliffCatalogBuilder(List<TranslationFile> translationFiles, String defaultDomain, Locale defaultLocale) {

        // TranslationFiles
        Assert.notNull(translationFiles, "List of TranslationFiles cant not be null");

        // Default Domain
        Assert.notNull(defaultDomain, "Default domain cant not be null");
        Assert.isTrue(!defaultDomain.trim().isEmpty(), "Default domain is empty");

        // Default Locale
        Assert.notNull(defaultLocale, "Default locale  cant not be null");
        Assert.isTrue(!defaultLocale.toString().trim().isEmpty(), "Default locale cant not be empty");

        this.translationFiles = translationFiles;
        this.defaultDomain = defaultDomain;
        this.defaultLocale = defaultLocale;
        this.translations = new ArrayList<>();
    }

    public BaseCatalog getBaseCatalog() {
        try {
            this.parseXliffDocuments(translationFiles);
            return BaseCatalog.builder(translations, this.defaultLocale, this.defaultDomain).build();
        } catch (ParserConfigurationException | IOException e) {
            throw new FatalError(e);
        }
    }

    private void parseXliffDocuments(List<TranslationFile> xliffFiles)
        throws ParserConfigurationException, IOException {

        for (TranslationFile xliffFile : xliffFiles) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            documentBuilder.setErrorHandler(new SaxErrorHandler());
            Document document;
            try {
                document = documentBuilder.parse(xliffFile.inputStream());
            } catch (SAXException e) {
                throw new XliffMessageSourceRuntimeException(e);
            }

            Element root = document.getDocumentElement();
            XliffDocument xliffDocument = new XliffDocument(root);

            if (!xliffDocument.isXliffDocument()) {
                continue;
            }

            String version = xliffDocument.getXliffVersion();

            XliffVersionInterface xliffSupported = this.supportedVersions
                .stream()
                .filter(o -> o.support(xliffDocument.getXliffVersion()))
                .findFirst()
                .orElse(null);

            if (xliffSupported != null) {
                xliffDocument.getTransUnits(xliffSupported.getTransUnitName(), xliffSupported.getIdentifier()).forEach(
                    transUnit -> this.translations.add(
                        new Translation(
                            xliffFile.locale(),
                            transUnit.code(),
                            transUnit.value(),
                            xliffFile.domain()
                        )
                    )
                );
            } else {
                throw new XliffMessageSourceVersionSupportException(
                    String.format("XLIFF version \"%s\" not supported.", version)
                );
            }
        }
    }

    public static final class XliffVersion2 implements XliffVersionInterface {

        @Override
        public boolean support(String version) {
            return List.of("2.0", "2.1").contains(version);
        }

        @Override
        public String getTransUnitName() {
            return "segment";
        }

        @Override
        public List<String> getIdentifier() {
            return List.of("id");
        }
    }

    public static final class XliffVersion12 implements XliffVersionInterface {

        @Override
        public boolean support(String version) {
            return version.equals("1.2");
        }

        @Override
        public String getTransUnitName() {
            return "trans-unit";
        }

        @Override
        public List<String> getIdentifier() {
            return List.of("resname", "id");
        }
    }

    public interface XliffVersionInterface {

        boolean support(String version);

        String getTransUnitName();

        List<String> getIdentifier();
    }
}
