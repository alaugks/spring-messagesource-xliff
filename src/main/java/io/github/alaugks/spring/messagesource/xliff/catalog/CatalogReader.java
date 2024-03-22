package io.github.alaugks.spring.messagesource.xliff.catalog;

import io.github.alaugks.spring.messagesource.xliff.catalog.xliff.XliffCatalog;

public final class CatalogReader {

    private final Catalog catalog;
    private final XliffCatalog xliffCatalog;

    public CatalogReader(Catalog catalog, XliffCatalog xliffCatalog) {
        this.catalog = catalog;
        this.xliffCatalog = xliffCatalog;
    }

    public Catalog loadCatalog() {
        return this.xliffCatalog.createCatalog(this.catalog);
    }
}
