package io.github.alaugks.spring.messagesource.xliff.catalog.finder;

import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogUtilities;

import java.util.Locale;

public final class CatalogFinder {
    private final CatalogAdapterInterface adapter;
    private final Locale defaultLocale;
    private final String domain;

    public CatalogFinder(CatalogAdapterInterface finder, Locale defaultLocale, String domain) {
        this.adapter = finder;
        this.defaultLocale = defaultLocale;
        this.domain = domain;
    }

    public String find(Locale locale, String code) {
        if (locale.toString().isEmpty()) {
            return null;
        }

        return this.fromCatalog(locale, code);
    }

    private String fromCatalog(Locale locale, String code) {
        String value;
        Locale localeLangRegion = CatalogUtilities.buildLocale(locale);
        Locale localeLang = CatalogUtilities.buildLocaleWithoutRegion(locale);
        Locale defaultLocaleLangRegion = CatalogUtilities.buildLocale(this.defaultLocale);
        Locale defaultLocaleLang = CatalogUtilities.buildLocaleWithoutRegion(this.defaultLocale);
        String domainCode = CatalogUtilities.concatCode(this.domain, code);

        // CodeLocaleRegion
        value = this.adapter.find(localeLangRegion, code);
        if (value != null) {
            return value;
        }

        // CodeLanguage
        value = this.adapter.find(localeLang, code);
        if (value != null) {
            return value;
        }

        // CodeDefaultLanguageRegion
        value = this.adapter.find(defaultLocaleLangRegion, code);
        if (value != null) {
            return value;
        }

        // CodeDefaultLanguage
        value = this.adapter.find(defaultLocaleLang, code);
        if (value != null) {
            return value; // Not reached by tests.
        }

        // DomainCodeLanguageRegion
        value = this.adapter.find(localeLangRegion, domainCode);
        if (value != null) {
            return value;
        }

        // DomainCodeLanguage
        value = this.adapter.find(localeLang, domainCode);
        if (value != null) {
            return value;
        }

        // DomainCodeDefaultLanguageRegion
        value = this.adapter.find(defaultLocaleLangRegion, domainCode);
        if (value != null) {
            return value;
        }

        // DomainCodeDefaultLanguage
        value = this.adapter.find(defaultLocaleLang, domainCode);
        if (value != null) {
            return value; // Not reached by tests.
        }

        return null;
    }

}
