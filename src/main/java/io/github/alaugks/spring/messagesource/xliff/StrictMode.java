// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

/**
 * Checks that reject invalid values in otherwise readable XLIFF documents.
 *
 * <p>Without an enabled check, invalid values are handled on a best-effort basis (lenient).
 * An enabled check rejects a violation with an
 * {@link io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceRuntimeException}.
 */
public enum StrictMode {

	/**
	 * Validates that the {@code target/@order} values of a unit's segments (XLIFF 2.x) are integers
	 * forming a continuous sequence 1..n without gaps or duplicates.
	 *
	 * <p>Without this check, a non-numeric {@code target/@order} sorts after all explicitly ordered segments.
	 */
	TARGET_ORDER
}
