package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.Catalog;
import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogCache;
import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogHandler;
import io.github.alaugks.spring.messagesource.xliff.catalog.xliff.XliffCatalogBuilder;
import io.github.alaugks.spring.messagesource.xliff.ressources.ResourcesLoader;
import org.springframework.cache.CacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.lang.Nullable;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

public final class XliffTranslationMessageSource implements MessageSource {

    private final CatalogHandler catalogHandler;

    private XliffTranslationMessageSource(Builder builder) {
        ResourcesLoader resourcesLoader = ResourcesLoader
                .builder()
                .setDefaultLocale(builder.defaultLocale)
                .setDefaultDomain(builder.defaultDomain)
                .setBasenamePattern(builder.basename)
                .setBasenamesPattern(builder.basenames)
                .build();

        XliffCatalogBuilder xliffCatalogBuilder = new XliffCatalogBuilder(resourcesLoader);
        xliffCatalogBuilder.setTranslationUnitIdentifiersOrdering(builder.translationUnitIdentifiers);

        this.catalogHandler = new CatalogHandler(
                new Catalog(builder.defaultLocale, builder.defaultDomain),
                new CatalogCache(builder.defaultLocale, builder.defaultDomain, builder.cacheManager),
                xliffCatalogBuilder
        );
    }

    public static Builder builder(CacheManager cacheManager) {
        return new Builder(cacheManager);
    }

    public static final class Builder {
        private final CacheManager cacheManager;
        private Locale defaultLocale;
        private String basename;
        private Iterable<String> basenames;
        private String defaultDomain = "messages";
        private List<String> translationUnitIdentifiers;

        private Builder(CacheManager cacheManager) {
            this.cacheManager = cacheManager;
        }

        public Builder setDefaultLocale(Locale locale) {
            this.defaultLocale = locale;
            return this;
        }

        public Builder setBasenamePattern(String basename) {
            this.basename = basename;
            return this;
        }

        public Builder setBasenamesPattern(Iterable<String> basenames) {
            this.basenames = basenames;
            return this;
        }

        public Builder setDefaultDomain(String defaultDomain) {
            this.defaultDomain = defaultDomain;
            return this;
        }

        public Builder setTranslationUnitIdentifiersOrdering(List<String> translationUnitIdentifiers) {
            this.translationUnitIdentifiers = translationUnitIdentifiers;
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
                args
        );
    }

    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        CatalogHandler.Translation translation = this.internalMessage(code, locale);
        if (translation.exists()) {
            return this.format(translation.toString(), args);
        }

        throw new NoSuchMessageException(code, locale);
    }

    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        String[] codes = resolvable.getCodes();
        if (codes != null) {
            for (String code : codes) {
                CatalogHandler.Translation translation = internalMessage(code, locale);
                if (translation.exists()) {
                    return this.format(translation.toString(), resolvable.getArguments());
                }
            }
        }
        if (resolvable instanceof DefaultMessageSourceResolvable) {
            String defaultMessage = resolvable.getDefaultMessage();
            if (defaultMessage != null) {
                return this.format(defaultMessage, resolvable.getArguments());
            }
        }

        throw new NoSuchMessageException(codes != null && codes.length > 0 ? codes[codes.length - 1] : "", locale);
    }

    public void initCache() {
        this.catalogHandler.initCache();
    }

    private CatalogHandler.Translation internalMessage(String code, Locale locale) throws NoSuchMessageException {
        return this.findInCatalog(locale, code);
    }

    private String internalMessageWithDefaultMessage(String code, @Nullable String defaultMessage, Locale locale) {
        CatalogHandler.Translation translation = this.findInCatalog(locale, code);
        if (translation.exists()) {
            return translation.toString();
        }
        return defaultMessage;
    }

    private CatalogHandler.Translation findInCatalog(Locale locale, String code) {
        return this.catalogHandler.get(locale, code);
    }

    private String format(@Nullable String message, @Nullable Object[] args) {
        if (message != null && args != null && args.length > 0) {
            return new MessageFormat(message).format(args);
        }
        return message;
    }
}
