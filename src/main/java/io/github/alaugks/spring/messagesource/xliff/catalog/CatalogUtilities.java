package io.github.alaugks.spring.messagesource.xliff.catalog;

import java.util.Locale;

public class CatalogUtilities {

    private CatalogUtilities() {
        throw new IllegalStateException("CatalogUtilities class");
    }

    public static String localeToKey(Locale locale) {
        return buildLocale(locale).toString().trim().toLowerCase().replace("_", "-");
    }

    public static String concatCode(String domain, String code) {
        return domain + "." + code;
    }

    public static Locale buildLocale(Locale locale) {
        Locale.Builder localeBuilder = new Locale.Builder();
        localeBuilder.setLanguage(locale.getLanguage());
        if (!locale.getCountry().isEmpty()) {
            localeBuilder.setRegion(locale.getCountry());
        }
        return localeBuilder.build();
    }

    public static Locale buildLocale(String language, String region) {
        Locale.Builder localeBuilder = new Locale.Builder();
        if (language != null && !language.isEmpty()) {
            localeBuilder.setLanguage(language);
            // Set region only is present
            if (region != null && !region.isEmpty()) {
                localeBuilder.setRegion(region);
            }
        }
        return localeBuilder.build();
    }
}
