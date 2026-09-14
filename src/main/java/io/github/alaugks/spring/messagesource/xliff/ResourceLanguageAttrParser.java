package io.github.alaugks.spring.messagesource.xliff;

import io.github.alaugks.spring.messagesource.base.records.TransFileTargetLocale;
import io.github.alaugks.spring.messagesource.base.records.TransFileTargetLocaleInterface;
import io.github.alaugks.spring.messagesource.base.resources.TargetLocaleResolverInterface;
import io.github.alaugks.spring.messagesource.xliff.exception.SaxErrorHandler;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceRuntimeException;
import io.github.alaugks.spring.messagesource.xliff.exception.XliffMessageSourceVersionSupportException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public final class ResourceLanguageAttrParser implements TargetLocaleResolverInterface {

	@Override
	public @Nullable TransFileTargetLocaleInterface resolve(Resource resource) {

		Element root;

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

			DocumentBuilder documentBuilder = factory.newDocumentBuilder();
			documentBuilder.setErrorHandler(new SaxErrorHandler());
			Document document;
			document = documentBuilder.parse(new ByteArrayInputStream(resource.getContentAsByteArray()));

			root = document.getDocumentElement();
		}
		catch (SAXException | IOException | ParserConfigurationException e) {
			throw new XliffMessageSourceRuntimeException(e);
		}

		if (root == null) {
			return null;
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
