package io.github.alaugks.spring.messagesource.xliff.mock;

import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogCache;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class MockCacheManagerTest {

    @Test
    void test_mockCacheManager_getCacheNames() {
        var mock = new MockCacheManager();
        mock.createCache(CatalogCache.CACHE_NAME);
        assertTrue(mock.getCacheNames().stream().anyMatch(CatalogCache.CACHE_NAME::equals));
    }

    @Test
    void test_mockCacheManager_getCache() {
        var mock = new MockCacheManager();

        assertNull(mock.getCache(CatalogCache.CACHE_NAME));

        mock.createCache(CatalogCache.CACHE_NAME);
        assertInstanceOf(Cache.class, mock.getCache(CatalogCache.CACHE_NAME));
    }

    @Test
    void test_mockCacheManager_get() {
        var mock = new MockCacheManager();
        mock.createCache(CatalogCache.CACHE_NAME);
        var cache = mock.getCache(CatalogCache.CACHE_NAME);

        cache.put("test-key", "text-value");
        assertEquals("text-value", Objects.requireNonNull(cache.get("test-key")).get());

        cache.put("test-key-2", "text-value-2");
        assertEquals("text-value", Objects.requireNonNull(cache.get("test-key")).get());
    }
}
