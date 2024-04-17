package io.github.alaugks.spring.messagesource.xliff.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import io.github.alaugks.spring.messagesource.xliff.catalog.XliffVersion12.Identifier;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

class XliffVersion12Test {

    @Test
    void test_readXliffFile() throws ParserConfigurationException, IOException, SAXException {
        var document = new XliffDocument(TestUtilities.getDocument("fixtures/xliff12.xliff"));
        XliffVersion12 version = new XliffVersion12();
        CatalogInterface catalog = new Catalog(Locale.forLanguageTag("en"), "domain");
        Locale locale = Locale.forLanguageTag("en");
        version.read(catalog, document, "domain", locale);

        assertEquals("Hello World (Xliff Version 1.2)", catalog.get(locale, "domain.code-1"));
    }


    @Test
    void test_default_identifier() {
        var identifier = new Identifier(Arrays.asList("resname", "id"));
        assertEquals(Arrays.asList("resname", "id"), identifier.getList());
    }

    @Test
    void test_custom_identifier() {
        var identifier = new Identifier(List.of("id"));
        assertEquals(List.of("id"), identifier.getList());
    }
}
