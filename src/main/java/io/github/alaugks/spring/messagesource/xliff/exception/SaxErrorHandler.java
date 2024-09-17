package io.github.alaugks.spring.messagesource.xliff.exception;

import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceSAXParseException.Error;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceSAXParseException.FatalError;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceSAXParseException.Warning;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public class SaxErrorHandler implements ErrorHandler {

	@Override
	public void warning(SAXParseException exception) {
		throw new Warning(exception);
	}

	@Override
	public void error(SAXParseException exception) {
		throw new Error(exception);
	}

	@Override
	public void fatalError(SAXParseException exception) {
		throw new FatalError(exception);
	}
}
