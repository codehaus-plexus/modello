package org.codehaus.modello.plugin.java;

/*
 * Copyright (c) 2004, Jason van Zyl
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
import java.io.Serializable;
import java.util.Iterator;
import java.util.Properties;

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.generator.java.javasource.JClass;
import org.codehaus.modello.generator.java.javasource.JField;
import org.codehaus.modello.generator.java.javasource.JMethod;
import org.codehaus.modello.generator.java.javasource.JParameter;
import org.codehaus.modello.generator.java.javasource.JSourceWriter;
import org.codehaus.modello.generator.java.javasource.JType;
import org.codehaus.modello.model.CodeSegment;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelDefault;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.plugin.AbstractModelloGenerator;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 * @version $Id$
 */
public class JavaModelloGenerator
    extends AbstractModelloGenerator
{
    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

        try
        {
            generateJava();
        }
        catch( IOException ex )
        {
            throw new ModelloException( "Exception while generating Java.", ex );
        }
    }

    private void generateJava()
        throws ModelloException, IOException
    {
        Model objectModel = getModel();

        String packageName = getBasePackageName();

        String directory = packageName.replace( '.', '/' );

        for ( Iterator i = objectModel.getClasses( getGeneratedVersion() ).iterator(); i.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) i.next();

            File f = new File( new File( getOutputDirectory(), directory ), modelClass.getName() + ".java" );

            if ( !f.getParentFile().exists() )
            {
                f.getParentFile().mkdirs();
            }

            FileWriter writer = new FileWriter( f );

            JSourceWriter sourceWriter = new JSourceWriter( writer );

            JClass jClass = new JClass( modelClass.getName() );

            jClass.addImport( "java.util.*" );

            jClass.setPackageName( packageName );

            if ( modelClass.getSuperClass() != null )
            {
                jClass.setSuperClass( modelClass.getSuperClass() );
            }

            jClass.addInterface( Serializable.class.getName() );

            for ( Iterator j = modelClass.getFields( getGeneratedVersion() ).iterator(); j.hasNext(); )
            {
                ModelField modelField = (ModelField) j.next();

                if ( modelField instanceof ModelAssociation )
                {
                    createAssociation( jClass, (ModelAssociation) modelField );
                }
                else
                {
                    createField( jClass, modelField );
                }
            }

            if ( modelClass.getCodeSegments( getGeneratedVersion() ) != null )
            {
                for ( Iterator iterator = modelClass.getCodeSegments( getGeneratedVersion() ).iterator(); iterator.hasNext(); )
                {
                    CodeSegment codeSegment = (CodeSegment) iterator.next();

                    jClass.addSourceCode( codeSegment.getCode() );
                }
            }

            jClass.print( sourceWriter );

            writer.flush();

            writer.close();
        }
    }

    private JField createField( ModelField modelField, ModelClass modelClass )
    {
        JType type;

        if ( modelField.getType().equals( "boolean" ) )
        {
            type = JType.Boolean;
        }
        else if ( modelField.getType().equals( "byte" ) )
        {
            type = JType.Byte;
        }
        else if ( modelField.getType().equals( "char" ) )
        {
            type = JType.Char;
        }
        else if ( modelField.getType().equals( "double" ) )
        {
            type = JType.Double;
        }
        else if ( modelField.getType().equals( "float" ) )
        {
            type = JType.Float;
        }
        else if ( modelField.getType().equals( "int" ) )
        {
            type = JType.Int;
        }
        else if ( modelField.getType().equals( "short" ) )
        {
            type = JType.Short;
        }
        else if ( modelField.getType().equals( "long" ) )
        {
            type = JType.Long;
        }
        else
        {
            type = new JClass( modelField.getType() );
        }

        JField field = new JField( type, modelField.getName() );

        if ( modelField.getDefaultValue() != null )
        {
            if ( modelField.getType().equals( "String" ) )
            {
                field.setInitString( "\"" + modelField.getDefaultValue() + "\"" );
            }
            else
            {
                field.setInitString( modelField.getDefaultValue() );
            }
        }

        return field;
    }

    private void createField( JClass jClass, ModelField modelField )
    {
        JField field = createField( modelField, modelField.getModelClass() );

        jClass.addField( field );

        jClass.addMethod( createGetter( field ) );

        jClass.addMethod( createSetter( field ) );
    }

    private JMethod createGetter( JField field )
    {
        String propertyName = capitalise( field.getName() );

        JMethod getter = new JMethod( field.getType(), "get" + propertyName );

        getter.getSourceCode().add( "return this." + field.getName() + ";" );

        return getter;
    }

    private JMethod createSetter( JField field )
    {
        String propertyName = capitalise( field.getName() );

        JMethod setter = new JMethod( null, "set" + propertyName );

        setter.addParameter( new JParameter( field.getType(), field.getName() ) );

        setter.getSourceCode().add( "this." + field.getName() + " = " + field.getName() + ";" );

        return setter;
    }

    private void createAssociation( JClass jClass, ModelAssociation modelAssociation )
        throws ModelloException
    {
        if ( ModelAssociation.MANY_MULTIPLICITY.equals( modelAssociation.getMultiplicity() ) )
        {
            JType type = new JClass( modelAssociation.getType() );

            JField jField = new JField( type, modelAssociation.getName() );

            if ( !isEmpty( modelAssociation.getComment() ) )
            {
                jField.setComment( modelAssociation.getComment() );
            }

            jField.setInitString( modelAssociation.getDefaultValue() );

            jClass.addField( jField );

            jClass.addMethod( createGetter( jField ) );

            jClass.addMethod( createSetter( jField ) );

            createAdder( jClass, modelAssociation );
        }
        else
        {
            createField( jClass, modelAssociation );
        }
    }

    private void createAdder( JClass jClass, ModelAssociation modelAssociation )
    {
        String fieldName = modelAssociation.getName();

        String parameterName = uncapitalise( modelAssociation.getTo() );

        String className;

        JType addType;

        if ( modelAssociation.getToClass() != null )
        {
            addType = new JClass( modelAssociation.getToClass().getName() );

            className = modelAssociation.getToClass().getName();
        }
        else
        {
            addType = new JClass( "String" );

            className = capitalise( singular( fieldName ) );
        }

        if ( modelAssociation.getType().equals( ModelDefault.PROPERTIES )
            || modelAssociation.getType().equals( ModelDefault.MAP ) )
        {
            JMethod adder = new JMethod( null, "add" + capitalise( singular( fieldName ) ) );

            adder.addParameter( new JParameter( new JClass( "String" ), "key" ) );

            adder.addParameter( new JParameter( new JClass( modelAssociation.getTo() ), "value" ) );

            adder.getSourceCode().add( "this." + fieldName + ".put( key, value );" );

            jClass.addMethod( adder );
        }
        else
        {
            JMethod adder = new JMethod( null, "add" + singular( capitalise( fieldName ) ) );

            adder.addParameter( new JParameter( addType, parameterName ) );

            adder.getSourceCode().add( "this." + fieldName + ".add( " + parameterName + " );" );

            jClass.addMethod( adder );
        }
    }
}
