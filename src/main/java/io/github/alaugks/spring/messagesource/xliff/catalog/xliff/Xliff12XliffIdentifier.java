package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Xliff12XliffIdentifier implements XliffIdentifierInterface {

    // https://docs.oasis-open.org/xliff/v1.2/xliff-profile-html/xliff-profile-html-1.2.html#General_Identifiers
    private List<String> unitIdentifiers = new ArrayList<>(Arrays.asList("resname", "id"));

    public Xliff12XliffIdentifier() {
    }

    public Xliff12XliffIdentifier(List<String> unitIdentifiers) {
        this.unitIdentifiers = unitIdentifiers;
    }

    @Override
    public List<String> getList() {
        return unitIdentifiers;
    }
}
