package io.github.alaugks.spring.messagesource.xliff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceRuntimeException;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class XliffTranslationMessageSourceCasesTest {

    @Test
    void test_withRegion_setDefaultLocale_notSet() {
        var builder = XliffTranslationMessageSource
            .builder(TestUtilities.getCache())
                .basenamePattern("translations/*");

        XliffMessageSourceRuntimeException exception = assertThrows(
                XliffMessageSourceRuntimeException.class, builder::build
        );
        assertEquals("Default language is not set or empty.", exception.getMessage());
    }

    @Test
    void test_withRegion_setDefaultLocale_empty() {
        var builder = XliffTranslationMessageSource
            .builder(TestUtilities.getCache())
                .basenamePattern("translations/*")
                .defaultLocale(Locale.forLanguageTag(""));

        XliffMessageSourceRuntimeException exception = assertThrows(
                XliffMessageSourceRuntimeException.class, builder::build
        );
        assertEquals("Default language is not set or empty.", exception.getMessage());
    }

    @Test
    void test_setDefaultDomain() {
        var messageSource = XliffTranslationMessageSource
            .builder(TestUtilities.getCache())
                .basenamePattern("translations/*")
                .defaultLocale(Locale.forLanguageTag("en"))
                .defaultDomain("otherdomain")
                .build();

        assertEquals("Hello World (otherdomain / en)", messageSource.getMessage(
            "hello_world",
            null,
            Locale.forLanguageTag("en-US")
        ));
    }

    @Test
    void test_setBasenamesPattern() {
        var messageSource = XliffTranslationMessageSource
            .builder(TestUtilities.getCache())
                .basenamesPattern(
                        List.of(
                                "translations_en/*",
                                "translations_de/*"
                        )
                )
                .defaultLocale(Locale.forLanguageTag("en"))
                .build();

        assertEquals(
            "Hello World (messages / en)",
            messageSource.getMessage("messages.hello_world", null, Locale.forLanguageTag("en"))
        );
        assertEquals(
            "Hello World (otherdomain / en)",
            messageSource.getMessage("otherdomain.hello_world", null, Locale.forLanguageTag("en"))
        );
        assertEquals(
            "Hallo Welt (messages / de)",
            messageSource.getMessage("messages.hello_world", null, Locale.forLanguageTag("de"))
        );
        assertEquals(
            "Hallo Welt (otherdomain / de)",
            messageSource.getMessage("otherdomain.hello_world", null, Locale.forLanguageTag("de"))
        );
    }
}
