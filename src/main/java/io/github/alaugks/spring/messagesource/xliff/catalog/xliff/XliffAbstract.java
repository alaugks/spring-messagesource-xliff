package io.github.alaugks.spring.messagesource.xliff.catalog.xliff;

import io.github.alaugks.spring.messagesource.xliff.catalog.CatalogInterface;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Locale;

// http://docs.oasis-open.org/xliff/xliff-core/v2.0/csprd01/xliff-core-v2.0-csprd01.html#segment
// http://docs.oasis-open.org/xliff/xliff-core/v2.1/os/xliff-core-v2.1-os.html#segment
// http://docs.oasis-open.org/xliff/v1.2/xliff-profile-html/xliff-profile-html-1.2.html#General_Identifiers
public class XliffAbstract {
    protected void readItems(
            String version,
            CatalogInterface catalog,
            String domain,
            Locale locale,
            NodeList translationNodes
    ) {
        for (int item = 0; item < translationNodes.getLength(); item++) {
            Node translationNode = translationNodes.item(item);

            /* <trans-unit id=""> */
            String id = XliffParserUtility.getId(translationNode);

            String resname = null;
            if (Xliff12.VERSION.equals(version)) {
                resname = XliffParserUtility.getResname(translationNode, "resname");
            }

            /* <trans-unit> */
            Element translationNodeElement = (Element) translationNode;
            /* <source> */
            String sourceValue = XliffParserUtility.getSource(translationNodeElement);
            /* <target> */
            String targetValue = XliffParserUtility.getTargetValue(translationNodeElement);
            /* code */
            String code = XliffParserUtility.getCode(id, resname, sourceValue);

            /* Add catalog */
            catalog.put(locale, domain, code, targetValue);
        }
    }
}
