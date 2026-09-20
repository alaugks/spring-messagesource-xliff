// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.base.AbstractBaseMessageSourceBuilder;
import io.github.alaugks.spring.messagesource.base.BaseMessageSourceBuilder;
import io.github.alaugks.spring.messagesource.base.resources.ResourceLoaderBuilder;
import io.github.alaugks.spring.messagesource.base.resources.TargetLocaleResolverInterface;
import java.util.List;
import java.util.Locale;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

/**
 * Entry point for assembling an XLIFF-backed Spring {@code MessageSource}.
 */
public final class XliffResourceMessageSource {

	/**
	 * Utility class — not intended to be instantiated.
	 */
	private XliffResourceMessageSource() {
		throw new UnsupportedOperationException("XliffResourceMessageSource not supported.");
	}

	/**
	 * Creates a new {@link Builder} for assembling an XLIFF-backed Spring
	 * {@code MessageSource}.
	 *
	 * <pre>{@code
	 *	return XliffResourceMessageSource
	 *		.builder(
	 *			Locale.forLanguageTag("en"),
	 *			"translations/*"
	 *		)
	 *		.build();
	 * }
	 * </pre>
	 *
	 * @param defaultLocale   the locale to fall back to when a translation is
	 *                        not available in the requested locale.
	 * @param locationPattern Spring resource pattern(s) describing where the
	 *                        XLIFF files are located.
	 * @return a new builder pre-configured with the given defaults.
	 */
	public static Builder builder(Locale defaultLocale, String locationPattern) {
		return new Builder(defaultLocale, List.of(locationPattern));
	}

	/**
	 * Creates a new {@link Builder} for assembling an XLIFF-backed Spring
	 * {@code MessageSource}.
	 *
	 * <pre>{@code
	 *	return XliffResourceMessageSource
	 *		.builder(
	 *			Locale.forLanguageTag("en"),
	 *			List.of("translations_de/*", "translations_en/*")
	 *		)
	 *		.build();
	 * }
	 * </pre>
	 *
	 * @param defaultLocale   the locale to fall back to when a translation is
	 *                        not available in the requested locale.
	 * @param locationPattern Spring resource pattern(s) describing where the
	 *                        XLIFF files are located.
	 * @return a new builder pre-configured with the given defaults.
	 */
	public static Builder builder(Locale defaultLocale, List<String> locationPattern) {
		return new Builder(defaultLocale, locationPattern);
	}

	/**
	 * Builder for assembling an XLIFF-backed Spring {@code MessageSource}.
	 */
	public static final class Builder extends AbstractBaseMessageSourceBuilder<Builder> {

		private final List<String> locationPattern;

		private List<String> fileExtensions = List.of("xlf", "xliff");

		private boolean validateSchema = false;

		@Nullable
		private TargetLocaleResolverInterface targetLocaleResolver;

		/**
		 * Creates a new builder with the given default locale and XLIFF file
		 * location pattern.
		 *
		 * @param defaultLocale   the locale to fall back to when a translation
		 *                        is not available in the requested locale.
		 * @param locationPattern Spring resource pattern(s) describing where
		 *                        the XLIFF files are located.
		 */
		public Builder(Locale defaultLocale, List<String> locationPattern) {
			super(defaultLocale);
			this.locationPattern = locationPattern;
		}

		/**
		 * Overrides the list of file extensions that are recognised as XLIFF
		 * files.
		 * <p>The defaults are {@code xlf} and {@code xliff}.
		 *
		 * @param fileExtensions the file extensions to consider (without the
		 *                       leading dot).
		 * @return this builder for chaining.
		 */
		public Builder fileExtensions(List<String> fileExtensions) {
			this.fileExtensions = fileExtensions;
			return this;
		}

		/**
		 * Controls whether each XLIFF document is validated against its OASIS XSD schema before its units are
		 * extracted.
		 * <p>Validation is disabled by default. Enable it to reject documents
		 * that do not conform to the schema; note that strict schemas reject files that are otherwise readable (for
		 * example XLIFF 1.2 files whose {@code <trans-unit>} elements omit the schema-required {@code id} attribute).
		 *
		 * @return this builder for chaining.
		 */
		public Builder enableSchemaValidation() {
			this.validateSchema = true;
			return this;
		}

		/**
		 * Sets whether each XLIFF document is validated against its OASIS XSD schema
		 * before its units are extracted.
		 *
		 * @param validateSchema {@code true} to enable schema validation, {@code false}
		 *                       to disable it.
		 * @return this builder for chaining.
		 * @deprecated since 4.1.0, use {@link #enableSchemaValidation()} instead.
		 */
		@Deprecated(since = "4.1.0")
		public Builder validateSchema(boolean validateSchema) {
			this.validateSchema = validateSchema;
			return this;
		}

		/**
		 * Configures the builder to use the XLIFF language attribute for determining
		 * the target locale of XLIFF files.
		 *
		 * This method sets the {@code fileNameParser} field to an instance of
		 * {@link XliffLanguageAttrParser}, enabling the extraction of the target
		 * locale directly from the language-related attributes defined in the XLIFF
		 * document.
		 *
		 * @return this builder instance for method chaining.
		 */
		public Builder useXliffLanguageAttribute() {
			this.targetLocaleResolver = new XliffLanguageAttrParser();
			return this;
		}

		/**
		 * Assigns a custom implementation of {@link TargetLocaleResolverInterface} to resolve the
		 * target locale of XLIFF files.
		 *
		 * @param targetLocaleResolver an implementation of {@link TargetLocaleResolverInterface}
		 *                               used to resolve the target locale of XLIFF files; must
		 *                               not be null.
		 * @return this builder instance for method chaining.
		 */
		public Builder targetLocaleResolver(TargetLocaleResolverInterface targetLocaleResolver) {
			Assert.notNull(targetLocaleResolver, "targetLocaleResolver must not be null");

			this.targetLocaleResolver = targetLocaleResolver;
			return this;
		}

		/**
		 * Assembles the configured {@link BaseMessageSourceBuilder} backed
		 * by an {@link XliffCatalog} loaded from the configured location
		 * pattern.
		 *
		 * @return the configured message source builder.
		 */
		public BaseMessageSourceBuilder build() {
			ResourceLoaderBuilder resourcesLoader = ResourceLoaderBuilder
				.builder(this.getDefaultLocale(), this.locationPattern)
				.fileExtensions(this.fileExtensions)
				.targetLocaleResolver(this.targetLocaleResolver)
				.build();

			XliffCatalog xliffCatalog = new XliffCatalog(
				resourcesLoader.getTranslationFiles(),
				this.validateSchema
			);

			return BaseMessageSourceBuilder
				.builder(this.getDefaultLocale(), xliffCatalog.getTransUnits())
				.parentMessageSource(this.getParentMessageSource())
				.useICU4j(this.isICU4jEnabled())
				.build();
		}
	}
}
