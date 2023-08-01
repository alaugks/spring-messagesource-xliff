package io.github.alaugks.spring.messagesource.xliff.ressources;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ResourcesFileNameParserTest {

    @ParameterizedTest()
    @MethodSource("dataProvider_parse")
    void test_parse(String filename, String domain, String language, String region) {
        ResourcesFileNameParser.Dto dto = new ResourcesFileNameParser(filename).parse();
        assertEquals(domain, dto.getDomain());
        assertEquals(language, dto.getLanguage());
        assertEquals(region, dto.getRegion());
    }

    private static Stream<Arguments> dataProvider_parse() {
        return Stream.of(
                Arguments.of(
                        "message",
                        "message",
                        null,
                        null
                ),
                Arguments.of(
                        "message_en",
                        "message",
                        "en",
                        null
                ),
                Arguments.of(
                        "message-en",
                        "message",
                        "en",
                        null
                ),
                Arguments.of(
                        "message_en_GB",
                        "message",
                        "en",
                        "GB"
                ),
                Arguments.of(
                        "message-en-GB",
                        "message",
                        "en",
                        "GB"
                )
        );
    }

    @Test
    void test_hasNoLocale() {
        ResourcesFileNameParser.Dto filename = new ResourcesFileNameParser("message").parse();
        assertFalse(filename.hasLocale());
    }

    @Test
    void test_hasLocale() {
        ResourcesFileNameParser.Dto filename = new ResourcesFileNameParser("message_de").parse();
        assertTrue(filename.hasLocale());
    }
}
