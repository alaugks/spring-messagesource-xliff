package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import io.github.alaugks.spring.messagesource.xliff.catalog.Catalog;
import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Xliff21Test {

    private Document document;

    @BeforeEach
    void beforeEach() throws ParserConfigurationException, IOException, SAXException {
        String filePath = "fixtures/xliff21.xliff";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
        this.document = TestUtilities.getDocument(inputStream);
    }

    @Test
    void test_readXliffFile() {
        Xliff2 version = new Xliff2();
        CatalogInterface catalog = new Catalog();
        Locale locale = Locale.forLanguageTag("en");
        version.read(catalog, document, "domain", locale);

        assertEquals("Hallo, Welt!", catalog.get(locale, "domain.code-1"));
        //assertEquals("Dies ist ein weiterer Satz.", catalog.get(locale, "code-2"));
    }
}
