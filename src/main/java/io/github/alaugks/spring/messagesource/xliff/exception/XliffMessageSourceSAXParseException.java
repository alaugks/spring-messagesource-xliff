// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff.exception;

/**
 * Base type for runtime exceptions that wrap a SAX parsing notification
 * raised while processing an XLIFF document.
 */
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

	/**
	 * A SAX warning reported by the parser.
	 */
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

	/**
	 * A recoverable SAX error reported by the parser.
	 */
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

	/**
	 * A non-recoverable SAX error reported by the parser.
	 */
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
