package org.codehaus.modello.generator.java;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.codehaus.modello.CodeSegment;
import org.codehaus.modello.Model;
import org.codehaus.modello.ModelAssociation;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelField;
import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.generator.AbstractGeneratorPlugin;
import org.codehaus.modello.generator.java.javasource.JClass;
import org.codehaus.modello.generator.java.javasource.JField;
import org.codehaus.modello.generator.java.javasource.JMethod;
import org.codehaus.modello.generator.java.javasource.JParameter;
import org.codehaus.modello.generator.java.javasource.JSourceWriter;
import org.codehaus.modello.generator.java.javasource.JType;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 * @version $Id$
 */
public class JavaGenerator
    extends AbstractGeneratorPlugin
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

        String packageName = getBasePackageName( objectModel );

        String directory = packageName.replace( '.', '/' );

        for ( Iterator i = objectModel.getClasses().iterator(); i.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) i.next();

            if ( outputElement( modelClass.getVersion(), modelClass.getName() ) )
            {
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

                for ( Iterator j = modelClass.getAllFields().iterator(); j.hasNext(); )
                {
                    ModelField modelField = (ModelField) j.next();

                    if ( outputElement( modelField.getVersion(), modelClass.getName() + "." + modelField.getName() ) )
                    {
                        createField( jClass, modelField );
                    }
                }

                for ( Iterator j = modelClass.getAllAssociations().iterator(); j.hasNext(); )
                {
                    ModelAssociation modelAssociation = (ModelAssociation) j.next();

                    if ( outputElement( modelAssociation.getVersion(), modelClass.getName() + "." + modelAssociation.getName() ) )
                    {
                        createAssociation( jClass, modelAssociation );
                    }
                }

                if ( modelClass.getCodeSegments() != null )
                {
                    for ( Iterator iterator = modelClass.getCodeSegments().iterator(); iterator.hasNext(); )
                    {
                        CodeSegment codeSegment = (CodeSegment) iterator.next();

                        if ( outputElement( codeSegment.getVersion(), "" ) )
                        {
                            jClass.addSourceCode( codeSegment.getCode() );
                        }
                    }
                }

                jClass.print( sourceWriter );

                writer.flush();

                writer.close();
            }
        }
    }
    protected String getBasePackageName( Model model )
    {
        StringBuffer sb = new StringBuffer();

        sb.append( model.getPackageName() );

        if ( isPackageWithVersion() )
        {
            sb.append( "." );

            sb.append( getModelVersion().toString() );
        }

        return sb.toString();
    }

    protected void addModelImports( JClass jClass )
        throws ModelloException
    {
        for ( Iterator i = getModel().getClasses().iterator(); i.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) i.next();

            if ( outputElement( modelClass.getVersion(), modelClass.getName() ) )
            {
                jClass.addImport( getBasePackageName( getModel() ) + "." + modelClass.getName() );
            }
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

        // Useful debugging code when debugging other generators.
        //setter.getSourceCode().add( "System.out.println( \"" + field.getName() + " = \" + " + field.getName() + " );" );

        return setter;
    }

    private void createAssociation( JClass jClass, ModelAssociation modelAssociation )
    {
        if ( !modelAssociation.getFromMultiplicity().equals( "1" ) )
        {
            throw new ModelloRuntimeException( "The java generator can only generate associations with a 1->* multiplicity. " + 
                "Found: " + modelAssociation.getFromMultiplicity() + "->" + modelAssociation.getToMultiplicity() );
        }

        if ( !modelAssociation.getToMultiplicity().equals( "*" ) )
        {
            throw new ModelloRuntimeException( "The java generator can only generate associations with a 1->* multiplicity. " + 
                "Found: " + modelAssociation.getFromMultiplicity() + "->" + modelAssociation.getToMultiplicity() );
        }

        JType type = new JClass( "java.util.List" );

        JField jField = new JField( type, modelAssociation.getFromRole() );

        if ( !isEmpty( modelAssociation.getComment() ) )
        {
            jField.setComment( modelAssociation.getComment() );
        }

        jField.setInitString( "new java.util.ArrayList()" );

        jClass.addField( jField );

        jClass.addMethod( createGetter( jField ) );

        jClass.addMethod( createSetter( jField ) );

        createAdder( jClass, modelAssociation );
    }

    private void createAdder( JClass jClass, ModelAssociation modelAssociation )
    {
/*
        if ( isCollection( modelAssociation.getName() ) )
        {
*/
            String fieldName = modelAssociation.getFromRole();

            String parameterName = singular( fieldName );

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

                className = capitalise( singular( modelAssociation.getFromRole() ) );
            }

            JMethod adder = new JMethod( null, "add" + className );

            adder.addParameter( new JParameter( addType, parameterName ) );

            adder.getSourceCode().add( fieldName + ".add( " + parameterName + " );" );

            jClass.addMethod( adder );
/*
        }
        else if ( field.getType().getName().equals( "java.util.Properties" ) )
        {
            String parameterName = singular( field.getName() );

            String className = capitalise( parameterName );

            JType addType = new JClass( "String" );

            JMethod adder = new JMethod( null, "add" + className );

            adder.addParameter( new JParameter( addType, "name" ) );

            adder.addParameter( new JParameter( addType, "value" ) );

            adder.getSourceCode().add( field.getName() + ".setProperty( name, value );" );

            jClass.addMethod( adder );
        }
*/
    }
}
