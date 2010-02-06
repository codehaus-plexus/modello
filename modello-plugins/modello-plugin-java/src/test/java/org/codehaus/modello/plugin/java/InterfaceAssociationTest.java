package org.codehaus.modello.plugin.java;

import java.util.Properties;

import org.codehaus.modello.AbstractModelloJavaGeneratorTest;
import org.codehaus.modello.ModelloParameterConstants;
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

        Properties parameters = new Properties();
        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, getOutputDirectory().getAbsolutePath() );
        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.toString( false ) );
        parameters.setProperty( ModelloParameterConstants.USE_JAVA5, Boolean.toString( true ) );
        parameters.setProperty( ModelloParameterConstants.VERSION, "4.0.0" );

        modello.generate( model, "java", parameters );

        compileGeneratedSources( true );

        verifyGeneratedCode( "InterfaceAssociationVerifier" );
    }

}
