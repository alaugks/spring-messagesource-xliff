package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceRuntimeException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class XliffMatchingResourcePatternResolverCasesTest {

    @Test
    void test_withRegion_setDefaultLocale_notSet() {
        var resolver = new XliffTranslationMessageSource(TestUtilities.getMockedCacheManager());
        resolver.setBasenamePattern("translations/*");

        XliffMessageSourceRuntimeException exception = assertThrows(
                XliffMessageSourceRuntimeException.class, resolver::initCache
        );
        assertEquals("Default language is not set or empty.", exception.getMessage());
    }

    @Test
    void test_withRegion_setDefaultLocale_empty() {
        var resolver = new XliffTranslationMessageSource(TestUtilities.getMockedCacheManager());
        resolver.setBasenamePattern("translations/*");
        resolver.setDefaultLocale(Locale.forLanguageTag(""));

        XliffMessageSourceRuntimeException exception = assertThrows(
                XliffMessageSourceRuntimeException.class, resolver::initCache
        );
        assertEquals("Default language is not set or empty.", exception.getMessage());
    }

    @Test
    void test_withRegion_enUS() {
        var resolver = new XliffTranslationMessageSource(TestUtilities.getMockedCacheManager());
        resolver.setBasenamePattern("translations/*");
        resolver.setDefaultLocale(Locale.forLanguageTag("en"));
        resolver.initCache();
        String message = resolver.getMessage(
                "hello_language",
                null,
                Locale.forLanguageTag("en-US")
        );
        assertEquals("Hello EN_US (messages)", message);
    }

    @Test
    void test_withRegion_fallback() {
        var resolver = new XliffTranslationMessageSource(TestUtilities.getMockedCacheManager());
        resolver.setBasenamePattern("translations/*");
        resolver.setDefaultLocale(Locale.forLanguageTag("en"));
        resolver.initCache();
        String message = resolver.getMessage(
                "hello_language",
                null,
                Locale.forLanguageTag("en-GB")
        );

        assertEquals("Hello EN (messages)", message);
    }

    @Test
    void test_setSefaultDomain() {
        var resolver = new XliffTranslationMessageSource(TestUtilities.getMockedCacheManager());
        resolver.setBasenamePattern("translations/*");
        resolver.setDefaultLocale(Locale.forLanguageTag("en"));
        resolver.setDefaultDomain("otherdomain");
        resolver.initCache();
        String message = resolver.getMessage(
                "hello_language",
                null,
                Locale.forLanguageTag("en-US")
        );
        assertEquals("Other Hello EN (otherdomain)", message);
    }

    @Test
    void test_setBasenamePattern() {
        var resolver = new XliffTranslationMessageSource(TestUtilities.getMockedCacheManager());
        resolver.setBasenamesPattern(List.of("translations/*"));
        resolver.setDefaultLocale(Locale.forLanguageTag("en"));
        resolver.setDefaultDomain("otherdomain");
        resolver.initCache();
        String message = resolver.getMessage(
                "hello_language",
                null,
                Locale.forLanguageTag("en-US")
        );
        assertEquals("Other Hello EN (otherdomain)", message);
    }
}
