// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff.exception;

import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceSAXParseException.Error;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceSAXParseException.FatalError;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceSAXParseException.Warning;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public class SaxErrorHandler implements ErrorHandler {

	/**
	 * Handles a SAX parser warning by wrapping it in a
	 * {@link Warning} and throwing it, so that warnings surface as
	 * runtime exceptions instead of being silently ignored.
	 *
	 * @param exception the SAX warning reported by the parser.
	 * @throws Warning always, wrapping the supplied exception.
	 */
	@Override
	public void warning(SAXParseException exception) {
		throw new Warning(exception);
	}

	/**
	 * Handles a recoverable SAX parser error by wrapping it in an
	 * {@link Error} and throwing it.
	 *
	 * @param exception the SAX error reported by the parser.
	 * @throws Error always, wrapping the supplied exception.
	 */
	@Override
	public void error(SAXParseException exception) {
		throw new Error(exception);
	}

	/**
	 * Handles a non-recoverable SAX parser error by wrapping it in a
	 * {@link FatalError} and throwing it.
	 *
	 * @param exception the fatal SAX error reported by the parser.
	 * @throws FatalError always, wrapping the supplied exception.
	 */
	@Override
	public void fatalError(SAXParseException exception) {
		throw new FatalError(exception);
	}
}
