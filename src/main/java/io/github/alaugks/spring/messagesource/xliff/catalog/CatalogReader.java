package io.github.alaugks.spring.messagesource.xliff.catalog;

import io.github.alaugks.spring.messagesource.xliff.catalog.xliff.XliffCatalogBuilder;

public class CatalogReader {

    private final Catalog catalog;
    private final XliffCatalogBuilder catalogBuilder;

    public CatalogReader(Catalog catalog, XliffCatalogBuilder catalogBuilder) {
        this.catalog = catalog;
        this.catalogBuilder = catalogBuilder;
    }

    public Catalog loadCatalog() {
        return this.catalogBuilder.createCatalog(this.catalog);
    }
}
