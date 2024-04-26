package io.github.alaugks.spring.messagesource.xliff.catalog;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.cache.Cache;

public final class CacheCatalog extends CatalogHandlerAbstract {

    private final Cache cache;

    private CacheCatalog(Builder builder) {
        this.nextHandler(builder.nextHandler);
        this.cache = builder.cache;
        this.build();
    }

    public static Builder builder(Cache cache) {
        return new Builder(cache);
    }

    public static final class Builder {

        private final Cache cache;
        private CatalogInterface nextHandler;

        Builder(Cache cache) {
            this.cache = cache;
        }

        public Builder nextHandler(CatalogInterface handler) {
            this.nextHandler = handler;
            return this;
        }

        public CacheCatalog build() {
            return new CacheCatalog(this);
        }
    }

    @Override
    public Map<String, Map<String, String>> getAll() {
        try {
            Map<String, Map<String, String>> result = new HashMap<>();
            Map<Object, Object> items = new HashMap<>((ConcurrentHashMap<?, ?>) this.cache.getNativeCache());
            items.forEach((code, value) -> {
                String[] split = code.toString().split("\\|");
                result.putIfAbsent(
                    split[0],
                    new HashMap<>()
                );
                result.get(split[0]).putIfAbsent(
                    split[1],
                    value.toString()
                );
            });
            return result;
        } catch (Exception e) {
            return super.getAll();
        }
    }

    @Override
    public String get(Locale locale, String code) {

        if (locale.toString().isEmpty() || code.isEmpty()) {
            return null;
        }

        String value = this.find(locale, code);
        if (value != null) {
            return value;
        }

        // Find in next Handler
        value = super.get(locale, code);
        if (value != null) {
            this.put(locale, code, value);
        }

        return value;
    }

    private void build() {
        super.getAll().forEach((langCode, catalogDomain) -> catalogDomain.forEach((code, value) ->
            this.put(
                Locale.forLanguageTag(
                    super.normalizeLocaleKey(langCode)
                ),
                code,
                value
            )
        ));
    }

    private void put(Locale locale, String code, String targetValue) {
        if (!locale.toString().isEmpty() && !code.isEmpty()) {
            this.cache.putIfAbsent(
                this.createCacheKey(locale, code),
                targetValue
            );
        }
    }

    private String find(Locale locale, String code) {
        Cache.ValueWrapper valueWrapper = this.cache.get(
            this.createCacheKey(locale, code)
        );

        if (valueWrapper != null) {
            return Objects.requireNonNull(valueWrapper.get()).toString();
        }

        return null;
    }

    private String createCacheKey(Locale locale, String code) {
        return super.localeToLocaleKey(locale) + "|" + code;
    }
}
