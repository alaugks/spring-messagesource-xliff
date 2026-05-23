// SPDX-License-Identifier: Apache-2.0
// Copyright 2023 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.catalog.CatalogMessageSourceBuilder;
import io.github.alaugks.spring.messagesource.catalog.catalog.CatalogInterface;
import io.github.alaugks.spring.messagesource.catalog.resources.LocationPattern;
import io.github.alaugks.spring.messagesource.catalog.resources.ResourcesLoader;
import io.github.alaugks.spring.messagesource.xliff.XliffCatalog.Xliff12Identifier;
import io.github.alaugks.spring.messagesource.xliff.XliffCatalog.Xliff2xIdentifier;
import io.github.alaugks.spring.messagesource.xliff.XliffCatalog.XliffIdentifier;
import java.util.List;
import java.util.Locale;

public class XliffResourceMessageSource {

	/**
	 * Utility class — not intended to be instantiated.
	 *
	 * @throws IllegalStateException always.
	 */
	private XliffResourceMessageSource() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Creates a new {@link Builder} for assembling an XLIFF-backed Spring
	 * {@code MessageSource}.
	 *
	 * <p>Before (Deprecated):</p>
	 *
	 * <pre>{@code
	 *	return XliffResourceMessageSource
	 *		.builder(
	 *			Locale.forLanguageTag("en"),
	 *			"translations/*"
	 *		)
	 *		.build();
	 *}
	 * </pre>
	 *
	 * <p>Now:</p>
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

	/**
	 * Creates a new {@link Builder} using a plain location pattern string.
	 *
	 * @param defaultLocale   the locale to fall back to when a translation is
	 *                        not available in the requested locale.
	 * @param locationPattern Spring resource pattern describing where the
	 *                        XLIFF files are located.
	 * @return a new builder pre-configured with the given defaults.
	 * @deprecated Use this instead: {@link #builder(Locale defaultLocale, LocationPattern locationPattern)}.
	 */
	@Deprecated(since = "2.1.0")
	public static Builder builder(Locale defaultLocale, String locationPattern) {
		return builder(defaultLocale, new LocationPattern(locationPattern));
	}

	/**
	 * Creates a new {@link Builder} using a list of plain location pattern
	 * strings.
	 *
	 * @param defaultLocale    the locale to fall back to when a translation is
	 *                         not available in the requested locale.
	 * @param locationPatterns Spring resource patterns describing where the
	 *                         XLIFF files are located.
	 * @return a new builder pre-configured with the given defaults.
	 * @deprecated Use this instead: {@link #builder(Locale defaultLocale, LocationPattern locationPattern)}.
	 */
	@Deprecated(since = "2.1.0")
	public static Builder builder(Locale defaultLocale, List<String> locationPatterns) {
		return builder(defaultLocale, new LocationPattern(locationPatterns));
	}

	public static final class Builder {

		private final Locale defaultLocale;

		private final LocationPattern locationPattern;

		private String defaultDomain = CatalogMessageSourceBuilder.DEFAULT_DOMAIN;

		private List<String> fileExtensions = List.of("xlf", "xliff");

		private List<XliffIdentifier> identifier = List.of(
				new Xliff12Identifier(List.of("resname", "id")),
				new Xliff2xIdentifier(List.of("id"))
		);

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
		 * Overrides the per-version identifier strategies used to resolve the
		 * key of each translation unit.
		 *
		 * @param identifier the identifier strategies to use.
		 * @return this builder for chaining.
		 */
		public Builder identifier(List<XliffIdentifier> identifier) {
			this.identifier = identifier;
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
					this.identifier
			);

			return CatalogMessageSourceBuilder
					.builder(xliffCatalog, this.defaultLocale)
					.defaultDomain(this.defaultDomain)
					.build();
		}
	}
}
