package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// https://docs.oasis-open.org/xliff/xliff-core/v2.0/csprd01/xliff-core-v2.0-csprd01.html#segment
// https://docs.oasis-open.org/xliff/xliff-core/v2.1/os/xliff-core-v2.1-os.html#segment
final class XliffVersion2 implements XliffInterface {
    private List<String> translationUnitIdentifiers = new ArrayList<>(List.of("id"));

    @Override
    public boolean support(String version) {
        return List.of("2.0", "2.1").contains(version);
    }

    @Override
    public void setUnitIdentifiersOrdering(List<String> unitIdentifiers) {
        this.translationUnitIdentifiers = unitIdentifiers;
    }

    @Override
    public void read(CatalogInterface catalog, XliffDocument dom, String domain, Locale locale) {
        dom.getTransUnits("segment", this.translationUnitIdentifiers).forEach(
                transUnit -> catalog.put(locale, domain, transUnit.getCode(), transUnit.getTargetValue())
        );
    }
}
