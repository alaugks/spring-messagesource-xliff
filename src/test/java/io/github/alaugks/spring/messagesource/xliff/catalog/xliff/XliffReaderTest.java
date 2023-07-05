package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class XliffReaderTest {

    private XliffReader reader;

    @BeforeEach
    void beforeEach() {
        this.reader = new XliffReader();
    }

    @Test
    void test_supportedVersions() {
        assertInstanceOf(Xliff12.class, this.reader.getReader("1.2"));
        assertInstanceOf(Xliff20.class, this.reader.getReader("2.0"));
        assertInstanceOf(Xliff21.class, this.reader.getReader("2.1"));
    }

    @Test
    void test_versionNotSupported() {
        assertNull(this.reader.getReader("1.0"));
    }
}
