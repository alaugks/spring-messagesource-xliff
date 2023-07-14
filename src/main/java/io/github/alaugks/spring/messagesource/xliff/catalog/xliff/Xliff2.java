package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogInterface;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class Xliff2 extends XliffAbstract implements XliffInterface {
    private List<String> translationUnitIdentifiers = new ArrayList<>(List.of("id"));

    @Override
    public boolean support(String version) {
        return List.of("2.0", "2.1").contains(version);
    }

    @Override
    public void setTranslationUnitIdentifiersOrdering(List<String> translationUnitIdentifiers) {
        this.translationUnitIdentifiers = translationUnitIdentifiers;
    }

    @Override
    public void read(CatalogInterface catalog, Document document, String domain, Locale locale) {
        NodeList translationUnits = XliffParserUtility.getTranslationUnits(document, "segment");
        this.readItems(catalog, domain, locale, translationUnits, this.translationUnitIdentifiers);
    }
}
