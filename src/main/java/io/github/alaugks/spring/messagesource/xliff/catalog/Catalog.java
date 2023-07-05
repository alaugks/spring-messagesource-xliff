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
            HashMap<String, String> languageCatalog = this.getLocaleHashMap(locale);
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
        if (locale.toString().length() > 0) {
            String concatCode = CatalogUtilities.contactCode(domain, code);
            if (this.localeExists(locale)) {
                HashMap<String, String> transUnit = this.getLocaleHashMap(locale);
                if (!transUnit.containsKey(concatCode)) {
                    this.getLocaleHashMap(locale).put(concatCode, targetValue);
                }
                return;
            }
            // Init catalog for locale
            HashMap<String, String> transUnit = new HashMap<>();
            transUnit.put(concatCode, targetValue);
            this.catalogMap.put(CatalogUtilities.localeToKey(locale), transUnit);
        }
    }

    public boolean localeExists(Locale locale) {
        if (locale.toString().length() > 0) {
            return this.catalogMap.containsKey(CatalogUtilities.localeToKey(locale));
        }
        return false;
    }


    private HashMap<String, String> getLocaleHashMap(Locale locale) {
        return this.catalogMap.get(CatalogUtilities.localeToKey(locale));
    }
}
