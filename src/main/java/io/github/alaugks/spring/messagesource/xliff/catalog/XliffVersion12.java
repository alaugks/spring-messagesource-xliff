package io.github.alaugks.spring.messagesource.xliff.catalog;

import java.util.List;
import java.util.Locale;

public final class XliffVersion12 implements XliffVersionInterface {

    private XliffIdentifierInterface transUnitIdentifier;

    public XliffVersion12() {
        this.transUnitIdentifier = new Identifier();
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

    public static final class Identifier implements XliffIdentifierInterface {

        // https://docs.oasis-open.org/xliff/v1.2/xliff-profile-html/xliff-profile-html-1.2.html#General_Identifiers
        private List<String> unitIdentifiers = List.of("resname", "id");

        public Identifier() {
        }

        public Identifier(List<String> unitIdentifiers) {
            this.unitIdentifiers = unitIdentifiers;
        }

        @Override
        public List<String> getList() {
            return this.unitIdentifiers;
        }
    }
}
