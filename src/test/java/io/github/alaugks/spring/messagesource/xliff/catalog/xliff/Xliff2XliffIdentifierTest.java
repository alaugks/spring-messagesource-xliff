package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class Xliff2XliffIdentifierTest {
    @Test
    void test_default() {
        var identifier = new Xliff2XliffIdentifier();
        assertEquals(List.of("id"), identifier.getList());
    }

    @Test
    void test_custom() {
        var identifier = new Xliff12XliffIdentifier(List.of("resname", "id"));
        assertEquals(List.of("resname", "id"), identifier.getList());
    }
}
