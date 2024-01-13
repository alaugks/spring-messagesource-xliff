package io.github.alaugks.spring.messagesource.xliff.catalog;

import io.github.alaugks.spring.messagesource.xliff.XliffCacheableKeyGenerator;
import io.github.alaugks.spring.messagesource.xliff.XliffTranslationMessageSource;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceCacheNotExistsException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public final class CatalogCache implements CatalogInterface {
    public static final String CACHE_NAME = "messagesource.xliff.catalog.CACHE";

    private static final Logger logger = LogManager.getLogger(XliffTranslationMessageSource.class.toString());
    private Cache cache;

    CatalogCache(CacheManager cacheManager) {
        this.loadCache(cacheManager);
    }

    private void loadCache(CacheManager cacheManager) {
        if (cacheManager != null) {
            Collection<String> caches = cacheManager.getCacheNames();
            if (caches.contains(CACHE_NAME)) {
                this.cache = cacheManager.getCache(CACHE_NAME);
                return;
            }
            throw new XliffMessageSourceCacheNotExistsException(
                    String.format("Cache with name [%s] not available.", CACHE_NAME)
            );
        }
        throw new XliffMessageSourceCacheNotExistsException(
                "org.springframework.cache.CacheManager not available."
        );
    }

    @Override
    public HashMap<String, HashMap<String, String>> getAll() {
        return new HashMap<>();
    }

    @Override
    public boolean has(Locale locale, String code) {
        return this.get(locale, code) != null;
    }

    @Override
    public String get(Locale locale, String code) {
        if (!locale.toString().isEmpty()) {
            return this.getValue(
                    this.cache.get(
                            XliffCacheableKeyGenerator.createCode(locale, code)
                    )
            );
        }
        return null;
    }

    @Override
    public void put(Locale locale, String domain, String code, String targetValue) {
        this.put(locale, CatalogUtilities.concatCode(domain, code), targetValue);
    }

    void put(Locale locale, String code, String targetValue) {
        if (!locale.toString().isEmpty()) {
            this.cache.put(
                    XliffCacheableKeyGenerator.createCode(locale, code),
                    targetValue
            );
        }
    }

    private String getValue(Cache.ValueWrapper valueWrapper) {
        if (valueWrapper != null) {
            return Objects.requireNonNull(valueWrapper.get()).toString();
        }
        return null;
    }

    void initCache(CatalogInterface catalog) {
        if (catalog != null) {
            logger.debug("Init xliff catalog cache");
            catalog.getAll().forEach((langCode, domain) -> domain.forEach((code, targetValue) ->
                this.put(
                        Locale.forLanguageTag(langCode.replace("_", "-")),
                        code,
                        targetValue
                )
            ));
        }
    }
}
