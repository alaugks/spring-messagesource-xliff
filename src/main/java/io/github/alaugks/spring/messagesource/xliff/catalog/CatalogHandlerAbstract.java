package io.github.alaugks.spring.messagesource.xliff.catalog;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

abstract class CatalogHandlerAbstract implements CatalogInterface {

    protected CatalogInterface nextHandler;

    public CatalogInterface nextHandler(CatalogInterface handler) {
        this.nextHandler = handler;
        return this;
    }

    @Override
    public Map<String, Map<String, String>> getAll() {
        if (this.nextHandler == null) {
            return new HashMap<>();
        }

        return this.nextHandler.getAll();
    }

    @Override
    public String get(Locale locale, String code) {
        if (this.nextHandler == null) {
            return null;
        }

        return this.nextHandler.get(locale, code);
    }

    protected String localeToLocaleKey(Locale locale) {
        Locale.Builder localeBuilder = new Locale.Builder();
        localeBuilder.setLanguage(locale.getLanguage());
        if (!locale.getCountry().isEmpty()) {
            localeBuilder.setRegion(locale.getCountry());
        }
        return normalizeLocaleKey(localeBuilder.build().toString());
    }

    protected String normalizeLocaleKey(String langCode) {
        return langCode.toLowerCase().replace("_", "-");
    }
}
