package io.github.alaugks.spring.messagesource.xliff.catalog.finder;

import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogUtilities;

import java.util.Locale;
import java.util.Map;

public class CatalogIOAdapter implements CatalogAdapterInterface {

    private final Map<String, Map<String, String>> items;

    public CatalogIOAdapter(Map<String, Map<String, String>> items) {
        this.items = items;
    }

    public String find(Locale locale, String code) {
        if (this.localeExists(locale)) {
            Map<String, String> languageCatalog = this.getLocaleMap(locale);
            if (languageCatalog.containsKey(code)) {
                return languageCatalog.get(code);
            }
        }
        return null;
    }

    private boolean localeExists(Locale locale) {
        if (!locale.toString().isEmpty()) {
            return this.items.containsKey(CatalogUtilities.localeToKey(locale));
        }
        return false;
    }

    private Map<String, String> getLocaleMap(Locale locale) {
        return this.items.get(CatalogUtilities.localeToKey(locale));
    }
}
