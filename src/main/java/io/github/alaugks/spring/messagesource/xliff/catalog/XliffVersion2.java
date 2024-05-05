package io.github.alaugks.spring.messagesource.xliff.catalog;

import java.util.List;

public final class XliffVersion2 implements XliffVersionInterface {

    @Override
    public boolean support(String version) {
        return List.of("2.0", "2.1").contains(version);
    }

    @Override
    public String getTransUnitName() {
        return "segment";
    }

    @Override
    public List<String> getIdentifier() {
        return List.of("id");
    }
}
