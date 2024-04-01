package io.github.alaugks.spring.messagesource.xliff.ressources;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ResourcesLoaderTest {

    @Test
    void test_setBasenamePattern() throws IOException {
        ResourcesLoader resourcesLoader = ResourcesLoader
                .builder()
                .defaultLocale(Locale.forLanguageTag("en"))
                .basenamePattern(
                        "translations/*"
                )
                .build();

        assertEquals(5, resourcesLoader.getTranslationFiles().size());
    }

    @Test
    void test_setBasenamePattern_domainMessages() throws IOException {
        ResourcesLoader resourcesLoader = ResourcesLoader
                .builder()
                .defaultLocale(Locale.forLanguageTag("en"))
                .basenamePattern(
                        "translations/messages*"
                )
                .build();

        assertEquals(3, resourcesLoader.getTranslationFiles().size());
    }


    @Test
    void test_setBasenamePattern_languageDe() throws IOException {
        ResourcesLoader resourcesLoader = ResourcesLoader
                .builder()
                .defaultLocale(Locale.forLanguageTag("en"))
                .basenamePattern(
                        "translations/*_de*"
                )
                .build();

        assertEquals(2, resourcesLoader.getTranslationFiles().size());
    }

    @Test
    void test_setBasenamesPattern() throws IOException {
        ResourcesLoader resourcesLoader = ResourcesLoader
                .builder()
                .defaultLocale(Locale.forLanguageTag("en"))
                .basenamesPattern(
                        List.of(
                                "translations_en/*",
                                "translations_de/*"
                        )
                )
                .build();

        assertEquals(4, resourcesLoader.getTranslationFiles().size());
    }

    @Test
    void test_Dto() throws IOException {
        ResourcesLoader resourcesLoader = ResourcesLoader
                .builder()
                .defaultLocale(Locale.forLanguageTag("en"))
                .basenamePattern(
                        "translations_en_US/*"
                )
                .build();

        ResourcesLoader.Dto dto = resourcesLoader.getTranslationFiles().get(0);

        assertEquals("messages", dto.getDomain());
        assertEquals("en_US", dto.getLocale().toString());
        assertInstanceOf(InputStream.class, dto.getInputStream());
    }
}
