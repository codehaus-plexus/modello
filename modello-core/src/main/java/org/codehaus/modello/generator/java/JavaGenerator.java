package org.codehaus.modello.generator.java;

import org.codehaus.modello.Model;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelField;
import org.codehaus.modello.CodeSegment;
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
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 * @version $Id$
 */
public class JavaGenerator
    extends AbstractGenerator
{
    public JavaGenerator( String model, String outputDirectory, String modelVersion )
    {
        super( model, outputDirectory, modelVersion );
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

                if ( modelClass.getFields() != null )
                {
                    int count = 1;

                    for ( Iterator j = modelClass.getFields().iterator(); j.hasNext(); )
                    {
                        ModelField modelField = (ModelField) j.next();

                        if ( outputElement( modelField.getVersion(), modelField.getName() ) )
                        {
                            if ( modelField.getName() == null )
                            {
                                throw new IllegalStateException( "Field name can't be null jField: element " + count + " in the definition of the class " + modelClass.getName() );
                            }

                            if ( modelField.getDelegateTo() != null )
                            {
                                JField delegate = createField( modelClass.getField( modelField.getDelegateTo() ), modelClass, count );

                                jClass.addMethod( createDelegateGetter( modelField, delegate ) );

                                jClass.addMethod( createDelegateSetter( modelField, delegate ) );
                            }
                            else
                            {
                                JField field = createField( modelField, modelClass, count );

                                jClass.addField( field );

                                jClass.addMethod( createGetter( field ) );

                                jClass.addMethod( createSetter( field ) );

                                createAdder( field, jClass, objectModel );
                            }

                            if ( modelClass.getCodeSegments() != null )
                            {
                                for ( Iterator iterator = modelClass.getCodeSegments().iterator(); iterator.hasNext(); )
                                {
                                    CodeSegment codeSegment = (CodeSegment) iterator.next();

                                    jClass.addSourceCode( codeSegment.getCode() );
                                }
                            }
                        }
                    }

                    count++;
                }

                jClass.print( sourceWriter );

                writer.flush();

                writer.close();
            }
        }
    }

    private JField createField( ModelField modelField, ModelClass modelClass, int entry )
    {
        if ( modelField.getType() == null )
        {
            throw new IllegalStateException( "Field type can't be null: jField element " + entry + " in the definition of the class " + modelClass.getName() );
        }

        JType type;

        if ( modelField.getType().equals( "boolean" ) )
        {
            type = JType.Boolean;
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

    private void createAdder( JField field, JClass jClass, Model objectModel )
    {
        if ( isCollection( field.getType().getName() ) )
        {
            String parameterName = singular( field.getName() );

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

            adder.getSourceCode().add( field.getName() + ".add( " + parameterName + " );" );

            jClass.addMethod( adder );
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
    }

    // --------------------------------------------------------------------------------------------
    // Delegates
    //
    // These are for use in the model when one element is being superceded by another but
    // you want to provide some backward compatibility. So for example in your xml you may
    // have an element named "pomVersion" which is being replaced with "modelVersion". So
    // you still want to accept "pomVersion" but we just delegate to our new "modelVersion"
    // element. So we can accept the new while supporting the old but internally within
    // the model we are using our new "modelVersion" element for everything.
    // --------------------------------------------------------------------------------------------

    private JMethod createDelegateGetter( ModelField field, JField delegate )
    {
        String propertyName = capitalise( field.getName() );

        String delegatePropertyName = capitalise( delegate.getName() );

        JMethod getter = new JMethod( delegate.getType(), "get" + propertyName );

        getter.getSourceCode().add( "return get" + delegatePropertyName + "();" );

        return getter;
    }

    private JMethod createDelegateSetter( ModelField field, JField delegate )
    {
        String propertyName = capitalise( field.getName() );

        String delegatePropertyName = capitalise( delegate.getName() );

        JMethod setter = new JMethod( null, "set" + propertyName );

        setter.addParameter( new JParameter( delegate.getType(), field.getName() ) );

        setter.getSourceCode().add( "set" + delegatePropertyName + "( " + field.getName() + " );" );

        return setter;
    }
}
