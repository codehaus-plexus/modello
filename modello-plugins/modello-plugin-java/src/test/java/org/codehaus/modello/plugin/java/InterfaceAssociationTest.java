package org.codehaus.modello.plugin.java;

import java.util.Properties;

import org.codehaus.modello.AbstractModelloJavaGeneratorTest;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;

public class InterfaceAssociationTest
    extends AbstractModelloJavaGeneratorTest
{
    public InterfaceAssociationTest()
    {
        super( "interfaceAssociationTest" );
    }

    public void testJavaGenerator()
        throws Throwable
    {
        if ( skipJava5FeatureTest() )
        {
            return;
        }

        ModelloCore modello = (ModelloCore) lookup( ModelloCore.ROLE );

        Model model = modello.loadModel( getXmlResourceReader( "/models/interfaceAssociation.mdo" ) );

        Properties parameters = getModelloParameters( "4.0.0", true );

        modello.generate( model, "java", parameters );

        compileGeneratedSources( true );

        verifyCompiledGeneratedSources( "InterfaceAssociationVerifier" );
    }

}
