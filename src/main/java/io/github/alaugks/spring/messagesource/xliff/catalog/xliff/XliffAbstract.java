package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogInterface;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;
import java.util.Locale;

// https://docs.oasis-open.org/xliff/v1.2/xliff-profile-html/xliff-profile-html-1.2.html#General_Identifiers
// https://docs.oasis-open.org/xliff/xliff-core/v2.0/csprd01/xliff-core-v2.0-csprd01.html#segment
// https://docs.oasis-open.org/xliff/xliff-core/v2.1/os/xliff-core-v2.1-os.html#segment
abstract class XliffAbstract {
    protected void readItems(
            CatalogInterface catalog,
            String domain,
            Locale locale,
            NodeList translationNodes,
            List<String> translationUnitIdentifiers
    ) {
        for (int item = 0; item < translationNodes.getLength(); item++) {
            Node translationNode = translationNodes.item(item);

            /* Translation Node */
            Element translationNodeElement = (Element) translationNode;
            /* <target> */
            String targetValue = XliffParserUtility.getTargetValue(translationNodeElement);
            /* code */
            String code = XliffParserUtility.getCode(translationNodeElement, translationUnitIdentifiers);

            /* Add catalog */
            catalog.put(locale, domain, code, targetValue);
        }
    }
}
