package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import java.util.List;

public interface XliffIdentifierInterface {

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
