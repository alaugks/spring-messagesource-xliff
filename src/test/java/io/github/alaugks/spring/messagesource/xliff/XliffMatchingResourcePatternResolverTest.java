package io.github.alaugks.spring.messagesource.xliff;

import java.util.Locale;
import org.junit.jupiter.api.BeforeAll;

@SuppressWarnings({"java:S2187"})
class XliffMatchingResourcePatternResolverTest extends XliffMatchingResourcePatternResolverAbstract {

    @BeforeAll
    static void beforeAll() {
        messageSource = new XliffTranslationMessageSource(TestUtilities.getMockedCacheManager());
        messageSource.setBasenamePattern("translations/*");
        messageSource.setDefaultLocale(Locale.forLanguageTag("en"));
    }
}
