package org.codehaus.modello;

/*
 * LICENSE
 */

import java.io.File;

import org.codehaus.modello.core.ModelloCore;
import org.codehaus.plexus.PlexusTestCase;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ModelloTest
    extends PlexusTestCase
{
    private Modello modello;

    private ModelBuilder builder;

    private String basedir;

    public ModelloTest()
    {
        basedir = System.getProperty( "basedir", new File( "" ).getAbsolutePath() );
    }

    protected ModelloCore getModelloCore()
        throws Exception
    {
        return (ModelloCore) lookup( ModelloCore.ROLE );
/*
        if ( modello == null )
        {
            modello = new Modello();
    
            modello.initialize();
        }

        return modello;
*/
    }
/*
    public ModelBuilder getModelBuilder()
        throws ModelloException
    {
        return getModello().getModelBuilder();
    }

    public File getTestFile( String name )
    {
        return new File( basedir, name );
    }
*/
    public String getTestPath( String name )
    {
        return new File( super.getTestPath( name ) ).getAbsolutePath();
    }
}
