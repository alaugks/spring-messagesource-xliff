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

/**
 * Checks that reject invalid values in otherwise readable XLIFF documents.
 *
 * <p>
 * Without an enabled check, invalid values are handled on a best-effort basis (lenient).
 * An enabled check rejects a violation with an
 * {@link io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceRuntimeException}.
 *
 * @author André Laugks
 */
public enum StrictMode {

	/**
	 * Validates that the {@code target/@order} values of a unit's segments (XLIFF 2.x)
	 * are integers forming a continuous sequence 1..n without gaps or duplicates.
	 *
	 * <p>
	 * Without this check, a non-numeric {@code target/@order} sorts after all explicitly
	 * ordered segments.
	 */
	TARGET_ORDER

}
