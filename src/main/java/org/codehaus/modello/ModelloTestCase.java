package org.codehaus.modello;

/*
 * LICENSE
 */

import java.io.File;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ModelloTestCase
    extends TestCase
{
    private Modello modello;

    private String basedir;

    public final void setUp()
        throws Exception
    {
        modello = new Modello();

        modello.initialize();

        basedir = System.getProperty( "basedir", new File( "" ).getAbsolutePath() );
    }

    protected Modello getModello()
    {
        return modello;
    }

    protected File getTestFile( String name )
    {
        return new File( basedir, name );
    }

    protected String getTestPath( String name )
    {
        return getTestFile( name ).getAbsolutePath();
    }
}
