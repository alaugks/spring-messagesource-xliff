package io.github.alaugks.spring.messagesource.xliff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Locale;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.DefaultMessageSourceResolvable;


/**
 * Order(100) -> getMessage(code, args, defaultMessage, locale)
 * Order(200) -> getMessage(code, args, locale)
 * Order(300) -> getMessage(resolvable, locale)
 */
@TestMethodOrder(OrderAnnotation.class)
class XliffTranslationMessageSourceTest {

    protected static MessageSource messageSource;

    @BeforeAll
    static void beforeAll() {
        messageSource = XliffTranslationMessageSource
            .builder(Locale.forLanguageTag("en"), "translations/*")
            .withCache(new ConcurrentMapCache("test-cache"))
            .build();
    }

    @Test
    @Order(100)
    void test_getMessage_Args_and_Default_messageExists() {
        assertEquals("Hello World (messages / en)", messageSource.getMessage(
            "hello_world",
            null,
            "My default message",
            Locale.forLanguageTag("en")
        ));

        assertEquals("Hallo Welt (messages / de)", messageSource.getMessage(
            "hello_world",
            null,
            "Meine Standardtext",
            Locale.forLanguageTag("de")
        ));
    }

    @Test
    @Order(100)
    void test_getMessage_Args_and_Default_messageNotExists() {
        assertEquals("My default message", messageSource.getMessage(
            "not_exists",
            null,
            "My default message",
            Locale.forLanguageTag("en")
        ));

        assertEquals("Meine Standardtext", messageSource.getMessage(
            "not_exists",
            null,
            "Meine Standardtext",
            Locale.forLanguageTag("de")
        ));
    }

    @Test
    @Order(100)
    void test_getMessage_Args_and_Default_messageNotExists_defaultIsNull() {
        assertNull(messageSource.getMessage(
            "not_exists",
            null,
            null,
            Locale.forLanguageTag("en")
        ));

        assertNull(messageSource.getMessage(
            "not_exists",
            null,
            null,
            Locale.forLanguageTag("de")
        ));
    }

    @Test
    @Order(100)
    void test_getMessage_Args_and_Default_messageExists_messageWithArgs() {
        Object[] args = {"Road Runner", "Wile E. Coyote"};
        assertEquals("Road Runner and Wile E. Coyote", messageSource.getMessage(
            "roadrunner",
            args,
            "My default message",
            Locale.forLanguageTag("en")
        ));

        assertEquals("Road Runner und Wile E. Coyote", messageSource.getMessage(
            "roadrunner",
            args,
            "My default message",
            Locale.forLanguageTag("de")
        ));
    }

    @Test
    @Order(100)
    void test_getMessage_Args_and_Default_messageNotExists_defaultMessageWithArgs() {
        Object[] args = {"Road Runner", "Wile E. Coyote"};
        assertEquals("Road Runner and Wile E. Coyote as default", messageSource.getMessage(
            "not_exists",
            args,
            "{0} and {1} as default",
            Locale.forLanguageTag("en")
        ));

        assertEquals("Road Runner and Wile E. Coyote as default", messageSource.getMessage(
            "not_exists",
            args,
            "{0} and {1} as default",
            Locale.forLanguageTag("de")
        ));
    }

    @Test
    @Order(200)
    void test_getMessage_Args_messageExists() {
        assertEquals("Hello World (messages / en)", messageSource.getMessage(
            "hello_world",
            null,
            Locale.forLanguageTag("en")
        ));

        assertEquals("Hallo Welt (messages / de)", messageSource.getMessage(
            "hello_world",
            null,
            Locale.forLanguageTag("de")
        ));
    }

    @Test
    @Order(200)
    void test_getMessage_Args_messageExists_messageWithArgs() {
        Object[] args = {"Road Runner", "Wile E. Coyote"};

        assertEquals("Road Runner and Wile E. Coyote", messageSource.getMessage(
            "roadrunner",
            args,
            Locale.forLanguageTag("en")
        ));

        assertEquals("Road Runner und Wile E. Coyote", messageSource.getMessage(
            "roadrunner",
            args,
            Locale.forLanguageTag("de")
        ));
    }

    @Test
    @Order(299)
    void test_getMessage_Args_NoSuchMessageException() {
        try {
            messageSource.getMessage(
                "not_exists",
                null,
                Locale.forLanguageTag("en")
            );
        } catch (NoSuchMessageException e) {
            assertEquals(NoSuchMessageException.class, e.getClass());
        }
    }

    @Test
    @Order(300)
    void test_getMessage_Resolvable_messageExists_messageWithArgs() {
        String[] codes = {"roadrunner"};
        Object[] args = {"Road Runner", "Wile E. Coyote"};

        assertEquals("Road Runner and Wile E. Coyote", messageSource.getMessage(
            new DefaultMessageSourceResolvable(
                codes,
                args
            ),
            Locale.forLanguageTag("en")
        ));

        assertEquals("Road Runner und Wile E. Coyote", messageSource.getMessage(
            new DefaultMessageSourceResolvable(
                codes,
                args
            ),
            Locale.forLanguageTag("de")
        ));
    }

    @Test
    @Order(300)
    void test_getMessage_Resolvable_messageNotExists_withDefaultMessage() {
        String[] codes = {"not_exists"};

        assertEquals("This is a default message.", messageSource.getMessage(
            new DefaultMessageSourceResolvable(
                codes,
                null,
                "This is a default message."
            ),
            Locale.forLanguageTag("en")
        ));

        assertEquals("Das ist ein Standardtext.", messageSource.getMessage(
            new DefaultMessageSourceResolvable(
                codes,
                null,
                "Das ist ein Standardtext."
            ),
            Locale.forLanguageTag("de")
        ));
    }

    @Test
    @Order(399)
    void test_getMessage_Resolvable_NoSuchMessageException() {
        Locale locale = Locale.forLanguageTag("en");
        String[] codes = {"not_exists"};
        DefaultMessageSourceResolvable resolvable = new DefaultMessageSourceResolvable(
            codes
        );

        assertThrows(NoSuchMessageException.class, () -> messageSource.getMessage(
            resolvable,
            locale
        ));
    }
}
