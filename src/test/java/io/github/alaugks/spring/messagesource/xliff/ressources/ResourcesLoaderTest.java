package io.github.alaugks.spring.messagesource.xliff.ressources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import io.github.alaugks.spring.messagesource.xliff.records.TranslationFile;
import java.io.InputStream;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class ResourcesLoaderTest {

    @Test
    void test_setBasenamePattern() {
        var resourcesLoader = TestUtilities.getResourcesLoader(Locale.forLanguageTag("en"), "translations/*");
        assertEquals(5, resourcesLoader.getTranslationFiles().size());
    }

    @Test
    void test_setBasenamePattern_domainMessages() {
        var resourcesLoader = TestUtilities.getResourcesLoader(Locale.forLanguageTag("en"), "translations/messages*");
        assertEquals(3, resourcesLoader.getTranslationFiles().size());
    }


    @Test
    void test_setBasenamePattern_languageDe() {
        var resourcesLoader = TestUtilities.getResourcesLoader(Locale.forLanguageTag("en"), "translations/*_de*");
        assertEquals(2, resourcesLoader.getTranslationFiles().size());
    }

    @Test
    void test_setBasenamesPattern() {
        var resourcesLoader = TestUtilities.getResourcesLoader(
            Locale.forLanguageTag("en"),
            "translations_en/*",
            "translations_de/*"
        );
        assertEquals(4, resourcesLoader.getTranslationFiles().size());
    }

    @Test
    void test_Dto() {
        var resourcesLoader = TestUtilities.getResourcesLoader(Locale.forLanguageTag("en"), "translations_en_US/*");

        TranslationFile translationFile = resourcesLoader.getTranslationFiles().get(0);

        assertEquals("messages", translationFile.domain());
        assertEquals("en_US", translationFile.locale().toString());
        assertInstanceOf(InputStream.class, translationFile.inputStream());
    }
}
