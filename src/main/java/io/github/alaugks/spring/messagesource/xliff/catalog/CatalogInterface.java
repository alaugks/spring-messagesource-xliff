package io.github.alaugks.spring.messagesource.xliff.catalog;

import java.util.HashMap;
import java.util.Locale;

public interface CatalogInterface {
    // HashMap<"language+region", HashMap<"code", "targetValue">>
    HashMap<String, HashMap<String, String>> getAll();

    String get(Locale locale, String code);

    void put(Locale locale, String domain, String code, String targetValue);

    boolean has(Locale locale, String code);
}
