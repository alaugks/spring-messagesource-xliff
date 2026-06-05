package io.github.alaugks.spring.messagesource.xliff;

import java.util.Map;

public interface XliffDocumentInterface {

    /**
     * Extracts the translation units from the document.
     *
     * @return ordered map of key to translated text; empty if the document is
     *         not an XLIFF document.
     */
    Map<String, String> getUnits();
}
