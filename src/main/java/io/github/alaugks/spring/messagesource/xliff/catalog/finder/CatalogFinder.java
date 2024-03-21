package io.github.alaugks.spring.messagesource.xliff.catalog.finder;

import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogUtilities;

import java.util.LinkedHashMap;
import java.util.Locale;

public class CatalogFinder {
    private final CatalogAdapterInterface adapter;
    private final Locale defaultLocale;
    private final String domain;

    public CatalogFinder(CatalogAdapterInterface finder, Locale defaultLocale, String domain) {
        this.adapter = finder;
        this.defaultLocale = defaultLocale;
        this.domain = domain;
    }

    public String find(Locale locale, String code) {
        if (null == locale || locale.toString().isEmpty()) {
            return null;
        }

        return this.getTranslationItemFromCatalog(locale, code);
    }

    protected static Locale buildLocaleWithLanguageRegion(Locale locale) {
        return CatalogUtilities.buildLocale(locale);
    }

    protected static Locale buildLocaleWithLanguage(Locale locale) {
        Locale.Builder localeBuilder = new Locale.Builder();
        localeBuilder.setLanguage(locale.getLanguage());
        return localeBuilder.build();
    }

    protected String getTranslationItemFromCatalog(Locale locale, String code) {
        // Find "code"
        String targetValue = this.findTranslationItemInCatalog(locale, code);
        if (targetValue == null) {
            // Find "domain.code"
            targetValue = this.findTranslationItemInCatalog(
                    locale,
                    CatalogUtilities.concatCode(this.domain, code)
            );
        }
        return targetValue;
    }

    protected String findTranslationItemInCatalog(Locale locale, String code) {
        String targetValue;
        LinkedHashMap<Integer, Locale> locales = new LinkedHashMap<>();
        // Follow the order
        locales.put(0, locale);
        locales.put(1, this.defaultLocale); // Fallback
        for (int i = 0; i < locales.size(); i++) {
            if(locales.containsKey(i) && locales.get(i) != null) {
                // Try with locale+region
                targetValue = this.adapter.find(
                        buildLocaleWithLanguageRegion(locales.get(i)),
                        code
                );

                // Try with locale
                if (targetValue == null) {
                    targetValue = this.adapter.find(
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
}
