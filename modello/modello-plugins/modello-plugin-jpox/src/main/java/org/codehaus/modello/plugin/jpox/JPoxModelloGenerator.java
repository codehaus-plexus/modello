package org.codehaus.modello.plugin.jpox;

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
import java.util.Properties;
import java.util.Iterator;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.plugin.store.AbstractVelocityModelloGenerator;
import org.codehaus.modello.plugin.store.metadata.StoreClassMetadata;
import org.codehaus.plexus.velocity.VelocityComponent;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class JPoxModelloGenerator
    extends AbstractVelocityModelloGenerator
{
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

//        context.put( "ojbMetadataId", JpoxClassMetadata.ID );

        context.put( "model", model );

        // ----------------------------------------------------------------------
        // Generate the JDO files
        // ----------------------------------------------------------------------

        writeTemplate( "/org/codehaus/modello/plugin/jpox/templates/jpox.jdo.vm",
                       new File( getOutputDirectory(), "jpox.jdo" ),
                       context );

        // ----------------------------------------------------------------------
        // Generate the JPoxStore
        // ----------------------------------------------------------------------

        String packageName = model.getPackageName( isPackageWithVersion(), super.getGeneratedVersion() );

        String className = model.getName() + "JPoxStore";

        writeClass( "/org/codehaus/modello/plugin/jpox/templates/JPoxStore.java.vm",
                    getOutputDirectory(), packageName, className,
                    context );
    }
}
