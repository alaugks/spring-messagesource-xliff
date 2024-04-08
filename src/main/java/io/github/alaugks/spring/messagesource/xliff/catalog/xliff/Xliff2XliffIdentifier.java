package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import java.util.List;

public final class Xliff2XliffIdentifier implements XliffIdentifierInterface {

    // https://docs.oasis-open.org/xliff/xliff-core/v2.0/csprd01/xliff-core-v2.0-csprd01.html#segment
    // https://docs.oasis-open.org/xliff/xliff-core/v2.1/os/xliff-core-v2.1-os.html#segment
    private List<String> unitIdentifiers = List.of("id");

    public Xliff2XliffIdentifier() {
    }

    public Xliff2XliffIdentifier(List<String> unitIdentifiers) {
        this.unitIdentifiers = unitIdentifiers;
    }

    @Override
    public List<String> getList() {
        return this.unitIdentifiers;
    }
}
