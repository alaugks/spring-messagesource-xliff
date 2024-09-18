package io.github.alaugks.spring.messagesource.xliff.exception;

import org.junit.jupiter.api.Test;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class SaxErrorHandlerTest {

	@Test
	void test_warning() {
		var handler = new SaxErrorHandler();
		assertThrows(
				XliffMessageSourceSAXParseException.Warning.class,
				() -> handler.warning(new SAXParseException("Warning", mock(Locator.class)))
		);
	}

	@Test
	void test_error() {
		var handler = new SaxErrorHandler();
		assertThrows(
				XliffMessageSourceSAXParseException.Error.class,
				() -> handler.error(new SAXParseException("Error", mock(Locator.class)))
		);
	}

	@Test
	void test_fatalError() {
		var handler = new SaxErrorHandler();
		assertThrows(
				XliffMessageSourceSAXParseException.FatalError.class,
				() -> handler.fatalError(new SAXParseException("FatalError", mock(Locator.class)))
		);
	}
}
