package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

// https://docs.oasis-open.org/xliff/v1.2/xliff-profile-html/xliff-profile-html-1.2.html#General_Identifiers
final class XliffVersion12 implements XliffVersionInterface {
    private List<String> translationUnitIdentifiers = new ArrayList<>(Arrays.asList("resname", "id"));

    @Override
    public boolean support(String version) {
        return version.equals("1.2");
    }

    @Override
    public void setUnitIdentifiersOrdering(List<String> unitIdentifiers) {
        this.translationUnitIdentifiers = unitIdentifiers;
    }

    @Override
    public void read(CatalogInterface catalog, XliffDocument dom, String domain, Locale locale) {
        dom.getTransUnits("trans-unit", this.translationUnitIdentifiers).forEach(
                transUnit -> catalog.put(locale, domain, transUnit.getCode(), transUnit.getTargetValue())
        );
    }
}
