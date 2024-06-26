package io.github.alaugks.spring.messagesource.xliff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceRuntimeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
    void test_setDefaultDomain() {
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

    @Test
    void test_messagesFormat_choice() {
        var messageSource = new XliffTranslationMessageSource(TestUtilities.getMockedCacheManager());
        messageSource.setBasenamesPattern(List.of("translations/*"));
        messageSource.setDefaultLocale(Locale.forLanguageTag("en"));
        messageSource.initCache();

        assertEquals("There are 10,000 files.", messageSource.getMessage(
            "format_choice",
            new Object[]{10000L},
            Locale.forLanguageTag("en")
        ));
        assertEquals("There is one file.", messageSource.getMessage(
            "format_choice",
            new Object[]{1},
            Locale.forLanguageTag("en")
        ));

        assertEquals("Es gibt 10.000 Dateien.", messageSource.getMessage(
            "format_choice",
            new Object[]{10000L},
            Locale.forLanguageTag("de")
        ));
        assertEquals("Es gibt eine Datei.", messageSource.getMessage(
            "format_choice",
            new Object[]{1},
            Locale.forLanguageTag("de")
        ));
    }

    @ParameterizedTest(name = "{index} => translationUnitIdentifiers={0}, code={1}, expected={2}, targetValue={3}")
    @MethodSource("dataProvider_setTranslationUnitIdentifiersOrdering")
    void test_setTranslationUnitIdentifiersOrdering(ArrayList<String> translationUnitIdentifiers, String code, String expected) {
        var resolver = new XliffTranslationMessageSource(TestUtilities.getMockedCacheManager());
        resolver.setBasenamesPattern(List.of("translations/*"));
        resolver.setDefaultLocale(Locale.forLanguageTag("en"));
        resolver.setTranslationUnitIdentifiersOrdering(translationUnitIdentifiers);
        resolver.initCache();

        String message = resolver.getMessage(
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
