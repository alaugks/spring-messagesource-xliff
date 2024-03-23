package io.github.alaugks.spring.messagesource.xliff.catalog;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CatalogHandlerTest {

    private Locale locale;
    private CatalogHandler catalogHandler;

    @BeforeEach
    void beforeEach() {
        this.locale = Locale.forLanguageTag("en");
        this.catalogHandler = TestUtilities.getCacheWrapperWithCachedTestCatalog(
                this.locale,
                "messages"
        );
    }

    @Test
    void test_get() {
        assertEquals("Hello EN (messages)", this.catalogHandler.get(this.locale, "hello_language").toString());
        // again
        assertEquals("Hello EN (messages)", this.catalogHandler.get(this.locale, "messages.hello_language").toString());
        // again
        assertEquals("Hello EN (messages)", this.catalogHandler.get(this.locale, "hello_language").toString());
        // again
        assertEquals("Hello EN (messages)", this.catalogHandler.get(this.locale, "messages.hello_language").toString());
    }

    @Test
    void test_get_notExists() {
        for (int i = 0; i < 3; i++) {
            assertEquals("not-exists-id", this.catalogHandler.get(this.locale, "not-exists-id").toString());
        }
    }

//    @Test
//    void test_put() {
//        this.catalogHandler.setDefaultDomain("foo");
//
//        this.catalogHandler.put(this.locale, "foo", "code", "foo_value");
//        assertEquals("foo_value", this.catalogHandler.get(this.locale, "code").toString());
//
//        this.catalogHandler.put(this.locale, "bar", "code", "bar_value");
//        assertEquals("bar_value", this.catalogHandler.get(this.locale, "bar.code").toString());
//    }

//    @Test
//    void test_getTargetValue_fromCache() {
//        XliffCatalog xliffCatalogBuilder = new XliffCatalog();
//        CatalogInterface catalog = TestUtilities.getTestCatalog();
//        ResourcesLoader resourcesLoader = TestUtilities.getResourcesLoader();
//
//        XliffCatalog mockedXliffCatalogBuilder = Mockito.spy(xliffCatalogBuilder);
//
//        CatalogHandler catalogHandler = new CatalogHandler(
//                resourcesLoader,
//                mockedXliffCatalogBuilder,
//                catalog,
//                new CatalogCache(TestUtilities.getMockedCacheManager())
//        );
//
//        CatalogHandler mockedCatalogWrapper = Mockito.spy(catalogHandler);
//        mockedCatalogWrapper.initCache();
//        mockedCatalogWrapper.get(this.locale, "messages.hello_language"); // From Cache
//        mockedCatalogWrapper.get(this.locale, "roadrunner"); // From Cache
//
//        // initCache() called createCatalog(resourcesLoader, catalog)
//        // But createCatalog(resourcesLoader, catalog) will not be called the second time.
//        InOrder orderVerifier = Mockito.inOrder(mockedCatalogWrapper, mockedXliffCatalogBuilder);
//        orderVerifier.verify(mockedCatalogWrapper).initCache();
//        orderVerifier.verify(mockedXliffCatalogBuilder).createCatalog(resourcesLoader, catalog);
//
//        verify(mockedCatalogWrapper, times(1)).initCache();
//        verify(mockedXliffCatalogBuilder, times(1)).createCatalog(resourcesLoader, catalog);
//    }

//    @Test
//    void test_getTargetValue_fromCatalog_fromCache() {
//        XliffCatalog xliffCatalogBuilder = new XliffCatalog();
//        CatalogInterface catalog = TestUtilities.getTestCatalog();
//        ResourcesLoader resourcesLoader = TestUtilities.getResourcesLoader();
//        XliffCatalog mockedXliffCatalogBuilder = Mockito.spy(xliffCatalogBuilder);
//
//        CatalogHandler catalogHandler = new CatalogHandler(
//                resourcesLoader,
//                mockedXliffCatalogBuilder,
//                catalog,
//                new CatalogCache(TestUtilities.getMockedCacheManager())
//        );
//
//        catalogHandler.get(this.locale, "messages.hello_language"); // From Files + Init Cache
//        catalogHandler.get(this.locale, "messages.hello_language"); // From Cache
//        catalogHandler.get(this.locale, "roadrunner"); // From Cache
//        catalogHandler.get(this.locale, "messages.hello_language"); // From Cache
//
//        verify(mockedXliffCatalogBuilder, times(1)).createCatalog(resourcesLoader, catalog);
//    }

//    @Test
//    void test_getTargetValue_fromCatalog_cacheNotInit() {
//        XliffCatalog xliffCatalogBuilder = new XliffCatalog();
//        CatalogInterface catalog = TestUtilities.getTestCatalog();
//        ResourcesLoader resourcesLoader = TestUtilities.getResourcesLoader();
//        XliffCatalog mockedXliffCatalogBuilder = Mockito.spy(xliffCatalogBuilder);
//
//        CatalogHandler catalogHandler = new CatalogHandler(
//                resourcesLoader,
//                mockedXliffCatalogBuilder,
//                catalog,
//                new CatalogCache(TestUtilities.getMockedCacheManager())
//        );
//
//        catalogHandler.get(this.locale, "messages.hello_language");
//
//        verify(mockedXliffCatalogBuilder, times(1)).createCatalog(resourcesLoader, catalog);
//    }

//    @Test
//    void test_fillCacheWithCodeBecauseCodeNotExists() {
//        XliffCatalog xliffCatalogBuilder = new XliffCatalog();
//        CatalogInterface catalog = TestUtilities.getTestCatalog();
//        ResourcesLoader resourcesLoader = TestUtilities.getResourcesLoader();
//        XliffCatalog mockedXliffCatalogBuilder = Mockito.spy(xliffCatalogBuilder);
//
//        CatalogHandler catalogHandler = new CatalogHandler(
//                resourcesLoader,
//                mockedXliffCatalogBuilder,
//                catalog,
//                new CatalogCache(TestUtilities.getMockedCacheManager())
//        );
//
//        CatalogHandler mockedCatalogWrapper = Mockito.spy(catalogHandler);
//        mockedCatalogWrapper.initCache();
//
//        // First: put
//        assertEquals(
//                "messages.hello_language_not_exists",
//                mockedCatalogWrapper.get(this.locale, "messages.hello_language_not_exists").toString()
//        );
//
//        // Second: Get from Cache
//        assertEquals(
//                "messages.hello_language_not_exists",
//                mockedCatalogWrapper.get(this.locale, "messages.hello_language_not_exists").toString()
//        );
//
//        InOrder orderVerifier = Mockito.inOrder(mockedCatalogWrapper, mockedXliffCatalogBuilder);
//        // code not exists in cache
//        orderVerifier.verify(mockedCatalogWrapper).initCache();
//        // initCache() called createCatalog()
//        orderVerifier.verify(mockedXliffCatalogBuilder).createCatalog(resourcesLoader, catalog);
//        // get() called createCatalog() because code not exists in cache
//        orderVerifier.verify(mockedXliffCatalogBuilder).createCatalog(resourcesLoader, catalog);
//        // code add cache
//        orderVerifier.verify(mockedCatalogWrapper).put(
//                this.locale,
//                "messages.hello_language_not_exists",
//                "messages.hello_language_not_exists"
//        );
//
//        verify(mockedCatalogWrapper, times(1)).initCache();
//        verify(mockedXliffCatalogBuilder, times(2)).createCatalog(resourcesLoader, catalog);
//        verify(mockedCatalogWrapper, times(1)).put(
//                this.locale,
//                "messages.hello_language_not_exists",
//                "messages.hello_language_not_exists"
//        );
//    }
}
