package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogInterface;
import java.util.List;
import java.util.Locale;

public final class XliffVersion12 implements XliffVersionInterface {

    private XliffIdentifierInterface transUnitIdentifier;

    public XliffVersion12() {
        this.transUnitIdentifier = new Xliff12XliffIdentifier();
    }

    @Override
    public boolean support(String version) {
        return version.equals("1.2");
    }

    @Override
    public void setTransUnitIdentifier(List<XliffIdentifierInterface> unitIdentifiers) {
        this.transUnitIdentifier = this.transUnitIdentifier.getEqualsClass(unitIdentifiers);
    }

    @Override
    public void read(CatalogInterface catalog, XliffDocument document, String domain, Locale locale) {
        document.getTransUnits("trans-unit", this.transUnitIdentifier.getList()).forEach(
            transUnit -> catalog.put(locale, domain, transUnit.code(), transUnit.value())
        );
    }

}
