package org.codehaus.modello.plugins.xml;

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

import java.util.List;

import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.Version;
import org.codehaus.modello.plugins.xml.metadata.XmlFieldMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlMetadataPlugin;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.testing.PlexusTest;
import org.codehaus.plexus.testing.PlexusTestConfiguration;
import org.junit.jupiter.api.Test;

import static org.codehaus.plexus.testing.PlexusExtension.getTestFile;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l </a>
 */
@PlexusTest
public class XmlModelloPluginTest implements PlexusTestConfiguration {
    @Inject
    private ModelloCore modello;

    @Inject
    private XmlMetadataPlugin object;

    @Test
    public void testConfiguration() throws Exception {

        assertNotNull(object);

        assertTrue(object instanceof XmlMetadataPlugin);
    }

    @Test
    public void testXmlPlugin() throws Exception {

        Model model = modello.loadModel(getTestFile("src/test/resources/model.mdo"));

        List<ModelClass> classes = model.getClasses(new Version("4.0.0"));

        assertEquals(2, classes.size());

        ModelClass clazz = (ModelClass) classes.get(0);

        assertEquals("Model", clazz.getName());

        assertEquals(3, clazz.getFields(new Version("4.0.0")).size());

        ModelField extend = clazz.getField("extend", new Version("4.0.0"));

        assertTrue(extend.hasMetadata(XmlFieldMetadata.ID));

        XmlFieldMetadata xml = (XmlFieldMetadata) extend.getMetadata(XmlFieldMetadata.ID);

        assertNotNull(xml);

        assertFalse(xml.isAttribute());

        extend = clazz.getField("extend", new Version("4.1.0"));

        assertTrue(extend.hasMetadata(XmlFieldMetadata.ID));

        xml = (XmlFieldMetadata) extend.getMetadata(XmlFieldMetadata.ID);

        assertNotNull(xml);

        assertTrue(xml.isAttribute());

        ModelField parent = clazz.getField("parent", new Version("4.0.0"));

        try {
            parent.getMetadata("foo");

            fail("Expected ModelloException");
        } catch (ModelloRuntimeException ex) {
            // expected
        }

        ModelField builder = clazz.getField("builder", new Version("4.0.0"));

        assertTrue(builder.hasMetadata(XmlFieldMetadata.ID));

        xml = (XmlFieldMetadata) builder.getMetadata(XmlFieldMetadata.ID);

        assertNotNull(xml);

        assertEquals("build", xml.getTagName());

        assertTrue(xml.isTrim());
    }

    @Override
    public void customizeConfiguration(ContainerConfiguration configuration) {
        configuration.setAutoWiring(true);
        configuration.setClassPathScanning(PlexusConstants.SCANNING_INDEX);
    }
}
