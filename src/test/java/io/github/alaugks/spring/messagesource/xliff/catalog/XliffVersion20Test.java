package io.github.alaugks.spring.messagesource.xliff.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import io.github.alaugks.spring.messagesource.xliff.catalog.XliffVersion2.Identifier;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class XliffVersion20Test {

    @Test
    void test_readXliffFile() {
        var baseCatalog = TestUtilities.getTestBaseCatalog("fixtures/xliff20.xliff", Locale.forLanguageTag("en"),
            "domain");

        assertEquals("Hello World (Xliff Version 2.0)", baseCatalog.get(Locale.forLanguageTag("en"), "domain.code-1"));
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
