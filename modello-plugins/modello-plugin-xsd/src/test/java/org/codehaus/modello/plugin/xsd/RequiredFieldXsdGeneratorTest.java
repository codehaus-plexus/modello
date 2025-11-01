package org.codehaus.modello.plugin.xsd;

/*
 * Copyright (c) 2004, Codehaus.org
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
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import java.io.File;
import java.io.StringReader;
import java.util.Map;

import org.codehaus.modello.AbstractModelloGeneratorTest;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.plexus.testing.PlexusTest;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test that verifies required fields are properly enforced in generated XSD schemas
 * without needing the XSD_ENFORCE_MANDATORY_ELEMENTS parameter.
 *
 * @author Modello Team
 */
@PlexusTest
public class RequiredFieldXsdGeneratorTest extends AbstractModelloGeneratorTest {
    @Inject
    private ModelloCore modello;

    public RequiredFieldXsdGeneratorTest() {
        super("required-field");
    }

    @Test
    public void testRequiredFieldsEnforcedWithoutParameter() throws Throwable {
        Model model = modello.loadModel(getXmlResourceReader("/required-field.mdo"));

        // Generate XSD WITHOUT the XSD_ENFORCE_MANDATORY_ELEMENTS parameter
        Map<String, Object> parameters = getModelloParameters("1.0.0");
        // Note: NOT setting XSD_ENFORCE_MANDATORY_ELEMENTS parameter

        modello.generate(model, "xsd", parameters);

        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(new StreamSource(new File(getOutputDirectory(), "test-1.0.0.xsd")));
        Validator validator = schema.newValidator();

        // Test 1: Valid XML with all required fields should pass
        String validXml = "<?xml version=\"1.0\"?>\n"
                + "<testModel xmlns=\"http://codehaus-plexus.github.io/TEST/1.0.0\">\n"
                + "  <requiredField>value</requiredField>\n"
                + "</testModel>";

        validator.validate(new StreamSource(new StringReader(validXml)));

        // Test 2: XML missing required field should fail
        String missingRequiredXml = "<?xml version=\"1.0\"?>\n"
                + "<testModel xmlns=\"http://codehaus-plexus.github.io/TEST/1.0.0\">\n"
                + "  <optionalField>value</optionalField>\n"
                + "</testModel>";

        try {
            validator.validate(new StreamSource(new StringReader(missingRequiredXml)));
            fail("Validation should have failed for XML missing required field");
        } catch (SAXParseException e) {
            // Expected - the error should mention the required field
            assertTrue(
                    e.getMessage().contains("requiredField") || e.getMessage().contains("expected"),
                    "Error message should indicate missing required field: " + e.getMessage());
        }

        // Test 3: XML with optional field omitted should pass
        String withoutOptionalXml = "<?xml version=\"1.0\"?>\n"
                + "<testModel xmlns=\"http://codehaus-plexus.github.io/TEST/1.0.0\">\n"
                + "  <requiredField>value</requiredField>\n"
                + "</testModel>";

        validator.validate(new StreamSource(new StringReader(withoutOptionalXml)));
    }
}
