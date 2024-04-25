package io.github.alaugks.spring.messagesource.xliff.records;

import java.util.Locale;

public record Translation(Locale locale, String code, String value, String domain) {

}
