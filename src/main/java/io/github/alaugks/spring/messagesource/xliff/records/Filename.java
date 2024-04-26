package io.github.alaugks.spring.messagesource.xliff.records;

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
            Locale.Builder localeBuilder = new Locale.Builder();
            if (language != null && !language.isEmpty()) {
                localeBuilder.setLanguage(language);
                if (region != null && !region.isEmpty()) {
                    localeBuilder.setRegion(region);
                }
            }
            return localeBuilder.build();
        } catch (IllformedLocaleException e) {
            return null;
        }
    }
}
