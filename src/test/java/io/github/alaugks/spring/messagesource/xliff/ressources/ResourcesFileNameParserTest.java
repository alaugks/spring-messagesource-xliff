package io.github.alaugks.spring.messagesource.xliff.ressources;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourcesFileNameParserTest {

    @Test
    void test_domain_withoutLocale() {
        ResourcesFileNameParser.Dto filename = new ResourcesFileNameParser("message").parse();
        assertEquals("message", filename.getDomain());
        assertNull(filename.getLanguage());
        assertNull(filename.getRegion());
    }

    @Test
    void test_domain_en() {
        ResourcesFileNameParser.Dto filename = new ResourcesFileNameParser("message_en").parse();
        assertEquals("message", filename.getDomain());
        assertEquals("en", filename.getLanguage());
        assertNull(filename.getRegion());
    }

    @Test
    void test_domain_en_withDash() {
        ResourcesFileNameParser.Dto filename = new ResourcesFileNameParser("message-en").parse();
        assertEquals("message", filename.getDomain());
        assertEquals("en", filename.getLanguage());
        assertNull(filename.getRegion());
    }

    @Test
    void test_domain_enGB() {
        ResourcesFileNameParser.Dto filename = new ResourcesFileNameParser("message_en_GB").parse();
        assertEquals("message", filename.getDomain());
        assertEquals("en", filename.getLanguage());
        assertEquals("GB", filename.getRegion());
    }

    @Test
    void test_domain_enGB_withDash() {
        ResourcesFileNameParser.Dto filename = new ResourcesFileNameParser("message-en-GB").parse();
        assertEquals("message", filename.getDomain());
        assertEquals("en", filename.getLanguage());
        assertEquals("GB", filename.getRegion());
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
