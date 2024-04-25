package io.github.alaugks.spring.messagesource.xliff.catalog;

import java.util.Locale;
import java.util.Map;
import org.springframework.cache.Cache;

public final class CatalogHandler {

    private final CatalogInterface catalog;

    public CatalogHandler(
        BaseCatalog baseCatalog,
        Cache cache
    ) {
        if (cache != null) {
            this.catalog = new CacheCatalog(cache);
            this.catalog.nextHandler(baseCatalog);
        } else {
            this.catalog = baseCatalog;
        }
    }

    public Map<String, Map<String, String>> getAll() {
        return this.catalog.getAll();
    }

    public String get(Locale locale, String code) {
        return this.catalog.get(locale, code);
    }
}
