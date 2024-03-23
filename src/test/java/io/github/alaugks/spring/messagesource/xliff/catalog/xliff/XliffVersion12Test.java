package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import io.github.alaugks.spring.messagesource.xliff.catalog.Catalog;
import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogInterface;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XliffVersion12Test {

    @Test
    void test_readXliffFile() throws ParserConfigurationException, IOException, SAXException {
        var document = new XliffDocument(TestUtilities.getDocument("fixtures/xliff12.xliff"));
        XliffVersion12 version = new XliffVersion12();
        CatalogInterface catalog = new Catalog(Locale.forLanguageTag("en"), "domain");
        Locale locale = Locale.forLanguageTag("en");
        version.read(catalog, document, "domain", locale);

        assertEquals("Hallo, Welt!", catalog.get(locale, "domain.code-1"));
        assertEquals("Dies ist ein weiterer Satz.", catalog.get(locale, "domain.code-2"));
    }
}
