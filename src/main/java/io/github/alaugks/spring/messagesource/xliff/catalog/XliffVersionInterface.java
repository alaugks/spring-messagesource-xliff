package io.github.alaugks.spring.messagesource.xliff.catalog;

import java.util.List;
import java.util.Locale;

public interface XliffVersionInterface {

    boolean support(String version);

    void setTransUnitIdentifier(List<XliffIdentifierInterface> unitIdentifiers);

    void read(CatalogInterface catalog, XliffDocument document, String domain, Locale locale);

    interface XliffIdentifierInterface {

        List<String> getList();

        default XliffIdentifierInterface getEqualsClass(List<XliffIdentifierInterface> unitIdentifiers) {
            if (unitIdentifiers != null) {
                return unitIdentifiers
                    .stream()
                    .filter(u -> u.getClass() == this.getClass())
                    .findFirst()
                    .orElse(this);
            }

            return this;
        }
    }
}
