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
            CatalogBuilder
                .builder(TestUtilities.getResourcesLoader())
                .build(),
            TestUtilities.getCache(), Locale.forLanguageTag("en"),
            "messages"
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
        assertEquals("Hello World (messages / en)", this.catalogHandler.get(this.locale, "hello_world").toString());
        assertEquals("Hello World (messages / en)",
            this.catalogHandler.get(this.locale, "messages.hello_world").toString());
    }

    @Test
    void test_get_notExists() {
        assertEquals("not-exists-id", this.catalogHandler.get(this.locale, "not-exists-id").toString());
    }

}
