package io.github.alaugks.spring.messagesource.xliff.catalog;

import java.util.List;

public final class XliffVersion12 implements XliffVersionInterface {

    @Override
    public boolean support(String version) {
        return version.equals("1.2");
    }

    @Override
    public String getTransUnitName() {
        return "trans-unit";
    }

    @Override
    public List<String> getIdentifier() {
        return List.of("resname", "id");
    }
}
