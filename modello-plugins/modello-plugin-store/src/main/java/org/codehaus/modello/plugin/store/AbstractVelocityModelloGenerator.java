package org.codehaus.modello.plugin.store;

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

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.codehaus.modello.ModelloException;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.Version;
import org.codehaus.modello.plugin.AbstractModelloGenerator;
import org.codehaus.modello.plugin.store.tool.JavaTool;
import org.codehaus.plexus.velocity.VelocityComponent;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractVelocityModelloGenerator
    extends AbstractModelloGenerator
{
    /**
     * @requirement
     */
    private VelocityComponent velocity;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected static Context makeStubVelocityContext( Model model, Version version )
    {
        String packageName = getGeneratedPackage( model, version );

        return makeStubVelocityContext( model, version, packageName );
    }

    private static String getGeneratedPackage( Model model, Version version )
    {
        return model.getDefaultPackageName( false, version );
    }

    protected static Context makeStubVelocityContext( Model model, Version version, String packageName )
    {
        List classes = model.getClasses( version );

        Map classFields = new HashMap();

        for ( Iterator it = classes.iterator(); it.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) it.next();

            List fields = modelClass.getFields( version );

            classFields.put( modelClass.getName(), fields );
        }

        Context context = new VelocityContext();

        context.put( "version", version );

        context.put( "model", model );

        context.put( "classes", classes );

        context.put( "classFields", classFields );

        context.put( "javaTool", new JavaTool() );

        context.put( "package", packageName );

        return context;
    }

    protected void writeClass( String templateName, File basedir, String packageName, String className,
                               Context context )
        throws ModelloException
    {
        File packageFile = new File( getOutputDirectory(), packageName.replace( '.', File.separatorChar ) );

        File file = new File( packageFile, className + ".java" );

        writeTemplate( templateName, file, context );
    }

    protected void writeTemplate( String templateName, File file, Context context )
        throws ModelloException
    {
        Template template = getTemplate( templateName );

        if ( template == null )
        {
            ClassLoader old = Thread.currentThread().getContextClassLoader();

            try
            {
                Thread.currentThread().setContextClassLoader( this.getClass().getClassLoader() );

                template = getTemplate( templateName );
            }
            finally
            {
                Thread.currentThread().setContextClassLoader( old );
            }
        }

        if ( template == null )
        {
            throw new ModelloException( "Could not find the template '" + templateName + "'." );
        }

        if ( !file.getParentFile().exists() )
        {
            if ( !file.getParentFile().mkdirs() )
            {
                throw new ModelloException(
                    "Error while creating parent directories for '" + file.getAbsolutePath() + "'." );
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

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private Template getTemplate( String name )
        throws ModelloException
    {
        try
        {
            return velocity.getEngine().getTemplate( name );
        }
        catch ( ResourceNotFoundException e )
        {
            return null;
        }
        catch ( ParseErrorException e )
        {
            throw new ModelloException( "Could not parse the template '" + name + "'.", e );
        }
        catch ( Exception e )
        {
            throw new ModelloException( "Error while loading template '" + name + "'.", e );
        }
    }
}
