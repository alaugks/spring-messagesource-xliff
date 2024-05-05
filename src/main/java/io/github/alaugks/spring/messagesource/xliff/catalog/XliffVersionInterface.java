package io.github.alaugks.spring.messagesource.xliff.catalog;

import java.util.List;

public interface XliffVersionInterface {

    boolean support(String version);

    String getTransUnitName();

    List<String> getIdentifier();
}
