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
    private String basedir;

    public void setUp()
        throws Exception
    {
        basedir = System.getProperty( "basedir" );

        if ( basedir == null )
        {
            basedir = new File( "" ).getAbsolutePath();
        }
    }

    protected String getBasedir()
    {
        return basedir;
    }

    protected String getTestFile( String fileName )
    {
        return new File( getBasedir(), fileName).getAbsolutePath();
    }
}
