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
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelDefault;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.plugin.java.javasource.JClass;
import org.codehaus.modello.plugin.java.javasource.JField;
import org.codehaus.modello.plugin.java.javasource.JMethod;
import org.codehaus.modello.plugin.java.javasource.JParameter;
import org.codehaus.modello.plugin.java.javasource.JSourceCode;
import org.codehaus.modello.plugin.java.javasource.JSourceWriter;
import org.codehaus.modello.plugin.java.javasource.JType;
import org.codehaus.modello.plugin.java.metadata.JavaFieldMetadata;
import org.codehaus.modello.plugin.model.ModelClassMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlAssociationMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlFieldMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlModelMetadata;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl </a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse </a>
 */
public class Xpp3WriterGenerator
    extends AbstractXpp3Generator
{
    private boolean requiresDomSupport;

    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

        requiresDomSupport = false;

        try
        {
            generateXpp3Writer();
        }
        catch ( IOException ex )
        {
            throw new ModelloException( "Exception while generating XPP3 Writer.", ex );
        }
    }

    private void generateXpp3Writer()
        throws ModelloException, IOException
    {
        Model objectModel = getModel();

        String packageName = objectModel.getDefaultPackageName( isPackageWithVersion(), getGeneratedVersion() )
            + ".io.xpp3";

        String marshallerName = getFileName( "Xpp3Writer" );

        JSourceWriter sourceWriter = newJSourceWriter( packageName, marshallerName );

        JClass jClass = new JClass( packageName + '.' + marshallerName );
        initHeader( jClass );
        suppressAllWarnings( objectModel, jClass );

        jClass.addImport( "org.codehaus.plexus.util.xml.pull.XmlSerializer" );
        jClass.addImport( "org.codehaus.plexus.util.xml.pull.MXSerializer" );
        jClass.addImport( "java.io.OutputStream" );
        jClass.addImport( "java.io.Writer" );
        jClass.addImport( "java.util.Iterator" );

        JField namespaceField = new JField( new JClass( "String" ), "NAMESPACE" );
        namespaceField.getModifiers().setFinal( true );
        namespaceField.getModifiers().setStatic( true );
        namespaceField.setInitString( "null" );
        jClass.addField( namespaceField );

        JField commentField = new JField( new JClass( "String" ), "fileComment" );
        commentField.setInitString( "null" );
        jClass.addField( commentField );

        // Add setComment method
        JMethod setComment = new JMethod( "setFileComment" );

        setComment.addParameter( new JParameter( new JClass( "String" ), "fileComment" ) );
        JSourceCode setCommentSourceCode = setComment.getSourceCode();
        setCommentSourceCode.add( "this.fileComment = fileComment;" );
        jClass.addMethod( setComment );

        addModelImports( jClass, null );

        String root = objectModel.getRoot( getGeneratedVersion() );

        ModelClass rootClass = objectModel.getClass( root, getGeneratedVersion() );

        String rootElement = resolveTagName( rootClass );

        // ----------------------------------------------------------------------
        // Write the write( Writer, Model ) method which will do the unmarshalling.
        // ----------------------------------------------------------------------

        JMethod marshall = new JMethod( "write" );

        String rootElementParameterName = uncapitalise( root );
        marshall.addParameter( new JParameter( new JClass( "Writer" ), "writer" ) );
        marshall.addParameter( new JParameter( new JClass( root ), rootElementParameterName ) );

        marshall.addException( new JClass( "java.io.IOException" ) );

        JSourceCode sc = marshall.getSourceCode();

        sc.add( "XmlSerializer serializer = new MXSerializer();" );

        sc.add(
            "serializer.setProperty( \"http://xmlpull.org/v1/doc/properties.html#serializer-indentation\", \"  \" );" );

        sc.add(
            "serializer.setProperty( \"http://xmlpull.org/v1/doc/properties.html#serializer-line-separator\", \"\\n\" );" );

        sc.add( "serializer.setOutput( writer );" );

        sc.add( "serializer.startDocument( " + rootElementParameterName + ".getModelEncoding(), null );" );

        sc.add( "write" + root + "( " + rootElementParameterName + ", \"" + rootElement + "\", serializer );" );

        sc.add( "serializer.endDocument();" );

        jClass.addMethod( marshall );

        // ----------------------------------------------------------------------
        // Write the write( OutputStream, Model ) method which will do the unmarshalling.
        // ----------------------------------------------------------------------

        marshall = new JMethod( "write" );

        marshall.addParameter( new JParameter( new JClass( "OutputStream" ), "stream" ) );
        marshall.addParameter( new JParameter( new JClass( root ), rootElementParameterName ) );

        marshall.addException( new JClass( "java.io.IOException" ) );

        sc = marshall.getSourceCode();

        sc.add( "XmlSerializer serializer = new MXSerializer();" );

        sc.add(
            "serializer.setProperty( \"http://xmlpull.org/v1/doc/properties.html#serializer-indentation\", \"  \" );" );

        sc.add(
            "serializer.setProperty( \"http://xmlpull.org/v1/doc/properties.html#serializer-line-separator\", \"\\n\" );" );

        sc.add( "serializer.setOutput( stream, " + rootElementParameterName + ".getModelEncoding() );" );

        sc.add( "serializer.startDocument( " + rootElementParameterName + ".getModelEncoding(), null );" );

        sc.add( "write" + root + "( " + rootElementParameterName + ", \"" + rootElement + "\", serializer );" );

        sc.add( "serializer.endDocument();" );

        jClass.addMethod( marshall );

        writeAllClasses( objectModel, jClass );

        if ( requiresDomSupport )
        {
            createWriteDomMethod( jClass );
        }

        jClass.print( sourceWriter );

        sourceWriter.close();
    }

    private void writeAllClasses( Model objectModel, JClass jClass )
        throws ModelloException
    {
        for ( ModelClass clazz : getClasses( objectModel ) )
        {
            writeClass( clazz, jClass );
        }
    }

    private void writeClass( ModelClass modelClass, JClass jClass )
        throws ModelloException
    {
        String className = modelClass.getName();

        String uncapClassName = uncapitalise( className );

        JMethod marshall = new JMethod( "write" + className );

        marshall.addParameter( new JParameter( new JClass( className ), uncapClassName ) );
        marshall.addParameter( new JParameter( new JClass( "String" ), "tagName" ) );
        marshall.addParameter( new JParameter( new JClass( "XmlSerializer" ), "serializer" ) );

        marshall.addException( new JClass( "java.io.IOException" ) );

        marshall.getModifiers().makePrivate();

        JSourceCode sc = marshall.getSourceCode();

        ModelClassMetadata classMetadata = (ModelClassMetadata) modelClass.getMetadata( ModelClassMetadata.ID );

        String namespace = null;
        XmlModelMetadata xmlModelMetadata = (XmlModelMetadata) modelClass.getModel().getMetadata( XmlModelMetadata.ID );

        // add namespace information for root element only
        if ( classMetadata.isRootElement() && ( xmlModelMetadata.getNamespace() != null ) )
        {
            sc.add( "if ( this.fileComment != null )" );
            sc.add( "{" );
            sc.add( "serializer.comment(this.fileComment);" );
            sc.add( "}" );

            namespace = xmlModelMetadata.getNamespace( getGeneratedVersion() );
            sc.add( "serializer.setPrefix( \"\", \"" + namespace + "\" );" );
        }

        if ( ( namespace != null ) && ( xmlModelMetadata.getSchemaLocation() != null ) )
        {
            String url = xmlModelMetadata.getSchemaLocation( getGeneratedVersion() );

            sc.add( "serializer.setPrefix( \"xsi\", \"http://www.w3.org/2001/XMLSchema-instance\" );" );

            sc.add( "serializer.startTag( NAMESPACE, tagName );" );

            sc.add( "serializer.attribute( \"\", \"xsi:schemaLocation\", \"" + namespace + " " + url + "\" );" );
        }
        else
        {
            sc.add( "serializer.startTag( NAMESPACE, tagName );" );
        }

        ModelField contentField = null;

        String contentValue = null;

        List<ModelField> modelFields = getFieldsForXml( modelClass, getGeneratedVersion() );

        // XML attributes
        for ( ModelField field : modelFields )
        {
            XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

            JavaFieldMetadata javaFieldMetadata = (JavaFieldMetadata) field.getMetadata( JavaFieldMetadata.ID );

            String fieldTagName = resolveTagName( field, xmlFieldMetadata );

            String type = field.getType();

            String value = uncapClassName + "." + getPrefix( javaFieldMetadata ) + capitalise( field.getName() ) + "()";

            if ( xmlFieldMetadata.isContent() )
            {
                contentField = field;
                contentValue = value;
                continue;
            }

            if ( xmlFieldMetadata.isAttribute() )
            {
                sc.add( getValueChecker( type, value, field ) );

                sc.add( "{" );
                sc.addIndented( "serializer.attribute( NAMESPACE, \"" + fieldTagName + "\", "
                                + getValue( field.getType(), value, xmlFieldMetadata ) + " );" );
                sc.add( "}" );
            }

        }

        if ( contentField != null )
        {
            XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) contentField.getMetadata( XmlFieldMetadata.ID );
            sc.add( "serializer.text( " + getValue( contentField.getType(), contentValue, xmlFieldMetadata ) + " );" );
        }

        // XML tags
        for ( ModelField field : modelFields )
        {
            XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

            if ( xmlFieldMetadata.isContent() )
            {
                // skip field with type Content
                continue;
            }

            JavaFieldMetadata javaFieldMetadata = (JavaFieldMetadata) field.getMetadata( JavaFieldMetadata.ID );

            String fieldTagName = resolveTagName( field, xmlFieldMetadata );

            String type = field.getType();

            String value = uncapClassName + "." + getPrefix( javaFieldMetadata ) + capitalise( field.getName() ) + "()";

            if ( xmlFieldMetadata.isAttribute() )
            {
                continue;
            }

            if ( field instanceof ModelAssociation )
            {
                ModelAssociation association = (ModelAssociation) field;

                String associationName = association.getName();

                if ( association.isOneMultiplicity() )
                {
                    sc.add( getValueChecker( type, value, association ) );

                    sc.add( "{" );
                    sc.addIndented( "write" + association.getTo() + "( (" + association.getTo() + ") " + value + ", \""
                                    + fieldTagName + "\", serializer );" );
                    sc.add( "}" );
                }
                else
                {
                    //MANY_MULTIPLICITY

                    XmlAssociationMetadata xmlAssociationMetadata =
                        (XmlAssociationMetadata) association.getAssociationMetadata( XmlAssociationMetadata.ID );

                    String valuesTagName = resolveTagName( fieldTagName, xmlAssociationMetadata );

                    type = association.getType();
                    String toType = association.getTo();

                    boolean wrappedItems = xmlAssociationMetadata.isWrappedItems();

                    if ( ModelDefault.LIST.equals( type ) || ModelDefault.SET.equals( type ) )
                    {
                        sc.add( getValueChecker( type, value, association ) );

                        sc.add( "{" );
                        sc.indent();

                        if ( wrappedItems )
                        {
                            sc.add( "serializer.startTag( NAMESPACE, " + "\"" + fieldTagName + "\" );" );
                        }

                        sc.add( "for ( Iterator iter = " + value + ".iterator(); iter.hasNext(); )" );

                        sc.add( "{" );
                        sc.indent();

                        if ( isClassInModel( association.getTo(), modelClass.getModel() ) )
                        {
                            sc.add( toType + " o = (" + toType + ") iter.next();" );

                            sc.add( "write" + toType + "( o, \"" + valuesTagName + "\", serializer );" );
                        }
                        else
                        {
                            sc.add( toType + " " + singular( uncapitalise( field.getName() ) ) + " = (" + toType
                                + ") iter.next();" );

                            sc.add( "serializer.startTag( NAMESPACE, " + "\"" + valuesTagName + "\" ).text( "
                                + singular( uncapitalise( field.getName() ) ) + " ).endTag( NAMESPACE, " + "\""
                                + valuesTagName + "\" );" );
                        }

                        sc.unindent();
                        sc.add( "}" );

                        if ( wrappedItems )
                        {
                            sc.add( "serializer.endTag( NAMESPACE, " + "\"" + fieldTagName + "\" );" );
                        }

                        sc.unindent();
                        sc.add( "}" );
                    }
                    else
                    {
                        //Map or Properties

                        sc.add( getValueChecker( type, value, field ) );

                        sc.add( "{" );
                        sc.indent();

                        if ( wrappedItems )
                        {
                            sc.add( "serializer.startTag( NAMESPACE, " + "\"" + fieldTagName + "\" );" );
                        }

                        sc.add( "for ( Iterator iter = " + value + ".keySet().iterator(); iter.hasNext(); )" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "String key = (String) iter.next();" );

                        sc.add( "String value = (String) " + value + ".get( key );" );

                        if ( xmlAssociationMetadata.isMapExplode() )
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

                        if ( wrappedItems )
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
                if ( "DOM".equals( field.getType() ) )
                {
                    if ( domAsXpp3 )
                    {
                        jClass.addImport( "org.codehaus.plexus.util.xml.Xpp3Dom" );
    
                        sc.addIndented( "((Xpp3Dom) " + value + ").writeToSerializer( NAMESPACE, serializer );" );
                    }
                    else
                    {
                        sc.addIndented( "writeDom( (org.w3c.dom.Element) " + value + ", serializer );" );
                    }

                    requiresDomSupport = true;
                }
                else
                {
                    sc.addIndented( "serializer.startTag( NAMESPACE, " + "\"" + fieldTagName + "\" ).text( "
                        + getValue( field.getType(), value, xmlFieldMetadata ) + " ).endTag( NAMESPACE, " + "\""
                        + fieldTagName + "\" );" );
                }
                sc.add( "}" );
            }
        }

        sc.add( "serializer.endTag( NAMESPACE, tagName );" );

        jClass.addMethod( marshall );
    }

    private void createWriteDomMethod( JClass jClass )
    {
        if ( domAsXpp3 )
        {
            return;
        }
        String type = "org.w3c.dom.Element";
        JMethod method = new JMethod( "writeDom" );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JType( type ), "dom" ) );
        method.addParameter( new JParameter( new JClass( "XmlSerializer" ), "serializer" ) );

        method.addException( new JClass( "java.io.IOException" ) );

        JSourceCode sc = method.getSourceCode();

        // start element
        sc.add( "serializer.startTag( NAMESPACE, dom.getTagName() );" );

        // attributes
        sc.add( "org.w3c.dom.NamedNodeMap attributes = dom.getAttributes();" );
        sc.add( "for ( int i = 0; i < attributes.getLength(); i++ )" );
        sc.add( "{" );

        sc.indent();
        sc.add( "org.w3c.dom.Node attribute = attributes.item( i );" );
        sc.add( "serializer.attribute( NAMESPACE, attribute.getNodeName(), attribute.getNodeValue() );" );
        sc.unindent();

        sc.add( "}" );

        // child nodes & text
        sc.add( "org.w3c.dom.NodeList children = dom.getChildNodes();" );
        sc.add( "for ( int i = 0; i < children.getLength(); i++ )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "org.w3c.dom.Node node = children.item( i );" );
        sc.add( "if ( node instanceof org.w3c.dom.Element)" );
        sc.add( "{" );
        sc.addIndented( "writeDom( (org.w3c.dom.Element) children.item( i ), serializer );" );
        sc.add( "}" );
        sc.add( "else" );
        sc.add( "{" );
        sc.addIndented( "serializer.text( node.getTextContent() );" );
        sc.add( "}" );
        sc.unindent();
        sc.add( "}" );

        sc.add( "serializer.endTag( NAMESPACE, dom.getTagName() );" );

        jClass.addMethod( method );
    }
}
