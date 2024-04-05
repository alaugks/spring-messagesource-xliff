package io.github.alaugks.spring.messagesource.xliff;

import org.junit.jupiter.api.BeforeAll;

import java.util.Locale;

@SuppressWarnings({"java:S2187"})
class XliffMatchingResourcePatternResolverInitCacheTest extends XliffMatchingResourcePatternResolverAbstract {

    @BeforeAll
    static void beforeAll() {
        messageSource = new XliffTranslationMessageSource(TestUtilities.getMockedCacheManager());
        messageSource.setBasenamePattern("translations/*");
        messageSource.setDefaultLocale(Locale.forLanguageTag("en"));
        messageSource.initCache();
    }
}
