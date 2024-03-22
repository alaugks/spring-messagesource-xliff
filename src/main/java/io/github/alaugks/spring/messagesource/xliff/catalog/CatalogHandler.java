package io.github.alaugks.spring.messagesource.xliff.catalog;

import io.github.alaugks.spring.messagesource.xliff.catalog.xliff.XliffCatalogBuilder;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class CatalogHandler {
    private final CatalogCache catalogCache;

    public CatalogHandler(
            Catalog catalog,
            CatalogCache catalogCache,
            XliffCatalogBuilder catalogBuilder
    ) {
        CatalogReader catalogReader = new CatalogReader(catalog, catalogBuilder);

        this.catalogCache = catalogCache;
        this.catalogCache.setNextHandler(catalogReader.loadCatalog());
    }

    public Map<String, Map<String, String>> getAll() {
        return this.catalogCache.getAll();
    }

    public Translation get(Locale locale, String code) {
        String targetValue = this.catalogCache.get(locale, code);

        if (targetValue != null) {
            return new Translation(code, targetValue);
        }

        // If targetValue not exists then init cache, because it was not in the cache. Cache empty?
        this.initCache();

        // If not exists then is the targetValue is the code.
        // Non-existing code is added to the cache to fetch the non-existing code from
        // the cache so as not to continue looking in the messagesource files.
        this.put(locale, code, code);
        return new Translation(code, code);
    }

    void put(Locale locale, String code, String targetValue) {
        this.catalogCache.put(
                locale,
                code,
                targetValue
        );
    }

    public void initCache() {
        this.catalogCache.initCache();
    }

    public static class Translation {
        String code;
        String targetValue;

        public Translation(String code, String targetValue) {
            this.code = code;
            this.targetValue = targetValue;
        }

        public boolean exists() {
            return !Objects.equals(this.code, this.targetValue);
        }

        public String toString() {
            return targetValue;
        }
    }
}
