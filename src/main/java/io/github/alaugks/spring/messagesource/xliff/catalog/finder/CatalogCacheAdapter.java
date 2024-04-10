package io.github.alaugks.spring.messagesource.xliff.catalog.finder;

import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogUtilities;
import java.util.Locale;
import java.util.Objects;
import org.springframework.cache.Cache;

public class CatalogCacheAdapter implements CatalogAdapterInterface {

    private final Cache cache;

    public CatalogCacheAdapter(Cache cache) {
        this.cache = cache;
    }

    @Override
    public String find(Locale locale, String code) {
        Cache.ValueWrapper valueWrapper = this.cache.get(
            CatalogUtilities.createCode(locale, code)
        );

        if (valueWrapper != null) {
            return Objects.requireNonNull(valueWrapper.get()).toString();
        }

        return null;
    }
}
