package io.github.alaugks.spring.messagesource.xliff.catalog;

import io.github.alaugks.spring.messagesource.xliff.records.Translation;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class BaseCatalog extends CatalogAbstractHandler {

    private final HashMap<String, Map<String, String>> catalogMap;
    private final Locale defaultLocale;
    private final String defaultDomain;

    public BaseCatalog(List<Translation> translations, Locale defaultLocal, String defaultDomain) {
        this.catalogMap = new HashMap<>();
        this.defaultLocale = defaultLocal;
        this.defaultDomain = defaultDomain;
        translations.forEach(t -> this.put(t.locale(), t.domain(), t.code(), t.value()));
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

        if (locale.toString().isEmpty() || code.isEmpty()) {
            return null;
        }

        String message = this.fromCatalog(locale, code);
        if (message != null) {
            return message;
        }

        return super.get(locale, code);
    }

    private void put(Locale locale, String domain, String code, String value) {
        if (!locale.toString().isEmpty() && !code.isEmpty()) {
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
