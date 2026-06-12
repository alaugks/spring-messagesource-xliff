// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff.exception;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

class SaxErrorHandlerTest {

	@Test
	void test_warning() {
		var handler = new SaxErrorHandler();
		assertThatThrownBy(
				() -> handler.warning(new SAXParseException("Warning", mock(Locator.class)))
		).isInstanceOf(XliffMessageSourceSAXParseException.Warning.class);
	}

	@Test
	void test_error() {
		var handler = new SaxErrorHandler();
		assertThatThrownBy(
				() -> handler.error(new SAXParseException("Error", mock(Locator.class)))
		).isInstanceOf(XliffMessageSourceSAXParseException.Error.class);
	}

	@Test
	void test_fatal_error() {
		var handler = new SaxErrorHandler();
		assertThatThrownBy(
				() -> handler.fatalError(new SAXParseException("FatalError", mock(Locator.class)))
		).isInstanceOf(XliffMessageSourceSAXParseException.FatalError.class);
	}
}
