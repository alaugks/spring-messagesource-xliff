package io.github.alaugks.spring.messagesource.xliff.catalog.finder;

import io.github.alaugks.spring.messagesource.xliff.XliffCacheableKeyGenerator;
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
        if (!locale.toString().isEmpty()) {
            return this.getValue(
                    this.cache.get(
                            XliffCacheableKeyGenerator.createCode(locale, code)
                    )
            );
        }

        return null;
    }

    private String getValue(Cache.ValueWrapper valueWrapper) {
        if (valueWrapper != null) {
            return Objects.requireNonNull(valueWrapper.get()).toString();
        }
        return null;
    }
}
