package io.github.alaugks.spring.messagesource.xliff.ressources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.alaugks.spring.messagesource.xliff.ressources.ResourcesFileNameParser.Filename;
import org.junit.jupiter.api.Test;

class ResourcesFilenameParserTest {

    @Test
    void test_domain_withoutLocale() {
        Filename filename = new ResourcesFileNameParser("message").parse();
        assertEquals("message", filename.domain());
        assertNull(filename.language());
        assertNull(filename.region());
    }

    @Test
    void test_domain_en() {
        Filename filename = new ResourcesFileNameParser("message_en").parse();
        assertEquals("message", filename.domain());
        assertEquals("en", filename.language());
        assertNull(filename.region());
        assertEquals("en", filename.locale().toString());
    }

    @Test
    void test_domain_en_withDash() {
        Filename filename = new ResourcesFileNameParser("message-en").parse();
        assertEquals("message", filename.domain());
        assertEquals("en", filename.language());
        assertNull(filename.region());
        assertEquals("en", filename.locale().toString());
    }

    @Test
    void test_domain_enGB() {
        Filename filename = new ResourcesFileNameParser("message_en_GB").parse();
        assertEquals("message", filename.domain());
        assertEquals("en", filename.language());
        assertEquals("GB", filename.region());
        assertEquals("en_GB", filename.locale().toString());
    }

    @Test
    void test_domain_enGB_withDash() {
        Filename filename = new ResourcesFileNameParser("message-en-GB").parse();
        assertEquals("message", filename.domain());
        assertEquals("en", filename.language());
        assertEquals("GB", filename.region());
        assertEquals("en_GB", filename.locale().toString());
    }

    @Test
    void test_hasNoLocale() {
        Filename filename = new ResourcesFileNameParser("message").parse();
        assertFalse(filename.hasLocale());
    }

    @Test
    void test_hasLocale() {
        Filename filename = new ResourcesFileNameParser("message_de").parse();
        assertTrue(filename.hasLocale());
    }
}
