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
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import java.io.File;
import java.util.Map;

import org.codehaus.modello.AbstractModelloGeneratorTest;
import org.codehaus.modello.ModelloException;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.plexus.testing.PlexusTest;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Checks that a DOM field accepts arbitrary attributes on its element, so that DOM merge hints such as
 * {@code combine.self} validate, while a Properties field does not.
 *
 * @see <a href="https://github.com/codehaus-plexus/modello/issues/482">issue 482</a>
 */
@PlexusTest
public class DomXsdGeneratorTest extends AbstractModelloGeneratorTest {
    @Inject
    private ModelloCore modello;

    public DomXsdGeneratorTest() {
        super("xsd-dom");
    }

    @Test
    public void testDomAttributesAreAllowed() throws Throwable {
        Model model = modello.loadModel(getXmlResourceReader("/dom.mdo"));

        Map<String, Object> parameters = getModelloParameters("1.0.0");

        modello.generate(model, "xsd", parameters);

        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(new StreamSource(new File(getOutputDirectory(), "dom-1.0.0.xsd")));
        Validator validator = schema.newValidator();

        try {
            validator.validate(new StreamSource(getClass().getResourceAsStream("/dom.xml")));
        } catch (SAXParseException e) {
            throw new ModelloException("line " + e.getLineNumber() + " column " + e.getColumnNumber(), e);
        }

        try {
            validator.validate(
                    new StreamSource(getClass().getResourceAsStream("/dom-invalid-properties-attribute.xml")));
            fail("attributes should not be allowed on a Properties element");
        } catch (SAXParseException e) {
            // ok, expected exception
            assertTrue(e.getMessage().contains("combine.self"));
        }
    }
}
