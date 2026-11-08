package org.codehaus.modello.plugin.xsd;

/*
 * Copyright (c) 2005, Codehaus.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import java.io.File;
import java.util.Map;

import org.codehaus.modello.AbstractModelloGeneratorTest;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.plexus.testing.PlexusTest;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the XSD generator emits {@code mixed="true"} on {@code xs:complexType}
 * elements when a Modello field is annotated with {@code xml.format="mixed"}.
 *
 * <p>Two cases are covered:
 * <ol>
 *   <li><b>Case 2 — body-level mixed content</b>: a field with {@code xml.format="mixed"}
 *       and no {@code xml.tagName}.  The named {@code xs:complexType} written by
 *       {@code writeComplexTypeDescriptor()} must carry {@code mixed="true"}.</li>
 *   <li><b>Case 1 — wrapper mixed content</b>: a list field with {@code xml.format="mixed"}
 *       <em>and</em> an explicit {@code xml.tagName}.  The anonymous {@code xs:complexType}
 *       emitted by {@code writeListElement()} must carry {@code mixed="true"}.</li>
 * </ol>
 *
 * <p>A control class with no mixed field verifies that {@code mixed="true"} is NOT
 * emitted for unrelated {@code xs:complexType} elements.
 *
 * @see <a href="https://github.com/codehaus-plexus/modello/pull/628">PR #628</a>
 */
@PlexusTest
public class MixedContentXsdGeneratorTest extends AbstractModelloGeneratorTest {

    @Inject
    private ModelloCore modello;

    public MixedContentXsdGeneratorTest() {
        super("xsd-mixed-content");
    }

    @Test
    public void testMixedContentXsd() throws Throwable {
        Model model = modello.loadModel(getXmlResourceReader("/mixed-content.mdo"));

        Map<String, Object> parameters = getModelloParameters("1.0.0");
        modello.generate(model, "xsd", parameters);

        File xsdFile = new File(getOutputDirectory(), "mixed-content-1.0.0.xsd");
        assertTrue(xsdFile.exists(), "Generated XSD file must exist");

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(xsdFile);

        XPath xpath = XPathFactory.newInstance().newXPath();

        // --- Case 2: MixedBody's named xs:complexType must have mixed="true" ---
        //
        // Expected fragment:
        //   <xs:complexType name="MixedBody" mixed="true">
        //
        NodeList mixedBodyTypes = (NodeList) xpath.evaluate(
                "//*[local-name()='complexType' and @name='MixedBody' and @mixed='true']", doc, XPathConstants.NODESET);
        assertEquals(
                1,
                mixedBodyTypes.getLength(),
                "MixedBody xs:complexType must have mixed=\"true\" "
                        + "(field with xml.format=\"mixed\" and no xml.tagName)");

        // --- Case 1: MixedWrapper's anonymous xs:complexType (inside wrapper element) ---
        //
        // Expected fragment (child of xs:element name="args"):
        //   <xs:element name="args">
        //     <xs:complexType mixed="true">
        //       <xs:sequence> ... </xs:sequence>
        //     </xs:complexType>
        //   </xs:element>
        //
        NodeList wrapperMixedTypes = (NodeList) xpath.evaluate(
                "//*[local-name()='complexType' and not(@name) and @mixed='true']", doc, XPathConstants.NODESET);
        assertEquals(
                1,
                wrapperMixedTypes.getLength(),
                "MixedWrapper must have exactly one anonymous xs:complexType with mixed=\"true\" "
                        + "(list field with xml.format=\"mixed\" and xml.tagName)");

        // --- Control: Plain xs:complexType must NOT have mixed="true" ---
        //
        NodeList plainMixedTypes = (NodeList) xpath.evaluate(
                "//*[local-name()='complexType' and @name='Plain' and @mixed='true']", doc, XPathConstants.NODESET);
        assertEquals(
                0, plainMixedTypes.getLength(), "Plain xs:complexType must NOT have mixed=\"true\" (no mixed field)");

        // --- Sanity: MixedBody without mixed="true" must not appear ---
        NodeList mixedBodyNotMixed = (NodeList) xpath.evaluate(
                "//*[local-name()='complexType' and @name='MixedBody' and not(@mixed='true')]",
                doc,
                XPathConstants.NODESET);
        assertEquals(
                0,
                mixedBodyNotMixed.getLength(),
                "MixedBody xs:complexType must have mixed=\"true\" — found one without it");
    }
}
