// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff.exception;

/**
 * Thrown when an XLIFF document declares a version not supported by this
 * library.
 */
public class XliffMessageSourceVersionSupportException extends RuntimeException {

	/**
	 * Creates a new exception indicating that the encountered XLIFF version is
	 * not supported by this library.
	 *
	 * @param message human-readable detail message describing the cause.
	 */
	public XliffMessageSourceVersionSupportException(String message) {
		super(message);
	}
}
