package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.Catalog;
import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogCache;
import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogInterface;
import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogWrapper;
import io.github.alaugks.spring.messagesource.xliff.catalog.xliff.XliffCatalogBuilder;
import io.github.alaugks.spring.messagesource.xliff.ressources.ResourcesLoader;
import io.github.alaugks.spring.messagesource.xliff.ressources.ResourcesLoaderInterface;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

public class TestUtilities {
    public static CatalogInterface getTestCatalog() {
        ResourcesLoader translationResourcesLoader = new ResourcesLoader();
        translationResourcesLoader.setBasenamePattern("translations/*");
        translationResourcesLoader.setDefaultLocale(Locale.forLanguageTag("en"));
        return new XliffCatalogBuilder().createCatalog(translationResourcesLoader, new Catalog());
    }

    public static CatalogWrapper getCacheWrapperWithCachedTestCatalog() {
        CatalogWrapper catalogWrapper = new CatalogWrapper(
                getMockedCacheManager(),
                getResourcesLoader(), new XliffCatalogBuilder(), TestUtilities.getTestCatalog()
        );
        catalogWrapper.initCache();
        return catalogWrapper;
    }

    public static CacheManager getMockedCacheManager() {
        return getMockedCacheManager(CatalogCache.CACHE_NAME);
    }

    public static CacheManager getMockedCacheManager(String cacheName) {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(List.of(cacheName));
        return cacheManager;
    }

    public static ResourcesLoaderInterface getResourcesLoader() {
        ResourcesLoader resourcesLoader = new ResourcesLoader();
        resourcesLoader.setBasenamePattern(
                "translations/*"
        );
        resourcesLoader.setDefaultLocale(Locale.forLanguageTag("en"));
        return resourcesLoader;
    }

    public static Document getDocument(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(inputStream);
    }
}
