package org.codehaus.modello.plugins.hibernate;

/*
 * LICENSE
 */

import java.io.File;
import java.util.Properties;

import org.codehaus.modello.FileUtils;
import org.codehaus.modello.Model;
import org.codehaus.modello.ModelloGeneratorTest;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.core.ModelloCore;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DhisModelTest
    extends ModelloGeneratorTest
{
    public DhisModelTest()
    {
        super( "dhis" );
    }

    public void testBasic()
        throws Throwable
    {
        Properties parameters = new Properties();

        parameters.setProperty( ModelloParameterConstants.VERSION, "2.0.0" );

        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, "false" );

        Model model = loadModel( "src/test/models/dhis.mdo" );

        ModelloCore modello = getModelloCore();

        File generatedSources = new File( getTestPath( "target/dhis/sources" ) );

        File classes = new File( getTestPath( "target/dhis/classes" ) );

        FileUtils.deleteDirectory( generatedSources );

        FileUtils.deleteDirectory( classes );

        generatedSources.mkdirs();

        classes.mkdirs();

        // This list must be up to date with the pom
//        addDependency( "commons-logging", "commons-logging", "1.0.4" );

        addDependency( "modello", "modello-core", "1.0-SNAPSHOT" );

        addDependency( "modello", "modello-xml-plugin", "1.0-SNAPSHOT" );

        addDependency( "hsqldb", "hsqldb", "1.7.2.2" );

        addDependency( "commons-lang", "commons-lang", "1.0.1" );

        addDependency( "hibernate", "hibernate", "2.0.3" );

        addDependency( "dom4j", "dom4j", "1.4" );

        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, getTestPath( "target/dhis/classes" ) );

        modello.generate( model, "hibernate", parameters );

        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, getTestPath( "target/dhis/sources" ) );

        modello.generate( model, "java", parameters );

        compile( generatedSources, classes );

//        ClassLoader classLoader = getTestClassLoader();
//
//        classLoader.loadClass( "no.uio.dhis.UserInfoRole" );
//        classLoader.loadClass( "no.uio.dhis.UserProfile" );
//        classLoader.loadClass( "no.uio.dhis.Model" );

        verify( "dhis.DhisVerifier", "dhis" );
    }
}
