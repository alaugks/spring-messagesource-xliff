package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class Xliff12XliffIdentifierTest {

    @Test
    void test_default() {
        var identifier = new Xliff12XliffIdentifier();
        assertEquals(Arrays.asList("resname", "id"), identifier.getList());
    }

    @Test
    void test_custom() {
        var identifier = new Xliff12XliffIdentifier(List.of("id"));
        assertEquals(List.of("id"), identifier.getList());
    }
}
