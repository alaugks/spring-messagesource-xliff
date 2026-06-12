// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.catalog.CatalogMessageSourceBuilder;
import io.github.alaugks.spring.messagesource.catalog.catalog.CatalogInterface;
import io.github.alaugks.spring.messagesource.catalog.resources.LocationPattern;
import io.github.alaugks.spring.messagesource.catalog.resources.ResourcesLoader;
import java.util.List;
import java.util.Locale;

public class XliffResourceMessageSource {

	/**
	 * Utility class — not intended to be instantiated.
	 */
	private XliffResourceMessageSource() {
		throw new IllegalStateException("Not public constructors");
	}

	/**
	 * Creates a new {@link Builder} for assembling an XLIFF-backed Spring
	 * {@code MessageSource}.
	 *
	 * <pre>{@code
	 *	import io.github.alaugks.spring.messagesource.catalog.resources.LocationPattern;
	 *
	 *	return XliffResourceMessageSource
	 *		.builder(
	 *			Locale.forLanguageTag("en"),
	 *			new LocationPattern("translations/*")
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
	public static Builder builder(Locale defaultLocale, LocationPattern locationPattern) {
		return new Builder(defaultLocale, locationPattern);
	}

	public static final class Builder {

		private final Locale defaultLocale;

		private final LocationPattern locationPattern;

		private String defaultDomain = CatalogMessageSourceBuilder.DEFAULT_DOMAIN;

		private List<String> fileExtensions = List.of("xlf", "xliff");

		private boolean validateSchema = false;

		/**
		 * Creates a new builder with the given default locale and XLIFF file
		 * location pattern.
		 *
		 * @param defaultLocale   the locale to fall back to when a translation
		 *                        is not available in the requested locale.
		 * @param locationPattern Spring resource pattern(s) describing where
		 *                        the XLIFF files are located.
		 */
		public Builder(Locale defaultLocale, LocationPattern locationPattern) {
			this.defaultLocale = defaultLocale;
			this.locationPattern = locationPattern;
		}

		/**
		 * Sets the default domain on the underlying
		 * {@link CatalogMessageSourceBuilder}. Codes whose domain matches this
		 * value are accessible by their bare code; codes from other domains
		 * must be looked up as {@code <domain>.<code>}.
		 * <p>The domain itself is always parsed from the XLIFF file name; this
		 * setting only controls which domain is treated as "default" when
		 * resolving codes.
		 *
		 * @param defaultDomain the new default domain.
		 * @return this builder for chaining.
		 */
		public Builder defaultDomain(String defaultDomain) {
			this.defaultDomain = defaultDomain;
			return this;
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
		 * Controls whether each XLIFF document is validated against its OASIS
		 * XSD schema before its units are extracted.
		 * <p>Validation is disabled by default. Enable it to reject documents
		 * that do not conform to the schema; note that strict schemas reject
		 * files that are otherwise readable (for example XLIFF 1.2 files whose
		 * {@code <trans-unit>} elements omit the schema-required {@code id}
		 * attribute).
		 *
		 * @param validateSchema {@code true} to validate against the schema.
		 * @return this builder for chaining.
		 */
		public Builder validateSchema(boolean validateSchema) {
			this.validateSchema = validateSchema;
			return this;
		}

		/**
		 * Assembles the configured {@link CatalogMessageSourceBuilder} backed
		 * by an {@link XliffCatalog} loaded from the configured location
		 * pattern.
		 *
		 * @return the configured message source builder.
		 */
		public CatalogMessageSourceBuilder build() {
			ResourcesLoader resourcesLoader = new ResourcesLoader(
					this.defaultLocale,
					this.locationPattern,
					this.fileExtensions
			);

			CatalogInterface xliffCatalog = new XliffCatalog(
					resourcesLoader.getTranslationFiles(),
					this.validateSchema
			);

			return CatalogMessageSourceBuilder
					.builder(xliffCatalog, this.defaultLocale)
					.defaultDomain(this.defaultDomain)
					.build();
		}
	}
}
