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

import java.util.Map;

import org.codehaus.modello.AbstractModelloGeneratorTest;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.plexus.testing.PlexusTest;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:olamy@apache.org">Brett Porter</a>
 */
@PlexusTest
public class ChangesXsdGeneratorTest extends AbstractModelloGeneratorTest {
    @Inject
    private ModelloCore modello;

    public ChangesXsdGeneratorTest() {
        super("xsd-changes");
    }

    @Test
    public void testXsdGenerator() throws Throwable {
        Model model = modello.loadModel(getXmlResourceReader("/changes.mdo"));

        // generate XSD file
        Map<String, Object> parameters = getModelloParameters("1.0.0");

        modello.generate(model, "xsd", parameters);

        // addDependency( "modello", "modello-core", "1.0-SNAPSHOT" );

        // TODO write verifier that compiles generated schema: use jaxp

        // verify( "org.codehaus.modello.generator.xml.xsd.XsdVerifier", "xsd" );

    }
}
