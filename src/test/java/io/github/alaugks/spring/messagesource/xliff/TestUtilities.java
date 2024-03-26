package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.Catalog;
import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogCache;
import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogHandler;
import io.github.alaugks.spring.messagesource.xliff.catalog.xliff.XliffReader;
import io.github.alaugks.spring.messagesource.xliff.ressources.ResourcesLoader;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestUtilities {
    public static Catalog getTestCatalog() {
        return XliffReader
                .builder(getResourcesLoader())
                .build()
                .createCatalog(
                        new Catalog(Locale.forLanguageTag("en"), "messages")
                );
    }

    public static CatalogHandler getCacheWrapperWithCachedTestCatalog(Locale locale, String domain) {
        CatalogHandler catalogHandler = new CatalogHandler(
                TestUtilities.getTestCatalog(),
                new CatalogCache(Locale.forLanguageTag("en"), "messages", getMockedCacheManager()),
                XliffReader
                        .builder(getResourcesLoader())
                        .build()
        );
        catalogHandler.initCache();
        return catalogHandler;
    }

    public static CacheManager getMockedCacheManager() {
        return getMockedCacheManager(CatalogCache.CACHE_NAME);
    }

    public static CacheManager getMockedCacheManager(String cacheName) {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(List.of(cacheName));
        return cacheManager;
    }

    public static ResourcesLoader getResourcesLoader() {
        return ResourcesLoader
                .builder()
                .setDefaultLocale(Locale.forLanguageTag("en"))
                .setBasenamePattern("translations/*")
                .build();
    }

    public static Document getDocument(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(inputStream);
    }

    public static Document getDocument(String path) throws ParserConfigurationException, SAXException, IOException {
        return TestUtilities.getDocument(TestUtilities.class.getClassLoader().getResourceAsStream(path));
    }

    public static Map<Object, Object> cacheToArray(Cache cache) {
        var nativeCache = (ConcurrentHashMap<?, ?>) cache.getNativeCache();
        return new HashMap<>(nativeCache);
    }

}
