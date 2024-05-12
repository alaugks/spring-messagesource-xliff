package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.base.BaseTranslationMessageSource;
import io.github.alaugks.spring.messagesource.base.catalog.CatalogCache;
import io.github.alaugks.spring.messagesource.base.catalog.CatalogHandler;
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

    private final MessageSource messageSource;

    private XliffTranslationMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public static Builder builder(String basename, Locale locale) {
        return new Builder(List.of(basename), locale);
    }

    public static Builder builder(List<String> basenames, Locale locale) {
        return new Builder(basenames, locale);
    }

    public static final class Builder {

        private final Locale defaultLocale;
        private final Set<String> basenames;
        private String defaultDomain = "messages";
        private List<String> fileExtensions = List.of("xlf", "xliff");
        private Cache cache;

        public Builder(List<String> basenames, Locale defaultLocale) {
            this.basenames = new HashSet<>(basenames);
            this.defaultLocale = defaultLocale;
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

        public XliffTranslationMessageSource build() {
            CatalogHandler.Builder builder = CatalogHandler.builder();

            if (this.cache != null) {
                builder.addHandler(new CatalogCache(this.cache));
            }

            builder.addHandler(
                new XliffCatalogBuilder(
                    this.basenames,
                    this.fileExtensions,
                    this.defaultDomain,
                    this.defaultLocale
                ).getBaseCatalog()
            );

            return new XliffTranslationMessageSource(
                new BaseTranslationMessageSource(builder.build())
            );
        }
    }

    @Nullable
    @Override
    public String getMessage(String code, @Nullable Object[] args, @Nullable String defaultMessage, Locale locale) {
        return this.messageSource.getMessage(code, args, defaultMessage, locale);
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        return this.messageSource.getMessage(code, args, locale);
    }

    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        return this.messageSource.getMessage(resolvable, locale);
    }
}
