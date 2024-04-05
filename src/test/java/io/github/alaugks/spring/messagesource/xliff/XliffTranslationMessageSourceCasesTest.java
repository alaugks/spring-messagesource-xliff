package io.github.alaugks.spring.messagesource.xliff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.alaugks.spring.messagesource.xliff.catalog.xliff.Xliff12XliffIdentifier;
import io.github.alaugks.spring.messagesource.xliff.catalog.xliff.Xliff2XliffIdentifier;
import io.github.alaugks.spring.messagesource.xliff.catalog.xliff.XliffIdentifierInterface;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceRuntimeException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class XliffTranslationMessageSourceCasesTest {

    @Test
    void test_withRegion_setDefaultLocale_notSet() {
        var builder = XliffTranslationMessageSource
                .builder(TestUtilities.getMockedCacheManager())
                .basenamePattern("translations/*");

        XliffMessageSourceRuntimeException exception = assertThrows(
                XliffMessageSourceRuntimeException.class, builder::build
        );
        assertEquals("Default language is not set or empty.", exception.getMessage());
    }

    @Test
    void test_withRegion_setDefaultLocale_empty() {
        var builder = XliffTranslationMessageSource
                .builder(TestUtilities.getMockedCacheManager())
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
                .builder(TestUtilities.getMockedCacheManager())
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
                .builder(TestUtilities.getMockedCacheManager())
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

    @ParameterizedTest(name = "{index} => translationUnitIdentifiers={0}, code={1}, expected={2}, value={3}")
    @MethodSource("dataProvider_setTranslationUnitIdentifiersOrdering")
    void test_setTranslationUnitIdentifiersOrdering(List<XliffIdentifierInterface> translationUnitIdentifiers, String code, String expected) {
        var messageSource = XliffTranslationMessageSource
                .builder(TestUtilities.getMockedCacheManager())
                .basenamePattern("identifier/*")
                .defaultLocale(Locale.forLanguageTag("en"))
                .setTransUnitIdentifier(translationUnitIdentifiers)
                .build();

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
                    Arrays.asList(new Xliff2XliffIdentifier(List.of("resname")), new Xliff12XliffIdentifier(List.of("resname"))),
                    "identifierv1.code-resname-a",
                    "Target A"
                ),
                Arguments.of(
                    Arrays.asList(new Xliff2XliffIdentifier(List.of("id")), new Xliff12XliffIdentifier(List.of("id"))),
                    "identifierv1.code-id-a",
                    "Target A"
                ),

                Arguments.of(
                    Arrays.asList(new Xliff2XliffIdentifier(List.of("resname", "id")), new Xliff12XliffIdentifier(List.of("resname", "id"))),
                    "identifierv1.code-id-b",
                    "Target B"
                ),
                Arguments.of(
                    Arrays.asList(new Xliff2XliffIdentifier(List.of("id", "resname")), new Xliff12XliffIdentifier(List.of("id", "resname"))),
                    "identifierv1.code-resname-c",
                    "Target C"
                ),

                Arguments.of(
                    Arrays.asList(new Xliff2XliffIdentifier(List.of("resname")), new Xliff12XliffIdentifier(List.of("resname"))),
                    "identifierv2.code-resname-a",
                    "Target A"
                ),
                Arguments.of(
                    Arrays.asList(new Xliff2XliffIdentifier(List.of("id")), new Xliff12XliffIdentifier(List.of("id"))),
                    "identifierv2.code-id-a",
                    "Target A"
                ),

                Arguments.of(
                    Arrays.asList(new Xliff2XliffIdentifier(List.of("resname", "id")), new Xliff12XliffIdentifier(List.of("resname", "id"))),
                    "identifierv2.code-id-b",
                    "Target B"
                ),
                Arguments.of(
                    Arrays.asList(new Xliff2XliffIdentifier(List.of("id", "resname")), new Xliff12XliffIdentifier(List.of("id", "resname"))),
                    "identifierv2.code-resname-c",
                    "Target C"
                )
        );
    }

}
