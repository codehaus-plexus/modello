package org.codehaus.modello.generator.java;

import org.codehaus.modello.Model;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelField;
import org.codehaus.modello.generator.AbstractGenerator;
import org.codehaus.modello.generator.java.javasource.JField;
import org.codehaus.modello.generator.java.javasource.JMethod;
import org.codehaus.modello.generator.java.javasource.JType;
import org.codehaus.modello.generator.java.javasource.JClass;
import org.codehaus.modello.generator.java.javasource.JSourceWriter;
import org.codehaus.modello.generator.java.javasource.JParameter;

import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;

/**
 *
 *
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 *
 * @version $Id$
 */
public class JavaGenerator
    extends AbstractGenerator
{
    public JavaGenerator( String model, String outputDirectory )
    {
        super( model, outputDirectory );
    }

    public void generate()
        throws Exception
    {
        Model objectModel = getModel();

        String packageName = objectModel.getPackageName();

        String directory = packageName.replace( '.', '/' );

        for ( Iterator i = objectModel.getClasses().iterator(); i.hasNext(); )
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

            if ( modelClass.getFields() != null )
            {
                int count = 1;

                for ( Iterator j = modelClass.getFields().iterator(); j.hasNext(); )
                {
                    ModelField modelField = (ModelField) j.next();

                    String fieldName = modelField.getName();

                    String fieldType = modelField.getType();

                    if ( fieldName == null )
                    {
                        throw new IllegalStateException(
                            "Field name can't be null jField: element " + count + " in the definition of the class " + modelClass.getName() );
                    }

                    if ( fieldType == null )
                    {
                        throw new IllegalStateException(
                            "Field type can't be null: jField element " + count + " in the definition of the class " + modelClass.getName() );

                    }

                    JType jType;

                    // If we are delegating then we don't need a field and we want the setter and getter
                    // to point to the delegate.

                    if ( modelField.getDelegateTo() != null )
                    {

                        if ( fieldType.equals( "boolean" ) )
                        {
                            jType = JType.Boolean;
                        }
                        else
                        {
                            jType = new JClass( fieldType );
                        }

                        // Field

                        JField jField = new JField( jType, fieldName );

                        if ( modelField.getDefaultValue() != null )
                        {
                            if ( modelField.getType().equals( "String" ) )
                            {
                                jField.setInitString( "\"" + modelField.getDefaultValue() + "\"" );
                            }
                            else
                            {
                                jField.setInitString( modelField.getDefaultValue() );
                            }
                        }

                        jClass.addField( jField );

                        // Properties

                        String propertyName = capitalise( fieldName );

                        // Getter

                        JMethod getter = new JMethod( jType, "get" + propertyName );

                        getter.getSourceCode().add( "return this." + fieldName + ";" );

                        jClass.addMethod( getter );

                        // Setter

                        JMethod setter = new JMethod( null, "set" + propertyName );

                        setter.addParameter( new JParameter( jType, fieldName ) );

                        setter.getSourceCode().add( "this." + fieldName + " = " + fieldName + ";" );

                        jClass.addMethod( setter );

                        // Add method

                        if ( isCollection( fieldType ) )
                        {
                            String parameterName = singular( fieldName );

                            String className = capitalise( parameterName );

                            JType addType;

                            if ( objectModel.getClassNames().contains( className ) )
                            {
                                addType = new JClass( className );
                            }
                            else
                            {
                                addType = new JClass( "String" );
                            }

                            JMethod adder = new JMethod( null, "add" + className );

                            adder.addParameter( new JParameter( addType, parameterName ) );

                            adder.getSourceCode().add( fieldName + ".add( " + parameterName + " );" );

                            jClass.addMethod( adder );
                        }

                        if ( fieldType.equals( "java.util.Properties" ) )
                        {
                            String parameterName = singular( fieldName );

                            String className = capitalise( parameterName );

                            JType addType = new JClass( "String" );

                            JMethod adder = new JMethod( null, "add" + className );

                            adder.addParameter( new JParameter( addType, "name" ) );

                            adder.addParameter( new JParameter( addType, "value" ) );

                            adder.getSourceCode().add( fieldName + ".setProperty( name, value );" );

                            jClass.addMethod( adder );
                        }

                        count++;
                    }

                    if ( modelClass.getCode() != null )
                    {
                        jClass.setSourceCode( modelClass.getCode() );
                    }
                }
            }


            jClass.print( sourceWriter );

            writer.flush();

            writer.close();
        }
    }
}
