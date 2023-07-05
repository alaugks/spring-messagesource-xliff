package io.github.alaugks.spring.messagesource.xliff.catalog;

import io.github.alaugks.spring.messagesource.xliff.ressources.ResourcesLoaderInterface;

public interface CatalogBuilderInterface {
    CatalogInterface createCatalog(ResourcesLoaderInterface resourceLoader, CatalogInterface catalog);
}
