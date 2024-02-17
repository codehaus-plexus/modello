package org.codehaus.modello.plugin.snakeyaml;

/*
 * Copyright (c) 2013, Codehaus.org
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

import java.util.List;
import java.util.Properties;

import org.codehaus.modello.AbstractModelloJavaGeneratorTest;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.Version;

/**
 * @author <a href="mailto:simonetripodi@apache.org">Simone Tripodi</a>
 */
public class SnakeyamlGeneratorTest extends AbstractModelloJavaGeneratorTest {
    public SnakeyamlGeneratorTest() {
        super("snakeyaml");
    }

    public void testGenerator() throws Throwable {
        ModelloCore modello = (ModelloCore) lookup(ModelloCore.ROLE);

        Model model = modello.loadModel(getXmlResourceReader("/ibcore-executor.mdo"));

        // check some elements read from the model
        List<ModelClass> classesList = model.getClasses(new Version("4.0.0"));

        assertEquals(3, classesList.size());

        ModelClass clazz = (ModelClass) classesList.get(0);

        assertEquals("GeneratedProcessExecution", clazz.getName());

        // now generate sources and test them
        Properties parameters = getModelloParameters("4.0.0");

        modello.generate(model, "java", parameters);
        modello.generate(model, "snakeyaml-reader", parameters);
        modello.generate(model, "snakeyaml-writer", parameters);

        addDependency("org.yaml", "snakeyaml");
        compileGeneratedSources();

        // TODO: see why without this, version system property is set to "2.4.1" value after verify
        System.setProperty("version", getModelloVersion());

        verifyCompiledGeneratedSources("org.codehaus.modello.generator.xml.sax.SaxVerifier");
    }
}
