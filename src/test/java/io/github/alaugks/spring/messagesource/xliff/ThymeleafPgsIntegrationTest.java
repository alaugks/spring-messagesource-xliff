// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.alaugks.spring.messagesource.catalog.resources.LocationPattern;
import java.util.Locale;
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

class ThymeleafPgsIntegrationTest {

	private SpringTemplateEngine templateEngine;

	@BeforeEach
	void setUp() {
		MessageSource messageSource = XliffResourceMessageSource
			.builder(Locale.forLanguageTag("en"), new LocationPattern("translations/*"))
			.enableICU4j()
			.build();

		StringTemplateResolver templateResolver = new StringTemplateResolver();
		templateResolver.setTemplateMode(TemplateMode.HTML);

		this.templateEngine = new SpringTemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		this.templateEngine.setTemplateEngineMessageSource(messageSource);
	}

	@ParameterizedTest
	@MethodSource("providePluralArguments")
	void test_plural_resolves_in_thymeleaf(int count, Locale locale, String expected) {
		String template = "<p th:text=\"#{files.file_deleted(${ {'count' : %s} })}\">delete text value</p>".formatted(count);
		assertThat(this.process(template, locale)).isEqualTo("<p>%s</p>".formatted(expected));
	}

	private static Stream<Arguments> providePluralArguments() {
		return Stream.of(
			Arguments.of(0, Locale.forLanguageTag("de"), "Sie haben keine Dateien gelöscht."),
			Arguments.of(1, Locale.forLanguageTag("de"), "Sie haben eine Datei gelöscht."),
			Arguments.of(1000, Locale.forLanguageTag("de"), "Sie haben 1.000 Dateien gelöscht."),
			Arguments.of(0, Locale.forLanguageTag("en"), "You deleted no files."),
			Arguments.of(1, Locale.forLanguageTag("en"), "You deleted one file."),
			Arguments.of(1000, Locale.forLanguageTag("en"), "You deleted 1,000 files.")
		);
	}

	@ParameterizedTest
	@MethodSource("provideGenderArguments")
	void test_gender_resolves_in_thymeleaf(String gender, Locale locale, String expected) {
		String template = "<p th:text=\"#{files.greeting(${ {'recipient_gender' : '%s'} })}\">gender value</p>".formatted(gender);
		assertThat(this.process(template, locale)).isEqualTo("<p>%s</p>".formatted(expected));
	}

	private static Stream<Arguments> provideGenderArguments() {
		return Stream.of(
			Arguments.of("feminine", Locale.forLanguageTag("de"), "Wie geht&#39;s ihr?"),
			Arguments.of("masculine", Locale.forLanguageTag("de"), "Wie geht&#39;s ihm?"),
			Arguments.of("nonbinary", Locale.forLanguageTag("de"), "Wie geht&#39;s ihnen?"),
			Arguments.of("feminine", Locale.forLanguageTag("en"), "How is she?")
		);
	}

	private String process(String template, Locale locale) {
		return this.templateEngine.process(template, new Context(locale));
	}
}
