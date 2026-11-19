/*
 * Copyright André Laugks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.alaugks.spring.messagesource.xliff;

import java.util.Locale;
import org.jspecify.annotations.Nullable;

/**
 * The XLIFF document's declared source and target language.
 *
 * @param sourceLanguage the {@code source-language} (XLIFF 1.2) or {@code srcLang} (XLIFF
 * 2.x) value, parsed as a BCP 47 language tag; {@code null} if absent.
 * @param targetLanguage the {@code target-language} (XLIFF 1.2) or {@code trgLang} (XLIFF
 * 2.x) value, parsed as a BCP 47 language tag; {@code null} if absent.
 * @author André Laugks
 */
record XliffLanguageAttr(@Nullable Locale sourceLanguage, @Nullable Locale targetLanguage) {

}
