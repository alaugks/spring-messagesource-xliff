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
        if (message != null) {
            return message;
        }

        return super.get(locale, code);
    }

    @Override
    public void put(Locale locale, String domain, String code, String value) {
        if (!locale.toString().isEmpty()) {
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
}
