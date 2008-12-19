package org.codehaus.modello.plugin.java;

/*
 * Copyright (c) 2004, Codehaus.org
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

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.model.BaseElement;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelInterface;
import org.codehaus.modello.plugin.AbstractModelloGenerator;
import org.codehaus.modello.plugin.java.JavaFieldMetadata;
import org.codehaus.modello.plugin.java.javasource.JClass;
import org.codehaus.modello.plugin.java.javasource.JSourceWriter;
import org.codehaus.plexus.util.WriterFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

/**
 * AbstractJavaModelloGenerator - similar in scope to {@link AbstractModelloGenerator} but with features that
 * java generators can use.
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 */
public abstract class AbstractJavaModelloGenerator
    extends AbstractModelloGenerator
{
    /**
     * Create a new java source file writer, with configured encoding.
     * 
     * @param packageName the package of the source file to create
     * @param className the class of the source file to create
     * @return a JSourceWriter with configured encoding
     * @throws IOException
     */
    protected JSourceWriter newJSourceWriter( String packageName, String className )
    throws IOException
    {
        String directory = packageName.replace( '.', File.separatorChar );

        File f = new File( new File( getOutputDirectory(), directory ), className + ".java" );

        if ( !f.getParentFile().exists() )
        {
            f.getParentFile().mkdirs();
        }

        Writer writer = ( getEncoding() == null ) ? WriterFactory.newPlatformWriter( f )
                        : WriterFactory.newWriter( f, getEncoding() );

        return new JSourceWriter( writer );
    }

    protected void addModelImports( JClass jClass, BaseElement baseElem )
        throws ModelloException
    {
        for ( Iterator i = getModel().getInterfaces( getGeneratedVersion() ).iterator(); i.hasNext(); )
        {
            ModelInterface modelInterface = (ModelInterface) i.next();

            if ( baseElem != null && baseElem instanceof ModelInterface )
            {
                if ( modelInterface.equals( (ModelInterface) baseElem )
                     || modelInterface.getPackageName( isPackageWithVersion(), getGeneratedVersion() ).equals(
                        ( (ModelInterface) baseElem ).getPackageName( isPackageWithVersion(),
                                                                      getGeneratedVersion() ) ) )
                {
                    continue;
                }
            }

            if ( isPackageWithVersion() )
            {
                jClass.addImport(
                    modelInterface.getPackageName( true, getGeneratedVersion() ) + "." + modelInterface.getName() );
            }
            else
            {
                jClass.addImport( modelInterface.getPackageName( false, null ) + "." + modelInterface.getName() );
            }
        }

        for ( Iterator i = getModel().getClasses( getGeneratedVersion() ).iterator(); i.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) i.next();

            if ( baseElem != null && baseElem instanceof ModelClass )
            {
                if ( modelClass.equals( (ModelClass) baseElem )
                     || modelClass.getPackageName( isPackageWithVersion(), getGeneratedVersion() ).equals(
                        ( (ModelClass) baseElem ).getPackageName( isPackageWithVersion(), getGeneratedVersion() ) ) )
                {
                    continue;
                }
            }

            if ( isPackageWithVersion() )
            {
                jClass.addImport(
                    modelClass.getPackageName( true, getGeneratedVersion() ) + "." + modelClass.getName() );
            }
            else
            {
                jClass.addImport( modelClass.getPackageName( false, null ) + "." + modelClass.getName() );
            }
        }
    }

    protected String getPrefix( JavaFieldMetadata javaFieldMetadata )
    {
        return javaFieldMetadata.isBooleanGetter() ? "is" : "get";
    }
}
