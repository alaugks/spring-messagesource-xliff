package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import io.github.alaugks.spring.messagesource.catalog.resources.ResourceLoaderBuilder;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

public class TestHelper {

    public static XliffCatalog getXliffCatalog(List<String> locationPatterns, Locale locale, boolean validateSchema) {
        return new XliffCatalog(
            ResourceLoaderBuilder.builder(locale, locationPatterns)
                .fileExtensions(List.of("xlf", "xliff"))
                .build()
                .getTranslationFiles(),
            validateSchema
        );
    }

    public static XliffCatalog getXliffCatalog(List<String> locationPatterns, Locale locale) {
        return getXliffCatalog(locationPatterns, locale, true);
    }

    public static String findInTransUnits(List<TransUnitInterface> transUnits, String locale, String code) {
        return transUnits
                .stream()
                .filter(t -> t.locale().toString().equals(locale) && t.code().equals(code))
                .findFirst()
                .get().value();
    }

    public static Document parseDocument(String xml) {
        return parseDocument(xml, false);
    }

    public static Document parseDocument(String xml, boolean validateSchema) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document document = factory.newDocumentBuilder().parse(
                new ByteArrayInputStream(xml.strip().getBytes(StandardCharsets.UTF_8))
            );

            if (validateSchema) {
                new XliffSchemaValidator().validate(
                    document,
                    document.getDocumentElement().getAttribute("version")
                );

            }

            return document;
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
