package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogHandler;
import io.github.alaugks.spring.messagesource.xliff.catalog.XliffCatalogBuilder;
import io.github.alaugks.spring.messagesource.xliff.ressources.ResourcesLoader;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.cache.Cache;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class XliffTranslationMessageSource implements MessageSource {

    private final CatalogHandler catalogHandler;

    private XliffTranslationMessageSource(Builder builder) {

        ResourcesLoader resourcesLoader = new ResourcesLoader(
            builder.defaultLocale,
            builder.basenames,
            List.of("xlf", "xliff")
        );

        XliffCatalogBuilder xliffCatalogBuilder = new XliffCatalogBuilder(
            resourcesLoader.getTranslationFiles(),
            builder.defaultDomain,
            builder.defaultLocale
        );

        this.catalogHandler = new CatalogHandler(
            xliffCatalogBuilder.getBaseCatalog(),
            builder.cache
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Cache cache;
        private Locale defaultLocale;
        private final Set<String> basenames = new HashSet<>();
        private String defaultDomain = "messages";

        public Builder defaultLocale(Locale locale) {
            this.defaultLocale = locale;
            return this;
        }

        public Builder basenamePattern(String basename) {
            this.basenamesPattern(List.of(basename));
            return this;
        }

        public Builder basenamesPattern(Iterable<String> basenames) {
            if (basenames != null) {
                Set<String> basenamesSet = StreamSupport.stream(basenames.spliterator(), false)
                    .collect(Collectors.toSet());
                for (String basename : basenamesSet) {
                    Assert.hasText(basename, "Basename must not be empty");
                    this.basenames.add(basename.trim());
                }
            }
            return this;
        }

        public Builder defaultDomain(String defaultDomain) {
            this.defaultDomain = defaultDomain;
            return this;
        }

        public Builder withCache(Cache cache) {
            this.cache = cache;
            return this;
        }

        public XliffTranslationMessageSource build() {
            return new XliffTranslationMessageSource(this);
        }
    }

    @Nullable
    public String getMessage(String code, @Nullable Object[] args, @Nullable String defaultMessage, Locale locale) {
        return this.format(
            this.internalMessageWithDefaultMessage(code, defaultMessage, locale),
            args,
            locale
        );
    }

    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        String message = this.internalMessage(code, locale);
        if (message != null) {
            return this.format(message, args, locale);
        }

        throw new NoSuchMessageException(code, locale);
    }

    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        String[] codes = resolvable.getCodes();
        if (codes != null) {
            for (String code : codes) {
                String message = this.internalMessage(code, locale);
                if (message != null) {
                    return this.format(message, resolvable.getArguments(), locale);
                }
            }
        }
        if (resolvable instanceof DefaultMessageSourceResolvable) {
            String defaultMessage = resolvable.getDefaultMessage();
            if (defaultMessage != null) {
                return this.format(defaultMessage, resolvable.getArguments(), locale);
            }
        }

        throw new NoSuchMessageException(codes != null && codes.length > 0 ? codes[codes.length - 1] : "", locale);
    }

    private String internalMessage(String code, Locale locale) throws NoSuchMessageException {
        return this.findInCatalog(locale, code);
    }

    private String internalMessageWithDefaultMessage(String code, @Nullable String defaultMessage, Locale locale) {
        String translation = this.findInCatalog(locale, code);
        if (translation != null) {
            return translation;
        }
        return defaultMessage;
    }

    private String findInCatalog(Locale locale, String code) {
        return this.catalogHandler.get(locale, code);
    }

    private String format(@Nullable String message, @Nullable Object[] args, Locale locale) {
        if (message != null && args != null && args.length > 0) {
            return new MessageFormat(message, locale).format(args);
        }
        return message;
    }
}
