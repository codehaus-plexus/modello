package org.codehaus.modello;

/*
 * LICENSE
 */

import java.io.File;
import java.io.FileReader;

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
    }

    protected Model loadModel( String name )
        throws Exception
    {
        ModelloCore modello = getModelloCore();

        return modello.loadModel( new FileReader( getTestPath( name ) ) );
    }

    public String getTestPath( String name )
    {
        return new File( super.getTestFile( name ) ).getAbsolutePath();
    }
}
