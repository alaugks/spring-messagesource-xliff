package io.github.alaugks.spring.messagesource.xliff;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XliffMatchingResourcePatternResolverFallbackTest {

    @Test
    void test_fallbackDefaultLanguage() {
        var resolver = new XliffTranslationMessageSource(TestUtilities.getMockedCacheManager());
        resolver.setBasenamePattern("translations/*");
        resolver.setDefaultLocale(Locale.forLanguageTag("en"));
        resolver.initCache();
        String message = resolver.getMessage(
                "hello_language",
                null,
                Locale.forLanguageTag("jp")
        );
        assertEquals("Hello EN (messages)", message);
    }

    @Test
    void test_getMessage_withDefaultMessage_messageNotExists_defaultMessageWithArgs() {
        var resolver = new XliffTranslationMessageSource(TestUtilities.getMockedCacheManager());
        resolver.setBasenamePattern("translations/*");
        resolver.setDefaultLocale(Locale.forLanguageTag("en"));
        resolver.initCache();

        Object[] args = {"Road Runner", "Wile E. Coyote"};
        String message = resolver.getMessage(
                "not_exists",
                args,
                "{0} and {1} as default",
                Locale.forLanguageTag("jp")
        );

        assertEquals("Road Runner and Wile E. Coyote as default", message);
    }
}
