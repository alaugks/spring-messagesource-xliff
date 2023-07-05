package io.github.alaugks.spring.messagesource.xliff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XliffCacheableKeyGeneratorTest {

    private Object target;
    private Method method;

    @BeforeEach
    void beforeEach() {
        this.target = new Object();
        this.method = getClass().getMethods()[0];
    }

    @Test
    void test_createKey_code_en() {
        XliffCacheableKeyGenerator generator = new XliffCacheableKeyGenerator();

        Locale locale = Locale.forLanguageTag("en");
        String code = "my-code";
        Object[] params = {code, "args", locale};

        assertEquals("en|my-code", XliffCacheableKeyGenerator.createCode(locale, code));
        assertEquals("en|my-code", generator.generate(this.target, this.method, params));
    }

    @Test
    void test_createKey_code_enUk() {
        XliffCacheableKeyGenerator generator = new XliffCacheableKeyGenerator();

        Locale locale = Locale.forLanguageTag("en-GB");
        String code = "my-code";
        Object[] params = {code, "args", locale};

        assertEquals("en-gb|my-code", XliffCacheableKeyGenerator.createCode(locale, code));
        assertEquals("en-gb|my-code", generator.generate(this.target, this.method, params));
    }

    @Test
    void test_Constants() {
        assertEquals("messagesource.xliff.KEY_GENERATOR", XliffCacheableKeyGenerator.GENERATOR_NAME);
    }
}
