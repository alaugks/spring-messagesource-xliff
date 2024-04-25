package io.github.alaugks.spring.messagesource.xliff.catalog;

import io.github.alaugks.spring.messagesource.xliff.records.Translation;
import java.util.List;
import java.util.Locale;

public final class XliffVersion2 implements XliffVersionInterface {

    private XliffIdentifierInterface transUnitIdentifier;

    public XliffVersion2() {
        this.transUnitIdentifier = new Identifier(List.of("id"));
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
    public void read(List<Translation> translations, XliffDocument document, String domain, Locale locale) {
        document.getTransUnits("segment", this.transUnitIdentifier.getList()).forEach(
            transUnit -> translations.add(new Translation(locale, transUnit.code(), transUnit.value(), domain))
        );
    }

    public static final class Identifier implements XliffIdentifierInterface {

        private final List<String> unitIdentifiers;

        public Identifier(List<String> unitIdentifiers) {
            this.unitIdentifiers = unitIdentifiers;
        }

        @Override
        public List<String> getList() {
            return this.unitIdentifiers;
        }
    }
}
