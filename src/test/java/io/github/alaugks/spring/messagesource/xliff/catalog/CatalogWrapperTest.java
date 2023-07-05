package io.github.alaugks.spring.messagesource.xliff.catalog;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CatalogWrapperTest {

    private Locale locale;
    private CatalogWrapper catalogWrapper;

    @BeforeEach
    void beforeEach() {
        this.locale = Locale.forLanguageTag("en");
        this.catalogWrapper = TestUtilities.getCacheWrapperWithCachedTestCatalog();
    }

    @Test
    void test_get() {
        assertEquals("Hello EN (messages)", this.catalogWrapper.get(locale, "hello_language").toString());
        // again
        assertEquals("Hello EN (messages)", this.catalogWrapper.get(locale, "messages.hello_language").toString());
        // again
        assertEquals("Hello EN (messages)", this.catalogWrapper.get(locale, "hello_language").toString());
        // again
        assertEquals("Hello EN (messages)", this.catalogWrapper.get(locale, "messages.hello_language").toString());
    }

    @Test
    void test_put() {
        this.catalogWrapper.setDefaultDomain("foo");

        this.catalogWrapper.put(locale, "foo", "code", "foo_value");
        assertEquals("foo_value", this.catalogWrapper.get(locale, "code").toString());

        this.catalogWrapper.put(locale, "bar", "code", "bar_value");
        assertEquals("bar_value", this.catalogWrapper.get(locale, "bar.code").toString());
    }
}
