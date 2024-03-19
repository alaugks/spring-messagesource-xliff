package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogCache;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;


class XliffMessageSourceTest {

    @Test
    void test_builder() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(List.of(CatalogCache.CACHE_NAME));

        MessageSource messageSource = XliffMessageSource
                .builder(cacheManager)
                .setDefaultLocale(Locale.forLanguageTag("en"))
                .setBasenamePattern("translations/*")
                .build();

        assertEquals(
                "Hello EN (messages)",
                messageSource.getMessage("hello_language", null, Locale.forLanguageTag("en"))
        );
    }

}
