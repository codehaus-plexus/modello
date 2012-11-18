package org.codehaus.modello.plugin.dom4j;

/*
 * Copyright (c) 2006, Codehaus.
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
import org.codehaus.modello.plugin.java.javasource.JMethod;
import org.codehaus.modello.plugin.java.javasource.JParameter;
import org.codehaus.modello.plugin.java.javasource.JSourceCode;
import org.codehaus.modello.plugin.java.javasource.JSourceWriter;
import org.codehaus.modello.plugin.java.javasource.JType;
import org.codehaus.modello.plugins.xml.AbstractXmlJavaGenerator;
import org.codehaus.modello.plugins.xml.metadata.XmlAssociationMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlFieldMetadata;
import org.codehaus.plexus.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Generator that reads a model using dom4j.
 * TODO: chunks are lifted from xpp3, including the tests. Can we abstract it in some way?
 *
 * @author <a href="mailto:brett@codehaus.org">Brett Porter</a>
 */
public class Dom4jReaderGenerator
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
            generateDom4jReader();
        }
        catch ( IOException ex )
        {
            throw new ModelloException( "Exception while generating Dom4j Reader.", ex );
        }
    }

    private void generateDom4jReader()
        throws ModelloException, IOException
    {
        Model objectModel = getModel();

        String packageName = objectModel.getDefaultPackageName( isPackageWithVersion(), getGeneratedVersion() )
            + ".io.dom4j";

        String unmarshallerName = getFileName( "Dom4jReader" );

        JSourceWriter sourceWriter = newJSourceWriter( packageName, unmarshallerName );

        JClass jClass = new JClass( packageName + '.' + unmarshallerName );
        initHeader( jClass );
        suppressAllWarnings( objectModel, jClass );

        jClass.addImport( "java.io.InputStream" );
        jClass.addImport( "java.io.IOException" );
        jClass.addImport( "java.io.Reader" );
        jClass.addImport( "java.net.URL" );
        jClass.addImport( "java.util.Date" );
        jClass.addImport( "java.util.Locale" );
        jClass.addImport( "java.text.DateFormat" );
        jClass.addImport( "java.text.ParsePosition" );
        jClass.addImport( "java.util.Iterator" );
        jClass.addImport( "org.dom4j.Attribute" );
        jClass.addImport( "org.dom4j.Document" );
        jClass.addImport( "org.dom4j.DocumentException" );
        jClass.addImport( "org.dom4j.Element" );
        jClass.addImport( "org.dom4j.Node" );
        jClass.addImport( "org.dom4j.io.SAXReader" );

        addModelImports( jClass, null );

        ModelClass root = objectModel.getClass( objectModel.getRoot( getGeneratedVersion() ), getGeneratedVersion() );
        JClass rootType = new JClass( root.getName() );

        // ----------------------------------------------------------------------
        // Write the read(XMLStreamReader,boolean) method which will do the unmarshalling.
        // ----------------------------------------------------------------------

        JMethod unmarshall = new JMethod( "read", rootType, null );
        unmarshall.getModifiers().makePrivate();

        unmarshall.addParameter( new JParameter( new JClass( "Document" ), "document" ) );
        unmarshall.addParameter( new JParameter( JType.BOOLEAN, "strict" ) );

        unmarshall.addException( new JClass( "IOException" ) );
        unmarshall.addException( new JClass( "DocumentException" ) );

        JSourceCode sc = unmarshall.getSourceCode();

        String className = root.getName();
        String variableName = uncapitalise( className );

        sc.add( "String encoding = document.getXMLEncoding();" );

        sc.add( className + ' ' + variableName + " = parse" + root.getName() + "( \"" + resolveTagName( root )
                + "\", document.getRootElement(), strict );" );

        sc.add( variableName + ".setModelEncoding( encoding );" );

        sc.add( "return " + variableName + ";" );

        jClass.addMethod( unmarshall );

        // ----------------------------------------------------------------------
        // Write the read(Reader[,boolean]) methods which will do the unmarshalling.
        // ----------------------------------------------------------------------

        unmarshall = new JMethod( "read", rootType, null );

        unmarshall.addParameter( new JParameter( new JClass( "Reader" ), "reader" ) );
        unmarshall.addParameter( new JParameter( JType.BOOLEAN, "strict" ) );

        unmarshall.addException( new JClass( "IOException" ) );
        unmarshall.addException( new JClass( "DocumentException" ) );

        sc = unmarshall.getSourceCode();

        sc.add( "SAXReader parser = new SAXReader();" );

        sc.add( "Document document = parser.read( reader );" );

        sc.add( "return read( document, strict );" );

        jClass.addMethod( unmarshall );

        // ----------------------------------------------------------------------

        unmarshall = new JMethod( "read", rootType, null );

        unmarshall.addParameter( new JParameter( new JClass( "Reader" ), "reader" ) );

        unmarshall.addException( new JClass( "IOException" ) );
        unmarshall.addException( new JClass( "DocumentException" ) );

        sc = unmarshall.getSourceCode();

        sc.add( "return read( reader, true );" );

        jClass.addMethod( unmarshall );

        // ----------------------------------------------------------------------
        // Write the read(InputStream[,boolean]) methods which will do the unmarshalling.
        // ----------------------------------------------------------------------

        unmarshall = new JMethod( "read", rootType, null );

        unmarshall.addParameter( new JParameter( new JClass( "InputStream" ), "stream" ) );
        unmarshall.addParameter( new JParameter( JType.BOOLEAN, "strict" ) );

        unmarshall.addException( new JClass( "IOException" ) );
        unmarshall.addException( new JClass( "DocumentException" ) );

        sc = unmarshall.getSourceCode();

        sc.add( "SAXReader parser = new SAXReader();" );

        sc.add( "Document document = parser.read( stream );" );

        sc.add( "return read( document, strict );" );

        jClass.addMethod( unmarshall );

        // ----------------------------------------------------------------------

        unmarshall = new JMethod( "read", rootType, null );

        unmarshall.addParameter( new JParameter( new JClass( "InputStream" ), "stream" ) );

        unmarshall.addException( new JClass( "IOException" ) );
        unmarshall.addException( new JClass( "DocumentException" ) );

        sc = unmarshall.getSourceCode();

        sc.add( "return read( stream, true );" );

        jClass.addMethod( unmarshall );

        // ----------------------------------------------------------------------
        // Write the read(URL[,boolean]) methods which will do the unmarshalling.
        // ----------------------------------------------------------------------

        unmarshall = new JMethod( "read", rootType, null );

        unmarshall.addParameter( new JParameter( new JClass( "URL" ), "url" ) );
        unmarshall.addParameter( new JParameter( JType.BOOLEAN, "strict" ) );

        unmarshall.addException( new JClass( "IOException" ) );
        unmarshall.addException( new JClass( "DocumentException" ) );

        sc = unmarshall.getSourceCode();

        sc.add( "SAXReader parser = new SAXReader();" );

        sc.add( "Document document = parser.read( url );" );

        sc.add( "return read( document, strict );" );

        jClass.addMethod( unmarshall );

        // ----------------------------------------------------------------------

        unmarshall = new JMethod( "read", rootType, null );

        unmarshall.addParameter( new JParameter( new JClass( "URL" ), "url" ) );

        unmarshall.addException( new JClass( "IOException" ) );
        unmarshall.addException( new JClass( "DocumentException" ) );

        sc = unmarshall.getSourceCode();

        sc.add( "return read( url, true );" );

        jClass.addMethod( unmarshall );

        // ----------------------------------------------------------------------
        // Write the class parsers
        // ----------------------------------------------------------------------

        writeAllClassesParser( objectModel, jClass );

        // ----------------------------------------------------------------------
        // Write helpers
        // ----------------------------------------------------------------------

        writeHelpers( jClass );

        if ( requiresDomSupport )
        {
            jClass.addImport( "org.codehaus.plexus.util.xml.Xpp3Dom" );
            writeDomHelpers( jClass );
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        jClass.print( sourceWriter );

        sourceWriter.close();
    }

    private void writeAllClassesParser( Model objectModel, JClass jClass )
    {
        ModelClass root = objectModel.getClass( objectModel.getRoot( getGeneratedVersion() ), getGeneratedVersion() );

        for ( ModelClass clazz : getClasses( objectModel ) )
        {
            writeClassParser( clazz, jClass, root.getName().equals( clazz.getName() ) );
        }
    }

    private void writeClassParser( ModelClass modelClass, JClass jClass, boolean rootElement )
    {
        String className = modelClass.getName();

        String capClassName = capitalise( className );

        String uncapClassName = uncapitalise( className );

        JMethod unmarshall = new JMethod( "parse" + capClassName, new JClass( className ), null );
        unmarshall.getModifiers().makePrivate();

        unmarshall.addParameter( new JParameter( new JClass( "String" ), "tagName" ) );
        unmarshall.addParameter( new JParameter( new JClass( "Element" ), "element" ) );
        unmarshall.addParameter( new JParameter( JType.BOOLEAN, "strict" ) );

        unmarshall.addException( new JClass( "IOException" ) );
        unmarshall.addException( new JClass( "DocumentException" ) );

        JSourceCode sc = unmarshall.getSourceCode();

        sc.add( className + " " + uncapClassName + " = new " + className + "();" );

        ModelField contentField = null;

        List<ModelField> modelFields = getFieldsForXml( modelClass, getGeneratedVersion() );

        // read all XML attributes first
        for ( ModelField field : modelFields )
        {
            XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

            if ( xmlFieldMetadata.isAttribute() )
            {
                String tagName = xmlFieldMetadata.getTagName();
                if ( tagName == null )
                {
                    tagName = field.getName();
                }

                sc.add( "if ( element.attributeValue( \"" + tagName + "\" ) != null  )" );
                sc.add(  "{" );
                sc.indent();

                writePrimitiveField( field, field.getType(), uncapClassName, "set" + capitalise( field.getName() ), sc,
                                     jClass, "element", "childElement" );

                sc.unindent();
                sc.add( "}" );
            }
            // TODO check if we have already one with this type and throws Exception
            if ( xmlFieldMetadata.isContent() )
            {
                contentField = field;
            }
        }

        if ( rootElement )
        {
            sc.add( "if ( strict )" );
            sc.add( "{" );
            sc.indent();

            sc.add( "if ( !element.getName().equals( tagName ) )" );
            sc.add( "{" );
            sc.addIndented(
                "throw new DocumentException( \"Error parsing model: root element tag is '\" + element.getName() + \"' instead of '\" + tagName + \"'\" );" );
            sc.add( "}" );

            sc.unindent();
            sc.add( "}" );
        }

        if ( contentField != null )
        {
            writePrimitiveField( contentField, contentField.getType(), uncapClassName,
                                 "set" + capitalise( contentField.getName() ), sc, jClass, null, "element" );
        }
        else
        {
            sc.add( "java.util.Set parsed = new java.util.HashSet();" );

            sc.add( "for ( Iterator i = element.nodeIterator(); i.hasNext(); )" );
            sc.add( "{" );
            sc.indent();

            sc.add( "Node node = (Node) i.next();" );

            sc.add( "if ( node.getNodeType() == Node.ELEMENT_NODE )" );
            // TODO: attach other NodeTypes to model in some way
            sc.add( "{" );
            sc.indent();

            sc.add( "Element childElement = (Element) node;" );

            boolean addElse = false;

            for ( ModelField field : modelFields )
            {
                XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

                if ( !xmlFieldMetadata.isAttribute() )
                {
                    processField( field, xmlFieldMetadata, addElse, sc, uncapClassName, jClass );

                    addElse = true;
                }
            }

            if ( addElse )
            {
                sc.add( "else" );

                sc.add( "{" );
                sc.indent();
            }

            sc.add( "checkUnknownElement( childElement, strict );" );

            if ( addElse )
            {
                sc.unindent();
                sc.add( "}" );
            }

            sc.unindent();
            sc.add( "}" );

            sc.unindent();
            sc.add( "}" );
        }

        sc.add( "return " + uncapClassName + ";" );

        jClass.addMethod( unmarshall );
    }

    /**
     * Generate code to process a field represented as an XML element.
     *
     * @param field the field to process
     * @param xmlFieldMetadata its XML metadata
     * @param addElse add an <code>else</code> statement before generating a new <code>if</code>
     * @param sc the method source code to add to
     * @param objectName the object name in the source
     * @param jClass the generated class source file
     */
    private void processField( ModelField field, XmlFieldMetadata xmlFieldMetadata, boolean addElse, JSourceCode sc,
                               String objectName, JClass jClass )
    {
        String fieldTagName = resolveTagName( field, xmlFieldMetadata );

        String capFieldName = capitalise( field.getName() );

        String singularName = singular( field.getName() );

        String alias;
        if ( StringUtils.isEmpty( field.getAlias() ) )
        {
            alias = "null";
        }
        else
        {
            alias = "\"" + field.getAlias() + "\"";
        }

        String tagComparison = ( addElse ? "else " : "" )
            + "if ( checkFieldWithDuplicate( childElement, \"" + fieldTagName + "\", " + alias + ", parsed ) )";

        if ( field instanceof ModelAssociation )
        {
            ModelAssociation association = (ModelAssociation) field;

            String associationName = association.getName();

            if ( association.isOneMultiplicity() )
            {
                sc.add( tagComparison );

                sc.add( "{" );
                sc.addIndented( objectName + ".set" + capFieldName + "( parse" + association.getTo() + "( \""
                                + fieldTagName + "\", childElement, strict ) );" );
                sc.add( "}" );
            }
            else
            {
                //MANY_MULTIPLICITY

                XmlAssociationMetadata xmlAssociationMetadata =
                    (XmlAssociationMetadata) association.getAssociationMetadata( XmlAssociationMetadata.ID );

                String valuesTagName = resolveTagName( fieldTagName, xmlAssociationMetadata );

                String type = association.getType();

                if ( ModelDefault.LIST.equals( type ) || ModelDefault.SET.equals( type ) )
                {
                    boolean wrappedItems = xmlAssociationMetadata.isWrappedItems();

                    if ( wrappedItems )
                    {
                        sc.add( tagComparison );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( type + " " + associationName + " = " + association.getDefaultValue() + ";" );

                        sc.add( objectName + ".set" + capFieldName + "( " + associationName + " );" );

                        sc.add( "for ( Iterator j = childElement.nodeIterator(); j.hasNext(); )" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "Node n = (Node) j.next();" );

                        sc.add( "if ( n.getNodeType() == Node.ELEMENT_NODE )" );
                        // TODO: track the whitespace in the model (other NodeTypes)

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "Element listElement = (Element) n;" );

                        sc.add( "if ( \"" + valuesTagName + "\".equals( listElement.getName() ) )" );

                        sc.add( "{" );
                        sc.indent();
                    }
                    else
                    {
                        sc.add( ( addElse ? "else " : "" )
                            + "if ( \"" + valuesTagName + "\".equals( childElement.getName() ) )" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "Element listElement = childElement;" );

                        sc.add( type + " " + associationName + " = " + objectName + ".get" + capFieldName + "();" );

                        sc.add( "if ( " + associationName + " == null )" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( associationName + " = " + association.getDefaultValue() + ";" );

                        sc.add( objectName + ".set" + capFieldName + "( " + associationName + " );" );

                        sc.unindent();
                        sc.add( "}" );
                    }

                    if ( isClassInModel( association.getTo(), field.getModelClass().getModel() ) )
                    {
                        sc.add( associationName + ".add( parse" + association.getTo() + "( \"" + valuesTagName
                            + "\", listElement, strict ) );" );
                    }
                    else
                    {
                        writePrimitiveField( association, association.getTo(), associationName, "add", sc, jClass,
                                             "childElement", "listElement" );
                    }

                    if ( wrappedItems )
                    {
                        sc.unindent();
                        sc.add( "}" );

                        sc.add( "else" );

                        sc.add( "{" );
                        sc.add( "}" );

                        sc.unindent();
                        sc.add( "}" );

                        sc.unindent();
                        sc.add( "}" );

                        sc.unindent();
                        sc.add( "}" );
                    }
                    else
                    {
                        sc.unindent();
                        sc.add( "}" );
                    }
                }
                else
                {
                    //Map or Properties

                    sc.add( tagComparison );

                    sc.add( "{" );
                    sc.indent();

                    if ( xmlAssociationMetadata.isMapExplode() )
                    {
                        sc.add( "for ( Iterator j = childElement.nodeIterator(); j.hasNext(); )" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "Node n = (Node) j.next();" );

                        sc.add( "if ( n.getNodeType() == Node.ELEMENT_NODE )" );
                        // TODO: track the whitespace in the model (other NodeTypes)

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "Element listElement = (Element) n;" );

                        sc.add( "if ( \"" + valuesTagName + "\".equals( listElement.getName() ) )" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "String key = null;" );

                        sc.add( "String value = null;" );

                        sc.add( "//" + xmlAssociationMetadata.getMapStyle() + " mode." );

                        sc.add( "for ( Iterator k = listElement.nodeIterator(); k.hasNext(); )" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "Node nd = (Node) k.next();" );

                        sc.add( "if ( nd.getNodeType() == Node.ELEMENT_NODE )" );
                        // TODO: track the whitespace in the model (other NodeTypes)

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "Element propertyElement = (Element) nd;" );

                        sc.add( "if ( \"key\".equals( propertyElement.getName() ) )" );

                        sc.add( "{" );
                        sc.addIndented( "key = propertyElement.getText();" );
                        sc.add( "}" );

                        sc.add( "else if ( \"value\".equals( propertyElement.getName() ) )" );

                        sc.add( "{" );
                        sc.addIndented( "value = propertyElement.getText()"
                                        + ( xmlFieldMetadata.isTrim() ? ".trim()" : "" ) + ";" );
                        sc.add( "}" );

                        sc.add( "else" );

                        sc.add( "{" );
                        sc.add( "}" );

                        sc.unindent();
                        sc.add( "}" );

                        sc.unindent();
                        sc.add( "}" );

                        sc.add( objectName + ".add" + capitalise( singularName ) + "( key, value );" );

                        sc.unindent();
                        sc.add( "}" );

                        sc.unindent();
                        sc.add( "}" );

                        sc.unindent();
                        sc.add( "}" );
                    }
                    else
                    {
                        //INLINE Mode

                        sc.add( "for ( Iterator j = childElement.nodeIterator(); j.hasNext(); )" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "Node n = (Node) j.next();" );

                        sc.add( "if ( n.getNodeType() == Node.ELEMENT_NODE )" );
                        // TODO: track the whitespace in the model (other NodeTypes)

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "Element listElement = (Element) n;" );

                        sc.add( "String key = listElement.getName();" );

                        sc.add( "String value = listElement.getText()"
                                + ( xmlFieldMetadata.isTrim() ? ".trim()" : "" ) + ";" );

                        sc.add( objectName + ".add" + capitalise( singularName ) + "( key, value );" );

                        sc.unindent();
                        sc.add( "}" );

                        sc.unindent();
                        sc.add( "}" );
                    }

                    sc.unindent();
                    sc.add( "}" );
                }
            }
        }
        else
        {
            sc.add( tagComparison );

            sc.add( "{" );
            sc.indent();

            //ModelField
            writePrimitiveField( field, field.getType(), objectName, "set" + capitalise( field.getName() ), sc,
                                 jClass, "element", "childElement" );

            sc.unindent();
            sc.add( "}" );
        }
    }

    private void writePrimitiveField( ModelField field, String type, String objectName, String setterName,
                                      JSourceCode sc, JClass jClass, String parentElementName, String childElementName )
    {
        XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

        String tagName = resolveTagName( field, xmlFieldMetadata );

        String parserGetter;
        if ( xmlFieldMetadata.isAttribute() )
        {
            parserGetter = parentElementName + ".attributeValue( \"" + tagName + "\" )";
        }
        else
        {
            parserGetter = childElementName + ".getText()";
        }

// TODO: this and a default
//        if ( fieldMetaData.isRequired() )
//        {
//            parserGetter = "getRequiredAttributeValue( " + parserGetter + ", \"" + tagName + "\", parser, strict, encoding )";
//        }
//

        if ( xmlFieldMetadata.isTrim() )
        {
            parserGetter = "getTrimmedValue( " + parserGetter + " )";
        }

        if ( "boolean".equals( type ) )
        {
            sc.add(
                objectName + "." + setterName + "( getBooleanValue( " + parserGetter + ", \"" + tagName + "\" ) );" );
        }
        else if ( "char".equals( type ) )
        {
            sc.add(
                objectName + "." + setterName + "( getCharacterValue( " + parserGetter + ", \"" + tagName + "\" ) );" );
        }
        else if ( "double".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( getDoubleValue( " + parserGetter + ", \"" + tagName
                + "\", strict ) );" );
        }
        else if ( "float".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( getFloatValue( " + parserGetter + ", \"" + tagName
                + "\", strict ) );" );
        }
        else if ( "int".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( getIntegerValue( " + parserGetter + ", \"" + tagName
                + "\", strict ) );" );
        }
        else if ( "long".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( getLongValue( " + parserGetter + ", \"" + tagName
                + "\", strict ) );" );
        }
        else if ( "short".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( getShortValue( " + parserGetter + ", \"" + tagName
                + "\", strict ) );" );
        }
        else if ( "byte".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( getByteValue( " + parserGetter + ", \"" + tagName
                + "\", strict ) );" );
        }
        else if ( "String".equals( type ) || "Boolean".equals( type ) )
        {
            // TODO: other Primitive types
            sc.add( objectName + "." + setterName + "( " + parserGetter + " );" );
        }
        else if ( "Date".equals( type ) )
        {
            sc.add( "String dateFormat = "
                + ( xmlFieldMetadata.getFormat() != null ? "\"" + xmlFieldMetadata.getFormat() + "\"" : "null" ) + ";" );
            sc.add( objectName + "." + setterName + "( getDateValue( " + parserGetter + ", \"" + tagName
                + "\", dateFormat ) );" );
        }
        else if ( "DOM".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( writeElementToXpp3Dom( " + childElementName + " ) );" );

            requiresDomSupport = true;
        }
        else
        {
            throw new IllegalArgumentException( "Unknown type: " + type );
        }
    }

    private void writeHelpers( JClass jClass )
    {
        JMethod method = new JMethod( "getTrimmedValue", new JClass( "String" ), null );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );

        JSourceCode sc = method.getSourceCode();

        sc.add( "if ( s != null )" );

        sc.add( "{" );
        sc.addIndented( "s = s.trim();" );
        sc.add( "}" );

        sc.add( "return s;" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

/* TODO
        method = new JMethod( new JClass( "String" ), "getRequiredAttributeValue" );
        method.addException( new JClass( "XmlPullParserException" ) );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( new JClass( "XmlPullParser" ), "parser" ) );
        method.addParameter( new JParameter( JClass.Boolean, "strict" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s == null )" );

        sc.add( "{" );
        sc.indent();

        sc.add( "if ( strict )" );

        sc.add( "{" );
        sc.addIndented(
            "throw new XmlPullParserException( \"Missing required value for attribute '\" + attribute + \"'\", parser, null );" );
        sc.add( "}" );

        sc.unindent();
        sc.add( "}" );

        sc.add( "return s;" );

        jClass.addMethod( method );
*/
        // --------------------------------------------------------------------

        method = new JMethod( "getBooleanValue", JType.BOOLEAN, null );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s != null )" );

        sc.add( "{" );
        sc.addIndented( "return Boolean.valueOf( s ).booleanValue();" );
        sc.add( "}" );

        sc.add( "return false;" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = new JMethod( "getCharacterValue", JType.CHAR, null );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s != null )" );

        sc.add( "{" );
        sc.addIndented( "return s.charAt( 0 );" );
        sc.add( "}" );

        sc.add( "return 0;" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = convertNumericalType( "getIntegerValue", JType.INT, "Integer.valueOf( s ).intValue()", "an integer" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = convertNumericalType( "getShortValue", JType.SHORT, "Short.valueOf( s ).shortValue()",
                                       "a short integer" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = convertNumericalType( "getByteValue", JType.BYTE, "Byte.valueOf( s ).byteValue()", "a byte" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = convertNumericalType( "getLongValue", JType.LONG, "Long.valueOf( s ).longValue()", "a long integer" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = convertNumericalType( "getFloatValue", JType.FLOAT, "Float.valueOf( s ).floatValue()",
                                       "a floating point number" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = convertNumericalType( "getDoubleValue", JType.DOUBLE, "Double.valueOf( s ).doubleValue()",
                                       "a floating point number" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = new JMethod( "getDateValue", new JClass( "java.util.Date" ), null );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "dateFormat" ) );
        method.addException( new JClass( "DocumentException" ) );

        writeDateParsingHelper( method.getSourceCode(), "new DocumentException( e.getMessage(), e )" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = new JMethod( "checkFieldWithDuplicate", JType.BOOLEAN, null );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "Element" ), "element" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "tagName" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "alias" ) );
        method.addParameter( new JParameter( new JClass( "java.util.Set" ), "parsed" ) );
        method.addException( new JClass( "DocumentException" ) );

        sc = method.getSourceCode();

        sc.add( "if ( !( element.getName().equals( tagName ) || element.getName().equals( alias ) ) )" );

        sc.add( "{" );
        sc.addIndented( "return false;" );
        sc.add( "}" );

        sc.add( "if ( !parsed.add( tagName ) )" );

        sc.add( "{" );
        sc.addIndented( "throw new DocumentException( \"Duplicated tag: '\" + tagName + \"'\" );" );
        sc.add( "}" );

        sc.add( "return true;" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = new JMethod( "checkUnknownElement", null, null );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "Element" ), "element" ) );
        method.addParameter( new JParameter( JType.BOOLEAN, "strict" ) );
        method.addException( new JClass( "DocumentException" ) );

        sc = method.getSourceCode();

        sc.add( "if ( strict )" );

        sc.add( "{" );
        sc.addIndented( "throw new DocumentException( \"Unrecognised tag: '\" + element.getName() + \"'\" );" );
        sc.add( "}" );

        jClass.addMethod( method );
    }

    private void writeDomHelpers( JClass jClass )
    {
        JMethod method = new JMethod( "writeElementToXpp3Dom", new JClass( "Xpp3Dom" ), null );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "Element" ), "element" ) );

        JSourceCode sc = method.getSourceCode();

        sc.add( "Xpp3Dom xpp3Dom = new Xpp3Dom( element.getName() );" );

        sc.add( "if ( element.elements().isEmpty() && element.getText() != null )" );
        sc.add( "{" );
        sc.addIndented( "xpp3Dom.setValue( element.getText() );" );
        sc.add( "}" );

        sc.add( "for ( Iterator i = element.attributeIterator(); i.hasNext(); )" );
        sc.add( "{" );
        sc.indent();

        sc.add( "Attribute attribute = (Attribute) i.next();" );
        sc.add( "xpp3Dom.setAttribute( attribute.getName(), attribute.getValue() );" );

        sc.unindent();
        sc.add( "}" );

        // TODO: would be nice to track whitespace in here

        sc.add( "for ( Iterator i = element.elementIterator(); i.hasNext(); )" );
        sc.add( "{" );
        sc.indent();

        sc.add( "Element child = (Element) i.next();" );
        sc.add( "xpp3Dom.addChild( writeElementToXpp3Dom( child ) );" );

        sc.unindent();
        sc.add( "}" );

        sc.add( "return xpp3Dom;" );

        jClass.addMethod( method );
    }

    private JMethod convertNumericalType( String methodName, JType returnType, String expression, String typeDesc )
    {
        JMethod method = new JMethod( methodName, returnType, null );
        method.addException( new JClass( "DocumentException" ) );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( JClass.BOOLEAN, "strict" ) );

        JSourceCode sc = method.getSourceCode();

        sc.add( "if ( s != null )" );

        sc.add( "{" );
        sc.indent();

        sc.add( "try" );

        sc.add( "{" );
        sc.addIndented( "return " + expression + ";" );
        sc.add( "}" );

        sc.add( "catch ( NumberFormatException nfe )" );

        sc.add( "{" );
        sc.indent();

        sc.add( "if ( strict )" );

        sc.add( "{" );
        sc.addIndented( "throw new DocumentException( \"Unable to parse element '\" + attribute + \"', must be "
                        + typeDesc + "\", nfe );" );
        sc.add( "}" );

        sc.unindent();

        sc.add( "}" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "return 0;" );

        return method;
    }
}
