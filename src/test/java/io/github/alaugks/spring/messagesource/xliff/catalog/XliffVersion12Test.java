package io.github.alaugks.spring.messagesource.xliff.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class XliffVersion12Test {

    @Test
    void test_readXliffFile() {
        var baseCatalog = TestUtilities.getTestBaseCatalog(
            "fixtures/xliff12.xliff",
            Locale.forLanguageTag("en"),
            "domain"
        );

        assertEquals("Hello World (Xliff Version 1.2)", baseCatalog.get(Locale.forLanguageTag("en"), "domain.code-1"));
    }
}
