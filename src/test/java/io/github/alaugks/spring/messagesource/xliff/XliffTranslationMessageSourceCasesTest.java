package io.github.alaugks.spring.messagesource.xliff;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class XliffTranslationMessageSourceCasesTest {

    XliffTranslationMessageSource messageSourceExample;

    XliffTranslationMessageSourceCasesTest() {
        this.messageSourceExample = XliffTranslationMessageSource
            .builder(TestUtilities.getCache())
            .basenamePattern("translations_example/*")
            .defaultLocale(Locale.forLanguageTag("en"))
            .build();
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

    @ParameterizedTest()
    @MethodSource("dataProvider_examples")
    void test_complex_locale_region_fallback(String code, String locale, Object expected) {
        String message = this.messageSourceExample.getMessage(
            code,
            null,
            Locale.forLanguageTag(locale)
        );
        assertEquals(expected, message);
    }

    private static Stream<Arguments> dataProvider_examples() {
        return Stream.of(
            Arguments.of("headline", "en", "Headline (en)"),
            Arguments.of("messages.headline", "en", "Headline (en)"),
            Arguments.of("text", "en", "Text (en)"),
            Arguments.of("messages.text", "en", "Text (en)"),
            Arguments.of("notice", "en", "Notice (en)"),
            Arguments.of("messages.notice", "en", "Notice (en)"),
            Arguments.of("payment.headline", "en", "Payment (en)"),
            Arguments.of("payment.text", "en", "Payment Text (en)"),

            Arguments.of("headline", "de", "Headline (de)"),
            Arguments.of("messages.headline", "de", "Headline (de)"),
            Arguments.of("text", "de", "Text (de)"),
            Arguments.of("messages.text", "de", "Text (de)"),
            Arguments.of("notice", "de", "Notice (en)"),
            Arguments.of("messages.notice", "de", "Notice (en)"),
            Arguments.of("payment.headline", "de", "Payment (de)"),
            Arguments.of("payment.text", "de", "Payment Text (de)"),

            Arguments.of("headline", "en-US", "Headline (en)"),
            Arguments.of("messages.headline", "en-US", "Headline (en)"),
            Arguments.of("text", "en-US", "Text (en-US)"),
            Arguments.of("messages.text", "en-US", "Text (en-US)"),
            Arguments.of("notice", "en-US", "Notice (en)"),
            Arguments.of("messages.notice", "en-US", "Notice (en)"),
            Arguments.of("payment.headline", "en-US", "Payment (en-US)"),
            Arguments.of("payment.text", "en-US", "Payment Text (en)"),

            Arguments.of("headline", "es", "Headline (es)"),
            Arguments.of("messages.headline", "es", "Headline (es)"),
            Arguments.of("text", "es", "Text (es)"),
            Arguments.of("messages.text", "es", "Text (es)"),
            Arguments.of("notice", "es", "Notice (en)"),
            Arguments.of("messages.notice", "es", "Notice (en)"),
            Arguments.of("payment.headline", "es", "Payment (es)"),
            Arguments.of("payment.text", "es", "Payment Text (es)"),

            Arguments.of("headline", "es-CR", "Headline (es-CR)"),
            Arguments.of("messages.headline", "es-CR", "Headline (es-CR)"),
            Arguments.of("text", "es-CR", "Text (es)"),
            Arguments.of("messages.text", "es-CR", "Text (es)"),
            Arguments.of("notice", "es-CR", "Notice (en)"),
            Arguments.of("messages.notice", "es-CR", "Notice (en)"),
            Arguments.of("payment.headline", "es-CR", "Payment (es-CR)"),
            Arguments.of("payment.text", "es-CR", "Payment Text (es)"),

            Arguments.of("headline", "jp", "Headline (en)"),
            Arguments.of("messages.headline", "jp", "Headline (en)"),
            Arguments.of("payment.headline", "jp", "Payment (en)"),
            Arguments.of("payment.text", "jp", "Payment Text (en)")
        );
    }
}
