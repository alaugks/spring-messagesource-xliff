package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogUtilities;
import java.lang.reflect.Method;
import java.util.Locale;
import org.springframework.cache.interceptor.KeyGenerator;

/**
 * @deprecated
 */
@Deprecated(since = "2.0.0")
@SuppressWarnings({"java:S1133"})
public final class XliffCacheableKeyGenerator implements KeyGenerator {

    public static final String GENERATOR_NAME = "messagesource.xliff.KEY_GENERATOR";

    public static String createCode(Locale locale, String code) {
        return CatalogUtilities.localeToKey(locale) + "|" + code;
    }

    public Object generate(Object target, Method method, Object... params) {
        return createCode(
                (Locale) params[2],
                params[0].toString()
        );
    }
}
