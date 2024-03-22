package io.github.alaugks.spring.messagesource.xliff.catalog;

import io.github.alaugks.spring.messagesource.xliff.catalog.finder.CatalogFileAdapter;
import io.github.alaugks.spring.messagesource.xliff.catalog.finder.CatalogFinder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class Catalog extends CatalogAbstractHandler {

    private final HashMap<String, Map<String, String>> catalogMap;
    private final Locale defaultLocale;
    private final String domain;

    public Catalog(Locale defaultLocal, String domain) {
        this.catalogMap = new HashMap<>();
        this.defaultLocale = defaultLocal;
        this.domain = domain;
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
        CatalogFinder finder = new CatalogFinder(
                new CatalogFileAdapter(this.getAll()),
                this.defaultLocale,
                this.domain
        );

        String message = finder.find(locale, code);
        if (null != message) {
            return message;
        }

        return super.get(locale, code);
    }

    @Override
    public void put(Locale locale, String domain, String code, String targetValue) {
        if (!locale.toString().isEmpty()) {
            String concatenatedCode = CatalogUtilities.concatCode(domain, code);
            if (this.localeExists(locale)) {
                Map<String, String> transUnit = this.getLocaleMap(locale);
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

    private boolean localeExists(Locale locale) {
        if (!locale.toString().isEmpty()) {
            return this.catalogMap.containsKey(CatalogUtilities.localeToKey(locale));
        }
        return false;
    }


    private Map<String, String> getLocaleMap(Locale locale) {
        return this.catalogMap.get(CatalogUtilities.localeToKey(locale));
    }
}
