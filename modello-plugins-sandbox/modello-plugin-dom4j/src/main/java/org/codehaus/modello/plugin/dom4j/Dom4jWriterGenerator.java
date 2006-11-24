package org.codehaus.modello.plugin.dom4j;

/*
 * Copyright (c) 2004, Joakim Erdfelt
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
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 */
public class Dom4jWriterGenerator
    extends AbstractDom4jGenerator
{

    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

        try
        {
            generateDom4jWriter();
        }
        catch ( IOException ex )
        {
            throw new ModelloException( "Exception while generating DOM4J Writer.", ex );
        }
    }

    private void generateDom4jWriter()
        throws ModelloException, IOException
    {
        Model objectModel = getModel();

        String packageName;

        if ( isPackageWithVersion() )
        {
            packageName = objectModel.getDefaultPackageName( true, getGeneratedVersion() );
        }
        else
        {
            packageName = objectModel.getDefaultPackageName( false, null );
        }

        packageName += ".io.dom4j";

        String directory = packageName.replace( '.', '/' );

        String marshallerName = getFileName( "Dom4jWriter" );

        File f = new File( new File( getOutputDirectory(), directory ), marshallerName + ".java" );

        if ( !f.getParentFile().exists() )
        {
            f.getParentFile().mkdirs();
        }

        FileWriter writer = new FileWriter( f );

        JSourceWriter sourceWriter = new JSourceWriter( writer );

        JClass jClass = new JClass( marshallerName );

        jClass.setPackageName( packageName );

        /* imports used by dom4j */
        jClass.addImport( "org.dom4j.Document" );
        jClass.addImport( "org.dom4j.DocumentFactory" );
        jClass.addImport( "org.dom4j.Element" );
        jClass.addImport( "org.dom4j.io.OutputFormat" );
        jClass.addImport( "org.dom4j.io.XMLWriter" );

        /* imports used by writer */
        jClass.addImport( "java.io.Writer" );
        jClass.addImport( "java.util.Iterator" );

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

        marshall.addException( new JClass( "java.io.IOException" ) );

        JSourceCode sc = marshall.getSourceCode();

        sc.add( "org.dom4j.Document doc = org.dom4j.DocumentFactory.getInstance().createDocument(\"\");" );
        // sc.add("org.dom4j.Element root = doc.getRootElement();");

        sc.add( "" );
        sc.add( "/* populate dom */" );

        sc.add( "org.dom4j.Element root = make" + root + "( " + rootElement + ", \"" + rootElement + "\" );" );
        sc.add( "doc.setRootElement( root );" );

        sc.add( "" );
        sc.add(
            "org.dom4j.io.XMLWriter xmlwriter = new org.dom4j.io.XMLWriter( writer, org.dom4j.io.OutputFormat.createPrettyPrint() );" );
        sc.add( "xmlwriter.write( doc );" );
        sc.add( "xmlwriter.flush();" );

        jClass.addMethod( marshall );

        writeAllClasses( objectModel, jClass );

        jClass.print( sourceWriter );

        writer.flush();

        writer.close();
    }

    private void writeAllClasses( Model objectModel, JClass jClass )
        throws IOException
    {
        for ( Iterator i = objectModel.getClasses( getGeneratedVersion() ).iterator(); i.hasNext(); )
        {
            ModelClass clazz = (ModelClass) i.next();

            writeClass( clazz, jClass );
        }
    }

    private void writeClass( ModelClass modelClass, JClass jClass )
        throws IOException
    {
        String className = modelClass.getName();

        String uncapClassName = uncapitalise( className );

        JMethod marshall = new JMethod( new JClass( "org.dom4j.Element" ), "make" + className );

        marshall.addParameter( new JParameter( new JClass( className ), uncapClassName ) );
        marshall.addParameter( new JParameter( new JClass( "String" ), "tagName" ) );

        marshall.addException( new JClass( "java.io.IOException" ) );

        marshall.getModifiers().makePrivate();

        JSourceCode sc = marshall.getSourceCode();

        sc.add( "org.dom4j.Element elem = org.dom4j.DocumentFactory.getInstance().createElement( tagName );" );


        sc.add( "if ( " + uncapClassName + " == null )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "return elem;" );
        sc.unindent();
        sc.add( "}" );

        sc.add( "" );
        sc.add( "/* XML Attributes */" );

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

            String value = uncapClassName + "." + getPrefix( javaFieldMetadata ) + capitalise( field.getName() ) + "()";

            if ( fieldMetadata.isAttribute() )
            {
                sc.add( getValueChecker( type, value, field ) );

                sc.add( "{" );

                sc.indent();

                sc.add( "elem.addAttribute( \"" + fieldTagName + "\", "
                        + getValue( field.getType(), value ) + " );" );

                sc.unindent();

                sc.add( "}" );
            }
        }

        sc.add( "" );
        sc.add( "/* XML Elements */" );

        // XML tags
        Iterator fieldIterator = modelClass.getAllFields( getGeneratedVersion(), true ).iterator();
        while ( fieldIterator.hasNext() )
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

            String value = uncapClassName + "." + getPrefix( javaFieldMetadata ) + capitalise( field.getName() ) + "()";

            if ( fieldMetadata.isAttribute() )
            {
                continue;
            }

            if ( field instanceof ModelAssociation )
            {
                breadcrumb( sc, "is a model association" );
                ModelAssociation association = (ModelAssociation) field;

                String associationName = association.getName();

                if ( ModelAssociation.ONE_MULTIPLICITY.equals( association.getMultiplicity() ) )
                {
                    breadcrumb( sc, "is a one multiplicity" );
                    sc.add( getValueChecker( type, value, association ) );

                    sc.add( "{" );

                    sc.indent();

                    sc.add( "elem.add( make" + association.getTo() + "( " + value + ", \"" + fieldTagName + "\" ) );" );

                    sc.unindent();

                    sc.add( "}" );
                }
                else
                {
                    //MANY_MULTIPLICITY

                    breadcrumb( sc, "is many multiplicity" );
                    type = association.getType();
                    String toType = association.getTo();

                    if ( ModelDefault.LIST.equals( type ) || ModelDefault.SET.equals( type ) )
                    {
                        breadcrumb( sc, "is a list. wrapped: " + wrappedList );
                        sc.add( getValueChecker( type, value, association ) );

                        sc.add( "{" );

                        sc.indent();

                        String addToElem = "elem";

                        if ( wrappedList )
                        {
                            sc.add( "org.dom4j.Element subelem = elem.addElement( \"" + fieldTagName + "\" );" );
                            addToElem = "subelem";
                        }

                        sc.add( "Iterator it = " + value + ".iterator();" );
                        sc.add( "while ( it.hasNext() )" );

                        sc.add( "{" );

                        sc.indent();

                        if ( isClassInModel( association.getTo(), modelClass.getModel() ) )
                        {
                            sc.add( toType + " " + uncapitalise( toType ) + " = (" + toType + ") it.next();" );

                            sc.add( addToElem + ".add( make" + toType + "( " + uncapitalise( toType ) + ", \"" +
                                    singularTagName + "\" ) );" );
                        }
                        else
                        {
                            String variableName = singular( uncapitalise( field.getName() ) );
                            sc.add( toType + " " + variableName + " = (" + toType
                                    + ") it.next();" );

                            sc.add(
                                addToElem + ".addElement( \"" + singularTagName + "\" ).setText( " + variableName + " );" );
                        }

                        sc.unindent();

                        sc.add( "}" );

                        sc.unindent();

                        sc.add( "}" );
                    }
                    else
                    {
                        //Map or Properties
                        breadcrumb( sc, "is a map or property" );

                        XmlAssociationMetadata xmlAssociationMetadata = (XmlAssociationMetadata) association
                            .getAssociationMetadata( XmlAssociationMetadata.ID );

                        sc.add( getValueChecker( type, value, field ) );

                        sc.add( "{" );

                        sc.indent();

                        if ( wrappedList )
                        {
                            sc.add( "org.dom4j.Element subelem = elem.addElement( \"" + fieldTagName + "\" );" );
                        }

                        sc.add( "Iterator it = " + value + ".keySet().iterator();" );
                        sc.add( "while ( it.hasNext() )" );
                        sc.add( "{" );

                        sc.indent();

                        sc.add( "String key = (String) it.next();" );

                        sc.add( "String value = (String) " + value + ".get( key );" );

                        if ( XmlAssociationMetadata.EXPLODE_MODE.equals( xmlAssociationMetadata.getMapStyle() ) )
                        {
                            sc.add( "org.dom4j.Element " + singular( associationName ) +
                                    "Elem = subelem.addElement( \"" + singular( associationName ) + "\" );" );
                            sc.add( singular( associationName ) + "Elem.addElement( \"key\" ).setText( key );" );
                            sc.add( singular( associationName ) + "Elem.addElement( \"value\" ).setText( value );" );
                        }
                        else
                        {
                            sc.add( "subelem.addElement( key ).setText( value );" );
                        }

                        sc.unindent();

                        sc.add( "}" );

                        sc.unindent();

                        sc.add( "}" );
                    }
                }
            }
            else
            {
                breadcrumb( sc, "is normal field." );
                sc.add( getValueChecker( type, value, field ) );

                sc.add( "{" );

                sc.indent();

                // TODO: handle DOM field type.
                if ( "DOM".equals( field.getType() ) )
                {
                    throw new IOException( "Unable to create dom4j writer for DOM field type." );
                }
                else
                {
                    sc.add( "elem.addElement( \"" + fieldTagName + "\" ).setText( " + getValue( field.getType(),
                                                                                                value ) + " );" );
                }

                sc.unindent();

                sc.add( "}" );
            }
        }

        sc.add( "return elem;" );

        jClass.addMethod( marshall );
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
        if ( "boolean".equals( type ) || "double".equals( type ) || "float".equals( type ) || "int".equals( type )
             || "long".equals( type ) || "short".equals( type ) )
        {
            return "if ( " + value + " != " + field.getDefaultValue() + " )";
        }
        else if ( "char".equals( type ) )
        {
            return "if ( " + value + " != '" + field.getDefaultValue() + "' )";
        }
        else if ( ModelDefault.LIST.equals( type ) || ModelDefault.SET.equals( type ) || ModelDefault.MAP.equals( type )
                  || ModelDefault.PROPERTIES.equals( type ) )
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
