package io.github.alaugks.spring.messagesource.xliff.mock;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MockCacheManager implements CacheManager {

    public final Map<String, Cache> caches = new HashMap<>();

    public void createCache(String cacheName) {
        this.caches.put(cacheName, new MockCache());
    }

    @Override
    public Cache getCache(String name) {
        if (this.caches.containsKey(name)) {
            return this.caches.get(name);
        }
        return null;
    }

    @Override
    public Collection<String> getCacheNames() {
        return this.caches.keySet();
    }
}
