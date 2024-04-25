package io.github.alaugks.spring.messagesource.xliff.records;

import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogUtilities;
import java.util.IllformedLocaleException;
import java.util.Locale;

public record Filename(String domain, String language, String region) {

    public boolean hasLocale() {
        Locale locale = locale();
        if (locale != null) {
            return !locale.toString().isEmpty();
        }
        return false;
    }

    public Locale locale() {
        try {
            return CatalogUtilities.buildLocale(this.language, this.region);
        } catch (IllformedLocaleException e) {
            return null;
        }
    }
}
