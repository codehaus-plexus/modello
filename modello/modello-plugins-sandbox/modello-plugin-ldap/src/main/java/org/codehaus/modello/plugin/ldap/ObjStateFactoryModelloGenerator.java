package org.codehaus.modello.plugin.ldap;

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
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.List;

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.generator.java.javasource.JClass;
import org.codehaus.modello.generator.java.javasource.JMethod;
import org.codehaus.modello.generator.java.javasource.JParameter;
import org.codehaus.modello.generator.java.javasource.JSourceCode;
import org.codehaus.modello.generator.java.javasource.JSourceWriter;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.plugin.AbstractModelloGenerator;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ObjStateFactoryModelloGenerator
    extends AbstractModelloGenerator
{
    // ----------------------------------------------------------------------
    // AbstractModelloGenerator Implementation
    // ----------------------------------------------------------------------

    public void generate( Model model, Properties properties )
        throws ModelloException
    {
        initialize( model, properties );

        for ( Iterator it = model.getClasses( getGeneratedVersion() ).iterator(); it.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) it.next();

            try
            {
                generateObjStateFactory( modelClass );
            }
            catch ( IOException e )
            {
                throw new ModelloException( "Exception while generating the ObjStateFactory for '" + modelClass.getName() + "'.", e );
            }
        }
    }

    public void generateObjStateFactory( ModelClass modelClass )
        throws ModelloException, IOException
    {
        String packageName;

        if ( isPackageWithVersion() )
        {
            packageName = modelClass.getPackageName( true, getGeneratedVersion() );
        }
        else
        {
            packageName = modelClass.getPackageName( false, null );
        }

        packageName += ".io.ldap";

        String directory = packageName.replace( '.', '/' );

        String objStateFactoryName = modelClass.getName() + "ObjStateFactory";

        File f = new File( new File( getOutputDirectory(), directory ), objStateFactoryName + ".java" );

        if ( !f.getParentFile().exists() )
        {
            if ( ! f.getParentFile().mkdirs() )
            {
                throw new ModelloException( "Could not make parent directories for '" + f.getAbsolutePath() + "'." );
            }
        }

        FileWriter writer = new FileWriter( f );

        JSourceWriter sourceWriter = new JSourceWriter( writer );

        JClass jClass = new JClass( objStateFactoryName );

        jClass.addInterface( "javax.naming.spi.DirObjectFactory" );

        JMethod method = new JMethod( new JClass( "Object" ), "getObjectInstance" );

        method.addParameter( new JParameter( new JClass( "Object" ), "obj" ) );

        method.addParameter( new JParameter( new JClass( "Name" ), "name" ) );

        method.addParameter( new JParameter( new JClass( "Context" ), "context" ) );

        method.addParameter( new JParameter( new JClass( "Hashtable" ), "enviroment" ) );

        method.addParameter( new JParameter( new JClass( "Attributes" ), "attributes" ) );

        // TODO: remove after tested
//        method.addException( new JClass( "Exception" ) );

        JSourceCode sc = method.getSourceCode();

        writeImplementation( modelClass, sc );

        jClass.addMethod( method );

        // ----------------------------------------------------------------------
        // Close the file
        // ----------------------------------------------------------------------

        jClass.print( sourceWriter );

        writer.flush();

        writer.close();
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void writeImplementation( ModelClass modelClass, JSourceCode sc )
        throws ModelloException
    {
        sc.add( "// ----------------------------------------------------------------------" );
        sc.add( "// Assert the parameters." );
        sc.add( "// ----------------------------------------------------------------------" );
        sc.add( "" );
        sc.add( "if ( attributes == null )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "throw new Exception( \"The attributes cannot be null.\" );" );
        sc.unindent();
        sc.add( "}" );
        sc.add( "" );

        sc.add( "// ----------------------------------------------------------------------" );
        sc.add( "// Check that the object is of the right type." );
        sc.add( "// ----------------------------------------------------------------------" );
        sc.add( "" );
        sc.add( "Attribute objectClass = attributes.get( \"objectClass\" );" );
        sc.add( "" );

        String objectType = "" + Character.toLowerCase( modelClass.getName().charAt( 0 ) );

        if ( modelClass.getName().length() > 1 )
        {
            objectType += modelClass.getName().substring( 1 );
        }

        sc.add( "if ( objectClass == null || !objectClass.contains( \"" + objectType + "\" ) )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "return null;" );
        sc.unindent();
        sc.add( "}" );
        sc.add( "" );

        sc.add( "// ----------------------------------------------------------------------" );
        sc.add( "// Create the object." );
        sc.add( "// ----------------------------------------------------------------------" );
        sc.add( "" );

        sc.add( modelClass.getName() + " object = new " + modelClass.getName() + "();" );
        sc.add( "" );

        List fields = modelClass.getFields( getGeneratedVersion() );

        for ( Iterator it = fields.iterator(); it.hasNext(); )
        {
            ModelField modelField = (ModelField) it.next();

            String name = modelField.getName();

            String uncapitalizedName = uncapitalise( name );

            String type = modelField.getType();

            sc.add( "// " + name );

            if ( type.equals( "java.lang.String" ) || type.equals( "String" ) )
            {
                sc.add( uncapitalizedName + " = (String) attributes.get( \"" + uncapitalizedName + "\" );" );
            }
            else if ( type.equals( "int" ) )
            {
                sc.add( uncapitalizedName + " = Integer.parseInt( (String) attributes.get( \"" + uncapitalizedName + "\" ) );" );
            }
            // TODO: Add parsing for the rest if the primitive types
            else if ( isCollection( type ) )
            {
                // ignore for now.
                sc.add( "// Ignoring collection field" );
                sc.add( "" );
                continue;
            }
            else if ( isMap( type ) )
            {
                // ignore for now.
                sc.add( "// Ignoring map field" );
                sc.add( "" );
                continue;
            }
            else if ( isClassInModel( name, getModel() ) )
            {
                // ignore for now.
                sc.add( "// Ignoring contained type field" );
                sc.add( "" );
                continue;
            }
            else
            {
                throw new ModelloException( "Don't know how to generate code to convert a LDAP Attribute to a '" + type + "'." );
            }

            sc.add( "object.set" + super.capitalise( modelField.getName() ) + "( " + uncapitalizedName + " );" );
            sc.add( "" );
        }

        sc.add( "return object;" );
    }
}
