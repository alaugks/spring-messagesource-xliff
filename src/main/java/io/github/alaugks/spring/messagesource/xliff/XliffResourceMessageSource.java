package io.github.alaugks.spring.messagesource.xliff;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.github.alaugks.spring.messagesource.catalog.CatalogMessageSourceBuilder;
import io.github.alaugks.spring.messagesource.catalog.catalog.Catalog;
import io.github.alaugks.spring.messagesource.catalog.catalog.CatalogInterface;
import io.github.alaugks.spring.messagesource.catalog.ressources.ResourcesLoader;
import io.github.alaugks.spring.messagesource.xliff.XliffCatalog.Xliff12Identifier;
import io.github.alaugks.spring.messagesource.xliff.XliffCatalog.Xliff2xIdentifier;
import io.github.alaugks.spring.messagesource.xliff.XliffCatalog.XliffIdentifierInterface;

public class XliffResourceMessageSource {

	private XliffResourceMessageSource() {
		throw new IllegalStateException(XliffResourceMessageSource.class.toString());
	}

	public static Builder builder(Locale defaultLocale, String locationPattern) {
		return new Builder(defaultLocale, List.of(locationPattern));
	}

	public static Builder builder(Locale defaultLocale, List<String> locationPatterns) {
		return new Builder(defaultLocale, locationPatterns);
	}

	public static final class Builder {

		private final Locale defaultLocale;

		private final Set<String> locationPatterns;

		private String defaultDomain = Catalog.DEFAULT_DOMAIN;

		private List<String> fileExtensions = List.of("xlf", "xliff");

		private List<XliffIdentifierInterface> identifier = List.of(
				new Xliff12Identifier(List.of("resname", "id")),
				new Xliff2xIdentifier(List.of("id"))
		);

		public Builder(Locale defaultLocale, List<String> locationPatterns) {
			this.defaultLocale = defaultLocale;
			this.locationPatterns = new HashSet<>(locationPatterns);
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
					this.locationPatterns,
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
