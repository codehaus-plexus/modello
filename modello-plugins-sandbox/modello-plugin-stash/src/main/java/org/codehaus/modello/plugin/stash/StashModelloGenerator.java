package org.codehaus.modello.plugin.stash;

/*
 * Copyright (c) 2005, Codehaus.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.util.Properties;
import java.io.Writer;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

import org.apache.velocity.context.Context;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ResourceNotFoundException;

import org.codehaus.modello.plugin.AbstractModelloGenerator;
import org.codehaus.modello.plugin.stash.metadata.StashClassMetadata;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.ModelloException;
import org.codehaus.plexus.velocity.VelocityComponent;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class StashModelloGenerator
    extends AbstractModelloGenerator
{
    /** @plexus.requirement */
    private VelocityComponent velocity;

    // ----------------------------------------------------------------------
    // AbstractModelloGenerator Implementation
    // ----------------------------------------------------------------------

    public void generate( Model model, Properties properties )
        throws ModelloException
    {
        initialize( model, properties );

        // ----------------------------------------------------------------------
        // Initialize the Velocity context
        // ----------------------------------------------------------------------

        Context context = new VelocityContext();

        context.put( "version", getGeneratedVersion() );

        context.put( "package", model.getPackageName( false, getGeneratedVersion() ) );

        context.put( "metadataId", StashClassMetadata.ID );

        context.put( "model", model );

        // ----------------------------------------------------------------------
        // Generate the code
        // ----------------------------------------------------------------------

        File packageFile = new File( getOutputDirectory(), model.getPackageName( false, getGeneratedVersion() ).replace( '.', File.separatorChar ) );

        File file = new File( packageFile, model.getName() + "StashStore.java" );

        if ( !file.getParentFile().exists() )
        {
            if ( !file.getParentFile().mkdirs() )
            {
                throw new ModelloException( "Error while creating parent directories for '" + file.getAbsolutePath() + "'." );
            }
        }

        String template = "/org/codehaus/modello/plugin/stash/templates/StashStore.vm";

        try
        {
            Writer writer = new FileWriter( file );

            velocity.getEngine().mergeTemplate( template, context, writer );

            writer.flush();

            writer.close();
        }
        catch ( IOException e )
        {
            throw new ModelloException( "Error while writing to file '" + file.getAbsolutePath() + "'." );
        }
        catch ( ParseErrorException e )
        {
            throw new ModelloException( "Error parsing velocity template: " + e.getMessage() + "." );
        }
        catch ( MethodInvocationException e )
        {
            throw new ModelloException( "Error while generating code.", e );
        }
        catch ( ResourceNotFoundException e )
        {
            throw new ModelloException( "Internal error: Could not find velocity template: '" + template + "'." );
        }
        catch ( Exception e )
        {
            throw new ModelloException( "Error while generating code.", e );
        }
    }
}
