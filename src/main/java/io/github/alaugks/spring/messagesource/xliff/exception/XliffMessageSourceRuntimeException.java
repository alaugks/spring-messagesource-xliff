// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff.exception;

public class XliffMessageSourceRuntimeException extends RuntimeException {

	/**
	 * Creates a new runtime exception that wraps an error encountered while
	 * processing XLIFF content.
	 *
	 * @param cause the underlying exception to wrap; may be {@code null}.
	 */
	public XliffMessageSourceRuntimeException(Throwable cause) {
		super(cause);
	}
}
