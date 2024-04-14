package io.github.alaugks.spring.messagesource.xliff.catalog;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class Catalog extends CatalogAbstractHandler {

    private final HashMap<String, Map<String, String>> catalogMap;
    private final Locale defaultLocale;
    private final String defaultDomain;

    public Catalog(Locale defaultLocal, String defaultDomain) {
        this.catalogMap = new HashMap<>();
        this.defaultLocale = defaultLocal;
        this.defaultDomain = defaultDomain;
    }

    @Override
    public Map<String, Map<String, String>> getAll() {
        if (!this.catalogMap.isEmpty()) {
            return this.catalogMap;
        }

        return super.getAll();
    }

    @Override
    public String get(Locale locale, String code) {

        if (locale.toString().isBlank() || code.isBlank()) {
            return null;
        }

        String message = this.fromCatalog(locale, code);
        if (message != null) {
            return message;
        }

        return super.get(locale, code);
    }

    @Override
    public void put(Locale locale, String domain, String code, String value) {
        if (!locale.toString().isBlank() || !code.isBlank()) {
            String localeKey = CatalogUtilities.localeToLocaleKey(locale);
            this.catalogMap.putIfAbsent(
                localeKey,
                new HashMap<>()
            );
            this.catalogMap.get(localeKey).putIfAbsent(
                CatalogUtilities.concatCode(domain, code),
                value
            );
        }
    }

    private String fromCatalog(Locale locale, String code) {
        String value;

        // Code+LocaleRegion
        value = this.findInCatalog(
            CatalogUtilities.buildLocale(locale),
            code
        );
        if (value != null) {
            return value;
        }

        // Code+LocaleRegion / DomainCode+LanguageRegion
        value = this.findInCatalog(
            CatalogUtilities.buildLocale(locale),
            CatalogUtilities.concatCode(this.defaultDomain, code)
        );
        if (value != null) {
            return value;
        }

        // Code+Language / DomainCode+Language
        value = this.findInCatalog(
            CatalogUtilities.buildLocaleWithoutRegion(locale),
            code
        );
        if (value != null) {
            return value;
        }

        // Code+Language / DomainCode+Language
        value = this.findInCatalog(
            CatalogUtilities.buildLocaleWithoutRegion(locale),
            CatalogUtilities.concatCode(this.defaultDomain, code)
        );
        if (value != null) {
            return value;
        }

        // Code+DefaultLanguageRegion / DomainCode+DefaultLanguageRegion
        value = this.findInCatalog(
            CatalogUtilities.buildLocale(this.defaultLocale),
            code
        );
        if (value != null) {
            return value;
        }

        // Code+DefaultLanguageRegion / DomainCode+DefaultLanguageRegion
        value = this.findInCatalog(
            CatalogUtilities.buildLocale(this.defaultLocale),
            CatalogUtilities.concatCode(this.defaultDomain, code)
        );
        if (value != null) {
            return value;
        }

        // Code+DefaultLanguage / DomainCode+DefaultLanguage
        value = this.findInCatalog(
            CatalogUtilities.buildLocaleWithoutRegion(this.defaultLocale),
            code
        );
        if (value != null) {
            return value;
        }

        // Code+DefaultLanguage / DomainCode+DefaultLanguage
        value = this.findInCatalog(
            CatalogUtilities.buildLocaleWithoutRegion(this.defaultLocale),
            CatalogUtilities.concatCode(this.defaultDomain, code)
        );
        if (value != null) {
            return value;
        }

        return value;
    }

    public String findInCatalog(Locale locale, String code) {
        if (this.catalogMap.containsKey(CatalogUtilities.localeToLocaleKey(locale))) {
            Map<String, String> languageCatalog = this.catalogMap.get(CatalogUtilities.localeToLocaleKey(locale));
            if (languageCatalog.containsKey(code)) {
                return languageCatalog.get(code);
            }
        }
        return null;
    }
}
