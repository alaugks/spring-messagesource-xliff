package io.github.alaugks.spring.messagesource.xliff.catalog;

import io.github.alaugks.spring.messagesource.xliff.TestUtilities;
import io.github.alaugks.spring.messagesource.xliff.catalog.xliff.XliffCatalogBuilder;
import io.github.alaugks.spring.messagesource.xliff.ressources.ResourcesLoaderInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CatalogWrapperTest {

    private Locale locale;
    private CatalogWrapper catalogWrapper;

    @BeforeEach
    void beforeEach() {
        this.locale = Locale.forLanguageTag("en");
        this.catalogWrapper = TestUtilities.getCacheWrapperWithCachedTestCatalog();
    }

    @Test
    void test_get() {
        assertEquals("Hello EN (messages)", this.catalogWrapper.get(this.locale, "hello_language").toString());
        // again
        assertEquals("Hello EN (messages)", this.catalogWrapper.get(this.locale, "messages.hello_language").toString());
        // again
        assertEquals("Hello EN (messages)", this.catalogWrapper.get(this.locale, "hello_language").toString());
        // again
        assertEquals("Hello EN (messages)", this.catalogWrapper.get(this.locale, "messages.hello_language").toString());
    }

    @Test
    void test_put() {
        this.catalogWrapper.setDefaultDomain("foo");

        this.catalogWrapper.put(this.locale, "foo", "code", "foo_value");
        assertEquals("foo_value", this.catalogWrapper.get(this.locale, "code").toString());

        this.catalogWrapper.put(this.locale, "bar", "code", "bar_value");
        assertEquals("bar_value", this.catalogWrapper.get(this.locale, "bar.code").toString());
    }

    @Test
    void test_getTargetValue_fromCache() {
        XliffCatalogBuilder xliffCatalogBuilder = new XliffCatalogBuilder();
        CatalogInterface catalog = TestUtilities.getTestCatalog();
        ResourcesLoaderInterface resourcesLoader = TestUtilities.getResourcesLoader();

        XliffCatalogBuilder mockedXliffCatalogBuilder = Mockito.spy(xliffCatalogBuilder);

        CatalogWrapper catalogWrapper = new CatalogWrapper(
                TestUtilities.getMockedCacheManager(),
                resourcesLoader,
                mockedXliffCatalogBuilder,
                catalog
        );

        CatalogWrapper mockedCatalogWrapper = Mockito.spy(catalogWrapper);
        mockedCatalogWrapper.initCache();
        mockedCatalogWrapper.get(this.locale, "messages.hello_language"); // From Cache
        mockedCatalogWrapper.get(this.locale, "roadrunner"); // From Cache

        // initCache() called createCatalog(resourcesLoader, catalog)
        // But createCatalog(resourcesLoader, catalog) will not be called the second time.
        InOrder orderVerifier = Mockito.inOrder(mockedCatalogWrapper, mockedXliffCatalogBuilder);
        orderVerifier.verify(mockedCatalogWrapper).initCache();
        orderVerifier.verify(mockedXliffCatalogBuilder).createCatalog(resourcesLoader, catalog);

        verify(mockedCatalogWrapper, times(1)).initCache();
        verify(mockedXliffCatalogBuilder, times(1)).createCatalog(resourcesLoader, catalog);
    }

    @Test
    void test_getTargetValue_fromCatalog_fromCache() {
        XliffCatalogBuilder xliffCatalogBuilder = new XliffCatalogBuilder();
        CatalogInterface catalog = TestUtilities.getTestCatalog();
        ResourcesLoaderInterface resourcesLoader = TestUtilities.getResourcesLoader();
        XliffCatalogBuilder mockedXliffCatalogBuilder = Mockito.spy(xliffCatalogBuilder);

        CatalogWrapper catalogWrapper = new CatalogWrapper(
                TestUtilities.getMockedCacheManager(),
                resourcesLoader,
                mockedXliffCatalogBuilder,
                catalog
        );

        catalogWrapper.get(this.locale, "messages.hello_language"); // From Files + Init Cache
        catalogWrapper.get(this.locale, "messages.hello_language"); // From Cache
        catalogWrapper.get(this.locale, "roadrunner"); // From Cache
        catalogWrapper.get(this.locale, "messages.hello_language"); // From Cache

        verify(mockedXliffCatalogBuilder, times(1)).createCatalog(resourcesLoader, catalog);
    }

    @Test
    void test_getTargetValue_fromCatalog_cacheNotInit() {
        XliffCatalogBuilder xliffCatalogBuilder = new XliffCatalogBuilder();
        CatalogInterface catalog = TestUtilities.getTestCatalog();
        ResourcesLoaderInterface resourcesLoader = TestUtilities.getResourcesLoader();
        XliffCatalogBuilder mockedXliffCatalogBuilder = Mockito.spy(xliffCatalogBuilder);

        CatalogWrapper catalogWrapper = new CatalogWrapper(
                TestUtilities.getMockedCacheManager(),
                resourcesLoader,
                mockedXliffCatalogBuilder,
                catalog
        );

        catalogWrapper.get(this.locale, "messages.hello_language");

        verify(mockedXliffCatalogBuilder, times(1)).createCatalog(resourcesLoader, catalog);
    }

    @Test
    void test_fillCacheWithCodeBecauseCodeNotExists() {
        XliffCatalogBuilder xliffCatalogBuilder = new XliffCatalogBuilder();
        CatalogInterface catalog = TestUtilities.getTestCatalog();
        ResourcesLoaderInterface resourcesLoader = TestUtilities.getResourcesLoader();
        XliffCatalogBuilder mockedXliffCatalogBuilder = Mockito.spy(xliffCatalogBuilder);

        CatalogWrapper catalogWrapper = new CatalogWrapper(
                TestUtilities.getMockedCacheManager(),
                resourcesLoader,
                mockedXliffCatalogBuilder,
                catalog
        );

        CatalogWrapper mockedCatalogWrapper = Mockito.spy(catalogWrapper);
        mockedCatalogWrapper.initCache();

        // First: put
        assertEquals(
                "messages.hello_language_not_exists",
                mockedCatalogWrapper.get(this.locale, "messages.hello_language_not_exists").toString()
        );

        // Second: Get from Cache
        assertEquals(
                "messages.hello_language_not_exists",
                mockedCatalogWrapper.get(this.locale, "messages.hello_language_not_exists").toString()
        );

        InOrder orderVerifier = Mockito.inOrder(mockedCatalogWrapper, mockedXliffCatalogBuilder);
        // code not exists in cache
        orderVerifier.verify(mockedCatalogWrapper).initCache();
        // initCache() called createCatalog()
        orderVerifier.verify(mockedXliffCatalogBuilder).createCatalog(resourcesLoader, catalog);
        // get() called createCatalog() because code not exists in cache
        orderVerifier.verify(mockedXliffCatalogBuilder).createCatalog(resourcesLoader, catalog);
        // code add cache
        orderVerifier.verify(mockedCatalogWrapper).put(
                this.locale,
                "messages",
                "messages.hello_language_not_exists",
                "messages.hello_language_not_exists"
        );

        verify(mockedCatalogWrapper, times(1)).initCache();
        verify(mockedXliffCatalogBuilder, times(2)).createCatalog(resourcesLoader, catalog);
        verify(mockedCatalogWrapper, times(1)).put(
                this.locale,
                "messages",
                "messages.hello_language_not_exists",
                "messages.hello_language_not_exists"
        );
    }
}
