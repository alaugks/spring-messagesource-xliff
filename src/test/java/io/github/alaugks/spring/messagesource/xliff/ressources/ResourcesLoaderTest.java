package io.github.alaugks.spring.messagesource.xliff.ressources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import io.github.alaugks.spring.messagesource.xliff.ressources.ResourcesLoader.TranslationFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class ResourcesLoaderTest {

    @Test
    void test_setBasenamePattern() throws IOException {
        ResourcesLoader resourcesLoader = ResourcesLoader
            .builder()
            .defaultLocale(Locale.forLanguageTag("en"))
            .basenamesPattern(TestUtilities.listToSet("translations/*"))
            .build();

        assertEquals(5, resourcesLoader.getTranslationFiles().size());
    }

    @Test
    void test_setBasenamePattern_domainMessages() throws IOException {
        ResourcesLoader resourcesLoader = ResourcesLoader
            .builder()
            .defaultLocale(Locale.forLanguageTag("en"))
            .basenamesPattern(TestUtilities.listToSet("translations/messages*"))
            .build();

        assertEquals(3, resourcesLoader.getTranslationFiles().size());
    }


    @Test
    void test_setBasenamePattern_languageDe() throws IOException {
        ResourcesLoader resourcesLoader = ResourcesLoader
            .builder()
            .defaultLocale(Locale.forLanguageTag("en"))
            .basenamesPattern(TestUtilities.listToSet("translations/*_de*"))
            .build();

        assertEquals(2, resourcesLoader.getTranslationFiles().size());
    }

    @Test
    void test_setBasenamesPattern() throws IOException {
        ResourcesLoader resourcesLoader = ResourcesLoader
            .builder()
            .defaultLocale(Locale.forLanguageTag("en"))
            .basenamesPattern(TestUtilities.listToSet("translations_en/*", "translations_de/*"))
            .build();

        assertEquals(4, resourcesLoader.getTranslationFiles().size());
    }

    @Test
    void test_Dto() throws IOException {
        ResourcesLoader resourcesLoader = ResourcesLoader
            .builder()
            .defaultLocale(Locale.forLanguageTag("en"))
            .basenamesPattern(TestUtilities.listToSet("translations_en_US/*"))
            .build();

        TranslationFile translationFile = resourcesLoader.getTranslationFiles().get(0);

        assertEquals("messages", translationFile.domain());
        assertEquals("en_US", translationFile.locale().toString());
        assertInstanceOf(InputStream.class, translationFile.inputStream());
    }
}
