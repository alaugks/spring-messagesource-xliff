package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogBuilder;
import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogHandler;
import io.github.alaugks.spring.messagesource.xliff.catalog.xliff.XliffIdentifierInterface;
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
        ResourcesLoader resourcesLoader = ResourcesLoader
            .builder()
            .defaultLocale(builder.defaultLocale)
            .basenamesPattern(builder.basenames)
            .build();

        this.catalogHandler = new CatalogHandler(
            CatalogBuilder.builder(resourcesLoader)
                .transUnitIdentifier(builder.transUnitIdentifier)
                .build(),
            builder.cache, builder.defaultLocale,
            builder.defaultDomain
        );

        this.catalogHandler.initCache();
    }

    public static Builder builder(Cache cacheManager) {
        return new Builder(cacheManager);
    }

    public static final class Builder {

        private final Cache cache;
        private Locale defaultLocale;
        private final Set<String> basenames = new HashSet<>();
        private String defaultDomain = "messages";
        private List<XliffIdentifierInterface> transUnitIdentifier;

        private Builder(Cache cache) {
            this.cache = cache;
        }

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

        public Builder setTransUnitIdentifier(List<XliffIdentifierInterface> transUnitIdentifier) {
            this.transUnitIdentifier = transUnitIdentifier;
            return this;
        }

        public XliffTranslationMessageSource build() {
            // Default Domain
            Assert.notNull(this.defaultDomain, "Default domain is null");
            Assert.isTrue(!this.defaultDomain.trim().isEmpty(), "Default domain is empty");

            // Default Locale
            Assert.notNull(this.defaultLocale, "Default locale is null");
            Assert.isTrue(!this.defaultLocale.toString().trim().isEmpty(), "Default locale is empty");

            // Basenames
            Assert.notEmpty(this.basenames, "Basename(s) is not set");

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
        String translation = this.internalMessage(code, locale);
        if (translation != null) {
            return this.format(translation, args);
        }

        throw new NoSuchMessageException(code, locale);
    }

    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        String[] codes = resolvable.getCodes();
        if (codes != null) {
            for (String code : codes) {
                String translation = this.internalMessage(code, locale);
                if (translation != null) {
                    return this.format(translation, resolvable.getArguments());
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

    private String format(@Nullable String message, @Nullable Object[] args) {
        if (message != null && args != null && args.length > 0) {
            return new MessageFormat(message).format(args);
        }
        return message;
    }
}
