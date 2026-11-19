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

import org.jspecify.annotations.Nullable;

/**
 * Thrown when XLIFF content cannot be processed due to an unrecoverable parsing error.
 *
 * @author André Laugks
 */
public final class XliffMessageSourceRuntimeException extends RuntimeException {

	/**
	 * Creates a new runtime exception that wraps an error encountered while processing
	 * XLIFF content.
	 * @param cause the underlying exception to wrap; may be {@code null}.
	 */
	public XliffMessageSourceRuntimeException(@Nullable Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new runtime exception with the specified detail message.
	 * @param message the detail message providing additional information about the
	 * exception.
	 */
	public XliffMessageSourceRuntimeException(String message) {
		super(message);
	}

}
