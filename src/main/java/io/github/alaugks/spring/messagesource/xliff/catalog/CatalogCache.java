package io.github.alaugks.spring.messagesource.xliff.catalog;

import io.github.alaugks.spring.messagesource.xliff.XliffCacheableKeyGenerator;
import io.github.alaugks.spring.messagesource.xliff.XliffTranslationMessageSource;
import io.github.alaugks.spring.messagesource.xliff.catalog.finder.CatalogCacheAdapter;
import io.github.alaugks.spring.messagesource.xliff.catalog.finder.CatalogFinder;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceCacheNotExistsException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

public final class CatalogCache extends ChainAbstractHandler {
    public static final String CACHE_NAME = "messagesource.xliff.catalog.CACHE";

    private static final Logger logger = LogManager.getLogger(XliffTranslationMessageSource.class.toString());
    private Cache cache;
    private final Locale defaultLocale;
    private final String domain;

    public CatalogCache(Locale defaultLocal, String domain, CacheManager cacheManager) {
        this.defaultLocale = defaultLocal;
        this.domain = domain;
        this.loadCache(cacheManager);
    }

    // TODO??? //NOSONAR
    public Cache getCache() {
        return cache;
    }

    @Override
    public String get(Locale locale, String code) {

        CatalogFinder finder = new CatalogFinder(
                new CatalogCacheAdapter(this.cache),
                this.defaultLocale,
                this.domain
        );

        String message = finder.find(locale, code);
        if (null != message) {
            return message;
        }

        return super.get(locale, code);
    }

    @Override
    public void initCache() {
        this.initCache(super.getAll());
    }

    @Override
    public boolean has(Locale locale, String code) {
        if (null != this.get(locale, code)) {
            return true;
        }
        return super.has(locale, code);
    }

    @Override
    public void put(Locale locale, String domain, String code, String targetValue) {
        this.put(locale, CatalogUtilities.concatCode(domain, code), targetValue);
    }

    public void put(Locale locale, String code, String targetValue) {
        if (!locale.toString().isEmpty()) {
            // TODO: Check if exists    //NOSONAR
            this.cache.put(
                    XliffCacheableKeyGenerator.createCode(locale, code),
                    targetValue
            );
        }
    }

    private void initCache(Map<String, Map<String, String>> catalog) {
        if (catalog != null) {
            logger.debug("Init xliff catalog cache");
            catalog.forEach((langCode, catalogDomain) -> catalogDomain.forEach((code, targetValue) ->
                this.put(
                        Locale.forLanguageTag(langCode.replace("_", "-")),
                        code,
                        targetValue
                )
            ));
        }
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
}
