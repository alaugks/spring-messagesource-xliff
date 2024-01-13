package io.github.alaugks.spring.messagesource.xliff.catalog;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CatalogUtilitiesTest {

    @Test
    void test_concat_Code() {
        assertEquals("domain.code", CatalogUtilities.concatCode("domain", "code"));
    }

    @Test
    void test_normalizeLocale() {
        assertEquals("en", CatalogUtilities.localeToKey(Locale.forLanguageTag("en")));
        assertEquals("en", CatalogUtilities.localeToKey(Locale.forLanguageTag("EN")));
        assertEquals("en-gb", CatalogUtilities.localeToKey(Locale.forLanguageTag("en-gb")));
        assertEquals("en-gb", CatalogUtilities.localeToKey(Locale.forLanguageTag("en-GB")));
        assertEquals("en-gb", CatalogUtilities.localeToKey(Locale.forLanguageTag("EN-GB")));

        Locale localeVariant1 = new Locale.Builder().setLanguage("de").setRegion("DE").setVariant("Cologne").build();
        assertEquals("de-de", CatalogUtilities.localeToKey(localeVariant1));

        Locale localeVariant2 = Locale.forLanguageTag("de-Germany");
        assertEquals("de", CatalogUtilities.localeToKey(localeVariant2));

        Locale localeVariant3 = Locale.forLanguageTag("hy-Latn-IT-arevela");
        assertEquals("hy-it", CatalogUtilities.localeToKey(localeVariant3));
    }

    @Test
    void test_buildLocale_Locale() {
        assertEquals("en", CatalogUtilities.buildLocale(Locale.forLanguageTag("en")).toString());
        assertEquals("en", CatalogUtilities.buildLocale(Locale.forLanguageTag("EN")).toString());
        assertEquals("en_GB", CatalogUtilities.buildLocale(Locale.forLanguageTag("en-gb")).toString());
        assertEquals("en_GB", CatalogUtilities.buildLocale(Locale.forLanguageTag("en-GB")).toString());
        assertEquals("en_GB", CatalogUtilities.buildLocale(Locale.forLanguageTag("EN-GB")).toString());

        Locale localeVariant1 = new Locale.Builder().setLanguage("de").setRegion("DE").setVariant("Cologne").build();
        assertEquals("de_DE", CatalogUtilities.buildLocale(localeVariant1).toString());

        Locale localeVariant2 = Locale.forLanguageTag("de-Germany");
        assertEquals("de", CatalogUtilities.buildLocale(localeVariant2).toString());

        Locale localeVariant3 = Locale.forLanguageTag("hy-Latn-IT-arevela");
        assertEquals("hy_IT", CatalogUtilities.buildLocale(localeVariant3).toString());

        Locale localeVariant4 = Locale.forLanguageTag("sr-Cyrl");
        assertEquals("sr", CatalogUtilities.buildLocale(localeVariant4).toString());
    }

    @Test
    void test_buildLocale_languageRegion() {
        assertEquals("en", CatalogUtilities.buildLocale("en", null).toString());
        assertEquals("", CatalogUtilities.buildLocale(null, "GB").toString());
        assertEquals("en", CatalogUtilities.buildLocale(Locale.forLanguageTag("EN")).toString());
        assertEquals("en_GB", CatalogUtilities.buildLocale("en", "gb").toString());
        assertEquals("en_GB", CatalogUtilities.buildLocale("en", "GB").toString());
        assertEquals("en_GB", CatalogUtilities.buildLocale("EN", "GB").toString());
    }
}
