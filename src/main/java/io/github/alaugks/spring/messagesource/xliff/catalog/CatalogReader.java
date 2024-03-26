package io.github.alaugks.spring.messagesource.xliff.catalog;

import io.github.alaugks.spring.messagesource.xliff.catalog.xliff.XliffReader;

public final class CatalogReader {

    private final Catalog catalog;
    private final XliffReader xliffReader;

    public CatalogReader(Catalog catalog, XliffReader xliffReader) {
        this.catalog = catalog;
        this.xliffReader = xliffReader;
    }

    public Catalog loadCatalog() {
        return this.xliffReader.createCatalog(this.catalog);
    }
}
