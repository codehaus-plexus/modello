package org.codehaus.modello.plugin.xpp3;

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
import java.util.List;
import java.util.Properties;

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.generator.java.javasource.JClass;
import org.codehaus.modello.generator.java.javasource.JField;
import org.codehaus.modello.generator.java.javasource.JMethod;
import org.codehaus.modello.generator.java.javasource.JParameter;
import org.codehaus.modello.generator.java.javasource.JSourceCode;
import org.codehaus.modello.generator.java.javasource.JSourceWriter;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelDefault;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.plugins.xml.XmlAssociationMetadata;
import org.codehaus.modello.plugins.xml.XmlFieldMetadata;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 *
 * @version $Id$
 */
public class Xpp3WriterGenerator
    extends AbstractXpp3Generator
{
    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

        try
        {
            generateXpp3Writer( model );
        }
        catch( IOException ex )
        {
            throw new ModelloException( "Exception while generating XDoc.", ex );
        }
    }

    private void generateXpp3Writer( Model model )
        throws ModelloException, IOException
    {
        Model objectModel = getModel();

        String packageName = getBasePackageName() + ".io.xpp3";

        String directory = packageName.replace( '.', '/' );

        String marshallerName = getFileName( "Xpp3Writer" );

        File f = new File( new File( getOutputDirectory(), directory ), marshallerName + ".java" );

        if ( !f.getParentFile().exists() )
        {
            f.getParentFile().mkdirs();
        }

        FileWriter writer = new FileWriter( f );

        JSourceWriter sourceWriter = new JSourceWriter( writer );

        JClass jClass = new JClass( marshallerName );

        jClass.setPackageName( packageName );

        jClass.addImport( "org.xmlpull.v1.XmlPullParser" );

        jClass.addImport( "org.xmlpull.v1.XmlPullParserFactory" );

        jClass.addImport( "org.xmlpull.v1.XmlSerializer" );

        jClass.addImport( "java.io.Writer" );

        jClass.addImport( "java.util.Iterator" );

        jClass.addField( new JField( new JClass( "org.xmlpull.v1.XmlSerializer" ), "serializer" ) );

        jClass.addField( new JField( new JClass( "String" ), "NAMESPACE" ) );

        /*

        There doesn't seem to be a way to add exceptions to constructors, we'll have to tweak javasource.

        JConstructor constructor = new JConstructor( jClass );

        constructor.getSourceCode().add( "XmlPullParserFactory factory = XmlPullParserFactory.newInstance( System.getProperty( XmlPullParserFactory.PROPERTY_NAME ), null );" );

        constructor.getSourceCode().add( "serializer = factory.newSerializer();" );

        constructor.getSourceCode().add( "serializer.setProperty( \"http://xmlpull.org/v1/doc/properties.html#serializer-indentation\", \"  \" );" );

        constructor.getSourceCode().add( "serializer.setProperty( \"http://xmlpull.org/v1/doc/properties.html#serializer-line-separator\", \"\\n\" );" );

        jClass.addConstructor( constructor );
        */

        addModelImports( jClass );

        String root = objectModel.getRoot();

        String rootElement = uncapitalise( root );

        // Write the parse method which will do the unmarshalling.

        JMethod marshall = new JMethod( null, "write" );

        marshall.addParameter( new JParameter( new JClass( "Writer" ), "writer" ) );

        marshall.addParameter( new JParameter( new JClass( root ), rootElement ) );

        marshall.addException( new JClass( "Exception" ) );

        JSourceCode sc = marshall.getSourceCode();

        sc.add( "XmlPullParserFactory factory = XmlPullParserFactory.newInstance( System.getProperty( XmlPullParserFactory.PROPERTY_NAME ), null );" );

        sc.add( "serializer = factory.newSerializer();" );

        sc.add( "serializer.setProperty( \"http://xmlpull.org/v1/doc/properties.html#serializer-indentation\", \"  \" );" );

        sc.add( "serializer.setProperty( \"http://xmlpull.org/v1/doc/properties.html#serializer-line-separator\", \"\\n\" );" );

        sc.add( "serializer.setOutput( writer );" );

        sc.add( "serializer.startTag( NAMESPACE, \"" + rootElement + "\" );" );

        writeClassMarshalling( (ModelClass) objectModel.getClasses( getGeneratedVersion() ).get( 0 ), sc );

        sc.add( "serializer.endTag( NAMESPACE, \"" + rootElement + "\" );" );

        jClass.addMethod( marshall );

        jClass.print( sourceWriter );

        writer.flush();

        writer.close();
    }

    private void writeClassMarshalling( ModelClass modelClass, JSourceCode sc )
        throws ModelloRuntimeException
    {
        writeClassMarshalling( modelClass, null, sc );
    }

    private void writeClassMarshalling( ModelClass modelClass, String objectName, JSourceCode sc )
        throws ModelloRuntimeException
    {
        List fields = modelClass.getAllFields( getGeneratedVersion(), true );

        int fieldCount = fields.size();

        for ( int i = 0; i < fieldCount; i++ )
        {
            ModelField field = (ModelField) fields.get( i );

            if ( field instanceof ModelAssociation &&
                ModelAssociation.MANY_MULTIPLICITY.equals( ( (ModelAssociation) field ).getMultiplicity() ) )
            {
                writeAssociationMarshalling( modelClass, (ModelAssociation) field, objectName, sc );
            }
            else
            {
                writeFieldMarshalling( modelClass, field, objectName, sc );
            }
        }
    }

    private void writeFieldMarshalling( ModelClass modelClass, ModelField field, String objectName, JSourceCode sc )
        throws ModelloRuntimeException
    {
        XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata)field.getMetadata( XmlFieldMetadata.ID );

        boolean attribute = xmlFieldMetadata.isAttribute();

        String tagName = xmlFieldMetadata.getTagName();

        String type = field.getType();

        String fieldName = field.getName();

        String className = capitalise( field.getName() );

        String modelClassName = uncapitalise( modelClass.getName() );

        if ( objectName != null )
        {
            modelClassName = objectName;
        }

        if ( tagName == null )
        {
            tagName = fieldName;
        }

        if ( isClassInModel( type, modelClass.getModel() ) )
        {
            if ( attribute )
            {
                throw new ModelloRuntimeException( "A class cannot be a serialized as a attribute." );
            }

            sc.add( "// Writing class in field" );

            sc.add( className + " " + fieldName + " = " + modelClassName + ".get" + className + "();" );

            sc.add( "if ( " + fieldName + " != null )" );

            sc.add( "{" );

            sc.indent();

            sc.add( "serializer.startTag( NAMESPACE, " + "\"" + tagName + "\" );" );

            writeClassMarshalling( modelClass.getModel().getClass( type, getGeneratedVersion() ), fieldName, sc );

            sc.add( "serializer.endTag( NAMESPACE, " + "\"" + tagName + "\" );" );

            sc.unindent();

            sc.add( "}" );
        }
        else
        {
            sc.add( "// Writing primitive in field" );

            String textValue = getValue( field.getType(), modelClassName + ".get" + className + "()" );

            // Write "if ( value != ...)"
            sc.add( getValueChecker( field.getType(), modelClassName + ".get" + className + "()", field ) );

            sc.add( "{" );

            sc.indent();

            if ( attribute )
            {
                sc.add( "serializer.attribute( NAMESPACE, \"" + tagName + "\", " +
                        textValue + " );" );
            }
            else
            {
                sc.add( "serializer.startTag( NAMESPACE, " + "\"" + tagName + "\" ).text( " +
                        textValue + " ).endTag( NAMESPACE, " + "\"" + tagName + "\" );" );
            }

            sc.unindent();

            sc.add( "}" );
        }
    }

    private void writeAssociationMarshalling( ModelClass modelClass, ModelAssociation association, String objectName, JSourceCode sc )
        throws ModelloRuntimeException
    {
        XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata)association.getMetadata( XmlFieldMetadata.ID );

        XmlAssociationMetadata xmlAssociationMetadata = (XmlAssociationMetadata)association.getAssociationMetadata( XmlAssociationMetadata.ID );

        boolean attribute = xmlFieldMetadata.isAttribute();

        String tagName = xmlFieldMetadata.getTagName();
        
        String modelClassName = uncapitalise( modelClass.getName() );

        String fieldName = association.getName();

        String getterName = capitalise( fieldName );

        String type = association.getTo();

        String singularFieldName = singular( fieldName );

        if ( objectName != null )
        {
            modelClassName = objectName;
        }

        if ( tagName == null )
        {
            tagName = fieldName;
        }

        if ( isClassInModel( type, association.getModelClass().getModel() ) )
        {
            String size = modelClassName + ".get" + getterName + "().size()";

            String index = getIndex();

            sc.add( "// Writing class association" );

            sc.add( "if ( " + modelClassName + ".get" + getterName + "() != null && " + modelClassName + ".get" + getterName + "().size() > 0 )" );

            sc.add( "{" );

            sc.indent();

            sc.add( "serializer.startTag( NAMESPACE, " + "\"" + tagName + "\" );" );

            sc.add( "for ( int " + index + " = 0; " + index + " < " + size + "; " + index + "++ )" );

            sc.add( "{" );

            sc.indent();

            sc.add( type + " " + singularFieldName +
                    " = (" + type + ") " + modelClassName + ".get" + getterName + "().get( " + index + " );" );

            sc.add( "serializer.startTag( NAMESPACE, " + "\"" + singular( tagName ) + "\" );" );

            writeClassMarshalling( association.getModelClass().getModel().getClass( type, getGeneratedVersion() ), singularFieldName, sc );

            sc.add( "serializer.endTag( NAMESPACE, " + "\"" + singular( tagName ) + "\" );" );

            sc.unindent();

            sc.add( "}" );

            sc.add( "serializer.endTag( NAMESPACE, " + "\"" + tagName + "\" );" );

            sc.unindent();

            sc.add( "}" );
        }
        else
        {
            sc.add( "// Writing other association" );

            sc.add( "if ( " + modelClassName + ".get" + getterName + "() != null && " + modelClassName + ".get" + getterName + "().size() > 0 )" );

            sc.add( "{" );

            sc.indent();

            sc.add( "serializer.startTag( NAMESPACE, " + "\"" + tagName + "\" );" );

            String size = modelClassName + ".get" + getterName + "().size()";

            String index = getIndex();

            if ( ModelDefault.MAP.equals( association.getType() )
                || ModelDefault.PROPERTIES.equals( association.getType() ) )
            {
                sc.add( "for( Iterator iter = " + modelClassName + ".get" + getterName + "().keySet().iterator(); iter.hasNext(); )" );

                sc.add( "{" );

                sc.indent();

                sc.add( "String key = (String) iter.next();" );

                sc.add( "String value = (String) " + modelClassName + ".get" + getterName + "().get( key );" );

                if ( "inline".equals( xmlAssociationMetadata.getMapStyle() ) )
                {
                    sc.add( "serializer.startTag( NAMESPACE, \"\" + key + \"\" ).text( value ).endTag( NAMESPACE, \"\" + key + \"\" );" );
                }
                else
                {
                    sc.add( "serializer.startTag( NAMESPACE, \"" + singular( fieldName ) + "\" );" );
                    sc.add( "serializer.startTag( NAMESPACE, \"key\" ).text( key ).endTag( NAMESPACE, \"key\" );" );
                    sc.add( "serializer.startTag( NAMESPACE, \"value\" ).text( value ).endTag( NAMESPACE, \"value\" );" );
                    sc.add( "serializer.endTag( NAMESPACE, \"" + singular( fieldName ) + "\" );" );
                }

                sc.unindent();

                sc.add( "}" );
            }
            else
            {
                sc.add( "for ( int " + index + "= 0; " + index + " < " + size + "; " + index + "++ )" );

                sc.add( "{" );

                sc.indent();

                sc.add( "String s = (String) " + modelClassName + ".get" + getterName + "().get( " + index + " );" );

                sc.add( "serializer.startTag( NAMESPACE, " + "\"" + singular( tagName ) + "\" ).text( s ).endTag( NAMESPACE, " + "\"" + singular( tagName ) + "\" );" );

                sc.unindent();

                sc.add( "}" );
            }

            sc.add( "serializer.endTag( NAMESPACE, " + "\"" + tagName + "\" );" );

            sc.unindent();

            sc.add( "}" );
        }
    }

    int index;

    private String getIndex()
    {
        index++;

        return "i" + Integer.toString( index );
    }
    
    private String getValue( String type, String initialValue )
    {
        String textValue = initialValue;

        if ( ! "String".equals( type ) )
        {
            textValue = "String.valueOf( " + textValue + " )";
        }

        return textValue;
    }

    private String getValueChecker( String type, String value, ModelField field )
    {
        if ( "boolean".equals( type )
            || "double".equals( type )
            || "float".equals( type )
            || "int".equals( type )
            || "long".equals( type )
            || "short".equals( type ) )
        {
            return "if ( " + value + " != " + field.getDefaultValue() + " )";
        }
        else if ( "char".equals( type ) )
        {
            return "if ( " + value + " != '" + field.getDefaultValue() + "' )";
        }
        else
        {
            return "if ( " + value + " != null )";
        }
    }
}
