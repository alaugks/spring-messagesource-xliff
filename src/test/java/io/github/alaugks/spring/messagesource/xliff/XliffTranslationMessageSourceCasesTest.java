package io.github.alaugks.spring.messagesource.xliff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.context.NoSuchMessageException;

class XliffTranslationMessageSourceCasesTest {

    @Test
    void test_defaultDomain() {
        var messageSource = XliffTranslationMessageSource
            .builder(Locale.forLanguageTag("en"), "translations/*")
            .defaultDomain("otherdomain")
            .build();

        assertEquals("Hello World (otherdomain / en)", messageSource.getMessage(
            "hello_world",
            null,
            Locale.forLanguageTag("en-US")
        ));
    }

    @Test
    void test_fileExtensions() {
        var messageSource = XliffTranslationMessageSource
            .builder(Locale.forLanguageTag("en"), "translations/*")
            .fileExtensions(List.of("xlf"))
            .build();

        var locale = Locale.forLanguageTag("en");
        assertThrows(NoSuchMessageException.class, () -> messageSource.getMessage(
            "hello_world",
            null,
            locale
        ));
    }

    @Test
    void test_setBasenamesPattern() {
        var messageSource = XliffTranslationMessageSource
            .builder(
                Locale.forLanguageTag("en"), List.of(
                    "translations_en/*",
                    "translations_de/*"
                )
            )
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
