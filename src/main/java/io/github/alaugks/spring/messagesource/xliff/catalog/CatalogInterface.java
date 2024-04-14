package io.github.alaugks.spring.messagesource.xliff.catalog;

import java.util.Locale;
import java.util.Map;

public interface CatalogInterface {

    CatalogInterface nextHandle(CatalogInterface handler);

    // HashMap<"language+region", HashMap<"code", "value">>
    Map<String, Map<String, String>> getAll();

    String get(Locale locale, String code);

    void put(Locale locale, String domain, String code, String value);
}
