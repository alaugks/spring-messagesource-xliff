package io.github.alaugks.spring.messagesource.xliff;

import java.util.Map;

/**
 * Reads translation units from a parsed XLIFF document.
 */
interface XliffDocumentInterface {

    /**
     * Extracts the translation units from the document.
     *
     * @return ordered map of key to translated text; empty if the document is
     *         not an XLIFF document.
     */
    Map<String, String> getUnits();

    /**
     * Extracts the declared source and target language from the document.
     *
     * @return the declared languages; both {@code null} when absent or when
     *         the document is not an XLIFF document.
     */
    XliffLanguages getLanguages();
}
