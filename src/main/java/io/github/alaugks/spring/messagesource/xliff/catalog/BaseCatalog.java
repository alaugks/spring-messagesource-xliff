package io.github.alaugks.spring.messagesource.xliff.catalog;

import io.github.alaugks.spring.messagesource.xliff.records.Translation;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class BaseCatalog extends CatalogHandlerAbstract {

    private final HashMap<String, Map<String, String>> catalogMap;
    private final Locale defaultLocale;
    private final String defaultDomain;
    private final List<Translation> translations;

    private BaseCatalog(Builder builder) {
        this.catalogMap = new HashMap<>();
        this.translations = builder.translations;
        this.defaultLocale = builder.defaultLocale;
        this.defaultDomain = builder.defaultDomain;
        this.build();
    }

    public static Builder builder(List<Translation> translations, Locale defaultLocal, String defaultDomain) {
        return new Builder(translations, defaultLocal, defaultDomain);
    }

    public static final class Builder {

        private final Locale defaultLocale;
        private final String defaultDomain;
        private final List<Translation> translations;

        Builder(List<Translation> translations, Locale defaultLocal, String defaultDomain) {
            this.translations = translations;
            this.defaultLocale = defaultLocal;
            this.defaultDomain = defaultDomain;
        }

        public BaseCatalog build() {
            return new BaseCatalog(this);
        }
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

    private void build() {
        this.translations.forEach(t -> this.put(t.locale(), t.domain(), t.code(), t.value()));
    }

    private void put(Locale locale, String domain, String code, String value) {
        if (!locale.toString().isEmpty() && !code.isEmpty()) {
            String localeKey = super.localeToLocaleKey(locale);
            this.catalogMap.putIfAbsent(
                localeKey,
                new HashMap<>()
            );
            this.catalogMap.get(localeKey).putIfAbsent(
                concatCode(domain, code),
                value
            );
        }
    }

    private String fromCatalog(Locale locale, String code) {
        String value;

        // Code+LocaleRegion
        value = this.findInCatalog(
            locale,
            code
        );
        if (value != null) {
            return value;
        }

        // Code+LocaleRegion / DomainCode+LanguageRegion
        value = this.findInCatalog(
            locale,
            concatCode(this.defaultDomain, code)
        );
        if (value != null) {
            return value;
        }

        // Code+Language / DomainCode+Language
        value = this.findInCatalog(
            buildLocaleWithoutRegion(locale),
            code
        );
        if (value != null) {
            return value;
        }

        // Code+Language / DomainCode+Language
        value = this.findInCatalog(
            buildLocaleWithoutRegion(locale),
            concatCode(this.defaultDomain, code)
        );
        if (value != null) {
            return value;
        }

        // Code+DefaultLanguageRegion / DomainCode+DefaultLanguageRegion
        value = this.findInCatalog(
            this.defaultLocale,
            code
        );
        if (value != null) {
            return value;
        }

        // Code+DefaultLanguageRegion / DomainCode+DefaultLanguageRegion
        value = this.findInCatalog(
            this.defaultLocale,
            concatCode(this.defaultDomain, code)
        );
        if (value != null) {
            return value;
        }

        return value;
    }

    public String findInCatalog(Locale locale, String code) {
        String localeKey = super.localeToLocaleKey(locale);
        if (this.catalogMap.containsKey(localeKey)) {
            Map<String, String> languageCatalog = this.catalogMap.get(localeKey);
            if (languageCatalog.containsKey(code)) {
                return languageCatalog.get(code);
            }
        }
        return null;
    }

    private static String concatCode(String domain, String code) {
        return domain + "." + code;
    }

    private static Locale buildLocaleWithoutRegion(Locale locale) {
        Locale.Builder localeBuilder = new Locale.Builder();
        localeBuilder.setLanguage(locale.getLanguage());
        return localeBuilder.build();
    }
}
