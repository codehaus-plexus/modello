package org.codehaus.modello.plugin.java;

import java.util.Properties;

import org.codehaus.modello.AbstractModelloJavaGeneratorTest;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.plexus.util.ReaderFactory;

public class InterfaceAssocitationTest
    extends AbstractModelloJavaGeneratorTest
{

    private String modelFile = "src/test/resources/models/interfaceAssocition.xml";

    public InterfaceAssocitationTest()
    {
        super( "interfaceAssocitationTest" );
    }

    public void testJavaGenerator()
        throws Throwable
    {
        String javaVersion = System.getProperty( "java.specification.version", "1.5" );

        if ( "1.5".compareTo( javaVersion ) > 0 )
        {
            System.out.println( "Skipped Java 5 feature test, not supported by current test environment ("
                + javaVersion + ")" );
            return;
        }

        ModelloCore modello = (ModelloCore) lookup( ModelloCore.ROLE );

        Properties parameters = new Properties();
        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, getOutputDirectory().getAbsolutePath() );
        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.toString( false ) );
        parameters.setProperty( ModelloParameterConstants.USE_JAVA5, Boolean.toString( true ) );
        parameters.setProperty( ModelloParameterConstants.VERSION, "4.0.0" );

        Model model = modello.loadModel( ReaderFactory.newXmlReader( getTestFile( modelFile ) ) );

        modello.generate( model, "java", parameters );

        compile( getOutputDirectory(), getOutputClasses(), true );

        verify( "InterfaceAssociationVerifier", "java" );
    }

}
