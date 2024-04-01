package io.github.alaugks.spring.messagesource.xliff.catalog;

import org.springframework.cache.CacheManager;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class CatalogHandler {
    private final CatalogCache catalogCache;

    public CatalogHandler(
            CatalogBuilder catalogBuilder,
            Locale defaultLocale,
            String defaultDomain,
            CacheManager cacheManager
    ) {
        this.catalogCache = new CatalogCache(defaultLocale, defaultDomain, cacheManager);
        this.catalogCache.setNextHandler(
                catalogBuilder.createCatalog(new Catalog(defaultLocale, defaultDomain))
        );
    }

    public Map<String, Map<String, String>> getAll() {
        return this.catalogCache.getAll();
    }

    public Translation get(Locale locale, String code) {
        String value = this.catalogCache.get(locale, code);

        if (value != null) {
            return new Translation(code, value);
        }

        // If value not exists then init cache, because it was not in the cache. Cache empty?
        this.initCache();

        // If not exists then is the value is the code.
        // Non-existing code is added to the cache to fetch the non-existing code from
        // the cache so as not to continue looking in the messagesource files.
        this.put(locale, code, code);
        return new Translation(code, code);
    }

    void put(Locale locale, String code, String value) {
        this.catalogCache.put(
                locale,
                code,
                value
        );
    }

    public void initCache() {
        this.catalogCache.initCache();
    }

    public static class Translation {
        String code;
        String value;

        public Translation(String code, String value) {
            this.code = code;
            this.value = value;
        }

        public boolean exists() {
            return !Objects.equals(this.code, this.value);
        }

        public String toString() {
            return value;
        }
    }
}
