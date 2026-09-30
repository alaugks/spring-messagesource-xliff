// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

/**
 * Controls how invalid values in otherwise readable XLIFF documents are handled.
 */
public enum ParsingMode {

	/**
	 * Invalid values are handled on a best-effort basis, e.g. a non-numeric
	 * {@code target/@order} sorts after all explicitly ordered segments. This is the default.
	 *
	 * <p>Keeps the behavior of previous versions.
	 */
	LENIENT,

	/**
	 * Invalid values are rejected with an
	 * {@link io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceRuntimeException}.
	 * Currently validates that the {@code target/@order} values of a unit's segments (XLIFF 2.x) are
	 * integers forming a continuous sequence 1..n without gaps or duplicates.
	 */
	STRICT
}
