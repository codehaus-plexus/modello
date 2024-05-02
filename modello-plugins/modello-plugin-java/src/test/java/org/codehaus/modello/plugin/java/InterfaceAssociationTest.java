package org.codehaus.modello.plugin.java;

import java.util.Map;

import org.codehaus.modello.AbstractModelloJavaGeneratorTest;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;

public class InterfaceAssociationTest extends AbstractModelloJavaGeneratorTest {
    public InterfaceAssociationTest() {
        super("interfaceAssociationTest");
    }

    public void testJavaGenerator() throws Throwable {
        ModelloCore modello = (ModelloCore) lookup(ModelloCore.ROLE);

        Model model = modello.loadModel(getXmlResourceReader("/models/interfaceAssociation.mdo"));

        Map<String, Object> parameters = getModelloParameters("4.0.0", 8);

        modello.generate(model, "java", parameters);

        compileGeneratedSources(8);

        verifyCompiledGeneratedSources("InterfaceAssociationVerifier");
    }
}
