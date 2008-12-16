package org.codehaus.modello.plugin.converters;

import java.io.File;
import java.util.Properties;

import org.codehaus.modello.AbstractModelloGeneratorTest;
import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.ReaderFactory;

public class UpgradeGeneratorTest
    extends AbstractModelloGeneratorTest
{
    private String modelFile = "src/test/resources/models/maven.mdo";

    public UpgradeGeneratorTest()
    {
        super( "upgrade" );
    }

    private File generatedSources;

    private File classes;

    public void testConverterGenerator()
        throws Throwable
    {
        generatedSources = getTestFile( "target/" + getName() + "/sources" );

        classes = getTestFile( "target/" + getName() + "/classes" );

        FileUtils.deleteDirectory( generatedSources );

        generatedSources.mkdirs();

        classes.mkdirs();

        ModelloCore modello = (ModelloCore) lookup( ModelloCore.ROLE );

        Properties parameters = new Properties();

        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, generatedSources.getAbsolutePath() );

        parameters.setProperty( ModelloParameterConstants.VERSION, "4.0.0" );

        parameters.setProperty( ModelloParameterConstants.ALL_VERSIONS, "3.0.0" );

        Model model = modello.loadModel( ReaderFactory.newXmlReader( getTestFile( modelFile ) ) );

        generateClasses( parameters, modello, model, "java" );

        generateClasses( parameters, modello, model, "stax-reader" );

        generateClasses( parameters, modello, model, "stax-writer" );

        generateClasses( parameters, modello, model, "upgrade" );

        addDependency( "stax", "stax-api", "1.0.1" );
        addDependency( "net.java.dev.stax-utils", "stax-utils", "20060502" );
        addDependency( "org.codehaus.woodstox", "wstx-asl", "3.2.0" );

        compile( generatedSources, classes );

        verify( "UpgradeVerifier", "upgrade" );
    }

    private void generateClasses( Properties parameters, ModelloCore modello, Model model, String t )
        throws ModelloException
    {
        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.toString( false ) );
        parameters.setProperty( ModelloParameterConstants.VERSION, "4.0.0" );
        modello.generate( model, t, parameters );

        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.toString( true ) );
        parameters.setProperty( ModelloParameterConstants.VERSION, "3.0.0" );
        modello.generate( model, t, parameters );
    }

}
