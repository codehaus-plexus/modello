package org.codehaus.modello.plugin.xpp3;

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
import org.codehaus.modello.plugin.java.JavaFieldMetadata;
import org.codehaus.modello.plugin.model.ModelClassMetadata;
import org.codehaus.modello.plugins.xml.XmlAssociationMetadata;
import org.codehaus.modello.plugins.xml.XmlFieldMetadata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl </a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse </a>
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
        catch ( IOException ex )
        {
            throw new ModelloException( "Exception while generating XPP3 Writer.", ex );
        }
    }

    private void generateXpp3Writer( Model model )
        throws ModelloException, IOException
    {
        Model objectModel = getModel();

        String packageName;

        if ( isPackageWithVersion() )
        {
            packageName = objectModel.getPackageName( true, getGeneratedVersion() );
        }
        else
        {
            packageName = objectModel.getPackageName( false, null );
        }

        packageName += ".io.xpp3";

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

        jClass.addImport( "org.codehaus.plexus.util.xml.pull.*" );

        jClass.addImport( "java.io.Writer" );

        jClass.addImport( "java.util.Iterator" );

        jClass.addField( new JField( new JClass( "org.codehaus.plexus.util.xml.pull.XmlSerializer" ), "serializer" ) );

        jClass.addField( new JField( new JClass( "String" ), "NAMESPACE" ) );

        addModelImports( jClass, null );

        String root = objectModel.getRoot( getGeneratedVersion() );

        ModelClass rootClass = objectModel.getClass( root, getGeneratedVersion() );

        ModelClassMetadata metadata = (ModelClassMetadata) rootClass.getMetadata( ModelClassMetadata.ID );

        String rootElement;
        if ( metadata == null || metadata.getTagName() == null )
        {
            rootElement = uncapitalise( root );
        }
        else
        {
            rootElement = metadata.getTagName();
        }

        // Write the parse method which will do the unmarshalling.

        JMethod marshall = new JMethod( null, "write" );

        marshall.addParameter( new JParameter( new JClass( "Writer" ), "writer" ) );

        marshall.addParameter( new JParameter( new JClass( root ), rootElement ) );

        marshall.addException( new JClass( "Exception" ) );

        JSourceCode sc = marshall.getSourceCode();

        sc.add( "serializer = new MXSerializer();" );

        sc.add(
            "serializer.setProperty( \"http://xmlpull.org/v1/doc/properties.html#serializer-indentation\", \"  \" );" );

        sc.add(
            "serializer.setProperty( \"http://xmlpull.org/v1/doc/properties.html#serializer-line-separator\", \"\\n\" );" );

        sc.add( "serializer.setOutput( writer );" );

        sc.add( "write" + root + "( " + rootElement + ", \"" + rootElement + "\", serializer );" );

        jClass.addMethod( marshall );

        writeAllClasses( objectModel, jClass );

        jClass.print( sourceWriter );

        writer.flush();

        writer.close();
    }

    private void writeAllClasses( Model objectModel, JClass jClass )
    {
        for ( Iterator i = objectModel.getClasses( getGeneratedVersion() ).iterator(); i.hasNext(); )
        {
            ModelClass clazz = (ModelClass) i.next();

            writeClass( clazz, jClass );
        }
    }

    private void writeClass( ModelClass modelClass, JClass jClass )
    {
        String className = modelClass.getName();

        String uncapClassName = uncapitalise( className );

        JMethod unmarshall = new JMethod( null, "write" + className );

        unmarshall.addParameter( new JParameter( new JClass( className ), uncapClassName ) );

        unmarshall.addParameter( new JParameter( new JClass( "String" ), "tagName" ) );

        unmarshall.addParameter( new JParameter( new JClass( "XmlSerializer" ), "serializer" ) );

        unmarshall.addException( new JClass( "Exception" ) );

        unmarshall.getModifiers().makePrivate();

        JSourceCode sc = unmarshall.getSourceCode();

        sc.add( "if ( " + uncapClassName + " != null )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "serializer.startTag( NAMESPACE, tagName );" );

        // XML attributes
        for ( Iterator i = modelClass.getAllFields( getGeneratedVersion(), true ).iterator(); i.hasNext(); )
        {
            ModelField field = (ModelField) i.next();

            XmlFieldMetadata fieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

            JavaFieldMetadata javaFieldMetadata = (JavaFieldMetadata) field.getMetadata( JavaFieldMetadata.ID );

            String fieldTagName = fieldMetadata.getTagName();

            if ( fieldTagName == null )
            {
                fieldTagName = field.getName();
            }

            String type = field.getType();

            String value = uncapClassName + "." + getPrefix( javaFieldMetadata ) + capitalise( field.getName() ) +
                "()";

            if ( fieldMetadata.isAttribute() )
            {
                sc.add( getValueChecker( type, value, field ) );

                sc.add( "{" );

                sc.indent();

                sc.add( "serializer.attribute( NAMESPACE, \"" + fieldTagName + "\", " +
                        getValue( field.getType(), value ) + " );" );

                sc.unindent();

                sc.add( "}" );
            }
        }

        // XML tags
        for ( Iterator fieldIterator = modelClass.getAllFields( getGeneratedVersion(), true ).iterator();
              fieldIterator.hasNext(); )
        {
            ModelField field = (ModelField) fieldIterator.next();

            XmlFieldMetadata fieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

            JavaFieldMetadata javaFieldMetadata = (JavaFieldMetadata) field.getMetadata( JavaFieldMetadata.ID );

            String fieldTagName = fieldMetadata.getTagName();

            if ( fieldTagName == null )
            {
                fieldTagName = field.getName();
            }

            String singularTagName = fieldMetadata.getAssociationTagName();
            if ( singularTagName == null )
            {
                singularTagName = singular( fieldTagName );
            }

            boolean wrappedList = XmlFieldMetadata.LIST_STYLE_WRAPPED.equals( fieldMetadata.getListStyle() );

            String type = field.getType();

            String value = uncapClassName + "." + getPrefix( javaFieldMetadata ) + capitalise( field.getName() ) +
                "()";

            if ( fieldMetadata.isAttribute() )
            {
                continue;
            }

            if ( field instanceof ModelAssociation )
            {
                ModelAssociation association = (ModelAssociation) field;

                String associationName = association.getName();

                if ( ModelAssociation.ONE_MULTIPLICITY.equals( association.getMultiplicity() ) )
                {
                    sc.add( getValueChecker( type, value, association ) );

                    sc.add( "{" );

                    sc.indent();

                    sc.add( "write" + association.getTo() + "( " + value + ", \"" + fieldTagName + "\", serializer );" );

                    sc.unindent();

                    sc.add( "}" );
                }
                else
                {
                    //MANY_MULTIPLICITY

                    type = association.getType();
                    String toType = association.getTo();

                    if ( ModelDefault.LIST.equals( type ) || ModelDefault.SET.equals( type ) )
                    {
                        sc.add( getValueChecker( type, value, association ) );

                        sc.add( "{" );

                        sc.indent();

                        if ( wrappedList )
                        {
                            sc.add( "serializer.startTag( NAMESPACE, " + "\"" + fieldTagName + "\" );" );
                        }

                        sc.add( "for ( Iterator iter = " + value + ".iterator(); iter.hasNext(); )" );

                        sc.add( "{" );

                        sc.indent();

                        if ( isClassInModel( association.getTo(), modelClass.getModel() ) )
                        {
                            sc.add( toType + " " + uncapitalise( toType ) + " = (" + toType + ") iter.next();" );

                            sc.add( "write" + toType + "( " + uncapitalise( toType ) + ", \"" + singularTagName +
                                    "\", serializer );" );
                        }
                        else
                        {
                            sc.add( toType + " " + singular( uncapitalise( field.getName() ) ) + " = (" + toType +
                                    ") iter.next();" );

                            sc.add( "serializer.startTag( NAMESPACE, " + "\"" + singularTagName + "\" ).text( " +
                                    singular( uncapitalise( field.getName() ) ) + " ).endTag( NAMESPACE, " + "\"" +
                                    singularTagName + "\" );" );
                        }

                        sc.unindent();

                        sc.add( "}" );

                        if ( wrappedList )
                        {
                            sc.add( "serializer.endTag( NAMESPACE, " + "\"" + fieldTagName + "\" );" );
                        }

                        sc.unindent();

                        sc.add( "}" );
                    }
                    else
                    {
                        //Map or Properties

                        XmlAssociationMetadata xmlAssociationMetadata = (XmlAssociationMetadata) association.getAssociationMetadata(
                            XmlAssociationMetadata.ID );

                        sc.add( getValueChecker( type, value, field ) );

                        sc.add( "{" );

                        sc.indent();

                        if ( wrappedList )
                        {
                            sc.add( "serializer.startTag( NAMESPACE, " + "\"" + fieldTagName + "\" );" );
                        }

                        sc.add( "for ( Iterator iter = " + value + ".keySet().iterator(); iter.hasNext(); )" );

                        sc.add( "{" );

                        sc.indent();

                        sc.add( "String key = (String) iter.next();" );

                        sc.add( "String value = (String) " + value + ".get( key );" );

                        if ( XmlAssociationMetadata.EXPLODE_MODE.equals( xmlAssociationMetadata.getMapStyle() ) )
                        {
                            sc.add( "serializer.startTag( NAMESPACE, \"" + singular( associationName ) + "\" );" );
                            sc.add(
                                "serializer.startTag( NAMESPACE, \"key\" ).text( key ).endTag( NAMESPACE, \"key\" );" );
                            sc.add(
                                "serializer.startTag( NAMESPACE, \"value\" ).text( value ).endTag( NAMESPACE, \"value\" );" );
                            sc.add( "serializer.endTag( NAMESPACE, \"" + singular( associationName ) + "\" );" );
                        }
                        else
                        {
                            sc.add(
                                "serializer.startTag( NAMESPACE, \"\" + key + \"\" ).text( value ).endTag( NAMESPACE, \"\" + key + \"\" );" );
                        }

                        sc.unindent();

                        sc.add( "}" );

                        if ( wrappedList )
                        {
                            sc.add( "serializer.endTag( NAMESPACE, " + "\"" + fieldTagName + "\" );" );
                        }

                        sc.unindent();

                        sc.add( "}" );
                    }
                }
            }
            else
            {
                sc.add( getValueChecker( type, value, field ) );

                sc.add( "{" );

                sc.indent();

                if ( "DOM".equals( field.getType() ) )
                {
                    sc.add( "serializer.text( " + value + ".toString() );" );
                }
                else
                {
                    sc.add( "serializer.startTag( NAMESPACE, " + "\"" + fieldTagName + "\" ).text( " +
                            getValue( field.getType(), value ) + " ).endTag( NAMESPACE, " + "\"" +
                            fieldTagName + "\" );" );
                }

                sc.unindent();

                sc.add( "}" );
            }
        }

        sc.add( "serializer.endTag( NAMESPACE, tagName );" );

        sc.unindent();

        sc.add( "}" );

        jClass.addMethod( unmarshall );
    }

    private String getPrefix( JavaFieldMetadata javaFieldMetadata )
    {
        return javaFieldMetadata.isBooleanGetter() ? "is" : "get";
    }

    private String getValue( String type, String initialValue )
    {
        String textValue = initialValue;

        if ( !"String".equals( type ) )
        {
            textValue = "String.valueOf( " + textValue + " )";
        }

        return textValue;
    }

    private String getValueChecker( String type, String value, ModelField field )
    {
        if ( "boolean".equals( type ) || "double".equals( type ) || "float".equals( type ) || "int".equals( type ) ||
            "long".equals( type ) || "short".equals( type ) )
        {
            return "if ( " + value + " != " + field.getDefaultValue() + " )";
        }
        else if ( "char".equals( type ) )
        {
            return "if ( " + value + " != '" + field.getDefaultValue() + "' )";
        }
        else if ( ModelDefault.LIST.equals( type ) || ModelDefault.SET.equals( type ) ||
            ModelDefault.MAP.equals( type ) ||
            ModelDefault.PROPERTIES.equals( type ) )
        {
            return "if ( " + value + " != null && " + value + ".size() > 0 )";
        }
        else if ( "String".equals( type ) && field.getDefaultValue() != null )
        {
            return "if ( " + value + " != null && !" + value + ".equals( \"" + field.getDefaultValue() + "\" ) )";
        }
        else
        {
            return "if ( " + value + " != null )";
        }
    }
}