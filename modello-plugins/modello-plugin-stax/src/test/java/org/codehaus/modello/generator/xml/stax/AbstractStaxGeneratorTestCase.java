package org.codehaus.modello.generator.xml.stax;

import org.codehaus.modello.AbstractModelloGeneratorTest;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.util.Properties;

public abstract class AbstractStaxGeneratorTestCase
    extends AbstractModelloGeneratorTest
{
    protected ModelloCore modello;

    public AbstractStaxGeneratorTestCase( String name )
    {
        super( name );
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();

        modello = (ModelloCore) container.lookup( ModelloCore.ROLE );
    }

    protected void verifyModel( Model model, String className )
        throws Throwable
    {
        File generatedSources = new File( getTestPath( "target/" + getName() + "/sources" ) );

        File classes = new File( getTestPath( "target/" + getName() + "/classes" ) );

        FileUtils.deleteDirectory( generatedSources );

        FileUtils.deleteDirectory( classes );

        generatedSources.mkdirs();

        classes.mkdirs();

        Properties parameters = new Properties();

        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, generatedSources.getAbsolutePath() );

        parameters.setProperty( ModelloParameterConstants.VERSION, "4.0.0" );

        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.toString( false ) );

        modello.generate( model, "java", parameters );

        modello.generate( model, "stax-writer", parameters );

        modello.generate( model, "stax-reader", parameters );

        Properties properties = new Properties( System.getProperties() );
        if ( properties.getProperty( "version" ) == null )
        {
            properties.load(
                getClass().getResourceAsStream( "/META-INF/maven/org.codehaus.modello/modello-core/pom.properties" ) );
        }
        addDependency( "org.codehaus.modello", "modello-core", properties.getProperty( "version" ) );

        addDependency( "net.java.dev.stax-utils", "stax-utils", "20060502" );
        addDependency( "stax", "stax-api", "1.0.1" );

        compile( generatedSources, classes );

        verify( className, getName() );
    }
}
