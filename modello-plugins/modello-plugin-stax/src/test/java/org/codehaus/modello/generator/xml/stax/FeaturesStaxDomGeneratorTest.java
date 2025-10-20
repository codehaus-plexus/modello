package org.codehaus.modello.generator.xml.stax;

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

import java.util.Map;

import org.codehaus.modello.AbstractModelloJavaGeneratorTest;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.plexus.testing.PlexusTest;
import org.junit.jupiter.api.Test;

/**
 * @author Hervé Boutemy
 */
@PlexusTest
public class FeaturesStaxDomGeneratorTest extends AbstractModelloJavaGeneratorTest {
    @Inject
    private ModelloCore modello;

    public FeaturesStaxDomGeneratorTest() {
        super("features-dom");
    }

    @Test
    public void testJavaGenerator() throws Throwable {
        Model model = modello.loadModel(getXmlResourceReader("/features.mdo"));

        Map<String, Object> parameters = getModelloParameters("1.0.0");
        parameters.put("modello.dom.xpp3", "false");

        modello.generate(model, "java", parameters);
        modello.generate(model, "stax-writer", parameters);
        modello.generate(model, "stax-reader", parameters);

        addDependency("org.codehaus.woodstox", "stax2-api");
        addDependency("com.fasterxml.woodstox", "woodstox-core");
        addDependency("org.xmlunit", "xmlunit-core");

        compileGeneratedSources(8);

        // TODO: see why without this, version system property is set to "2.4.1" value after verify
        System.setProperty("version", getModelloVersion());

        verifyCompiledGeneratedSources("org.codehaus.modello.generator.xml.stax.StaxFeaturesVerifier");
    }
}
