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
public class ModelloTest
    extends TestCase
{
    private Modello modello;

    private ModelBuilder builder;

    private String basedir;

    public ModelloTest()
    {
        basedir = System.getProperty( "basedir", new File( "" ).getAbsolutePath() );
    }

    protected Modello getModello()
        throws ModelloException
    {
        if ( modello == null )
        {
            modello = new Modello();
    
            modello.initialize();
        }

        return modello;
    }

    protected ModelBuilder getModelBuilder()
        throws ModelloException
    {
        return getModello().getModelBuilder();
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
