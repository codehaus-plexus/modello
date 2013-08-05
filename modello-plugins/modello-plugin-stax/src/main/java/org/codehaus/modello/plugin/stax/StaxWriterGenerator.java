package org.codehaus.modello.plugin.stax;

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
import org.codehaus.modello.plugin.java.javasource.JConstructor;
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
public class StaxWriterGenerator
    extends AbstractStaxGenerator
{

    private boolean requiresDomSupport;

    private StaxSerializerGenerator serializerGenerator;

    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

        requiresDomSupport = false;

        try
        {
            generateStaxWriter();
        }
        catch ( IOException ex )
        {
            throw new ModelloException( "Exception while generating StAX Writer.", ex );
        }
        
        serializerGenerator.generate( model, parameters );
    }

    private void generateStaxWriter()
        throws ModelloException, IOException
    {
        Model objectModel = getModel();

        String packageName = objectModel.getDefaultPackageName( isPackageWithVersion(), getGeneratedVersion() )
            + ".io.stax";

        String marshallerName = getFileName( "StaxWriter" );

        JSourceWriter sourceWriter = newJSourceWriter( packageName, marshallerName );

        JClass jClass = new JClass( packageName + '.' + marshallerName );
        initHeader( jClass );
        suppressAllWarnings( objectModel, jClass );

        jClass.addImport( "java.io.IOException" );
        jClass.addImport( "java.io.OutputStream" );
        jClass.addImport( "java.io.Writer" );
        jClass.addImport( "java.io.StringWriter" );
        jClass.addImport( "java.text.DateFormat" );
        jClass.addImport( "java.util.Iterator" );
        jClass.addImport( "java.util.Locale" );
        jClass.addImport( "java.util.jar.Manifest" );
        jClass.addImport( "javax.xml.stream.*" );

        addModelImports( jClass, null );

        jClass.addField( new JField( JType.INT, "curId" ) );
        jClass.addField( new JField( new JType( "java.util.Map" ), "idMap" ) );
        JConstructor constructor = new JConstructor( jClass );
        constructor.getSourceCode().add( "idMap = new java.util.HashMap();" );
        jClass.addConstructor( constructor );

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
        marshall.addException( new JClass( "XMLStreamException" ) );

        JSourceCode sc = marshall.getSourceCode();

        sc.add( "XMLOutputFactory factory = XMLOutputFactory.newInstance();" );

        // currently, only woodstox supports Windows line endings. It works with Java 6/RI and stax <= 1.1.1 as well
        // but we have no way to detect them
        sc.add( "boolean supportWindowsLineEndings = false;" );
        sc.add( "if ( factory.isPropertySupported( \"com.ctc.wstx.outputEscapeCr\" ) )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "factory.setProperty( \"com.ctc.wstx.outputEscapeCr\", Boolean.FALSE );" );
        sc.add( "supportWindowsLineEndings = true;" );
        sc.unindent();
        sc.add( "}" );

        sc.add( "if ( factory.isPropertySupported( \"org.codehaus.stax2.automaticEmptyElements\" ) )" );
        sc.add( "{" );
        sc.addIndented( "factory.setProperty( \"org.codehaus.stax2.automaticEmptyElements\", Boolean.FALSE );" );
        sc.add( "}" );

        sc.add(
            "IndentingXMLStreamWriter serializer = new IndentingXMLStreamWriter( factory.createXMLStreamWriter( writer ) );" );

        sc.add( "if ( supportWindowsLineEndings )" );
        sc.add( "{" );
        sc.addIndented( "serializer.setNewLine( serializer.getLineSeparator() );" );
        sc.add( "}" );

        sc.add( "serializer.writeStartDocument( " + rootElementParameterName + ".getModelEncoding(), \"1.0\" );" );

        sc.add( "write" + root + "( " + rootElementParameterName + ", \"" + rootElement + "\", serializer );" );

        sc.add( "serializer.writeEndDocument();" );

        jClass.addMethod( marshall );

        // ----------------------------------------------------------------------
        // Write the write( OutputStream, Model ) method which will do the unmarshalling.
        // ----------------------------------------------------------------------

        marshall = new JMethod( "write" );

        marshall.addParameter( new JParameter( new JClass( "OutputStream" ), "stream" ) );
        marshall.addParameter( new JParameter( new JClass( root ), rootElementParameterName ) );

        marshall.addException( new JClass( "java.io.IOException" ) );
        marshall.addException( new JClass( "XMLStreamException" ) );

        sc = marshall.getSourceCode();

        sc.add( "XMLOutputFactory factory = XMLOutputFactory.newInstance();" );

        // currently, only woodstox supports Windows line endings. It works with Java 6/RI and stax <= 1.1.1 as well
        // but we have no way to detect them
        sc.add( "boolean supportWindowsLineEndings = false;" );
        sc.add( "if ( factory.isPropertySupported( \"com.ctc.wstx.outputEscapeCr\" ) )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "factory.setProperty( \"com.ctc.wstx.outputEscapeCr\", Boolean.FALSE );" );
        sc.add( "supportWindowsLineEndings = true;" );
        sc.unindent();
        sc.add( "}" );

        sc.add( "if ( factory.isPropertySupported( \"org.codehaus.stax2.automaticEmptyElements\" ) )" );
        sc.add( "{" );
        sc.addIndented( "factory.setProperty( \"org.codehaus.stax2.automaticEmptyElements\", Boolean.FALSE );" );
        sc.add( "}" );

        sc.add( "IndentingXMLStreamWriter serializer = new IndentingXMLStreamWriter( factory.createXMLStreamWriter( stream, "
            + rootElementParameterName + ".getModelEncoding() ) );" );

        sc.add( "if ( supportWindowsLineEndings )" );
        sc.add( "{" );
        sc.addIndented( "serializer.setNewLine( serializer.getLineSeparator() );" );
        sc.add( "}" );

        sc.add( "serializer.writeStartDocument( " + rootElementParameterName + ".getModelEncoding(), \"1.0\" );" );

        sc.add( "write" + root + "( " + rootElementParameterName + ", \"" + rootElement + "\", serializer );" );

        sc.add( "serializer.writeEndDocument();" );

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
        marshall.getModifiers().makePrivate();

        marshall.addParameter( new JParameter( new JClass( className ), uncapClassName ) );
        marshall.addParameter( new JParameter( new JClass( "String" ), "tagName" ) );
        marshall.addParameter( new JParameter( new JClass( "XMLStreamWriter" ), "serializer" ) );

        marshall.addException( new JClass( "java.io.IOException" ) );
        marshall.addException( new JClass( "XMLStreamException" ) );

        JSourceCode sc = marshall.getSourceCode();

        sc.add( "if ( " + uncapClassName + " != null )" );

        sc.add( "{" );
        sc.indent();

        ModelClassMetadata classMetadata = (ModelClassMetadata) modelClass.getMetadata( ModelClassMetadata.ID );

        String namespace = null;
        XmlModelMetadata xmlModelMetadata = (XmlModelMetadata) modelClass.getModel().getMetadata( XmlModelMetadata.ID );

        // add namespace information for root element only
        if ( classMetadata.isRootElement() && ( xmlModelMetadata.getNamespace() != null ) )
        {
            namespace = xmlModelMetadata.getNamespace( getGeneratedVersion() );
            sc.add( "serializer.setDefaultNamespace( \"" + namespace + "\" );" );
        }

        sc.add( "serializer.writeStartElement( tagName );" );

        if ( namespace != null )
        {
            sc.add( "serializer.writeDefaultNamespace( \"" + namespace + "\" );" );

            if ( xmlModelMetadata.getSchemaLocation() != null )
            {
                String url = xmlModelMetadata.getSchemaLocation( getGeneratedVersion() );

                sc.add( "serializer.setPrefix( \"xsi\", \"http://www.w3.org/2001/XMLSchema-instance\" );" );
                sc.add( "serializer.writeNamespace( \"xsi\", \"http://www.w3.org/2001/XMLSchema-instance\" );" );
                sc.add( "serializer.writeAttribute( \"http://www.w3.org/2001/XMLSchema-instance\", \"schemaLocation\", \""
                    + namespace + " " + url + "\" );" );
            }
        }

        if ( isAssociationPartToClass( modelClass ) )
        {
            if ( modelClass.getIdentifierFields( getGeneratedVersion() ).size() != 1 )
            {
                writeIdMapCheck( sc, uncapClassName, "modello.id" );
            }
        }

        ModelField contentField = null;

        String contentValue = null;

        List<ModelField> modelFields = getFieldsForXml( modelClass, getGeneratedVersion() );

        // XML attributes
        for ( ModelField field : modelFields )
        {
            XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

            String fieldTagName = resolveTagName( field, xmlFieldMetadata );

            String type = field.getType();

            String value = getFieldValue( uncapClassName, field );

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
                sc.addIndented( "serializer.writeAttribute( \"" + fieldTagName + "\", "
                    + getValue( field.getType(), value, xmlFieldMetadata ) + " );" );
                sc.add( "}" );
            }
        }

        if ( contentField != null )
        {
            XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) contentField.getMetadata( XmlFieldMetadata.ID );
            sc.add( "serializer.writeCharacters( " + getValue( contentField.getType(), contentValue, xmlFieldMetadata ) + " );" );
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

            String fieldTagName = resolveTagName( field, xmlFieldMetadata );

            String type = field.getType();

            String value = getFieldValue( uncapClassName, field );

            if ( xmlFieldMetadata.isAttribute() )
            {
                continue;
            }

            if ( field instanceof ModelAssociation )
            {
                ModelAssociation association = (ModelAssociation) field;

                String associationName = association.getName();

                ModelField referenceIdentifierField = getReferenceIdentifierField( association );

                if ( association.isOneMultiplicity() )
                {
                    sc.add( getValueChecker( type, value, association ) );
                    sc.add( "{" );
                    sc.indent();

                    if ( referenceIdentifierField != null )
                    {
                        // if xml.reference, then store as a reference instead

                        sc.add( "serializer.writeStartElement( \"" + fieldTagName + "\" );" );

                        writeElementAttribute( sc, referenceIdentifierField, value );

                        sc.add( "serializer.writeEndElement();" );
                    }
                    else
                    {
                        sc.add( "write" + association.getTo() + "( (" + association.getTo() + ") " + value + ", \""
                            + fieldTagName + "\", serializer );" );
                    }

                    sc.unindent();
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
                            sc.add( "serializer.writeStartElement( " + "\"" + fieldTagName + "\" );" );
                        }

                        sc.add( "for ( Iterator iter = " + value + ".iterator(); iter.hasNext(); )" );

                        sc.add( "{" );
                        sc.indent();

                        if ( isClassInModel( association.getTo(), modelClass.getModel() ) )
                        {
                            sc.add( toType + " o = (" + toType + ") iter.next();" );

                            if ( referenceIdentifierField != null )
                            {
                                sc.add( "serializer.writeStartElement( \"" + valuesTagName + "\" );" );

                                writeElementAttribute( sc, referenceIdentifierField, "o" );

                                sc.add( "serializer.writeEndElement();" );
                            }
                            else
                            {
                                sc.add( "write" + toType + "( o, \"" + valuesTagName + "\", serializer );" );
                            }
                        }
                        else
                        {
                            sc.add( toType + " " + singular( uncapitalise( field.getName() ) ) + " = (" + toType
                                + ") iter.next();" );

                            sc.add( "serializer.writeStartElement( " + "\"" + valuesTagName + "\" );" );
                            sc.add(
                                "serializer.writeCharacters( " + singular( uncapitalise( field.getName() ) ) + " );" );
                            sc.add( "serializer.writeEndElement();" );
                        }

                        sc.unindent();
                        sc.add( "}" );

                        if ( wrappedItems )
                        {
                            sc.add( "serializer.writeEndElement();" );
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
                            sc.add( "serializer.writeStartElement( " + "\"" + fieldTagName + "\" );" );
                        }

                        sc.add( "for ( Iterator iter = " + value + ".keySet().iterator(); iter.hasNext(); )" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "String key = (String) iter.next();" );

                        sc.add( "String value = (String) " + value + ".get( key );" );

                        if ( xmlAssociationMetadata.isMapExplode() )
                        {
                            sc.add( "serializer.writeStartElement( \"" + singular( associationName ) + "\" );" );
                            sc.add( "serializer.writeStartElement( \"key\" );" );
                            sc.add( "serializer.writeCharacters( key );" );
                            sc.add( "serializer.writeEndElement();" );
                            sc.add( "serializer.writeStartElement( \"value\" );" );
                            sc.add( "serializer.writeCharacters( value );" );
                            sc.add( "serializer.writeEndElement();" );
                            sc.add( "serializer.writeEndElement();" );
                        }
                        else
                        {
                            sc.add( "serializer.writeStartElement( \"\" + key + \"\" );" );
                            sc.add( "serializer.writeCharacters( value );" );
                            sc.add( "serializer.writeEndElement();" );
                        }

                        sc.unindent();
                        sc.add( "}" );

                        if ( wrappedItems )
                        {
                            sc.add( "serializer.writeEndElement();" );
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
                    sc.add( "writeDom( (" + ( domAsXpp3 ? "Xpp3Dom" : "org.w3c.dom.Element" ) + ") " + value
                        + ", serializer );" );

                    requiresDomSupport = true;
                }
                else
                {
                    sc.add( "serializer.writeStartElement( " + "\"" + fieldTagName + "\" );" );
                    sc.add(
                        "serializer.writeCharacters( " + getValue( field.getType(), value, xmlFieldMetadata ) + " );" );
                    sc.add( "serializer.writeEndElement();" );
                }

                sc.unindent();
                sc.add( "}" );
            }
        }

        sc.add( "serializer.writeEndElement();" );

        sc.unindent();
        sc.add( "}" );

        jClass.addMethod( marshall );
    }

    private void writeElementAttribute( JSourceCode sc, ModelField referenceIdentifierField, String value )
    {
        if ( referenceIdentifierField instanceof DummyIdModelField )
        {
            writeIdMapCheck( sc, value, referenceIdentifierField.getName() );
        }
        else
        {
            String v = getValue( referenceIdentifierField.getType(), getFieldValue( value, referenceIdentifierField ),
                                 (XmlFieldMetadata) referenceIdentifierField.getMetadata( XmlFieldMetadata.ID ) );
            sc.add( "serializer.writeAttribute( \"" + referenceIdentifierField.getName() + "\", " + v + " );" );
        }
    }

    private static void writeIdMapCheck( JSourceCode sc, String value, String attributeName )
    {
        sc.add( "if ( !idMap.containsKey( " + value + " ) )" );
        sc.add( "{" );
        sc.indent();

        sc.add( "++curId;" );
        sc.add( "String id = String.valueOf( curId );" );
        sc.add( "idMap.put( " + value + ", id );" );
        sc.add( "serializer.writeAttribute( \"" + attributeName + "\", id );" );

        sc.unindent();
        sc.add( "}" );
        sc.add( "else" );
        sc.add( "{" );
        sc.addIndented( "serializer.writeAttribute( \"" + attributeName + "\", (String) idMap.get( " + value + " ) );" );
        sc.add( "}" );
    }

    private String getFieldValue( String uncapClassName, ModelField field )
    {
        JavaFieldMetadata javaFieldMetadata = (JavaFieldMetadata) field.getMetadata( JavaFieldMetadata.ID );

        return uncapClassName + "." + getPrefix( javaFieldMetadata ) + capitalise( field.getName() ) + "()";
    }

    private void createWriteDomMethod( JClass jClass )
    {
        if ( domAsXpp3 )
        {
            jClass.addImport( "org.codehaus.plexus.util.xml.Xpp3Dom" );
        }
        String type = domAsXpp3 ? "Xpp3Dom" : "org.w3c.dom.Element";
        JMethod method = new JMethod( "writeDom" );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JType( type ), "dom" ) );
        method.addParameter( new JParameter( new JType( "XMLStreamWriter" ), "serializer" ) );

        method.addException( new JClass( "XMLStreamException" ) );

        JSourceCode sc = method.getSourceCode();

        // start element
        sc.add( "serializer.writeStartElement( dom.get" + ( domAsXpp3 ? "Name" : "TagName" ) + "() );" );

        // attributes
        if ( domAsXpp3 )
        {
            sc.add( "String[] attributeNames = dom.getAttributeNames();" );
            sc.add( "for ( int i = 0; i < attributeNames.length; i++ )" );
            sc.add( "{" );
    
            sc.indent();
            sc.add( "String attributeName = attributeNames[i];" );
            sc.add( "serializer.writeAttribute( attributeName, dom.getAttribute( attributeName ) );" );
            sc.unindent();
    
            sc.add( "}" );
        }
        else
        {
            sc.add( "org.w3c.dom.NamedNodeMap attributes = dom.getAttributes();" );
            sc.add( "for ( int i = 0; i < attributes.getLength(); i++ )" );
            sc.add( "{" );
    
            sc.indent();
            sc.add( "org.w3c.dom.Node attribute = attributes.item( i );" );
            sc.add( "serializer.writeAttribute( attribute.getNodeName(), attribute.getNodeValue() );" );
            sc.unindent();
    
            sc.add( "}" );
        }

        // child nodes & text
        if ( domAsXpp3 )
        {
            sc.add( "Xpp3Dom[] children = dom.getChildren();" );
            sc.add( "for ( int i = 0; i < children.length; i++ )" );
            sc.add( "{" );
            sc.addIndented( "writeDom( children[i], serializer );" );
            sc.add( "}" );

            sc.add( "String value = dom.getValue();" );
            sc.add( "if ( value != null )" );
            sc.add( "{" );
            sc.addIndented( "serializer.writeCharacters( value );" );
            sc.add( "}" );
        }
        else
        {
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
            sc.addIndented( "serializer.writeCharacters( node.getTextContent() );" );
            sc.add( "}" );
            sc.unindent();
            sc.add( "}" );
        }

        sc.add( "serializer.writeEndElement();" );

        jClass.addMethod( method );
    }
}
