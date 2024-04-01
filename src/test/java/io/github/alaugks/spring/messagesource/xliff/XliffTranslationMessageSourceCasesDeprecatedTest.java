package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Deprecated(since = "1.2.1")
class XliffTranslationMessageSourceCasesDeprecatedTest {

    @Test
    void test_withRegion_setDefaultLocale_notSet() {
        var messageSource = new XliffTranslationMessageSource(TestUtilities.getMockedCacheManager());
        messageSource.setBasenamePattern("translations/*");

        XliffMessageSourceRuntimeException exception = assertThrows(
                XliffMessageSourceRuntimeException.class, messageSource::initCache
        );
        assertEquals("Default language is not set or empty.", exception.getMessage());
    }

    @Test
    void test_withRegion_setDefaultLocale_empty() {
        var messageSource = new XliffTranslationMessageSource(TestUtilities.getMockedCacheManager());
        messageSource.setBasenamePattern("translations/*");
        messageSource.setDefaultLocale(Locale.forLanguageTag(""));

        XliffMessageSourceRuntimeException exception = assertThrows(
                XliffMessageSourceRuntimeException.class, messageSource::initCache
        );
        assertEquals("Default language is not set or empty.", exception.getMessage());
    }

    @Test
    void test_setDefaultDomain() {
        var messageSource = new XliffTranslationMessageSource(TestUtilities.getMockedCacheManager());
        messageSource.setBasenamePattern("translations/*");
        messageSource.setDefaultLocale(Locale.forLanguageTag("en"));
        messageSource.setDefaultDomain("otherdomain");

        String message = messageSource.getMessage(
                "hello_language",
                null,
                Locale.forLanguageTag("en-US")
        );
        assertEquals("Other Hello EN (otherdomain)", message);
    }

    @Test
    void test_setBasenamesPattern() {
        var messageSource = new XliffTranslationMessageSource(TestUtilities.getMockedCacheManager());
        messageSource.setBasenamesPattern(List.of(
                "translations_en/*",
                "translations_de/*"
        ));
        messageSource.setDefaultLocale(Locale.forLanguageTag("en"));
        messageSource.initCache();

        assertEquals("value_messages_en", messageSource.getMessage("messages.hello_language", null, Locale.forLanguageTag("en")));
        assertEquals("value_otherdomain_en", messageSource.getMessage("otherdomain.hello_language", null, Locale.forLanguageTag("en")));
        assertEquals("value_messages_de", messageSource.getMessage("messages.hello_language", null, Locale.forLanguageTag("de")));
        assertEquals("value_otherdomain_de", messageSource.getMessage("otherdomain.hello_language", null, Locale.forLanguageTag("de")));
    }

    @ParameterizedTest(name = "{index} => translationUnitIdentifiers={0}, code={1}, expected={2}, value={3}")
    @MethodSource("dataProvider_setTranslationUnitIdentifiersOrdering")
    void test_setTranslationUnitIdentifiersOrdering(ArrayList<String> translationUnitIdentifiers, String code, String expected) {
        var messageSource = new XliffTranslationMessageSource(TestUtilities.getMockedCacheManager());
        messageSource.setBasenamesPattern(List.of("translations/*"));
        messageSource.setDefaultLocale(Locale.forLanguageTag("en"));
        messageSource.setTranslationUnitIdentifiersOrdering(translationUnitIdentifiers);
        messageSource.initCache();

        String message = messageSource.getMessage(
                code,
                null,
                Locale.forLanguageTag("en")
        );
        assertEquals(expected, message);
    }

    private static Stream<Arguments> dataProvider_setTranslationUnitIdentifiersOrdering() {
        return Stream.of(
                Arguments.of(
                        new ArrayList<>(List.of("resname")),
                        "code-resname-a",
                        "Target"
                ),
                Arguments.of(
                        new ArrayList<>(List.of("id")),
                        "code-id-a",
                        "Target"
                ),
                Arguments.of(
                        new ArrayList<>(Arrays.asList("resname", "id")),
                        "code-resname-a",
                        "Target"
                ),
                Arguments.of(
                        new ArrayList<>(Arrays.asList("id", "resname")),
                        "code-id-a",
                        "Target"
                )
        );
    }

}
