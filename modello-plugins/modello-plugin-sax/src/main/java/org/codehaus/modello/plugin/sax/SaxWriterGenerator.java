package org.codehaus.modello.plugin.sax;

/*
 * Copyright (c) 2013, Codehaus.org
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

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelDefault;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.plugin.java.javasource.JClass;
import org.codehaus.modello.plugin.java.javasource.JConstructor;
import org.codehaus.modello.plugin.java.javasource.JField;
import org.codehaus.modello.plugin.java.javasource.JMethod;
import org.codehaus.modello.plugin.java.javasource.JParameter;
import org.codehaus.modello.plugin.java.javasource.JSourceCode;
import org.codehaus.modello.plugin.java.javasource.JSourceWriter;
import org.codehaus.modello.plugin.java.javasource.JType;
import org.codehaus.modello.plugin.java.metadata.JavaFieldMetadata;
import org.codehaus.modello.plugin.model.ModelClassMetadata;
import org.codehaus.modello.plugins.xml.AbstractXmlJavaGenerator;
import org.codehaus.modello.plugins.xml.metadata.XmlAssociationMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlFieldMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlModelMetadata;

/**
 * @since 1.8
 * @author <a href="mailto:simonetripodi@apache.org">Simone Tripodi</a>
 */
public class SaxWriterGenerator
    extends AbstractXmlJavaGenerator
{

    private boolean requiresDomSupport;

    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

        requiresDomSupport = false;

        try
        {
            generateSaxWriter();
        }
        catch ( IOException ex )
        {
            throw new ModelloException( "Exception while generating SAX Writer.", ex );
        }
    }

    private void generateSaxWriter()
        throws ModelloException, IOException
    {
        Model objectModel = getModel();

        String packageName = objectModel.getDefaultPackageName( isPackageWithVersion(), getGeneratedVersion() )
            + ".io.sax";

        String marshallerName = getFileName( "SaxWriter" );

        JSourceWriter sourceWriter = newJSourceWriter( packageName, marshallerName );

        JClass jClass = new JClass( packageName + '.' + marshallerName );
        initHeader( jClass );
        suppressAllWarnings( objectModel, jClass );

        jClass.addImport( "java.io.IOException" );
        jClass.addImport( "java.io.OutputStream" );
        jClass.addImport( "java.io.OutputStreamWriter" );
        jClass.addImport( "java.io.UnsupportedEncodingException" );
        jClass.addImport( "java.io.Writer" );
        jClass.addImport( "java.util.Iterator" );
        jClass.addImport( "java.util.Properties" );
        jClass.addImport( "javax.xml.transform.OutputKeys" );
        jClass.addImport( "javax.xml.transform.TransformerException" );
        jClass.addImport( "javax.xml.transform.TransformerFactory" );
        jClass.addImport( "javax.xml.transform.sax.SAXTransformerFactory" );
        jClass.addImport( "javax.xml.transform.sax.TransformerHandler" );
        jClass.addImport( "javax.xml.transform.stream.StreamResult" );
        jClass.addImport( "org.xml.sax.ContentHandler" );
        jClass.addImport( "org.xml.sax.SAXException" );
        jClass.addImport( "org.xml.sax.helpers.AttributesImpl" );

        JField namespaceField = new JField( new JClass( "String" ), "NAMESPACE" );
        namespaceField.getModifiers().setFinal( true );
        namespaceField.getModifiers().setStatic( true );
        namespaceField.setInitString( "\"\"" );
        jClass.addField( namespaceField );

        JField factoryField = new JField( new JClass( "SAXTransformerFactory" ), "transformerFactory" );
        factoryField.getModifiers().setFinal( true );
        factoryField.setInitString( "(SAXTransformerFactory) TransformerFactory.newInstance()" );
        jClass.addField( factoryField );

        addModelImports( jClass, null );

        String root = objectModel.getRoot( getGeneratedVersion() );

        ModelClass rootClass = objectModel.getClass( root, getGeneratedVersion() );

        String rootElement = resolveTagName( rootClass );

        JConstructor saxWriterConstructor = new JConstructor( jClass );
        JSourceCode sc = saxWriterConstructor.getSourceCode();
        sc.add( "transformerFactory.setAttribute( \"indent-number\", 2 );" );

        jClass.addConstructor( saxWriterConstructor );

        // ----------------------------------------------------------------------
        // Write the write( Writer, Model ) method which will do the unmarshalling.
        // ----------------------------------------------------------------------

        JMethod marshall = new JMethod( "write" );

        String rootElementParameterName = uncapitalise( root );
        marshall.addParameter( new JParameter( new JClass( "Writer" ), "writer" ) );
        marshall.addParameter( new JParameter( new JClass( root ), rootElementParameterName ) );

        marshall.addException( new JClass( "SAXException" ) );
        marshall.addException( new JClass( "TransformerException" ) );

        sc = marshall.getSourceCode();

        sc.add( "TransformerHandler transformerHandler = transformerFactory.newTransformerHandler();" );

        sc.add( "Properties format = new Properties();" );
        sc.add( "format.put( OutputKeys.ENCODING, " + rootElementParameterName + ".getModelEncoding() );" );
        sc.add( "format.put( OutputKeys.INDENT, \"yes\" );" );
        sc.add( "format.put( OutputKeys.MEDIA_TYPE, \"text/xml\" );" );
        sc.add( "format.put( OutputKeys.METHOD, \"xml\" );" );

        sc.add( "transformerHandler.getTransformer().setOutputProperties( format );" );
        sc.add( "transformerHandler.setResult( new StreamResult( writer ) );" );

        sc.add( "write( transformerHandler, " + rootElementParameterName + " );" );

        jClass.addMethod( marshall );

        // ----------------------------------------------------------------------
        // Write the write( OutputStream, Model ) method which will do the unmarshalling.
        // ----------------------------------------------------------------------

        marshall = new JMethod( "write" );

        marshall.addParameter( new JParameter( new JClass( "OutputStream" ), "stream" ) );
        marshall.addParameter( new JParameter( new JClass( root ), rootElementParameterName ) );

        marshall.addException( new JClass( "SAXException" ) );
        marshall.addException( new JClass( "TransformerException" ) );
        marshall.addException( new JClass( "UnsupportedEncodingException" ) );

        sc = marshall.getSourceCode();

        sc.add( "write( new OutputStreamWriter( stream, " + rootElementParameterName + ".getModelEncoding() ), " + rootElementParameterName + " );" );

        jClass.addMethod( marshall );

        // ----------------------------------------------------------------------
        // Write the write( ContentHandler, Model ) method which will do the unmarshalling.
        // ----------------------------------------------------------------------

        marshall = new JMethod( "write" );

        marshall.addParameter( new JParameter( new JClass( "ContentHandler" ), "contentHandler" ) );
        marshall.addParameter( new JParameter( new JClass( root ), rootElementParameterName ) );

        marshall.addException( new JClass( "SAXException" ) );

        sc = marshall.getSourceCode();

        sc.add( "write( contentHandler, " + rootElementParameterName + ", true );" );

        jClass.addMethod( marshall );

        // ----------------------------------------------------------------------
        // Write the write( ContentHandler, Model, boolean ) method which will do the unmarshalling.
        // ----------------------------------------------------------------------

        marshall = new JMethod( "write" );

        marshall.addParameter( new JParameter( new JClass( "ContentHandler" ), "contentHandler" ) );
        marshall.addParameter( new JParameter( new JClass( root ), rootElementParameterName ) );
        marshall.addParameter( new JParameter( JType.BOOLEAN, "startDocument" ) );

        marshall.addException( new JClass( "SAXException" ) );

        sc = marshall.getSourceCode();

        sc.add( "if ( startDocument )" );
        sc.add( "{" );
        sc.addIndented( "contentHandler.startDocument();" );
        sc.add( "}" );

        sc.add( "AttributesImpl attributes = new AttributesImpl();" );

        sc.add( "write" + root + "( " + rootElementParameterName + ", \"" + rootElement + "\", contentHandler, attributes );" );

        sc.add( "if ( startDocument )" );
        sc.add( "{" );
        sc.addIndented( "contentHandler.endDocument();" );
        sc.add( "}" );

        jClass.addMethod( marshall );

        // ----------------------------------------------------------------------
        // Write the writeText( String, ContentHandler ) method which will help unmarshalling textual values.
        // ----------------------------------------------------------------------

        marshall = new JMethod( "writeText" );
        marshall.getModifiers().makePrivate();

        marshall.addParameter( new JParameter( new JClass( "String" ), "text" ) );
        marshall.addParameter( new JParameter( new JClass( "ContentHandler" ), "contentHandler" ) );

        marshall.addException( new JClass( "SAXException" ) );

        sc = marshall.getSourceCode();

        sc.add( "contentHandler.characters( text.toCharArray(), 0, text.length() );" );

        // generate the code

        jClass.addMethod( marshall );

        writeAllClasses( objectModel, jClass );

        if ( requiresDomSupport )
        {
            createWriteDomMethod( jClass );
            createXpp3DomMethod( jClass );
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
        marshall.addParameter( new JParameter( new JClass( "ContentHandler" ), "contentHandler" ) );
        marshall.addParameter( new JParameter( new JClass( "AttributesImpl" ), "attributes" ) );

        marshall.addException( new JClass( "SAXException" ) );

        marshall.getModifiers().makePrivate();

        JSourceCode sc = marshall.getSourceCode();

        ModelClassMetadata classMetadata = (ModelClassMetadata) modelClass.getMetadata( ModelClassMetadata.ID );

        String namespace = null;
        XmlModelMetadata xmlModelMetadata = (XmlModelMetadata) modelClass.getModel().getMetadata( XmlModelMetadata.ID );

        ModelField contentField = null;

        String contentValue = null;

        List<ModelField> modelFields = getFieldsForXml( modelClass, getGeneratedVersion() );

        boolean needsToCleanAttributes = false;

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
                sc.addIndented( "attributes.addAttribute( NAMESPACE, \""
                                + fieldTagName
                                + "\", \""
                                + fieldTagName
                                + "\", \"CDATA\", "
                                + getValue( field.getType(), value, xmlFieldMetadata )
                                + " );" );
                sc.add( "}" );

                needsToCleanAttributes = true;
            }

        }

        // add namespace information for root element only
        if ( classMetadata.isRootElement() && ( xmlModelMetadata.getNamespace() != null ) )
        {
            namespace = xmlModelMetadata.getNamespace( getGeneratedVersion() );
            sc.add( "contentHandler.startPrefixMapping( \"\", \"" + namespace + "\" );" );
        }

        if ( ( namespace != null ) && ( xmlModelMetadata.getSchemaLocation() != null ) )
        {
            String url = xmlModelMetadata.getSchemaLocation( getGeneratedVersion() );

            sc.add( "contentHandler.startPrefixMapping( \"xsi\", \"http://www.w3.org/2001/XMLSchema-instance\" );" );

            sc.add( "attributes.addAttribute( \"http://www.w3.org/2001/XMLSchema-instance\", \"schemaLocation\", \"xsi:schemaLocation\", \"CDATA\", \"" + namespace + " " + url + "\" );" );
        }

        sc.add( "contentHandler.startElement( NAMESPACE, tagName, tagName, attributes );" );

        if ( needsToCleanAttributes )
        {
            sc.add( "attributes.clear();" );
        }

        if ( contentField != null )
        {
            XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) contentField.getMetadata( XmlFieldMetadata.ID );
            sc.add( "writeText( " + getValue( contentField.getType(), contentValue, xmlFieldMetadata ) + ", contentHandler );" );
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
                                    + fieldTagName + "\", contentHandler, attributes );" );
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
                            sc.add( "contentHandler.startElement( NAMESPACE, \"" + fieldTagName + "\", \"" + fieldTagName + "\", attributes );" );
                        }

                        sc.add( "for ( Iterator iter = " + value + ".iterator(); iter.hasNext(); )" );

                        sc.add( "{" );
                        sc.indent();

                        if ( isClassInModel( association.getTo(), modelClass.getModel() ) )
                        {
                            sc.add( toType + " o = (" + toType + ") iter.next();" );

                            sc.add( "write" + toType + "( o, \"" + valuesTagName + "\", contentHandler, attributes );" );
                        }
                        else
                        {
                            sc.add( toType + " " + singular( uncapitalise( field.getName() ) ) + " = (" + toType
                                + ") iter.next();" );

                            sc.add( "contentHandler.startElement( NAMESPACE, \"" + valuesTagName + "\", \"" + valuesTagName + "\", attributes );" );
                            sc.add( "writeText( " + singular( uncapitalise( field.getName() ) ) + ", contentHandler );" );
                            sc.add( "contentHandler.endElement( NAMESPACE, \"" + valuesTagName + "\", \"" + valuesTagName + "\" );" );
                        }

                        sc.unindent();
                        sc.add( "}" );

                        if ( wrappedItems )
                        {
                            sc.add( "contentHandler.endElement( NAMESPACE, \"" + fieldTagName + "\", \"" + fieldTagName + "\" );" );
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
                            sc.add( "contentHandler.startElement( NAMESPACE, \"" + fieldTagName + "\", \"" + fieldTagName + "\", attributes );" );
                        }

                        sc.add( "for ( Iterator iter = " + value + ".keySet().iterator(); iter.hasNext(); )" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "String key = (String) iter.next();" );

                        sc.add( association.getTo() + " value = (" + association.getTo() + ") " + value + ".get( key );" );

                        if ( xmlAssociationMetadata.isMapExplode() )
                        {
                            sc.add( "contentHandler.startElement( NAMESPACE, \"" + singular( associationName ) + "\", \"" + singular( associationName ) + "\", attributes );" );

                            sc.add( "contentHandler.startElement( NAMESPACE, \"key\", \"key\", attributes );" );
                            sc.add( "writeText( key, contentHandler );" );
                            sc.add( "contentHandler.endElement( NAMESPACE, \"key\", \"key\" );" );

                            if ( isClassInModel( association.getTo(), association.getModelClass().getModel() ) )
                            {
                                sc.add( "write" + association.getTo() + "( value, \"value\", contentHandler, attributes );" );
                            }
                            else
                            {
                                sc.add( "contentHandler.startElement( NAMESPACE, \"value\", \"value\", attributes );" );
                                sc.add( "writeText( " + getValue( association.getTo(), "value", xmlFieldMetadata ) + ", contentHandler );" );
                                sc.add( "contentHandler.endElement( NAMESPACE, \"value\", \"value\" );" );
                            }

                            sc.add( "contentHandler.endElement( NAMESPACE, \"" + singular( associationName ) + "\", \"" + singular( associationName ) + "\" );" );
                        }
                        else
                        {
                            if ( isClassInModel( association.getTo(), association.getModelClass().getModel() ) )
                            {
                                sc.add( "write" + association.getTo() + "( value, key, contentHandler, attributes );" );
                            }
                            else
                            {
                                sc.add( "contentHandler.startElement( NAMESPACE, key, key, attributes );" );
                                sc.add( "writeText( " + getValue( association.getTo(), "value", xmlFieldMetadata ) + ", contentHandler );" );
                                sc.add( "contentHandler.endElement( NAMESPACE, key, key );" );
                            }
                        }

                        sc.unindent();
                        sc.add( "}" );

                        if ( wrappedItems )
                        {
                            sc.add( "contentHandler.endElement( NAMESPACE, \"" + fieldTagName + "\", \"" + fieldTagName + "\" );" );
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
                        sc.addIndented( "writeDom( (Xpp3Dom) " + value + ", contentHandler, attributes );" );
                    }
                    else
                    {
                        sc.addIndented( "writeDom( (Element) " + value + ", contentHandler );" );
                    }

                    requiresDomSupport = true;
                }
                else
                {
                    sc.indent();
                    sc.add( "contentHandler.startElement( NAMESPACE, \"" + fieldTagName + "\", \"" + fieldTagName + "\", attributes );" );
                    sc.add( "writeText( " + getValue( field.getType(), value, xmlFieldMetadata ) + ", contentHandler );" );
                    sc.add( "contentHandler.endElement( NAMESPACE, \"" + fieldTagName + "\", \"" + fieldTagName + "\" );" );
                    sc.unindent();
                }
                sc.add( "}" );
            }
        }

        sc.add( "contentHandler.endElement( NAMESPACE, tagName, tagName );" );

        jClass.addMethod( marshall );
    }

    private void createXpp3DomMethod( JClass jClass )
    {
        jClass.addImport( "org.codehaus.plexus.util.xml.Xpp3Dom" );

        JMethod method = new JMethod( "writeDom" );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JType( "Xpp3Dom" ), "dom" ) );
        method.addParameter( new JParameter( new JClass( "ContentHandler" ), "contentHandler" ) );
        method.addParameter( new JParameter( new JClass( "AttributesImpl" ), "attributes" ) );

        method.addException( new JClass( "SAXException" ) );

        JSourceCode sc = method.getSourceCode();

        sc.add( "String[] attributeNames = dom.getAttributeNames();" );
        sc.add( "if ( attributeNames != null && attributeNames.length > 0 )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "String attributeName;" );
        sc.add( "String attributeValue;" );
        sc.add( "for ( int i = 0; i < attributeNames.length; i++ )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "attributeName = attributeNames[i];" );
        sc.add( "attributeValue = dom.getAttribute( attributeName );" );
        sc.add( "attributes.addAttribute( NAMESPACE, attributeName, attributeName, \"CDATA\", attributeValue );" );
        sc.unindent();
        sc.add( "}" );
        sc.unindent();
        sc.add( "}" );

        sc.add( "contentHandler.startElement( NAMESPACE, dom.getName(), dom.getName(), attributes );" );

        sc.add( "if ( attributeNames != null && attributeNames.length > 0 )" );
        sc.add( "{" );
        sc.addIndented( "attributes.clear();" );
        sc.add( "}" );

        sc.add( "Xpp3Dom[] children = dom.getChildren();" );
        sc.add( "if ( children != null &&  children.length > 0 )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "for ( int i = 0; i < children.length; i++ )" );
        sc.add( "{" );
        sc.addIndented( "writeDom( children[i], contentHandler, attributes );" );
        sc.add( "}" );
        sc.unindent();
        sc.add( "}" );

        sc.add( "String value = dom.getValue();" );
        sc.add( "if ( value != null )" );
        sc.add( "{" );
        sc.addIndented( "writeText( value, contentHandler );" );
        sc.add( "}" );
        sc.add( "contentHandler.endElement( NAMESPACE, dom.getName(), dom.getName() );" );

        jClass.addMethod( method );
    }

    private void createWriteDomMethod( JClass jClass )
    {
        jClass.addImport( "org.w3c.dom.Element" );
        jClass.addImport( "javax.xml.transform.Transformer" );
        jClass.addImport( "javax.xml.transform.dom.DOMSource" );
        jClass.addImport( "javax.xml.transform.sax.SAXResult" );

        JMethod method = new JMethod( "writeDom" );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JType( "Element" ), "dom" ) );
        method.addParameter( new JParameter( new JClass( "ContentHandler" ), "contentHandler" ) );

        method.addException( new JClass( "SAXException" ) );

        JSourceCode sc = method.getSourceCode();

        sc.add( "try" );
        sc.add( "{" );
        sc.indent();
        sc.add( "Transformer transformer = transformerFactory.newTransformer();" );
        sc.add( "DOMSource source = new DOMSource( dom );" );
        sc.add( "SAXResult result = new SAXResult( contentHandler );" );
        sc.add( "transformer.transform( source, result );" );
        sc.unindent();
        sc.add( "}" );

        sc.add( "catch ( TransformerException e )" );
        sc.add( "{" );
        sc.addIndented( "throw new SAXException( \"Impossible to convert DOM element, see nested exceptions.\", e );" );
        sc.add( "}" );

        jClass.addMethod( method );
    }

}
