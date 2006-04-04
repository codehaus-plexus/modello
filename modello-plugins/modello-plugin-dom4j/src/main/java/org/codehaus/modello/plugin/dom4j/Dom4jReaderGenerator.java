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
import org.codehaus.modello.plugin.AbstractModelloGenerator;
import org.codehaus.modello.plugin.java.javasource.JClass;
import org.codehaus.modello.plugin.java.javasource.JMethod;
import org.codehaus.modello.plugin.java.javasource.JParameter;
import org.codehaus.modello.plugin.java.javasource.JSourceCode;
import org.codehaus.modello.plugin.java.javasource.JSourceWriter;
import org.codehaus.modello.plugin.java.javasource.JType;
import org.codehaus.modello.plugins.xml.XmlAssociationMetadata;
import org.codehaus.modello.plugins.xml.XmlClassMetadata;
import org.codehaus.modello.plugins.xml.XmlFieldMetadata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

/**
 * Generator that reads a model using dom4j.
 * TODO: chunks are lifted from xpp3, including the tests. Can we abstract it in some way?
 *
 * @author <a href="mailto:brett@codehaus.org">Brett Porter</a>
 */
public class Dom4jReaderGenerator
    extends AbstractModelloGenerator
{
    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

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

        String unmarshallerName = getFileName( "Dom4jReader" );

        File f = new File( new File( getOutputDirectory(), directory ), unmarshallerName + ".java" );

        if ( !f.getParentFile().exists() )
        {
            f.getParentFile().mkdirs();
        }

        FileWriter writer = new FileWriter( f );

        JSourceWriter sourceWriter = new JSourceWriter( writer );

        JClass jClass = new JClass( unmarshallerName );

        jClass.setPackageName( packageName );

        jClass.addImport( "java.io.IOException" );

        jClass.addImport( "java.io.Reader" );

        jClass.addImport( "java.util.Date" );

        jClass.addImport( "java.text.DateFormat" );

        jClass.addImport( "java.text.ParsePosition" );

        jClass.addImport( "java.util.Iterator" );

        jClass.addImport( "org.codehaus.plexus.util.xml.Xpp3Dom" );

        jClass.addImport( "org.dom4j.Attribute" );

        jClass.addImport( "org.dom4j.Document" );

        jClass.addImport( "org.dom4j.DocumentException" );

        jClass.addImport( "org.dom4j.Element" );

        jClass.addImport( "org.dom4j.Node" );

        jClass.addImport( "org.dom4j.io.SAXReader" );

        addModelImports( jClass, null );

        // ----------------------------------------------------------------------
        // Write the parse method which will do the unmarshalling.
        // ----------------------------------------------------------------------

        ModelClass root = objectModel.getClass( objectModel.getRoot( getGeneratedVersion() ), getGeneratedVersion() );

        JMethod unmarshall = new JMethod( new JClass( root.getName() ), "read" );

        unmarshall.addParameter( new JParameter( new JClass( "Reader" ), "reader" ) );

        unmarshall.addParameter( new JParameter( JType.Boolean, "strict" ) );

        unmarshall.addException( new JClass( "IOException" ) );

        unmarshall.addException( new JClass( "DocumentException" ) );

        JSourceCode sc = unmarshall.getSourceCode();

        sc.add( "SAXReader parser = new SAXReader();" );

        sc.add( "Document document = parser.read( reader );" );

        sc.add( "String encoding = document.getXMLEncoding();" );

        sc.add( "return parse" + root.getName() + "( \"" + getTagName( root ) +
            "\", document.getRootElement(), strict, encoding );" );

        jClass.addMethod( unmarshall );

        unmarshall = new JMethod( new JClass( root.getName() ), "read" );

        unmarshall.addParameter( new JParameter( new JClass( "Reader" ), "reader" ) );

        unmarshall.addException( new JClass( "IOException" ) );

        unmarshall.addException( new JClass( "DocumentException" ) );

        sc = unmarshall.getSourceCode();
        sc.add( "return read( reader, true );" );

        jClass.addMethod( unmarshall );

        // ----------------------------------------------------------------------
        // Write the class parsers
        // ----------------------------------------------------------------------

        writeAllClassesParser( objectModel, jClass );

        // ----------------------------------------------------------------------
        // Write helpers
        // ----------------------------------------------------------------------

        writeHelpers( jClass );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        jClass.print( sourceWriter );

        writer.flush();

        writer.close();
    }

    private String getTagName( ModelClass root )
    {
        XmlClassMetadata metadata = (XmlClassMetadata) root.getMetadata( XmlClassMetadata.ID );

        String tagName = metadata.getTagName();

        if ( tagName == null )
        {
            tagName = uncapitalise( root.getName() );
        }

        return tagName;

    }

    protected String getFileName( String suffix )
        throws ModelloException
    {
        String name = getModel().getName();

        return name + suffix;
    }

    private void writeAllClassesParser( Model objectModel, JClass jClass )
    {
        ModelClass root = objectModel.getClass( objectModel.getRoot( getGeneratedVersion() ), getGeneratedVersion() );

        for ( Iterator i = objectModel.getClasses( getGeneratedVersion() ).iterator(); i.hasNext(); )
        {
            ModelClass clazz = (ModelClass) i.next();

            if ( root.getName().equals( clazz.getName() ) )
            {
                writeClassParser( clazz, jClass, true );
            }
            else
            {
                writeClassParser( clazz, jClass, false );
            }
        }
    }

    private void writeClassParser( ModelClass modelClass, JClass jClass, boolean rootElement )
    {
        String className = modelClass.getName();

        String capClassName = capitalise( className );

        String uncapClassName = uncapitalise( className );

        JMethod unmarshall = new JMethod( new JClass( className ), "parse" + capClassName );

        unmarshall.addParameter( new JParameter( new JClass( "String" ), "tagName" ) );

        unmarshall.addParameter( new JParameter( new JClass( "Element" ), "element" ) );

        unmarshall.addParameter( new JParameter( JType.Boolean, "strict" ) );

        unmarshall.addParameter( new JParameter( new JClass( "String" ), "encoding" ) );

        unmarshall.addException( new JClass( "IOException" ) );

        unmarshall.addException( new JClass( "DocumentException" ) );

        unmarshall.getModifiers().makePrivate();

        JSourceCode sc = unmarshall.getSourceCode();

        sc.add( className + " " + uncapClassName + " = new " + className + "();" );

        sc.add( uncapClassName + ".setModelEncoding( encoding );" );

        for ( Iterator i = modelClass.getAllFields( getGeneratedVersion(), true ).iterator(); i.hasNext(); )
        {
            ModelField field = (ModelField) i.next();

            XmlFieldMetadata fieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

            if ( fieldMetadata.isAttribute() )
            {
                writePrimitiveField( field, field.getType(), uncapClassName, "set" + capitalise( field.getName() ), sc,
                                     jClass, "element", "childElement" );
            }
        }

        if ( rootElement )
        {
            sc.add( "if ( strict )" );
            sc.add( "{" );
            sc.indent();

            sc.add( "if ( !element.getName().equals( tagName ) )" );
            sc.add( "{" );
            sc.indent();

            sc.add(
                "throw new DocumentException( \"Error parsing model: root element tag is '\" + element.getName() + \"' instead of '\" + tagName + \"'\" );" );

            sc.unindent();
            sc.add( "}" );

            sc.unindent();
            sc.add( "}" );
        }

        sc.add( "java.util.Set parsed = new java.util.HashSet();" );

        sc.add( "for ( Iterator i = element.nodeIterator(); i.hasNext(); )" );
        sc.add( "{" );
        sc.indent();

        sc.add( "Node node = (Node) i.next();" );

        sc.add( "if ( node.getNodeType() != Node.ELEMENT_NODE )" );
        sc.add( "{" );
        sc.indent();

        // TODO: attach to model in some way

        sc.unindent();
        sc.add( "}" );
        sc.add( "else" );
        sc.add( "{" );
        sc.indent();

        sc.add( "Element childElement = (Element) node;" );

        String statement = "if";

        for ( Iterator i = modelClass.getAllFields( getGeneratedVersion(), true ).iterator(); i.hasNext(); )
        {
            ModelField field = (ModelField) i.next();

            XmlFieldMetadata fieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

            if ( !fieldMetadata.isAttribute() )
            {
                processField( fieldMetadata, field, statement, sc, uncapClassName, modelClass, jClass );

                statement = "else if";
            }
        }

        if ( statement.startsWith( "else" ) )
        {
            sc.add( "else" );

            sc.add( "{" );

            sc.indent();
        }

        sc.add( "if ( strict )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "throw new DocumentException( \"Unrecognised tag: '\" + childElement.getName() + \"'\" );" );

        sc.unindent();

        sc.add( "}" );

        if ( statement.startsWith( "else" ) )
        {
            sc.unindent();

            sc.add( "}" );
        }

        sc.unindent();
        sc.add( "}" );

        sc.unindent();
        sc.add( "}" );

        sc.add( "return " + uncapClassName + ";" );

        jClass.addMethod( unmarshall );
    }

    private void processField( XmlFieldMetadata fieldMetadata, ModelField field, String statement, JSourceCode sc,
                               String uncapClassName, ModelClass modelClass, JClass jClass )
    {
        String tagName = fieldMetadata.getTagName();

        if ( tagName == null )
        {
            tagName = field.getName();
        }

        String singularTagName = fieldMetadata.getAssociationTagName();

        if ( singularTagName == null )
        {
            singularTagName = singular( tagName );
        }

        boolean wrappedList = XmlFieldMetadata.LIST_STYLE_WRAPPED.equals( fieldMetadata.getListStyle() );

        String capFieldName = capitalise( field.getName() );

        String singularName = singular( field.getName() );

        String optionalCheck = "";
        if ( field.getAlias() != null && field.getAlias().length() > 0 )
        {
            optionalCheck = "|| childElement.getName().equals( \"" + field.getAlias() + "\" ) ";
        }

        String tagComparison =
            statement + " ( childElement.getName().equals( \"" + tagName + "\" ) " + optionalCheck + " )";

        if ( field instanceof ModelAssociation )
        {
            ModelAssociation association = (ModelAssociation) field;

            String associationName = association.getName();

            if ( ModelAssociation.ONE_MULTIPLICITY.equals( association.getMultiplicity() ) )
            {
                sc.add( tagComparison );

                sc.add( "{" );

                sc.indent();

                addCodeToCheckIfParsed( sc, tagName );

                sc.add( uncapClassName + ".set" + capFieldName + "( parse" + association.getTo() + "( \"" + tagName +
                    "\", childElement, strict, encoding ) );" );

                sc.unindent();

                sc.add( "}" );
            }
            else
            {
                //MANY_MULTIPLICITY

                String type = association.getType();

                if ( ModelDefault.LIST.equals( type ) || ModelDefault.SET.equals( type ) )
                {
                    if ( wrappedList )
                    {
                        sc.add( tagComparison );

                        sc.add( "{" );

                        sc.indent();

                        addCodeToCheckIfParsed( sc, tagName );

                        sc.add( type + " " + associationName + " = " + association.getDefaultValue() + ";" );

                        sc.add( uncapClassName + ".set" + capFieldName + "( " + associationName + " );" );

                        sc.add( "for ( Iterator j = childElement.nodeIterator(); j.hasNext(); )" );

                        sc.add( "{" );

                        sc.indent();

                        sc.add( "Node n = (Node) j.next();" );

                        sc.add( "if ( n.getNodeType() != Node.ELEMENT_NODE )" );

                        sc.add( "{" );
                        sc.indent();

                        // TODO: track the whitespace in the model

                        sc.unindent();
                        sc.add( "}" );

                        sc.add( "else" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "Element listElement = (Element) n;" );

                        sc.add( "if ( listElement.getName().equals( \"" + singularTagName + "\" ) )" );

                        sc.add( "{" );

                        sc.indent();
                    }
                    else
                    {
                        sc.add( statement + " ( childElement.getName().equals( \"" + singularTagName + "\" ) )" );

                        sc.add( "{" );

                        sc.indent();

                        sc.add( "Element listElement = childElement;" );

                        sc.add( type + " " + associationName + " = " + uncapClassName + ".get" + capFieldName + "();" );

                        sc.add( "if ( " + associationName + " == null )" );

                        sc.add( "{" );

                        sc.indent();

                        sc.add( associationName + " = " + association.getDefaultValue() + ";" );

                        sc.add( uncapClassName + ".set" + capFieldName + "( " + associationName + " );" );

                        sc.unindent();

                        sc.add( "}" );
                    }

                    if ( isClassInModel( association.getTo(), modelClass.getModel() ) )
                    {
                        sc.add( associationName + ".add( parse" + association.getTo() + "( \"" + singularTagName +
                            "\", listElement, strict, encoding ) );" );
                    }
                    else
                    {
                        writePrimitiveField( association, association.getTo(), associationName, "add", sc, jClass,
                                             "childElement", "listElement" );
                    }

                    if ( wrappedList )
                    {
                        sc.unindent();

                        sc.add( "}" );

                        sc.add( "else" );

                        sc.add( "{" );

                        sc.indent();

                        sc.unindent();

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

                    addCodeToCheckIfParsed( sc, tagName );

                    XmlAssociationMetadata xmlAssociationMetadata =
                        (XmlAssociationMetadata) association.getAssociationMetadata( XmlAssociationMetadata.ID );

                    if ( XmlAssociationMetadata.EXPLODE_MODE.equals( xmlAssociationMetadata.getMapStyle() ) )
                    {
                        sc.add( "for ( Iterator j = childElement.nodeIterator(); j.hasNext(); )" );

                        sc.add( "{" );

                        sc.indent();

                        sc.add( "Node n = (Node) j.next();" );

                        sc.add( "if ( n.getNodeType() != Node.ELEMENT_NODE )" );

                        sc.add( "{" );
                        sc.indent();

                        // TODO: track the whitespace in the model

                        sc.unindent();
                        sc.add( "}" );

                        sc.add( "else" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "Element listElement = (Element) n;" );

                        sc.add( "if ( listElement.getName().equals( \"" + singularTagName + "\" ) )" );

                        sc.add( "{" );

                        sc.indent();

                        sc.add( "String key = null;" );

                        sc.add( "String value = null;" );

                        sc.add( "//" + xmlAssociationMetadata.getMapStyle() + " mode." );

                        sc.add( "for ( Iterator k = listElement.nodeIterator(); k.hasNext(); )" );

                        sc.add( "{" );

                        sc.indent();

                        sc.add( "Node nd = (Node) k.next();" );

                        sc.add( "if ( nd.getNodeType() != Node.ELEMENT_NODE )" );

                        sc.add( "{" );
                        sc.indent();

                        // TODO: track the whitespace in the model

                        sc.unindent();
                        sc.add( "}" );

                        sc.add( "else" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "Element propertyElement = (Element) nd;" );

                        sc.add( "if ( propertyElement.getName().equals( \"key\" ) )" );

                        sc.add( "{" );

                        sc.indent();

                        sc.add( "key = propertyElement.getText();" );

                        sc.unindent();

                        sc.add( "}" );

                        sc.add( "else if ( propertyElement.getName().equals( \"value\" ) )" );

                        sc.add( "{" );

                        sc.indent();

                        sc.add( "value = propertyElement.getText()" );

                        if ( fieldMetadata.isTrim() )
                        {
                            sc.add( ".trim()" );
                        }

                        sc.add( ";" );

                        sc.unindent();

                        sc.add( "}" );

                        sc.add( "else" );

                        sc.add( "{" );

                        sc.indent();

                        sc.unindent();

                        sc.add( "}" );

                        sc.unindent();

                        sc.add( "}" );

                        sc.unindent();

                        sc.add( "}" );

                        sc.add( uncapClassName + ".add" + capitalise( singularName ) + "( key, value );" );

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

                        sc.add( "if ( n.getNodeType() != Node.ELEMENT_NODE )" );

                        sc.add( "{" );
                        sc.indent();

                        // TODO: track the whitespace in the model

                        sc.unindent();
                        sc.add( "}" );

                        sc.add( "else" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "Element listElement = (Element) n;" );

                        sc.add( "String key = listElement.getName();" );

                        sc.add( "String value = listElement.getText()" );

                        if ( fieldMetadata.isTrim() )
                        {
                            sc.add( ".trim()" );
                        }

                        sc.add( ";" );

                        sc.add( uncapClassName + ".add" + capitalise( singularName ) + "( key, value );" );

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

            addCodeToCheckIfParsed( sc, tagName );

            //ModelField
            writePrimitiveField( field, field.getType(), uncapClassName, "set" + capitalise( field.getName() ), sc,
                                 jClass, "element", "childElement" );

            sc.unindent();

            sc.add( "}" );
        }
    }

    private void addCodeToCheckIfParsed( JSourceCode sc, String tagName )
    {
        sc.add( "if ( parsed.contains( \"" + tagName + "\" ) )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "throw new DocumentException( \"Duplicated tag: '\" + element.getName() + \"'\");" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "parsed.add( \"" + tagName + "\" );" );
    }

    private void writePrimitiveField( ModelField field, String type, String objectName, String setterName,
                                      JSourceCode sc, JClass jClass, String parentElementName, String childElementName )
    {
        XmlFieldMetadata fieldMetaData = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

        String tagName = fieldMetaData.getTagName();

        if ( tagName == null )
        {
            tagName = field.getName();
        }

        String parserGetter;
        if ( fieldMetaData.isAttribute() )
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

        if ( fieldMetaData.isTrim() )
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
            sc.add( objectName + "." + setterName + "( getDoubleValue( " + parserGetter + ", \"" + tagName +
                "\", strict ) );" );
        }
        else if ( "float".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( getFloatValue( " + parserGetter + ", \"" + tagName +
                "\", strict ) );" );
        }
        else if ( "int".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( getIntegerValue( " + parserGetter + ", \"" + tagName +
                "\", strict ) );" );
        }
        else if ( "long".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( getLongValue( " + parserGetter + ", \"" + tagName +
                "\", strict ) );" );
        }
        else if ( "short".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( getShortValue( " + parserGetter + ", \"" + tagName +
                "\", strict ) );" );
        }
        else if ( "String".equals( type ) || "Boolean".equals( type ) )
        {
            // TODO: other Primitive types
            sc.add( objectName + "." + setterName + "( " + parserGetter + " );" );
        }
        else if ( "Date".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( getDateValue( " + parserGetter + ", \"" + tagName + "\" ) );" );
        }
        else if ( "DOM".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( writeElementToXpp3Dom( " + childElementName + " ) );" );
        }
        else
        {
            throw new IllegalArgumentException( "Unknown type: " + type );
        }
    }

    private void writeHelpers( JClass jClass )
    {
        JMethod method = new JMethod( new JClass( "String" ), "getTrimmedValue" );

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );

        JSourceCode sc = method.getSourceCode();

        sc.add( "if ( s != null )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "s = s.trim();" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "return s;" );

        jClass.addMethod( method );

/* TODO
        method = new JMethod( new JClass( "String" ), "getRequiredAttributeValue" );
        method.addException( new JClass( "XmlPullParserException" ) );

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

        sc.indent();

        sc.add(
            "throw new XmlPullParserException( \"Missing required value for attribute '\" + attribute + \"'\", parser, null );" );

        sc.unindent();

        sc.add( "}" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "return s;" );

        jClass.addMethod( method );
*/
        method = new JMethod( JType.Boolean, "getBooleanValue" );

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s != null )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "return Boolean.valueOf( s ).booleanValue();" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "return false;" );

        jClass.addMethod( method );

        method = new JMethod( JType.Char, "getCharacterValue" );

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s != null )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "return s.charAt( 0 );" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "return 0;" );

        jClass.addMethod( method );

        method = new JMethod( JType.Int, "getIntegerValue" );

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( JClass.Boolean, "strict" ) );
        method.addException( new JClass( "DocumentException" ) );

        sc = method.getSourceCode();

        convertNumericalType( sc, "Integer.valueOf( s ).intValue()", "an integer" );

        jClass.addMethod( method );

        method = new JMethod( JType.Short, "getShortValue" );

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( JClass.Boolean, "strict" ) );
        method.addException( new JClass( "DocumentException" ) );

        sc = method.getSourceCode();

        convertNumericalType( sc, "Short.valueOf( s ).shortValue()", "a short integer" );

        jClass.addMethod( method );

        method = new JMethod( JType.Long, "getLongValue" );

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( JClass.Boolean, "strict" ) );
        method.addException( new JClass( "DocumentException" ) );

        sc = method.getSourceCode();

        convertNumericalType( sc, "Long.valueOf( s ).longValue()", "a long integer" );

        jClass.addMethod( method );

        method = new JMethod( JType.Float, "getFloatValue" );

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( JClass.Boolean, "strict" ) );
        method.addException( new JClass( "DocumentException" ) );

        sc = method.getSourceCode();

        convertNumericalType( sc, "Float.valueOf( s ).floatValue()", "a floating point number" );

        jClass.addMethod( method );

        method = new JMethod( JType.Double, "getDoubleValue" );

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( JClass.Boolean, "strict" ) );
        method.addException( new JClass( "DocumentException" ) );

        sc = method.getSourceCode();

        convertNumericalType( sc, "Double.valueOf( s ).doubleValue()", "a floating point number" );

        jClass.addMethod( method );

        method = new JMethod( new JClass( "java.util.Date" ), "getDateValue" );

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addException( new JClass( "DocumentException" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s != null )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "DateFormat dateParser = DateFormat.getDateTimeInstance( DateFormat.FULL, DateFormat.FULL );" );

        sc.add( "dateParser.setLenient( true );" );

        sc.add( "try" );
        sc.add( "{" );
        sc.indent();

        sc.add( "return dateParser.parse( s );" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "catch ( java.text.ParseException e )" );
        sc.add( "{" );
        sc.indent();

        sc.add( "throw new DocumentException( e.getMessage() );" );

        sc.unindent();

        sc.add( "}" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "return null;" );

        jClass.addMethod( method );

        method = new JMethod( new JClass( "Xpp3Dom" ), "writeElementToXpp3Dom" );

        method.addParameter( new JParameter( new JClass( "Element" ), "element" ) );

        sc = method.getSourceCode();

        sc.add( "Xpp3Dom xpp3Dom = new Xpp3Dom( element.getName() );" );

        sc.add( "if ( element.getText() != null )" );
        sc.add( "{" );
        sc.indent();

        sc.add( "xpp3Dom.setValue( element.getText() );" );

        sc.unindent();
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

    private void convertNumericalType( JSourceCode sc, String expression, String typeDesc )
    {
        sc.add( "if ( s != null )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "try" );

        sc.add( "{" );

        sc.indent();

        sc.add( "return " + expression + ";" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "catch ( NumberFormatException e )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "if ( strict )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "throw new DocumentException( \"Unable to parse element '\" + attribute + \"', must be " + typeDesc +
            "\" );" );

        sc.unindent();

        sc.add( "}" );

        sc.unindent();

        sc.add( "}" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "return 0;" );
    }
}
