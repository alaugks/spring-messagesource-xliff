package io.github.alaugks.spring.messagesource.xliff.ressources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ResourcesLoaderTest {

    ArrayList<ResourcesLoader.Dto> resources;
    private ResourcesLoader resourcesLoader;

    @BeforeEach
    void beforeEach() {
        this.resourcesLoader = new ResourcesLoader();
        this.resourcesLoader.setDefaultLocale(Locale.forLanguageTag("en"));

    }

    @ParameterizedTest()
    @MethodSource("dataProvider_setBasenamePattern")
    void test_setBasenamePattern(String basename, int size) throws IOException {
        this.resourcesLoader.setBasenamePattern(basename);
        assertEquals(size, this.resourcesLoader.getResourcesInputStream().size());
    }

    private static Stream<Arguments> dataProvider_setBasenamePattern() {
        return Stream.of(
                Arguments.of("translations/*", 5),
                Arguments.of("translations/messages*", 3),
                Arguments.of("translations/*_de*", 2)
        );
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
