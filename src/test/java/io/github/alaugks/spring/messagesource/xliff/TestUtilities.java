package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.BaseCatalog;
import io.github.alaugks.spring.messagesource.xliff.catalog.XliffCatalogBuilder;
import io.github.alaugks.spring.messagesource.xliff.records.TranslationFile;
import io.github.alaugks.spring.messagesource.xliff.ressources.ResourcesLoader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
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

    public static BaseCatalog getTestBaseCatalog(String ressource, Locale locale, String domain) {
        var files = new ArrayList<TranslationFile>();
        files.add(
            new TranslationFile(
                domain,
                locale,
                TestUtilities.class.getClassLoader().getResourceAsStream(ressource)
            )
        );

        XliffCatalogBuilder xliffCatalogBuilder = new XliffCatalogBuilder(
            files,
            domain,
            locale
        );

        return xliffCatalogBuilder.getBaseCatalog();
    }

    public static BaseCatalog getTestBaseCatalog() {
        return new XliffCatalogBuilder(
            getResourcesLoader().getTranslationFiles(),
            "messages",
            Locale.forLanguageTag("en")
        ).getBaseCatalog();
    }

    public static Cache getCache() {
        return getMockedCacheManager(CATALOG_CACHE).getCache(CATALOG_CACHE);
    }

    public static CacheManager getMockedCacheManager(String cacheName) {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(List.of(cacheName));
        return cacheManager;
    }

    public static ResourcesLoader getResourcesLoader(Locale locale, String... list) {
        return new ResourcesLoader(
            locale,
            listToSet(list),
            List.of("xlf", "xliff")
        );
    }

    public static ResourcesLoader getResourcesLoader() {
        return getResourcesLoader(
            Locale.forLanguageTag("en"),
            "translations/*"
        );
    }


    public static Document getDocument(InputStream inputStream)
        throws ParserConfigurationException, SAXException, IOException {
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

    public static Set<String> listToSet(String... list) {
        return Arrays.stream(list).collect(Collectors.toSet());
    }

}
