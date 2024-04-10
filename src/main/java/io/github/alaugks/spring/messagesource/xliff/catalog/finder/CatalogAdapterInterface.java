package io.github.alaugks.spring.messagesource.xliff.catalog.finder;

import java.util.Locale;

public interface CatalogAdapterInterface {

    String find(Locale locale, String code);
}
