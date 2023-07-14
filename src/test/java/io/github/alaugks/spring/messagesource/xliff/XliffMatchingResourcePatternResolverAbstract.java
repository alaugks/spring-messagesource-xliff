package io.github.alaugks.spring.messagesource.xliff;

import org.junit.jupiter.api.Test;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.DefaultMessageSourceResolvable;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

abstract class XliffMatchingResourcePatternResolverAbstract {

    protected static XliffTranslationMessageSource messageSource;

    @Test
    void test_message_withDefaultMessage_messageExists() {
        String message = messageSource.getMessage(
                "hello_language",
                null,
                "My default message",
                Locale.forLanguageTag("en")
        );

        assertEquals("Hello EN (messages)", message);
    }

    @Test
    void test_getMessage_withDefaultMessage_messageNotExists() {
        String message = messageSource.getMessage(
                "not_exists",
                null,
                "My default message",
                Locale.forLanguageTag("en")
        );

        assertEquals("My default message", message);
    }

    @Test
    void test_getMessage_withDefaultMessage_messageExists_messageWithArgs() {
        Object[] args = {"Road Runner", "Wile E. Coyote"};
        String message = messageSource.getMessage(
                "roadrunner",
                args,
                "My default message",
                Locale.forLanguageTag("en")
        );

        assertEquals("Road Runner and Wile E. Coyote", message);
    }

    @Test
    void test_getMessage_withDefaultMessage_messageNotExists_defaultMessageWithArgs() {
        Object[] args = {"Road Runner", "Wile E. Coyote"};
        String message = messageSource.getMessage(
                "not_exists",
                args,
                "{0} and {1} as default",
                Locale.forLanguageTag("en")
        );

        assertEquals("Road Runner and Wile E. Coyote as default", message);
    }

    @Test
    void test_getMessage_messageExists() {
        String message = messageSource.getMessage(
                "hello_language",
                null,
                Locale.forLanguageTag("en")
        );

        assertEquals("Hello EN (messages)", message);
    }

    @Test
    void test_getMessage_messageNotExists() {
        Locale locale =  Locale.forLanguageTag("en");

        NoSuchMessageException exception = assertThrows(NoSuchMessageException.class, () -> {
            messageSource.getMessage(
                    "not_exists",
                    null,
                    locale
            );
        });

        assertEquals("No message found under code 'not_exists' for locale 'en'.", exception.getMessage());
    }

    @Test
    void test_getMessage_messageExists_messageWithArgs() {
        Object[] args = {"Road Runner", "Wile E. Coyote"};
        String message = messageSource.getMessage(
                "roadrunner",
                args,
                Locale.forLanguageTag("en")
        );

        assertEquals("Road Runner and Wile E. Coyote", message);
    }

    @Test
    void test_getMessage_resolvable_messageExists_messageWithArgs() {
        String[] codes = {"roadrunner"};
        Object[] args = {"Road Runner", "Wile E. Coyote"};
        DefaultMessageSourceResolvable resolvable = new DefaultMessageSourceResolvable(
                codes,
                args
        );
        String message = messageSource.getMessage(
                resolvable,
                Locale.forLanguageTag("en")
        );

        assertEquals("Road Runner and Wile E. Coyote", message);
    }

    @Test
    void test_getMessage_resolvable_messageNotExists() {
        Locale locale = Locale.forLanguageTag("en");
        String[] codes = {"not_exists"};
        DefaultMessageSourceResolvable resolvable = new DefaultMessageSourceResolvable(
                codes
        );

        NoSuchMessageException exception = assertThrows(NoSuchMessageException.class, () -> {
            messageSource.getMessage(
                    resolvable,
                    locale
            );
        });
        assertEquals("No message found under code 'not_exists' for locale 'en'.", exception.getMessage());
    }

    @Test
    void test_getMessage_resolvable_messageNotExists_withDefaultMessage() {
        String[] codes = {"not_exists"};
        String defaultMessage = "This is a default message.";
        DefaultMessageSourceResolvable resolvable = new DefaultMessageSourceResolvable(
                codes,
                null,
                defaultMessage
        );
        String message = messageSource.getMessage(
                resolvable,
                Locale.forLanguageTag("en")
        );

        assertEquals(defaultMessage, message);
    }
}
