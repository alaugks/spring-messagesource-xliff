package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.Catalog;
import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogWrapper;
import io.github.alaugks.spring.messagesource.xliff.catalog.xliff.XliffCatalogBuilder;
import io.github.alaugks.spring.messagesource.xliff.ressources.ResourcesLoader;
import io.github.alaugks.spring.messagesource.xliff.ressources.ResourcesLoaderInterface;
import org.springframework.cache.CacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.lang.Nullable;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

public final class XliffMessageSource implements MessageSource {

    private final CatalogWrapper catalogWrapper;

    private XliffMessageSource(Builder builder) {
        ResourcesLoaderInterface resourcesLoader = new ResourcesLoader();
        resourcesLoader.setDefaultLocale(builder.defaultLocale);
        resourcesLoader.setBasenamePattern(builder.basename);
        resourcesLoader.setBasenamesPattern(builder.basenames);

        XliffCatalogBuilder xliffCatalogBuilder = new XliffCatalogBuilder();
        xliffCatalogBuilder.setTranslationUnitIdentifiersOrdering(builder.translationUnitIdentifiers);

        this.catalogWrapper = new CatalogWrapper(
                builder.cacheManager,
                resourcesLoader,
                xliffCatalogBuilder,
                new Catalog()
        );

        this.catalogWrapper.setDefaultDomain(builder.defaultDomain);
    }

    public static Builder builder(CacheManager cacheManager) {
        return new Builder(cacheManager);
    }

    public static final class Builder {
        private final CacheManager cacheManager;
        private Locale defaultLocale = Locale.forLanguageTag("en");
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

        public XliffMessageSource build() {
            return new XliffMessageSource(this);
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
        CatalogWrapper.Translation translation = this.internalMessage(code, locale);
        if (translation.exists()) {
            return this.format(translation.toString(), args);
        }

        throw new NoSuchMessageException(code, locale);
    }

    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        String[] codes = resolvable.getCodes();
        if (codes != null) {
            for (String code : codes) {
                CatalogWrapper.Translation translation = internalMessage(code, locale);
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

    private CatalogWrapper.Translation internalMessage(String code, Locale locale) throws NoSuchMessageException {
        return this.findInCatalog(locale, code);
    }

    private String internalMessageWithDefaultMessage(String code, @Nullable String defaultMessage, Locale locale) {
        CatalogWrapper.Translation translation = this.findInCatalog(locale, code);
        if (translation.exists()) {
            return translation.toString();
        }
        return defaultMessage;
    }

    private CatalogWrapper.Translation findInCatalog(Locale locale, String code) {
        return this.catalogWrapper.get(locale, code);
    }

    public void initCache() {
        this.catalogWrapper.initCache();
    }

    private String format(@Nullable String message, @Nullable Object[] args) {
        if (message != null && args != null && args.length > 0) {
            return new MessageFormat(message).format(args);
        }
        return message;
    }
}
