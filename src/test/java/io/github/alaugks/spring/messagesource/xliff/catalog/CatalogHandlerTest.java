package io.github.alaugks.spring.messagesource.xliff.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CatalogHandlerTest {

    private CatalogHandler catalogHandler;
    private Locale locale;

    @BeforeEach
    void beforeEach() {
        this.locale = Locale.forLanguageTag("en");

        this.catalogHandler = new CatalogHandler(
            TestUtilities.getTestBaseCatalog(),
            null
        );
    }

    @Test
    void test_getAll() {
        assertEquals(3, this.catalogHandler.getAll().size());
    }

    @Test
    void test_get() {
        assertEquals("Hello World (messages / en)", this.catalogHandler.get(this.locale, "hello_world"));
        assertEquals("Hello World (messages / en)", this.catalogHandler.get(this.locale, "messages.hello_world"));
    }

}
