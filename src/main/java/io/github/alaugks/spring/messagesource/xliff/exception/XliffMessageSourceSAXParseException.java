/*
 * Copyright 2023-2025 André Laugks <alaugks@gmail.com>
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

public class XliffMessageSourceSAXParseException extends RuntimeException {

	public XliffMessageSourceSAXParseException(Throwable cause) {
		super(cause);
	}

	public static class Warning extends XliffMessageSourceSAXParseException {

		public Warning(Throwable cause) {
			super(cause);
		}
	}

	public static class Error extends XliffMessageSourceSAXParseException {

		public Error(Throwable cause) {
			super(cause);
		}
	}

	public static class FatalError extends XliffMessageSourceSAXParseException {

		public FatalError(Throwable cause) {
			super(cause);
		}
	}
}
