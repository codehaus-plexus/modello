package org.codehaus.modello.plugins.hibernate;

/*
 * LICENSE
 */

import java.io.FileReader;
import java.util.Properties;

import org.codehaus.modello.AbstractModelloGeneratorTest;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class HibernateModelloGeneratorTest
    extends AbstractModelloGeneratorTest
{
    public HibernateModelloGeneratorTest()
    {
        super( "hibernate" );
    }

    public void testBasic()
        throws Exception
    {
        ModelloCore modello = (ModelloCore) lookup( ModelloCore.ROLE );

        Model model = modello.loadModel( new FileReader( getTestFile( "src/test/models/simple.mdo" ) ) );

        Properties parameters = new Properties();

        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, getTestPath( "target/hibernate/output" ) );

        parameters.setProperty( ModelloParameterConstants.VERSION, "4.0.0" );

        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.TRUE.toString() );

        modello.generate( model, "hibernate", parameters );
    }
}
