package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.Catalog;
import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogBuilder;
import io.github.alaugks.spring.messagesource.xliff.ressources.ResourcesLoader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class TestUtilities {

    public static final String CATALOG_CACHE = "test-cache";

    public static Catalog getTestCatalog() {
        return CatalogBuilder
                .builder(getResourcesLoader())
                .build()
                .createCatalog(
                        new Catalog(Locale.forLanguageTag("en"), "messages")
                );
    }

    public static Cache getCache() {
        return getMockedCacheManager(CATALOG_CACHE).getCache(CATALOG_CACHE);
    }

    public static CacheManager getMockedCacheManager() {
        return getMockedCacheManager(CATALOG_CACHE);
    }

    public static CacheManager getMockedCacheManager(String cacheName) {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(List.of(cacheName));
        return cacheManager;
    }

    public static ResourcesLoader getResourcesLoader() {
        return ResourcesLoader
                .builder()
                .defaultLocale(Locale.forLanguageTag("en"))
                .basenamePattern("translations/*")
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
