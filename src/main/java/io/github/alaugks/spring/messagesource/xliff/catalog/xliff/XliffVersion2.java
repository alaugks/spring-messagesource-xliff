package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogInterface;

import java.util.List;
import java.util.Locale;

public final class XliffVersion2 implements XliffVersionInterface {

    private XliffIdentifierInterface transUnitIdentifier;

    public XliffVersion2() {
        this.transUnitIdentifier = new Xliff2XliffIdentifier();
    }

    @Override
    public boolean support(String version) {
        return List.of("2.0", "2.1").contains(version);
    }

    @Override
    public void setTransUnitIdentifier(List<XliffIdentifierInterface> unitIdentifiers) {
        if (unitIdentifiers != null) {
            this.transUnitIdentifier = unitIdentifiers
                .stream()
                .filter(u -> u.getClass() == Xliff2XliffIdentifier.class)
                .findFirst()
                .orElse(this.transUnitIdentifier);
        }
    }

    @Override
    public void read(CatalogInterface catalog, XliffDocument document, String domain, Locale locale) {
        document.getTransUnits("segment", this.transUnitIdentifier.getList()).forEach(
                transUnit -> catalog.put(locale, domain, transUnit.getCode(), transUnit.getTargetValue())
        );
    }

}
