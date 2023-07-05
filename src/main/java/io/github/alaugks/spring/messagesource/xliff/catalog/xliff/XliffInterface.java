package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogInterface;
import org.w3c.dom.Document;

import java.util.Locale;

public interface XliffInterface {
    boolean support(String version);

    void read(CatalogInterface catalog, Document document, String domain, Locale locale);
}
