package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogInterface;
import org.w3c.dom.Document;

import java.util.Locale;

final class Xliff21 implements XliffInterface {
    static final String VERSION = "2.1";

    @Override
    public boolean support(String version) {
        return version.equals(VERSION);
    }

    @Override
    public void read(CatalogInterface catalog, Document document, String domain, Locale locale) {
        new Xliff20().read(catalog, document, domain, locale);
    }
}
