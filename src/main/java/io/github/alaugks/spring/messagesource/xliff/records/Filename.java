package io.github.alaugks.spring.messagesource.xliff.records;

import java.util.IllformedLocaleException;
import java.util.Locale;

public record Filename(String domain, String language, String region) {

    public boolean hasLocale() {
        Locale locale = locale();
        return locale != null && !locale.toString().isEmpty();
    }

    public Locale locale() {
        try {
            if (language != null && !language.isEmpty()) {
                Locale.Builder localeBuilder = new Locale.Builder();
                localeBuilder.setLanguage(language);
                if (region != null && !region.isEmpty()) {
                    localeBuilder.setRegion(region);
                }
                return localeBuilder.build();
            }
            return null;
        } catch (IllformedLocaleException e) {
            return null;
        }
    }
}
