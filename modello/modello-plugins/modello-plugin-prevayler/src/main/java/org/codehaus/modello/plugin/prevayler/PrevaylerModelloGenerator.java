package org.codehaus.modello.plugin.prevayler;

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

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.context.Context;

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.plugin.AbstractModelloGenerator;
import org.codehaus.modello.plugin.store.metadata.StoreClassMetadata;
import org.codehaus.plexus.velocity.VelocityComponent;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PrevaylerModelloGenerator
    extends AbstractModelloGenerator
{
    /** @requirement */
    private VelocityComponent velocity;

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

        context.put( "storeMetadataId", StoreClassMetadata.ID );

//        context.put( "ojbMetadataId", HibernateClassMetadata.ID );

        context.put( "model", model );

        // ----------------------------------------------------------------------
        // Generate the code
        // ----------------------------------------------------------------------

        String packageName = model.getPackageName( false, getGeneratedVersion() );

        File packageFile = new File( getOutputDirectory(), packageName.replace( '.', File.separatorChar ) );

        File file = new File( packageFile, model.getName() + "PrevaylerStore.java" );

        if ( !file.getParentFile().exists() )
        {
            if ( !file.getParentFile().mkdirs() )
            {
                throw new ModelloException( "Error while creating parent directories for '" + file.getAbsolutePath() + "'." );
            }
        }

        String template = "/org/codehaus/modello/plugin/prevayler/templates/PrevaylerStore.vm";

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        Class realmClassLoader = classLoader.getClass();

        try
        {
            Method getRealm = realmClassLoader.getDeclaredMethod( "getRealm", new Class[] {} );

            getRealm.setAccessible( true );

            Object classRealm = getRealm.invoke( classLoader, new Object[] {} );

            Method display = classRealm.getClass().getMethod( "display", new Class[] {} );

            display.invoke( classRealm, new Object[] {} );
        }
        catch ( NoSuchMethodException e )
        {
            throw new RuntimeException( e.toString(), e );
        }
        catch ( IllegalAccessException e )
        {
            throw new RuntimeException( e.toString(), e );
        }
        catch ( InvocationTargetException e )
        {
            throw new RuntimeException( e.toString(), e );
        }

        writeTemplate( template, file, context );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void writeTemplate( String templateName, File file, Context context )
        throws ModelloException
    {
        Template template = null;

        try
        {
            template = velocity.getEngine().getTemplate( templateName );
        }
        catch ( Exception e )
        {
            ClassLoader old = Thread.currentThread().getContextClassLoader();

            try
            {
                Thread.currentThread().setContextClassLoader( this.getClass().getClassLoader() );

                template = velocity.getEngine().getTemplate( templateName );
            }
            catch ( Exception e1 )
            {
                throw new ModelloException( "Could not find the template '" + templateName + "'." );
            }
            finally
            {
                Thread.currentThread().setContextClassLoader( old );
            }
        }

        try
        {
            Writer writer = new FileWriter( file );

            template.merge( context, writer );

            writer.close();
        }
        catch ( Exception e )
        {
            throw new ModelloException( "Error while generating code.", e );
        }
    }
}
