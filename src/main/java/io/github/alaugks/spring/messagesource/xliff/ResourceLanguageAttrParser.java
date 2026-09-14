package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.base.records.TransFileTargetLocale;
import io.github.alaugks.spring.messagesource.base.records.TransFileTargetLocaleInterface;
import io.github.alaugks.spring.messagesource.base.resources.TargetLocaleResolverInterface;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceRuntimeException;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceVersionSupportException;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.Resource;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

class ResourceLanguageAttrParser implements TargetLocaleResolverInterface {

	@Override
	public @Nullable TransFileTargetLocaleInterface resolve(Resource resource) {

		Element root;

		try {
			root = XliffDocumentParser.parseRootElement(
				XliffDocumentParser.newDocumentBuilderFactory(),
				resource.getContentAsByteArray()
			);
		}
		catch (SAXException | IOException | ParserConfigurationException e) {
			throw new XliffMessageSourceRuntimeException(e);
		}

		String version = XliffDocument.readVersion(root);
		if (version == null) {
			return null;
		}

		XliffLanguages langAttr = switch (version) {
			case "1.2" -> new Xliff12Document(root).getLanguages();
			case "2.0", "2.1", "2.2" -> new Xliff2xDocument(root).getLanguages();
			default -> throw new XliffMessageSourceVersionSupportException(
				String.format(
					"XLIFF version \"%s\" not supported. Supported versions: 1.2, 2.0, 2.1 and 2.2",
					version
				)
			);
		};

		if (langAttr.targetLanguage() == null) {
			throw new XliffMessageSourceVersionSupportException(
				String.format(
					"Target language not defined in XLIFF file: %s",
					version
				)
			);
		}

		return new TransFileTargetLocale(
			langAttr.targetLanguage().getLanguage(),
			langAttr.targetLanguage().getCountry()
		);
	}
}
