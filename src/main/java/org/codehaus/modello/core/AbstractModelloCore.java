package org.codehaus.modello.core;

/*
 * LICENSE
 */

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.codehaus.modello.Model;
import org.codehaus.modello.ModelValidationException;
import org.codehaus.modello.ModelloException;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractModelloCore
    extends AbstractLogEnabled
    implements ModelloCore
{
    // ----------------------------------------------------------------------
    // Partial ModelloCore implementation
    // ----------------------------------------------------------------------

    public Model input( Reader reader )
        throws ModelloException, ModelValidationException
    {
        return loadModel( reader );
    }

    public void output( Model model, Writer writer )
        throws ModelloException
    {
        saveModel( model, writer );
    }

    // ----------------------------------------------------------------------
    // Util methods
    // ----------------------------------------------------------------------

    private Reader getReader( File file )
        throws ModelloException
    {
        if ( !file.exists() )
        {
            throw new ModelloException( "The specified file doesn't exists: " + file.getAbsolutePath() );
        }

        try
        {
            return new FileReader( file );
        }
        catch( IOException ex )
        {
            throw new ModelloException( "Error opening the file: " + file.getAbsolutePath() );
        }
    }

    private Writer getWriter( File file )
        throws ModelloException
    {
        try
        {
            return new FileWriter( file );
        }
        catch( IOException ex )
        {
            throw new ModelloException( "Error opening the file: " + file.getAbsolutePath() );
        }
    }
}
