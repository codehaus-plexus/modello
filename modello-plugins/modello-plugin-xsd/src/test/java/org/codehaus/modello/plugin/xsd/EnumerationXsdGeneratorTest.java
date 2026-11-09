package org.codehaus.modello.plugin.xsd;

/*
 * Copyright (c) 2024, Codehaus.org
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
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import java.io.File;
import java.io.StringReader;
import java.util.Map;

import org.codehaus.modello.AbstractModelloGeneratorTest;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.plexus.testing.PlexusTest;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests that {@code xsd.enumerations}, {@code xsd.suggestions} and {@code xsd.pattern} field
 * metadata attributes are correctly translated to XSD facets by {@link XsdGenerator}.
 *
 * @see <a href="https://github.com/codehaus-plexus/modello/issues/633">issue 633</a>
 */
@PlexusTest
public class EnumerationXsdGeneratorTest extends AbstractModelloGeneratorTest {

    @Inject
    private ModelloCore modello;

    public EnumerationXsdGeneratorTest() {
        super("xsd-enum-test");
    }

    @Test
    public void testEnumerationConstraints() throws Throwable {
        Model model = modello.loadModel(getXmlResourceReader("/xsd-enum-test.mdo"));

        Map<String, Object> parameters = getModelloParameters("1.0.0");
        modello.generate(model, "xsd", parameters);

        File xsdFile = new File(getOutputDirectory(), "xsd-enum-test-1.0.0.xsd");
        assertTrue(xsdFile.exists(), "Generated XSD file should exist: " + xsdFile);

        // ---- Structural assertions on the generated XSD ----

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(xsdFile);
        XPath xpath = XPathFactory.newInstance().newXPath();

        // 1. scope must have a xs:restriction with xs:enumeration children
        NodeList scopeEnums = (NodeList) xpath.evaluate(
                "//*[local-name()='element' and @name='scope']"
                        + "//*[local-name()='restriction' and @base='xs:string']"
                        + "/*[local-name()='enumeration']",
                doc,
                XPathConstants.NODESET);
        assertEquals(5, scopeEnums.getLength(), "scope should have 5 strict enumerations");

        // 2. packaging must have a xs:union containing xs:enumeration members
        NodeList packagingEnums = (NodeList) xpath.evaluate(
                "//*[local-name()='element' and @name='packaging']"
                        + "//*[local-name()='union']"
                        + "//*[local-name()='enumeration']",
                doc,
                XPathConstants.NODESET);
        assertEquals(5, packagingEnums.getLength(), "packaging should have 5 enumeration suggestions inside a union");

        // 3. artifactId must have a xs:pattern facet
        NodeList patternNodes = (NodeList) xpath.evaluate(
                "//*[local-name()='element' and @name='artifactId']" + "//*[local-name()='pattern']",
                doc,
                XPathConstants.NODESET);
        assertEquals(1, patternNodes.getLength(), "artifactId should have exactly one xs:pattern facet");
        assertEquals(
                "[a-zA-Z0-9._\\-]+",
                patternNodes.item(0).getAttributes().getNamedItem("value").getTextContent(),
                "artifactId pattern value should match");

        // 4. description must still be plain xs:string (no inline simpleType)
        NodeList descType = (NodeList) xpath.evaluate(
                "//*[local-name()='element' and @name='description' and @type='xs:string']",
                doc,
                XPathConstants.NODESET);
        assertEquals(1, descType.getLength(), "description should remain a plain xs:string element");

        // ---- Validation assertions ----

        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(new StreamSource(xsdFile));
        Validator validator = schema.newValidator();

        // Valid document passes
        validator.validate(new StreamSource(getClass().getResourceAsStream("/xsd-enum-test-valid.xml")));

        // Invalid scope (strict enumeration) must be rejected
        try {
            validator.validate(new StreamSource(getClass().getResourceAsStream("/xsd-enum-test-invalid-scope.xml")));
            fail("Validation should have failed for invalid scope value");
        } catch (SAXParseException e) {
            // expected
            assertTrue(
                    e.getMessage().contains("scope") || e.getMessage().contains("unknown-scope"),
                    "Error should mention the invalid scope value: " + e.getMessage());
        }

        // Invalid artifactId (pattern violation) must be rejected
        try {
            validator.validate(new StreamSource(getClass().getResourceAsStream("/xsd-enum-test-invalid-pattern.xml")));
            fail("Validation should have failed for invalid artifactId pattern");
        } catch (SAXParseException e) {
            // expected
            assertTrue(
                    e.getMessage().contains("artifactId") || e.getMessage().contains("pattern"),
                    "Error should mention the invalid artifactId value: " + e.getMessage());
        }

        // Suggested value outside the enumeration (custom packaging type) must PASS validation
        String customPackagingXml = "<?xml version=\"1.0\"?>"
                + "<artifact xmlns=\"http://codehaus-plexus.github.io/XSD-ENUM-TEST/1.0.0\">"
                + "<scope>test</scope>"
                + "<packaging>custom-type</packaging>"
                + "<artifactId>my-lib</artifactId>"
                + "</artifact>";
        validator.validate(new StreamSource(new StringReader(customPackagingXml)));
    }
}
