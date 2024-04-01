package io.github.alaugks.spring.messagesource.xliff.exception;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public class SaxErrorHandler implements ErrorHandler {
    @Override
    public void warning(SAXParseException exception) {
        throw new XliffMessageSourceSAXParseWarningException(exception);
    }

    @Override
    public void error(SAXParseException exception) {
        throw new XliffMessageSourceSAXParseErrorException(exception);
    }

    @Override
    public void fatalError(SAXParseException exception) {
        throw new XliffMessageSourceSAXParseFatalErrorException(exception);
    }
}
