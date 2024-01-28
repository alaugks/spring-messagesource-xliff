package io.github.alaugks.spring.messagesource.xliff.catalog;

import java.util.HashMap;
import java.util.Locale;

public final class Catalog implements CatalogInterface {

    private final HashMap<String, HashMap<String, String>> catalogMap;

    public Catalog() {
        this.catalogMap = new HashMap<>();
    }

    @Override
    public HashMap<String, HashMap<String, String>> getAll() {
        return this.catalogMap;
    }

    @Override
    public String get(Locale locale, String code) {
        if (this.localeExists(locale)) {
            HashMap<String, String> languageCatalog = this.getLocaleMap(locale);
            if (languageCatalog.containsKey(code)) {
                return languageCatalog.get(code);
            }
        }
        return null;
    }

    public boolean has(Locale locale, String code) {
        return this.get(locale, code) != null;
    }

    @Override
    public void put(Locale locale, String domain, String code, String targetValue) {
        if (!locale.toString().isEmpty()) {
            String concatenatedCode = CatalogUtilities.concatCode(domain, code);
            if (this.localeExists(locale)) {
                HashMap<String, String> transUnit = this.getLocaleMap(locale);
                if (!transUnit.containsKey(concatenatedCode)) {
                    this.getLocaleMap(locale).put(concatenatedCode, targetValue);
                }
                return;
            }
            // Init catalog for locale
            HashMap<String, String> transUnit = new HashMap<>();
            transUnit.put(concatenatedCode, targetValue);
            this.catalogMap.put(CatalogUtilities.localeToKey(locale), transUnit);
        }
    }

    public boolean localeExists(Locale locale) {
        if (!locale.toString().isEmpty()) {
            return this.catalogMap.containsKey(CatalogUtilities.localeToKey(locale));
        }
        return false;
    }


    private HashMap<String, String> getLocaleMap(Locale locale) {
        return this.catalogMap.get(CatalogUtilities.localeToKey(locale));
    }
}
