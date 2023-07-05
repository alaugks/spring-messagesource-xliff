package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogInterface;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.Locale;

final class Xliff20 extends XliffAbstract implements XliffInterface {
    static final String VERSION = "2.0";

    @Override
    public boolean support(String version) {
        return version.equals(VERSION);
    }

    @Override
    public void read(CatalogInterface catalog, Document document, String domain, Locale locale) {
        NodeList translationNodes = XliffParserUtility.getTranslationNodes(document, "segment");
        this.readItems(VERSION, catalog, domain, locale, translationNodes);
    }
}
