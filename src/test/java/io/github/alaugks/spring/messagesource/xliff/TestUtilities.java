package io.github.alaugks.spring.messagesource.xliff;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

public class TestUtilities {

    public static final String CATALOG_CACHE = "test-cache";

    public static Cache getCache() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(List.of(CATALOG_CACHE));
        return cacheManager.getCache(CATALOG_CACHE);
    }

    public static Map<Object, Object> cacheToArray(Cache cache) {
        var nativeCache = (ConcurrentHashMap<?, ?>) cache.getNativeCache();
        return new HashMap<>(nativeCache);
    }
}
