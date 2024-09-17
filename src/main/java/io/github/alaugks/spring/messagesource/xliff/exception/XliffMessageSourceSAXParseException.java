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
