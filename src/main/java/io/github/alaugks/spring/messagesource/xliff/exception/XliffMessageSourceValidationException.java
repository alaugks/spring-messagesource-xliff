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

package io.github.alaugks.spring.messagesource.xliff.exception;

/**
 * Thrown when an XLIFF document fails validation against its OASIS XSD schema.
 *
 * @author André Laugks
 */
public final class XliffMessageSourceValidationException extends RuntimeException {

	/**
	 * Creates a new exception indicating that an XLIFF document failed validation against
	 * its OASIS XSD schema.
	 * @param message human-readable detail message listing the schema violations.
	 */
	public XliffMessageSourceValidationException(String message) {
		super(message);
	}

}
