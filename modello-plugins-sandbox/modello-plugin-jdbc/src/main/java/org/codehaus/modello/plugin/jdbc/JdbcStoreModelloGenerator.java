package org.codehaus.modello.plugin.jdbc;

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
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.Version;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.plugin.store.AbstractVelocityModelloGenerator;
import org.codehaus.modello.plugin.store.metadata.StoreClassMetadata;
import org.codehaus.modello.plugin.store.metadata.StoreFieldMetadata;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class JdbcStoreModelloGenerator
    extends AbstractVelocityModelloGenerator
{
    public void generate( Model model, Properties properties )
        throws ModelloException
    {
        initialize( model, properties );

        Version version = getGeneratedVersion();

        List classes = model.getClasses( version );

        Map classFields = new HashMap();

        for ( Iterator it = classes.iterator(); it.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) it.next();

            List fields = modelClass.getFields( version );

            classFields.put( modelClass.getName(), fields );
        }

        // ----------------------------------------------------------------------
        // Initialize the Velocity context
        // ----------------------------------------------------------------------

        Context context = new VelocityContext();

        context.put( "version", getGeneratedVersion() );

        context.put( "package", model.getPackageName( false, version ) );

        context.put( "storeClassMetadataId", StoreClassMetadata.ID );

        context.put( "storeFieldMetadataId", StoreFieldMetadata.ID );

        context.put( "model", model );

        context.put( "classes", classes );

        context.put( "classFields", classFields );

        // ----------------------------------------------------------------------
        // Generate the JdbcStore
        // ----------------------------------------------------------------------

        String packageName = model.getPackageName( isPackageWithVersion(), super.getGeneratedVersion() );

        String className = model.getName() + "JdbcStore";

        writeClass( "/org/codehaus/modello/plugin/jdbc/templates/JdbcStore.java.vm",
                    getOutputDirectory(), packageName, className,
                    context );
    }
}
