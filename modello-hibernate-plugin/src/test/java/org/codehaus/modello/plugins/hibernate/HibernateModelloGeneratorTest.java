package org.codehaus.modello.plugins.hibernate;

/*
 * LICENSE
 */

import java.util.Properties;

import org.codehaus.modello.Model;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.ModelloTest;
import org.codehaus.modello.core.ModelloCore;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class HibernateModelloGeneratorTest
    extends ModelloTest
{
    private Properties parameters = new Properties();

    public void setUp()
        throws Exception
    {
        super.setUp();

        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, getTestPath( "target/hibernate/output" ) );

        parameters.setProperty( ModelloParameterConstants.VERSION, "4.0.0" );

        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.TRUE.toString() );
    }

    public void testBasic()
        throws Exception
    {
        Model model = loadModel( "src/test/models/simple.mdo" );

        ModelloCore modello = getModelloCore();

        modello.generate( model, "hibernate", parameters );
    }
}
