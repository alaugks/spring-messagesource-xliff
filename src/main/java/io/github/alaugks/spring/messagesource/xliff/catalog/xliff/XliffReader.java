package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import java.util.Set;

final class XliffReader {
    Set<XliffInterface> supportedVersions;

    public XliffReader() {
        this.supportedVersions = Set.of(
                new Xliff12(),
                new Xliff20(),
                new Xliff21()
        );
    }

    public XliffInterface getReader(String version) {
        for (XliffInterface xliffClass : this.supportedVersions) {
            if (xliffClass.support(version)) {
                return xliffClass;
            }
        }
        return null;
    }
}
