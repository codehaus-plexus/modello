package org.codehaus.modello.plugin.java;

import javax.inject.Inject;

import java.util.Map;

import org.codehaus.modello.AbstractModelloJavaGeneratorTest;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.plexus.testing.PlexusTest;
import org.junit.jupiter.api.Test;

@PlexusTest
public class InterfaceAssociationTest extends AbstractModelloJavaGeneratorTest {
    @Inject
    private ModelloCore modello;

    public InterfaceAssociationTest() {
        super("interfaceAssociationTest");
    }

    @Test
    public void testJavaGenerator() throws Throwable {
        Model model = modello.loadModel(getXmlResourceReader("/models/interfaceAssociation.mdo"));

        Map<String, Object> parameters = getModelloParameters("4.0.0", 8);

        modello.generate(model, "java", parameters);

        compileGeneratedSources(8);

        verifyCompiledGeneratedSources("InterfaceAssociationVerifier");
    }
}
