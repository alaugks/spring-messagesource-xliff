package io.github.alaugks.spring.messagesource.xliff.ressources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import io.github.alaugks.spring.messagesource.xliff.records.TranslationFile;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class ResourcesLoaderTest {

    @Test
    void test_setBasenamePattern() {
        var resourcesLoader = new ResourcesLoader(
            Locale.forLanguageTag("en"),
            new HashSet<>(List.of("translations/*")),
            List.of("xlf", "xliff")
        );

        assertEquals(5, resourcesLoader.getTranslationFiles().size());
    }

    @Test
    void test_setBasenamePattern_domainMessages() {
        var resourcesLoader = new ResourcesLoader(
            Locale.forLanguageTag("en"),
            new HashSet<>(List.of("translations/messages*")),
            List.of("xlf", "xliff")
        );

        assertEquals(3, resourcesLoader.getTranslationFiles().size());
    }


    @Test
    void test_setBasenamePattern_languageDe() {
        var resourcesLoader = new ResourcesLoader(
            Locale.forLanguageTag("en"),
            new HashSet<>(List.of("translations/*_de*")),
            List.of("xlf", "xliff")
        );

        assertEquals(2, resourcesLoader.getTranslationFiles().size());
    }

    @Test
    void test_setBasenamesPattern() {
        var resourcesLoader = new ResourcesLoader(
            Locale.forLanguageTag("en"),
            new HashSet<>(List.of("translations_en/*", "translations_de/*")),
            List.of("xlf", "xliff")
        );

        assertEquals(4, resourcesLoader.getTranslationFiles().size());
    }

    @Test
    void test_Dto() {
        var resourcesLoader = new ResourcesLoader(
            Locale.forLanguageTag("en"),
            new HashSet<>(List.of("translations_en_US/*")),
            List.of("xlf", "xliff")
        );

        TranslationFile translationFile = resourcesLoader.getTranslationFiles().get(0);

        assertEquals("messages", translationFile.domain());
        assertEquals("en_US", translationFile.locale().toString());
        assertInstanceOf(InputStream.class, translationFile.inputStream());
    }
}
