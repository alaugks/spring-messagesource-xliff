/*
 * Copyright 2023-2025 André Laugks <alaugks@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.catalog.CatalogMessageSourceBuilder;
import io.github.alaugks.spring.messagesource.catalog.catalog.Catalog;
import io.github.alaugks.spring.messagesource.catalog.catalog.CatalogInterface;
import io.github.alaugks.spring.messagesource.catalog.resources.LocationPattern;
import io.github.alaugks.spring.messagesource.catalog.resources.ResourcesLoader;
import io.github.alaugks.spring.messagesource.xliff.XliffCatalog.Xliff12Identifier;
import io.github.alaugks.spring.messagesource.xliff.XliffCatalog.Xliff2xIdentifier;
import io.github.alaugks.spring.messagesource.xliff.XliffCatalog.XliffIdentifierInterface;
import java.util.List;
import java.util.Locale;

public class XliffResourceMessageSource {

	private XliffResourceMessageSource() {
		throw new IllegalStateException(XliffResourceMessageSource.class.toString());
	}

	public static Builder builder(Locale defaultLocale, LocationPattern locationPattern) {
		return new Builder(defaultLocale, locationPattern);
	}

	/**
	 * @deprecated Use this instead: {@link #builder(Locale defaultLocale, LocationPattern locationPattern)}.
	 */
	@Deprecated(since = "2.1.0")
	public static Builder builder(Locale defaultLocale, String locationPattern) {
		return builder(defaultLocale, new LocationPattern(locationPattern));
	}

	/**
	 * @deprecated Use this instead: {@link #builder(Locale defaultLocale, LocationPattern locationPattern)}.
	 */
	@Deprecated(since = "2.1.0")
	public static Builder builder(Locale defaultLocale, List<String> locationPatterns) {
		return builder(defaultLocale, new LocationPattern(locationPatterns));
	}

	public static final class Builder {

		private final Locale defaultLocale;

		private final LocationPattern locationPattern;

		private String defaultDomain = Catalog.DEFAULT_DOMAIN;

		private List<String> fileExtensions = List.of("xlf", "xliff");

		private List<XliffIdentifierInterface> identifier = List.of(
				new Xliff12Identifier(List.of("resname", "id")),
				new Xliff2xIdentifier(List.of("id"))
		);

		public Builder(Locale defaultLocale, LocationPattern locationPattern) {
			this.defaultLocale = defaultLocale;
			this.locationPattern = locationPattern;
		}

		public Builder defaultDomain(String defaultDomain) {
			this.defaultDomain = defaultDomain;
			return this;
		}

		public Builder fileExtensions(List<String> fileExtensions) {
			this.fileExtensions = fileExtensions;
			return this;
		}

		public Builder identifier(List<XliffIdentifierInterface> identifier) {
			this.identifier = identifier;
			return this;
		}

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
