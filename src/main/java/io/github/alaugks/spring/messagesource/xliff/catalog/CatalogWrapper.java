package io.github.alaugks.spring.messagesource.xliff.catalog;

import io.github.alaugks.spring.messagesource.xliff.XliffTranslationMessageSource;
import io.github.alaugks.spring.messagesource.xliff.ressources.ResourcesLoaderInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.CacheManager;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Objects;

public class CatalogWrapper {
    private static final Logger logger = LogManager.getLogger(XliffTranslationMessageSource.class.toString());
    private final CatalogCache catalogCache;
    private final CatalogInterface catalog;
    private final CatalogBuilderInterface catalogBuilder;
    private final ResourcesLoaderInterface resourcesLoader;
    private String defaultDomain = "messages";

    public CatalogWrapper(CacheManager cacheManager,
                          ResourcesLoaderInterface resourcesLoader,
                          CatalogBuilderInterface catalogBuilder,
                          CatalogInterface catalog
    ) {
        this.catalog = catalog;
        this.resourcesLoader = resourcesLoader;
        this.catalogBuilder = catalogBuilder;
        this.catalogCache = new CatalogCache(cacheManager);
    }

    private static Locale buildLocaleWithLanguageRegion(Locale locale) {
        return CatalogUtilities.buildLocale(locale);
    }

    private static Locale buildLocaleWithLanguage(Locale locale) {
        Locale.Builder localeBuilder = new Locale.Builder();
        localeBuilder.setLanguage(locale.getLanguage());
        return localeBuilder.build();
    }

    public Translation get(Locale locale, String code) {
        // Check cache
        String targetValue = this.getTranslationItemFromCatalog(
                this.catalogCache,
                locale,
                code
        );

        // Exists in cache?
        if (targetValue != null) {
            return new Translation(code, targetValue);
        }

        // Check catalog
        CatalogInterface loadedCatalog = this.loadCatalog();
        targetValue = this.getTranslationItemFromCatalog(
                loadedCatalog,
                locale,
                code
        );

        // If exists then init cache, because it was not in the cache. Cache empty?
        if (targetValue != null) {
            logger.debug("Re-init xliff catalog cache");
            this.initCache(loadedCatalog);
        }

        // If not exists then is the targetValue is the code.
        //
        // Non-existing code is added to the cache to fetch the non-existing code from
        // the cache so as not to continue looking in the messagesource files.
        String targetValueCode = null;
        if (targetValue == null) {
            targetValueCode = code;
            this.put(locale, code, targetValueCode);
        }

        return new Translation(code, (targetValue != null ? targetValue : targetValueCode));
    }

    void put(Locale locale, String code, String targetValue) {
        this.catalogCache.put(
                locale,
                code,
                targetValue
        );
    }

    void put(Locale locale, String domain, String code, String targetValue) {
        this.catalogCache.put(
                locale,
                CatalogUtilities.concatCode(domain, code),
                targetValue
        );
    }

    public void setDefaultDomain(String defaultDomain) {
        this.defaultDomain = defaultDomain;
    }

    public void initCache(CatalogInterface catalog) {
        // Without cache do not get load catalog
        this.catalogCache.initCache(catalog);
    }

    public void initCache() {
        // Without cache do not get load catalog
        this.catalogCache.initCache(this.loadCatalog());
    }

    private CatalogInterface loadCatalog() {
        return this.catalogBuilder.createCatalog(this.resourcesLoader, this.catalog);
    }

    private String findTranslationItemInCatalog(CatalogInterface catalog, Locale locale, String code) {
        String targetValue;
        LinkedHashMap<Integer, Locale> locales = new LinkedHashMap<>();
        // Follow the order
        locales.put(0, locale); // First
        locales.put(1, this.resourcesLoader.getDefaultLocale()); // Second
        for (int i = 0; i < locales.size(); i++) {
            if(locales.containsKey(i) && locales.get(i) != null) {
                // Try with locale+region
                targetValue = catalog.get(
                        buildLocaleWithLanguageRegion(locales.get(i)),
                        code
                );

                // Try with locale
                if (targetValue == null) {
                    targetValue = catalog.get(
                            buildLocaleWithLanguage(locales.get(i)),
                            code
                    );
                }

                if (targetValue != null) {
                    return targetValue;
                }
            }
        }
        return null;
    }

    private String getTranslationItemFromCatalog(CatalogInterface catalog, Locale locale, String code) {
        // Find "code"
        String targetValue = this.findTranslationItemInCatalog(catalog, locale, code);
        if (targetValue == null) {
            // Find "domain.code"
            targetValue = this.findTranslationItemInCatalog(
                    catalog,
                    locale,
                    CatalogUtilities.concatCode(this.defaultDomain, code)
            );
        }
        return targetValue;
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
