package io.github.alaugks.spring.messagesource.xliff.ressources;

import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogUtilities;

import java.util.IllformedLocaleException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ResourcesFileNameParser {
    private final String filename;

    public ResourcesFileNameParser(String filename) {
        this.filename = filename;
    }

    public Dto parse() {
        String regexp = "^(?<domain>[a-z0-9]+)(?:([_-](?<language>[a-z]+))(?:[_-](?<region>[a-z]+))?)?";
        Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(this.filename);

        if (matcher.find()) {
            String domain = this.getGroup(matcher, "domain");
            String language = this.getGroup(matcher, "language");
            String region = this.getGroup(matcher, "region");
            return new Dto(
                    domain,
                    language,
                    region
            );
        }
        return null;
    }

    public static class Dto {
        private final String domain;
        private final String language;
        private final String region;

        public Dto(String domain, String lang, String country) {
            this.domain = domain;
            this.language = lang;
            this.region = country;
        }

        public String getDomain() {
            return domain;
        }

        public String getLanguage() {
            return language;
        }

        public String getRegion() {
            return region;
        }

        public boolean hasLocale() {
            return getLocale() != null && !getLocale().toString().isEmpty();
        }

        public Locale getLocale() {
            try {
                return CatalogUtilities.buildLocale(this.language, this.region);
            } catch (IllformedLocaleException e) {
                return null;
            }
        }
    }

    private String getGroup(Matcher matcher, String groupName) {
        try {
            return matcher.group(groupName);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return null;
        }
    }
}
