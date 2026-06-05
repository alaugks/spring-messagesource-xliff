// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff.exception;

public class XliffMessageSourceValidationException extends RuntimeException {

	/**
	 * Creates a new exception indicating that an XLIFF document failed
	 * validation against its OASIS XSD schema.
	 *
	 * @param message human-readable detail message listing the schema
	 *                violations.
	 */
	public XliffMessageSourceValidationException(String message) {
		super(message);
	}
}
