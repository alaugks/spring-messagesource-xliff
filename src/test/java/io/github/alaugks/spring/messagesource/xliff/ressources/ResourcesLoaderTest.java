package io.github.alaugks.spring.messagesource.xliff.ressources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ResourcesLoaderTest {

    ArrayList<ResourcesLoader.Dto> resources;
    private ResourcesLoader resourcesLoader;

    @BeforeEach
    void beforeEach() throws IOException {
        this.resourcesLoader = new ResourcesLoader();
        this.resourcesLoader.setDefaultLocale(Locale.forLanguageTag("en"));

    }

    @Test
    void test_setBasenamePattern() throws IOException {
        this.resourcesLoader.setBasenamePattern(
                "translations/*"
        );
        assertEquals(5, this.resourcesLoader.getResourcesInputStream().size());
    }

    @Test
    void test_setBasenamePattern_domainMessages() throws IOException {
        this.resourcesLoader.setBasenamePattern(
                "translations/messages*"
        );
        assertEquals(3, this.resourcesLoader.getResourcesInputStream().size());
    }


    @Test
    void test_setBasenamePattern_languageDe() throws IOException {
        this.resourcesLoader.setBasenamePattern(
                "translations/*_de*"
        );
        assertEquals(2, this.resourcesLoader.getResourcesInputStream().size());
    }

    @Test
    void test_setBasenamesPattern() throws IOException {
        this.resourcesLoader.setBasenamesPattern(
                List.of(
                        "translations_en/*",
                        "translations_de/*"
                )
        );
        assertEquals(4, this.resourcesLoader.getResourcesInputStream().size());
    }

    @Test
    void test_Dto() throws IOException {
        this.resourcesLoader.setBasenamePattern(
                "translations_en_US/*"
        );
        ResourcesLoader.Dto dto = this.resourcesLoader.getResourcesInputStream().get(0);
        assertEquals("messages", dto.getDomain());
        assertEquals("en_US", dto.getLocale().toString());
        assertInstanceOf(InputStream.class, dto.getInputStream());
    }
}
