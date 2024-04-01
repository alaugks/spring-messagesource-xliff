package io.github.alaugks.spring.messagesource.xliff.catalog;

import io.github.alaugks.spring.messagesource.xliff.XliffCacheableKeyGenerator;
import io.github.alaugks.spring.messagesource.xliff.catalog.finder.CatalogCacheAdapter;
import io.github.alaugks.spring.messagesource.xliff.catalog.finder.CatalogFinder;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceCacheNotExistsException;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CatalogCache extends CatalogAbstractHandler {
    public static final String CACHE_NAME = "messagesource.xliff.catalog.CACHE";

    private Cache cache;
    private final Locale defaultLocale;
    private final String domain;

    public CatalogCache(Locale defaultLocal, String domain, CacheManager cacheManager) {
        this.defaultLocale = defaultLocal;
        this.domain = domain;
        this.loadCache(cacheManager);
    }

    @Override
    public Map<String, Map<String, String>> getAll() {
        try {
            Map<String, Map<String, String>> result = new HashMap<>();
            Map<Object, Object> items = new HashMap<>((ConcurrentHashMap<?, ?>) this.cache.getNativeCache());
            items.forEach((code, value) -> {
                String[] split = code.toString().split("\\|");
                result.putIfAbsent(
                        split[0],
                        new HashMap<>()
                );
                result.get(split[0]).putIfAbsent(
                        split[1],
                        value.toString()
                );
            });
            return result;
        } catch (Exception e) {
            return super.getAll();
        }
    }

    @Override
    public String get(Locale locale, String code) {

        CatalogFinder finder = new CatalogFinder(
                new CatalogCacheAdapter(this.cache),
                this.defaultLocale,
                this.domain
        );

        String message = finder.find(locale, code);
        if (message != null) {
            return message;
        }

        return super.get(locale, code);
    }

    @Override
    public void put(Locale locale, String domain, String code, String value) {
        this.put(locale, CatalogUtilities.concatCode(domain, code), value);
    }

    public void put(Locale locale, String code, String value) {
        if (!locale.toString().isEmpty()) {
            this.cache.putIfAbsent(
                    XliffCacheableKeyGenerator.createCode(locale, code),
                    value
            );
        }
    }

    public void initCache() {
        super.getAll().forEach((langCode, catalogDomain) -> catalogDomain.forEach((code, value) ->
                this.put(
                        Locale.forLanguageTag(langCode.replace("_", "-")),
                        code,
                        value
                )
        ));
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
