package io.github.alaugks.spring.messagesource.xliff.catalog;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CatalogHandlerTest {

    private CatalogHandler catalogHandler;
    private Locale locale;

    @BeforeEach
    void beforeEach() {
        this.locale = Locale.forLanguageTag("en");
        this.catalogHandler = new CatalogHandler(
                CatalogBuilder
                        .builder(TestUtilities.getResourcesLoader())
                        .build(),
                Locale.forLanguageTag("en"),
                "messages",
                TestUtilities.getMockedCacheManager()
        );
    }

    @Test
    void test_put() {
        this.catalogHandler.put(this.locale, "my-test-code", "my-test-value");
        assertEquals("my-test-value", this.catalogHandler.get(locale, "my-test-code").toString());
    }

    @Test
    void test_initCache() {
        assertEquals(0, this.catalogHandler.getAll().size());
        this.catalogHandler.initCache();
        assertEquals(3, this.catalogHandler.getAll().size());
    }

    @Test
    void test_get() {
        assertEquals("Hello EN (messages)", this.catalogHandler.get(this.locale, "hello_language").toString());
        assertEquals("Hello EN (messages)", this.catalogHandler.get(this.locale, "messages.hello_language").toString());
    }

    @Test
    void test_get_notExists() {
        assertEquals("not-exists-id", this.catalogHandler.get(this.locale, "not-exists-id").toString());
    }

}
