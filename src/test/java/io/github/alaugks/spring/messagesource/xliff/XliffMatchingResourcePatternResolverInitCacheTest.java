package io.github.alaugks.spring.messagesource.xliff;

import org.junit.jupiter.api.BeforeEach;

import java.util.Locale;

class XliffMatchingResourcePatternResolverInitCacheTest extends XliffMatchingResourcePatternResolverAbstract {

    @BeforeEach
    void beforeEach() {
        this.resolver = new XliffTranslationMessageSource(TestUtilities.getMockedCacheManager());
        this.resolver.setBasenamePattern("translations/*");
        this.resolver.setDefaultLocale(Locale.forLanguageTag("en"));
        this.resolver.initCache();
    }
}
