package io.github.alaugks.spring.messagesource.xliff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.DefaultMessageSourceResolvable;


/**
 * Order(100) -> getMessage(code, args, defaultMessage, locale) Order(200) -> getMessage(code, args, locale) Order(300)
 * -> getMessage(resolvable, locale)
 */
@TestMethodOrder(OrderAnnotation.class)
class XliffTranslationMessageSourceTest {

    protected static XliffTranslationMessageSource messageSource;

    @BeforeAll
    static void beforeAll() {
        messageSource = XliffTranslationMessageSource
                .builder(TestUtilities.getMockedCacheManager())
                .basenamePattern("translations/*")
                .defaultLocale(Locale.forLanguageTag("en"))
                .build();
        messageSource.initCache();
    }

    @Test
    @Order(100)
    void test_getMessage_Args_and_Default_messageExists() {
        assertEquals("Hello EN (messages)", messageSource.getMessage(
            "hello_language",
            null,
            "My default message",
            Locale.forLanguageTag("en")
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
    }

    @Test
    @Order(199)
    void test_getMessage_Args_and_Default_Nullable() {
        assertNull(messageSource.getMessage(
            "not_exists",
            null,
            null,
            Locale.forLanguageTag("en")
        ));
    }

    @Test
    @Order(200)
    void test_getMessage_Args_messageExists() {
        assertEquals("Hello EN (messages)", messageSource.getMessage(
            "hello_language",
            null,
            Locale.forLanguageTag("en")
        ));
    }

    @Test
    @Order(200)
    void test_getMessage_Args_messageExists_messageWithArgs() {
        Object[] args = {"Road Runner", "Wile E. Coyote"};
        String message = messageSource.getMessage(
                "roadrunner",
                args,
                Locale.forLanguageTag("en")
        );

        assertEquals("Road Runner and Wile E. Coyote", message);
    }

    @Order(200)
    @ParameterizedTest()
    @MethodSource("dataProvider_fallback")
    void test_getMessage_Args_fallback(String code, Objects[] args, Locale locale, Object expected) {
        String message = messageSource.getMessage(
            code,
            args,
            locale
        );
        assertEquals(expected, message);
    }

    @Test
    @Order(299)
    void test_getMessage_Args_NoSuchMessageException() {
        assertThrows(NoSuchMessageException.class, () -> messageSource.getMessage(
            "not_exists",
            null,
            Locale.forLanguageTag("en")
        ));
    }

    @Test
    @Order(300)
    void test_getMessage_Resolvable_messageExists_messageWithArgs() {
        String[] codes = {"roadrunner"};
        Object[] args = {"Road Runner", "Wile E. Coyote"};
        DefaultMessageSourceResolvable resolvable = new DefaultMessageSourceResolvable(
                codes,
                args
        );

        assertEquals("Road Runner and Wile E. Coyote", messageSource.getMessage(
            resolvable,
            Locale.forLanguageTag("en")
        ));
    }

    @Test
    @Order(300)
    void test_getMessage_Resolvable_messageNotExists_withDefaultMessage() {
        String[] codes = {"not_exists"};
        String defaultMessage = "This is a default message.";
        DefaultMessageSourceResolvable resolvable = new DefaultMessageSourceResolvable(
                codes,
                null,
                defaultMessage
        );

        assertEquals(defaultMessage, messageSource.getMessage(
            resolvable,
            Locale.forLanguageTag("en")
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

    private static Stream<Arguments> dataProvider_fallback() {
        return Stream.of(
                Arguments.of(
                        "hello_language",
                        null,
                        Locale.forLanguageTag("jp"),
                        "Hello EN (messages)"
                ),
                Arguments.of(
                        "hello_language",
                        null,
                        Locale.forLanguageTag("en-GB"),
                        "Hello EN (messages)"
                ),
                Arguments.of(
                        "hello_language",
                        null,
                        Locale.forLanguageTag("en-US"),
                        "Hello EN_US (messages)"
                )
        );
    }

}
