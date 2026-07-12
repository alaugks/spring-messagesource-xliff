// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.catalog.resources.LocationPattern;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.MessageSource;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the examples documented in docs/README-XLIFF-2.2-PGS.md. Each test mirrors a README
 * section and its getMessage() and Thymeleaf snippets against the messages(_de).xliff resources.
 */
class Xliff22PgsReadmeExamplesTest {

	private MessageSource messageSource;
	private SpringTemplateEngine templateEngine;

	@BeforeEach
	void set_up() {
		this.messageSource = XliffResourceMessageSource
			.builder(Locale.forLanguageTag("en"), new LocationPattern("translations_readme_pgs/*"))
			.enableICU4j()
			.validateSchema(true)
			.build();

		StringTemplateResolver templateResolver = new StringTemplateResolver();
		templateResolver.setTemplateMode(TemplateMode.HTML);

		this.templateEngine = new SpringTemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		this.templateEngine.setTemplateEngineMessageSource(this.messageSource);
	}

	@ParameterizedTest
	@MethodSource("provider_plural_numeric_cases")
	void test_plural_numeric_cases(int count, Locale locale, String expected) {
		assertThat(
			this.messageSource.getMessage("file_deleted", new Object[]{Map.of("count", count)}, locale)
		).isEqualTo(expected);
	}

	@ParameterizedTest
	@MethodSource("provider_plural_cldr_keywords")
	void test_plural_cldr_keywords(int count, Locale locale, String expected) {
		assertThat(
			this.messageSource.getMessage("cart_summary", new Object[]{Map.of("count", count)}, locale)
		).isEqualTo(expected);
	}


	@ParameterizedTest
	@MethodSource("provider_gender")
	void test_gender(String recipientGender, Locale locale, String expected) {
		assertThat(
			this.messageSource.getMessage("greeting", new Object[]{Map.of("recipient_gender", recipientGender)}, locale)
		).isEqualTo(expected);
	}

	@ParameterizedTest
	@MethodSource("provider_plural_numeric_cases")
	void test_plural_numeric_cases_in_thymeleaf(int count, Locale locale, String expected) {
		String template = "<p th:text=\"#{file_deleted(${ {'count' : %s} })}\">delete text value</p>".formatted(count);
		assertThat(this.process(template, locale)).isEqualTo("<p>%s</p>".formatted(expected));
	}

	@ParameterizedTest
	@MethodSource("provider_plural_cldr_keywords")
	void test_plural_cldr_keywords_in_thymeleaf(int count, Locale locale, String expected) {
		String template = "<p th:text=\"#{cart_summary(${ {'count' : %s} })}\">cart summary value</p>".formatted(count);
		assertThat(this.process(template, locale)).isEqualTo("<p>%s</p>".formatted(expected));
	}

	@ParameterizedTest
	@MethodSource("provider_gender")
	void test_gender_in_thymeleaf(String recipientGender, Locale locale, String expected) {
		String template = "<p th:text=\"#{greeting(${ {'recipient_gender' : '%s'} })}\">gender value</p>".formatted(recipientGender);
		assertThat(this.process(template, locale)).isEqualTo("<p>%s</p>".formatted(expected));
	}

	static Stream<Arguments> provider_plural_numeric_cases() {
		return Stream.of(
			Arguments.of(0, Locale.forLanguageTag("de"), "Sie haben keine Dateien gelöscht."),
			Arguments.of(1, Locale.forLanguageTag("de"), "Sie haben eine Datei gelöscht."),
			Arguments.of(1000, Locale.forLanguageTag("de"), "Sie haben 1.000 Dateien gelöscht."),
			Arguments.of(0, Locale.forLanguageTag("en"), "You deleted no files."),
			Arguments.of(1, Locale.forLanguageTag("en"), "You deleted one file."),
			Arguments.of(1000, Locale.forLanguageTag("en"), "You deleted 1,000 files.")
		);
	}

	static Stream<Arguments> provider_gender() {
		return Stream.of(
			Arguments.of("feminine", Locale.forLanguageTag("de"), "Wie geht es ihr?"),
			Arguments.of("masculine", Locale.forLanguageTag("de"), "Wie geht es ihm?"),
			Arguments.of("nonbinary", Locale.forLanguageTag("de"), "Wie geht es ihnen?"),
			Arguments.of("feminine", Locale.forLanguageTag("en"), "How is she?")
		);
	}

	static Stream<Arguments> provider_plural_cldr_keywords() {
		return Stream.of(
			Arguments.of(1, Locale.forLanguageTag("de"), "Ein Artikel liegt in Ihrem Warenkorb und ist bereit zur Kasse."),
			Arguments.of(5, Locale.forLanguageTag("de"), "Mehrere Artikel liegen in Ihrem Warenkorb und sind bereit zur Kasse."),
			Arguments.of(1, Locale.forLanguageTag("en"), "There is one item in your shopping cart, ready for checkout."),
			Arguments.of(5, Locale.forLanguageTag("en"), "There are several items in your shopping cart, ready for checkout.")
		);
	}

	String process(String template, Locale locale) {
		return this.templateEngine.process(template, new Context(locale));
	}
}
