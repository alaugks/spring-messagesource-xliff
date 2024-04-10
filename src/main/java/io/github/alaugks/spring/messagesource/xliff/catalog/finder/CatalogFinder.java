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

        // Code+LocaleRegion / DomainCode+LanguageRegion
        value = this.fromCatalogSubStep(localeLangRegion, code);
        if (value != null) {
            return value;
        }

        // Code+Language / DomainCode+Language
        value = this.fromCatalogSubStep(localeLang, code);
        if (value != null) {
            return value;
        }

        // Code+DefaultLanguageRegion / DomainCode+DefaultLanguageRegion
        value = this.fromCatalogSubStep(defaultLocaleLangRegion, code);
        if (value != null) {
            return value;
        }

        // Code+DefaultLanguage / DomainCode+DefaultLanguage
        value = this.fromCatalogSubStep(defaultLocaleLang, code);
        return value;
    }

    private String fromCatalogSubStep(Locale locale, String code) {
        String value = this.adapter.find(locale, code);
        if (value != null) {
            return value;
        }
        // Check with domain as prefix
        return this.adapter.find(locale, CatalogUtilities.concatCode(this.domain, code));
    }
}
