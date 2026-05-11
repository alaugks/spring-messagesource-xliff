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

	/**
	 * Creates a new runtime exception that wraps a SAX parsing error raised
	 * while processing an XLIFF document.
	 *
	 * @param cause the underlying SAX exception to wrap.
	 */
	public XliffMessageSourceSAXParseException(Throwable cause) {
		super(cause);
	}

	public static class Warning extends XliffMessageSourceSAXParseException {

		/**
		 * Creates a new exception representing a SAX warning emitted by the
		 * underlying XML parser.
		 *
		 * @param cause the original SAX exception reported by the parser.
		 */
		public Warning(Throwable cause) {
			super(cause);
		}
	}

	public static class Error extends XliffMessageSourceSAXParseException {

		/**
		 * Creates a new exception representing a recoverable SAX error emitted
		 * by the underlying XML parser.
		 *
		 * @param cause the original SAX exception reported by the parser.
		 */
		public Error(Throwable cause) {
			super(cause);
		}
	}

	public static class FatalError extends XliffMessageSourceSAXParseException {

		/**
		 * Creates a new exception representing a non-recoverable SAX error
		 * emitted by the underlying XML parser.
		 *
		 * @param cause the original SAX exception reported by the parser.
		 */
		public FatalError(Throwable cause) {
			super(cause);
		}
	}
}
