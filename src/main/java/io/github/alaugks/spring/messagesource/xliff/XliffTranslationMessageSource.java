package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.base.BaseTranslationMessageSource;
import io.github.alaugks.spring.messagesource.base.catalog.CatalogCache;
import io.github.alaugks.spring.messagesource.base.catalog.CatalogHandler;
import io.github.alaugks.spring.messagesource.xliff.XliffCatalog.Xliff12Identifier;
import io.github.alaugks.spring.messagesource.xliff.XliffCatalog.Xliff2xIdentifier;
import io.github.alaugks.spring.messagesource.xliff.XliffCatalog.XliffIdentifierInterface;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.cache.Cache;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.lang.Nullable;

public class XliffTranslationMessageSource implements MessageSource {

    private final MessageSource messageSourceDelegate;

    private XliffTranslationMessageSource(MessageSource messageSourceDelegate) {
        this.messageSourceDelegate = messageSourceDelegate;
    }

    public static Builder builder(Locale defaultLocale, String basename) {
        return new Builder(defaultLocale, List.of(basename));
    }

    public static Builder builder(Locale defaultLocale, List<String> basenames) {
        return new Builder(defaultLocale, basenames);
    }

    public static final class Builder {

        private final Locale defaultLocale;
        private final Set<String> basenames;
        private String defaultDomain = "messages";
        private List<String> fileExtensions = List.of("xlf", "xliff");
        private Cache cache;
        private List<XliffIdentifierInterface> identifier = List.of(
            new Xliff12Identifier(List.of("resname", "id")),
            new Xliff2xIdentifier(List.of("id"))
        );

        public Builder(Locale defaultLocale, List<String> basenames) {
            this.defaultLocale = defaultLocale;
            this.basenames = new HashSet<>(basenames);

        }

        public Builder defaultDomain(String defaultDomain) {
            this.defaultDomain = defaultDomain;
            return this;
        }

        public Builder withCache(Cache cache) {
            this.cache = cache;
            return this;
        }

        public Builder fileExtensions(List<String> fileExtensions) {
            this.fileExtensions = fileExtensions;
            return this;
        }

        public Builder identifier(List<XliffIdentifierInterface> identifier) {
            this.identifier = identifier;
            return this;
        }

        public XliffTranslationMessageSource build() {
            CatalogHandler.Builder catalogHandler = CatalogHandler.builder(
                new XliffCatalog(
                    this.basenames,
                    this.fileExtensions,
                    this.defaultDomain,
                    this.defaultLocale,
                    this.identifier
                ).createCatalog()
            );

            if (this.cache != null) {
                catalogHandler.catalogCache(new CatalogCache(this.cache));
            }

            return new XliffTranslationMessageSource(
                new BaseTranslationMessageSource(catalogHandler.build())
            );
        }
    }

    @Nullable
    @Override
    public String getMessage(String code, @Nullable Object[] args, @Nullable String defaultMessage, Locale locale) {
        return this.messageSourceDelegate.getMessage(code, args, defaultMessage, locale);
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        return this.messageSourceDelegate.getMessage(code, args, locale);
    }

    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        return this.messageSourceDelegate.getMessage(resolvable, locale);
    }
}
