package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.Catalog;
import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogCache;
import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogWrapper;
import io.github.alaugks.spring.messagesource.xliff.catalog.xliff.XliffCatalogBuilder;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceRuntimeException;
import io.github.alaugks.spring.messagesource.xliff.ressources.ResourcesLoader;
import io.github.alaugks.spring.messagesource.xliff.ressources.ResourcesLoaderInterface;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.lang.Nullable;

@SuppressWarnings("java:S1133")
public class XliffTranslationMessageSource implements MessageSource {

    private final CatalogWrapper catalogWrapper;
    private final ResourcesLoaderInterface resourcesLoader = new ResourcesLoader();
    private final XliffCatalogBuilder xliffCatalogBuilder = new XliffCatalogBuilder();

    /**
     * @deprecated
     */
    @Deprecated(since = "2.0.0")
    public XliffTranslationMessageSource() {
        this(new ConcurrentMapCacheManager(CatalogCache.CACHE_NAME));
    }

    /**
     * @deprecated
     */
    @Deprecated(since = "2.0.0")
    public XliffTranslationMessageSource(CacheManager cacheManager) {
        this.catalogWrapper = new CatalogWrapper(
                cacheManager,
                this.resourcesLoader,
                this.xliffCatalogBuilder,
                new Catalog()
        );
    }

    /**
     * @deprecated
     */
    @Deprecated(since = "2.0.0")
    public XliffTranslationMessageSource setDefaultLocale(Locale locale) {
        this.resourcesLoader.setDefaultLocale(locale);
        return this;
    }

    /**
     * @deprecated
     */
    @Deprecated(since = "2.0.0")
    public XliffTranslationMessageSource setBasenamePattern(String basename) {
        this.resourcesLoader.setBasenamePattern(basename);
        return this;
    }

    /**
     * @deprecated
     */
    @Deprecated(since = "2.0.0")
    public XliffTranslationMessageSource setBasenamesPattern(Iterable<String> basenames) {
        this.resourcesLoader.setBasenamesPattern(basenames);
        return this;
    }

    /**
     * @deprecated
     */
    @Deprecated(since = "2.0.0")
    public XliffTranslationMessageSource setDefaultDomain(String defaultDomain) {
        this.catalogWrapper.setDefaultDomain(defaultDomain);
        return this;
    }

    /**
     * @deprecated
     */
    @Deprecated(since = "2.0.0")
    public XliffTranslationMessageSource setTranslationUnitIdentifiersOrdering(List<String> translationUnitIdentifiers) {
        this.xliffCatalogBuilder.setTranslationUnitIdentifiersOrdering(translationUnitIdentifiers);
        return this;
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
        CatalogWrapper.Translation translation = this.internalMessage(code, locale);
        if (translation.exists()) {
            return this.format(translation.toString(), args, locale);
        }

        throw new NoSuchMessageException(code, locale);
    }

    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        String[] codes = resolvable.getCodes();
        if (codes != null) {
            for (String code : codes) {
                CatalogWrapper.Translation translation = internalMessage(code, locale);
                if (translation.exists()) {
                    return this.format(translation.toString(), resolvable.getArguments(), locale);
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
        if (this.resourcesLoader.getDefaultLocale() == null || this.resourcesLoader.getDefaultLocale().toString().isEmpty()) {
            throw new XliffMessageSourceRuntimeException("Default language is not set or empty.");
        }
        //this.catalogWrapper.initCache();
    }

    private String format(@Nullable String message, @Nullable Object[] args, Locale locale) {
        if (message != null && args != null && args.length > 0) {
            return new MessageFormat(message, locale).format(args);
        }
        return message;
    }
}
