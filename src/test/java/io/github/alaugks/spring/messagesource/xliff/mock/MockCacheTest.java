package io.github.alaugks.spring.messagesource.xliff.mock;

import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.test.util.AssertionErrors.assertNull;

class MockCacheTest {

    @Test
    void test_put_get() {
        var mock = new MockCache();
        mock.put("key1", "value1");
        mock.put("key2", "value2");
        assertInstanceOf(Cache.ValueWrapper.class, mock.get("key1"));

        assertEquals("value1", mock.get("key1").get().toString());
        assertEquals("value2", mock.get("key2").get().toString());
    }

    @Test
    void test_clear() {
        var mock = new MockCache();
        mock.put("key1", "value1");
        assertEquals("value1", mock.get("key1").get().toString());
        mock.clear();
        assertNull("value1", Objects.requireNonNull(mock.get("key1")).get());
    }

}
