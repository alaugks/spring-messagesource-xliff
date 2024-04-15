package io.github.alaugks.spring.messagesource.xliff.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import java.io.IOException;
import java.util.Locale;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

class XliffVersion21Test {

    @Test
    void test_readXliffFile() throws ParserConfigurationException, IOException, SAXException {
        var document = new XliffDocument(TestUtilities.getDocument("fixtures/xliff21.xliff"));
        XliffVersion2 version = new XliffVersion2();
        CatalogInterface catalog = new Catalog(Locale.forLanguageTag("en"), "domain");
        Locale locale = Locale.forLanguageTag("en");
        version.read(catalog, document, "domain", locale);

        assertEquals("Hello World (Xliff Version 2.1)", catalog.get(locale, "domain.code-1"));
    }

}
