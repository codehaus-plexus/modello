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
import org.codehaus.modello.generator.java.javasource.JInterface;
import org.codehaus.modello.generator.java.javasource.JMethod;
import org.codehaus.modello.generator.java.javasource.JParameter;
import org.codehaus.modello.generator.java.javasource.JSourceCode;
import org.codehaus.modello.generator.java.javasource.JSourceWriter;
import org.codehaus.modello.generator.java.javasource.JType;
import org.codehaus.modello.model.CodeSegment;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelDefault;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.ModelInterface;
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

        for ( Iterator i = objectModel.getInterfaces( getGeneratedVersion() ).iterator(); i.hasNext(); )
        {
            ModelInterface modelInterface = (ModelInterface) i.next();

            String packageName = null;

            if ( isPackageWithVersion() )
            {
                packageName = modelInterface.getPackageName( true, getGeneratedVersion() );
            }
            else
            {
                packageName = modelInterface.getPackageName( false, null );
            }

            String directory = packageName.replace( '.', '/' );

            File f = new File( new File( getOutputDirectory(), directory ), modelInterface.getName() + ".java" );

            if ( !f.getParentFile().exists() )
            {
                f.getParentFile().mkdirs();
            }

            FileWriter writer = new FileWriter( f );

            JSourceWriter sourceWriter = new JSourceWriter( writer );

            JInterface jInterface = new JInterface( modelInterface.getName() );

            jInterface.addImport( "java.util.*" );

            jInterface.setPackageName( packageName );

            if ( modelInterface.getSuperInterface() != null )
            {
                jInterface.addInterface( modelInterface.getSuperInterface() );
            }

            if ( modelInterface.getCodeSegments( getGeneratedVersion() ) != null )
            {
                for ( Iterator iterator = modelInterface.getCodeSegments( getGeneratedVersion() ).iterator(); iterator.hasNext(); )
                {
                    CodeSegment codeSegment = (CodeSegment) iterator.next();

                    //TODO : add this method to jInterface or remove codeSegments and add method tag
                    //jInterface.addSourceCode( codeSegment.getCode() );
                }
            }

            jInterface.print( sourceWriter );

            writer.flush();

            writer.close();
        }

        for ( Iterator i = objectModel.getClasses( getGeneratedVersion() ).iterator(); i.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) i.next();

            JavaClassMetadata javaClassMetadata = (JavaClassMetadata) modelClass.getMetadata( JavaClassMetadata.ID );

            String packageName = null;

            if ( isPackageWithVersion() )
            {
                packageName = modelClass.getPackageName( true, getGeneratedVersion() );
            }
            else
            {
                packageName = modelClass.getPackageName( false, null );
            }

            String directory = packageName.replace( '.', '/' );

            File f = new File( new File( getOutputDirectory(), directory ), modelClass.getName() + ".java" );

            if ( !f.getParentFile().exists() )
            {
                f.getParentFile().mkdirs();
            }

            FileWriter writer = new FileWriter( f );

            JSourceWriter sourceWriter = new JSourceWriter( writer );

            JClass jClass = new JClass( modelClass.getName() );

            jClass.addImport( "java.util.*" );

            addModelImports( jClass, modelClass );

            jClass.setPackageName( packageName );
            
            if ( javaClassMetadata.isAbstract() )
            {
                jClass.getModifiers().setAbstract( true );
            }

            if ( modelClass.getSuperClass() != null )
            {
                jClass.setSuperClass( modelClass.getSuperClass() );
            }

            if ( modelClass.getInterfaces().size() > 0 )
            {
                for( Iterator j = modelClass.getInterfaces().iterator(); j.hasNext(); )
                {
                    ModelInterface modelInterface = (ModelInterface) j.next();

                    jClass.addInterface( modelInterface.getName() );
                }
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
        JavaFieldMetadata javaFieldMetadata = (JavaFieldMetadata) modelField.getMetadata( JavaFieldMetadata.ID );

        JField field = createField( modelField, modelField.getModelClass() );

        jClass.addField( field );

        if ( javaFieldMetadata.isGetter() )
        {
            jClass.addMethod( createGetter( field, modelField ) );
        }

        if ( javaFieldMetadata.isSetter() )
        {
            jClass.addMethod( createSetter( field, modelField ) );
        }
    }

    private JMethod createGetter( JField field, ModelField modelField )
    {
        String propertyName = capitalise( field.getName() );

        String prefix = modelField.getType().equals( "boolean" ) ? "is" : "get";

        JMethod getter = new JMethod( field.getType(), prefix + propertyName );

        getter.getSourceCode().add( "return this." + field.getName() + ";" );

        return getter;
    }

    private JMethod createSetter( JField field, ModelField modelField )
    {
        String propertyName = capitalise( field.getName() );

        JMethod setter = new JMethod( null, "set" + propertyName );

        setter.addParameter( new JParameter( field.getType(), field.getName() ) );

        JSourceCode sc = setter.getSourceCode();

        if ( modelField instanceof ModelAssociation
            && isBidirectionalAssociation( (ModelAssociation) modelField )
            && ModelAssociation.ONE_MULTIPLICITY.equals( ( (ModelAssociation) modelField ).getMultiplicity() ) )
        {
            ModelAssociation modelAssociation = (ModelAssociation) modelField;

            setter.addException( new JClass( "Exception" ) );

            sc.add( "if ( this." + field.getName() + " != null )" );

            sc.add( "{" );

            sc.indent();

            sc.add( "this." + field.getName() + ".break" + modelAssociation.getModelClass().getName() + "Association( this );" );

            sc.unindent();

            sc.add( "}" );

            sc.add( "this." + field.getName() + " = " + field.getName() + ";" );

            sc.add( "this." + field.getName() + ".create" + modelAssociation.getModelClass().getName() + "Association( this );" );
        }
        else
        {
            sc.add( "this." + field.getName() + " = " + field.getName() + ";" );
        }

        return setter;
    }

    private void createAssociation( JClass jClass, ModelAssociation modelAssociation )
        throws ModelloException
    {
        JavaFieldMetadata javaFieldMetadata = (JavaFieldMetadata)modelAssociation.getMetadata( JavaFieldMetadata.ID );

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

            if ( javaFieldMetadata.isGetter() )
            {
                jClass.addMethod( createGetter( jField, modelAssociation ) );
            }

            if ( javaFieldMetadata.isSetter() )
            {
                jClass.addMethod( createSetter( jField, modelAssociation ) );
            }

            if ( javaFieldMetadata.isAdder() )
            {
                createAdder( jClass, modelAssociation );
            }
        }
        else
        {
            createField( jClass, modelAssociation );
        }

        if ( isBidirectionalAssociation( modelAssociation ) )
        {
            JMethod createMethod = new JMethod( null, "create" + modelAssociation.getTo() + "Association" );

            createMethod.addParameter( new JParameter( new JClass( modelAssociation.getTo() ), uncapitalise( modelAssociation.getTo() ) ) );

            createMethod.addException( new JClass( "Exception" ) );

            JSourceCode sc = createMethod.getSourceCode();

            if ( ModelAssociation.ONE_MULTIPLICITY.equals( modelAssociation.getMultiplicity() ) )
            {
                sc.add( "if ( this." + modelAssociation.getName() + " != null )" );

                sc.add( "{" );

                sc.indent();

                sc.add( "break" + modelAssociation.getTo() + "Association( this." + modelAssociation.getName() + " );" );

                sc.unindent();

                sc.add( "}" );

                sc.add( "this." + modelAssociation.getName() + " = " + uncapitalise( modelAssociation.getTo() ) + ";" );
            }
            else
            {
                sc.add( "if ( this." + modelAssociation.getName() + ".contains(" + uncapitalise( modelAssociation.getTo() ) + ") )" );

                sc.add( "{" );

                sc.indent();

                sc.add( "throw new Exception( \"" + uncapitalise( modelAssociation.getTo() ) + " is already assigned.\" );" );

                sc.unindent();

                sc.add( "}" );

                sc.add( "this." + modelAssociation.getName() + ".add( " + uncapitalise( modelAssociation.getTo() ) + " );" );
            }

            jClass.addMethod( createMethod );

            JMethod breakMethod = new JMethod( null, "break" + modelAssociation.getTo() + "Association" );

            breakMethod.addParameter( new JParameter( new JClass( modelAssociation.getTo() ), uncapitalise( modelAssociation.getTo() ) ) );

            breakMethod.addException( new JClass( "Exception" ) );

            sc = breakMethod.getSourceCode();

            if ( ModelAssociation.ONE_MULTIPLICITY.equals( modelAssociation.getMultiplicity() ) )
            {
                sc.add( "if ( this." + modelAssociation.getName() + " != " + uncapitalise( modelAssociation.getTo() ) + " )" );

                sc.add( "{" );

                sc.indent();

                sc.add( "throw new Exception( \"" + uncapitalise( modelAssociation.getTo() ) + " isn't associated.\" );" );

                sc.unindent();

                sc.add( "}" );

                sc.add( "this." + modelAssociation.getName() + " = null;" );
            }
            else
            {
                sc.add( "if ( ! this." + modelAssociation.getName() + ".contains( " + uncapitalise( modelAssociation.getTo() ) + " ) )" );

                sc.add( "{" );

                sc.indent();

                sc.add( "throw new Exception( \"" + uncapitalise( modelAssociation.getTo() ) + " isn't associated.\" );" );

                sc.unindent();

                sc.add( "}" );

                sc.add( "this." + modelAssociation.getName() + ".remove( " + uncapitalise( modelAssociation.getTo() ) + " );" );
            }

            jClass.addMethod( breakMethod );
        }
    }

    private void createAdder( JClass jClass, ModelAssociation modelAssociation )
    {
        String fieldName = modelAssociation.getName();

        String parameterName = uncapitalise( modelAssociation.getTo() );

        boolean bidirectionalAssociation = isBidirectionalAssociation( modelAssociation );

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

            if ( bidirectionalAssociation )
            {
                adder.addException( new JClass( "Exception" ) );

                adder.getSourceCode().add( parameterName + ".create" + modelAssociation.getModelClass().getName() + "Association( this );" );
            }

            jClass.addMethod( adder );

            JMethod remover = new JMethod( null, "remove" + singular( capitalise( fieldName ) ) );

            remover.addParameter( new JParameter( addType, parameterName ) );

            if ( bidirectionalAssociation )
            {
                remover.addException( new JClass( "Exception" ) );

                remover.getSourceCode().add( parameterName + ".break" + modelAssociation.getModelClass().getName() + "Association( this );" );
            }

            remover.getSourceCode().add( "this." + fieldName + ".remove( " + parameterName + " );" );

            jClass.addMethod( remover );
        }
    }

    private boolean isBidirectionalAssociation( ModelAssociation association )
    {
        String to = association.getTo();

        Model model = association.getModelClass().getModel();

        if ( isClassInModel( association.getTo(), model ) )
        {
            ModelClass toClass = association.getToClass();

            for ( Iterator j = toClass.getFields( getGeneratedVersion() ).iterator(); j.hasNext(); )
            {
                ModelField modelField = (ModelField) j.next();

                if ( modelField instanceof ModelAssociation )
                {
                    ModelAssociation modelAssociation = (ModelAssociation) modelField;

                    if ( isClassInModel( modelAssociation.getTo(), model ) )
                    {
                        ModelClass totoClass = modelAssociation.getToClass();

                        if ( association.getModelClass().equals( totoClass ) )
                        {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
