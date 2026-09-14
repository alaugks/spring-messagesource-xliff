// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import java.util.Locale;
import org.jspecify.annotations.Nullable;

/**
 * The XLIFF document's declared source and target language.
 *
 * @param sourceLanguage the {@code source-language} (XLIFF 1.2) or
 *                        {@code srcLang} (XLIFF 2.x) value, parsed as a BCP 47
 *                        language tag; {@code null} if absent.
 * @param targetLanguage the {@code target-language} (XLIFF 1.2) or
 *                        {@code trgLang} (XLIFF 2.x) value, parsed as a BCP 47
 *                        language tag; {@code null} if absent.
 */
record XliffLanguages(@Nullable Locale sourceLanguage, @Nullable Locale targetLanguage) {

}
