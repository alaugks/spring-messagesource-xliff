package io.github.alaugks.spring.messagesource.xliff.catalog.finder;

import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogUtilities;
import java.util.Locale;
import java.util.Map;

public final class CatalogFileAdapter implements CatalogAdapterInterface {

    private final Map<String, Map<String, String>> items;

    public CatalogFileAdapter(Map<String, Map<String, String>> items) {
        this.items = items;
    }

    public String find(Locale locale, String code) {
        if (this.items.containsKey(CatalogUtilities.localeToLocaleKey(locale))) {
            Map<String, String> languageCatalog = this.items.get(CatalogUtilities.localeToLocaleKey(locale));
            if (languageCatalog.containsKey(code)) {
                return languageCatalog.get(code);
            }
        }
        return null;
    }
}
