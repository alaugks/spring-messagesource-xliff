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
