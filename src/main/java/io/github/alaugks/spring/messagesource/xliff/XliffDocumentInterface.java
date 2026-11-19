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

import java.util.Map;

/**
 * Reads translation units from a parsed XLIFF document.
 *
 * @author André Laugks
 */
interface XliffDocumentInterface {

	/**
	 * Extracts the translation units from the document.
	 * @return ordered map of key to translated text; empty if the document is not an
	 * XLIFF document.
	 */
	Map<String, String> getUnits();

	/**
	 * Extracts the declared source and target language from the document.
	 * @return the declared languages; both {@code null} when absent or when the document
	 * is not an XLIFF document.
	 */
	XliffLanguageAttr getLanguages();

}
