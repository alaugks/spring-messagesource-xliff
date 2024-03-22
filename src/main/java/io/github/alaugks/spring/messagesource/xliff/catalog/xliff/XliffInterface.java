package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogInterface;
import java.util.List;
import java.util.Locale;
import org.w3c.dom.Document;

public interface XliffInterface {
    boolean support(String version);
    @Deprecated(since = "1.2")
    void setTranslationUnitIdentifiersOrdering(List<String> translationUnitIdentifiers);
    void read(CatalogInterface catalog, Document document, String domain, Locale locale);
}
