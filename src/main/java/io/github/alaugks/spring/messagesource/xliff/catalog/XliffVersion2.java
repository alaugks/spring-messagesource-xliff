package io.github.alaugks.spring.messagesource.xliff.catalog;

import java.util.List;
import java.util.Locale;

public final class XliffVersion2 implements XliffVersionInterface {

    private XliffIdentifierInterface transUnitIdentifier;

    public XliffVersion2() {
        this.transUnitIdentifier = new Identifier();
    }

    @Override
    public boolean support(String version) {
        return List.of("2.0", "2.1").contains(version);
    }

    @Override
    public void setTransUnitIdentifier(List<XliffIdentifierInterface> unitIdentifiers) {
        this.transUnitIdentifier = this.transUnitIdentifier.getEqualsClass(unitIdentifiers);
    }

    @Override
    public void read(CatalogInterface catalog, XliffDocument document, String domain, Locale locale) {
        document.getTransUnits("segment", this.transUnitIdentifier.getList()).forEach(
            transUnit -> catalog.put(locale, domain, transUnit.code(), transUnit.value())
        );
    }

    public static final class Identifier implements XliffIdentifierInterface {

        // https://docs.oasis-open.org/xliff/xliff-core/v2.0/csprd01/xliff-core-v2.0-csprd01.html#segment
        // https://docs.oasis-open.org/xliff/xliff-core/v2.1/os/xliff-core-v2.1-os.html#segment
        private List<String> unitIdentifiers = List.of("id");

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
