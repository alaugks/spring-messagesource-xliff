package io.github.alaugks.spring.messagesource.xliff;

import org.junit.jupiter.api.BeforeAll;

import java.util.Locale;

class XliffMatchingResourcePatternResolverTest extends XliffMatchingResourcePatternResolverAbstract {

    @BeforeAll
    static void beforeAll() {
        messageSource = new XliffTranslationMessageSource(TestUtilities.getMockedCacheManager());
        messageSource.setBasenamePattern("translations/*");
        messageSource.setDefaultLocale(Locale.forLanguageTag("en"));
    }
}
