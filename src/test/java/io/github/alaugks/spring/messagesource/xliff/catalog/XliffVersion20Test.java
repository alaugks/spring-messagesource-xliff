package io.github.alaugks.spring.messagesource.xliff.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import io.github.alaugks.spring.messagesource.xliff.catalog.XliffVersion2.Identifier;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

class XliffVersion20Test {

    @Test
    void test_readXliffFile() throws ParserConfigurationException, IOException, SAXException {
        var document = new XliffDocument(TestUtilities.getDocument("fixtures/xliff20.xliff"));
        XliffVersion2 version = new XliffVersion2();
        CatalogInterface catalog = new Catalog(Locale.forLanguageTag("en"), "domain");
        Locale locale = Locale.forLanguageTag("en");
        version.read(catalog, document, "domain", locale);

        assertEquals("Hello World (Xliff Version 2.0)", catalog.get(locale, "domain.code-1"));
    }

    @Test
    void test_default_identifier() {
        var identifier = new Identifier(List.of("id"));
        assertEquals(List.of("id"), identifier.getList());
    }

    @Test
    void test_custom_identifier() {
        var identifier = new Identifier(List.of("resname", "id"));
        assertEquals(List.of("resname", "id"), identifier.getList());
    }
}
