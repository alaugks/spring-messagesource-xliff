package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogInterface;
import java.util.List;
import java.util.Locale;

public interface XliffVersionInterface {

    boolean support(String version);

    void setTransUnitIdentifier(List<XliffIdentifierInterface> unitIdentifiers);

    void read(CatalogInterface catalog, XliffDocument document, String domain, Locale locale);
}
