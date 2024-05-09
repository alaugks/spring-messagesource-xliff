package io.github.alaugks.spring.messagesource.xliff.record;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceRuntimeException;
import io.github.alaugks.spring.messagesource.xliff.records.Filename;
import org.junit.jupiter.api.Test;

class FilenameTest {

    @Test
    void test_hasNoLocale() {
        Filename filename = new Filename("messages", null, null);

        assertFalse(filename.hasLocale());
    }

    @Test
    void test_hasLocale() {
        Filename filename = new Filename("messages", "en", null);

        assertTrue(filename.hasLocale());
    }

    @Test
    void test_formatedException() {
        Filename filename = new Filename("messages", "en", "bar");

        assertThrows(XliffMessageSourceRuntimeException.class, filename::locale);
    }
}
