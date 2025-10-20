package org.codehaus.modello.generator.xml.stax;

import javax.inject.Inject;

import java.util.List;

import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.Version;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 */
public class StaxGeneratorWrongVersionTest extends AbstractStaxGeneratorTestCase {

    @Inject
    private ModelloCore modello;

    public StaxGeneratorWrongVersionTest() throws ComponentLookupException {
        super("stax-wrong-version");
    }

    @Test
    public void testStaxReaderVersionInField() throws Throwable {
        Model model = modello.loadModel(getXmlResourceReader("/version-in-field.mdo"));

        List<ModelClass> classesList = model.getClasses(new Version("4.0.0"));

        assertEquals(1, classesList.size());

        ModelClass clazz = (ModelClass) classesList.get(0);

        assertEquals("Model", clazz.getName());

        verifyModel(model, "org.codehaus.modello.generator.xml.stax.StaxVerifierWrongVersion");
    }
}
