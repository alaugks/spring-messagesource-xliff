package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogInterface;

import java.util.List;
import java.util.Locale;

public interface XliffInterface {
    boolean support(String version);
    @Deprecated(since = "1.3")
    void setTranslationUnitIdentifiersOrdering(List<String> translationUnitIdentifiers);
    void read(CatalogInterface catalog, XliffDocument document, String domain, Locale locale);
}
