package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogInterface;
import org.w3c.dom.Document;

import java.util.List;
import java.util.Locale;

public interface XliffInterface {
    boolean support(String version);

    void setTranslationUnitIdentifiersOrdering(List<String> translationUnitIdentifiers);

    void read(CatalogInterface catalog, Document document, String domain, Locale locale);
}
