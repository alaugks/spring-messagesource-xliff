package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogInterface;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

final class XliffVersion12 extends XliffVersionAbstract implements XliffInterface {
    private List<String> translationUnitIdentifiers = new ArrayList<>(Arrays.asList("resname", "id"));

    @Override
    public boolean support(String version) {
        return version.equals("1.2");
    }

    @Override
    public void setTranslationUnitIdentifiersOrdering(List<String> translationUnitIdentifiers) {
        this.translationUnitIdentifiers = translationUnitIdentifiers;
    }

    @Override
    public void read(CatalogInterface catalog, Document document, String domain, Locale locale) {
        NodeList translationUnits = XliffReader.getTranslationUnits(document, "trans-unit");
        this.readItems(catalog, domain, locale, translationUnits, this.translationUnitIdentifiers);
    }
}
