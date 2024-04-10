package io.github.alaugks.spring.messagesource.xliff.catalog;

import java.util.Locale;
import java.util.Map;
import org.springframework.cache.Cache;

public final class CatalogHandler {

    private final CatalogCache catalogCache;

    public CatalogHandler(
        CatalogBuilder catalogBuilder,
        Cache cache,
        Locale defaultLocale,
        String defaultDomain
    ) {
        this.catalogCache = new CatalogCache(defaultLocale, defaultDomain, cache);
        this.catalogCache.setNextHandler(
            catalogBuilder.createCatalog(new Catalog(defaultLocale, defaultDomain))
        );
    }

    public Map<String, Map<String, String>> getAll() {
        return this.catalogCache.getAll();
    }

    public String get(Locale locale, String code) {
        String value = this.catalogCache.get(locale, code);

        if (value != null) {
            return value;
        }

        // If value not exists then init cache, because it was not in the cache.
        this.initCache();

        return null;
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
}
