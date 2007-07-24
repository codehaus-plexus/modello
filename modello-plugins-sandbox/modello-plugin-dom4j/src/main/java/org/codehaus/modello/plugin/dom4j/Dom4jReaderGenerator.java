package org.codehaus.modello.plugin.dom4j;

/*
 * Copyright (c) 2005, Joakim Erdfelt
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
import org.codehaus.modello.generator.java.javasource.JType;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelDefault;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.plugins.xml.XmlAssociationMetadata;
import org.codehaus.modello.plugins.xml.XmlClassMetadata;
import org.codehaus.modello.plugins.xml.XmlFieldMetadata;
import org.codehaus.plexus.util.WriterFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Properties;

/**
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 */
public class Dom4jReaderGenerator
    extends AbstractDom4jGenerator
{
    private String conditionalTrim( ModelField field, String elem, String method )
    {
        XmlFieldMetadata fieldMetaData = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

        if ( fieldMetaData.isTrim() )
        {
            return elem + "." + method + "Trim";
        }
        else
        {
            return elem + "." + method;
        }
    }

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
            throw new ModelloException( "Exception while generating DOM4J Reader.", ex );
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

        Writer writer = WriterFactory.newPlatformWriter( f );

        JSourceWriter sourceWriter = new JSourceWriter( writer );

        JClass jClass = new JClass( unmarshallerName );

        jClass.setPackageName( packageName );

        // imports that dom4j uses.
        jClass.addImport( "org.dom4j.Document" );
        jClass.addImport( "org.dom4j.DocumentException" );
        jClass.addImport( "org.dom4j.Element" );
        jClass.addImport( "org.dom4j.io.SAXReader" );

        // imports that dom4j reader uses.
        jClass.addImport( "java.io.Reader" );
        jClass.addImport( "java.io.IOException" );

        // imports that helpers use.
        jClass.addImport( "java.text.DateFormat" );
        jClass.addImport( "java.text.ParsePosition" );

        addModelImports( jClass, null );

        // ----------------------------------------------------------------------
        // Write option setters
        // ----------------------------------------------------------------------

        // ----------------------------------------------------------------------
        // Write the parse method which will do the unmarshalling.
        // ----------------------------------------------------------------------


        ModelClass root = objectModel.getClass( objectModel.getRoot( getGeneratedVersion() ), getGeneratedVersion() );

        JMethod unmarshall = new JMethod( new JClass( root.getName() ), "read" );

        unmarshall.addParameter( new JParameter( new JClass( "Reader" ), "reader" ) );

        unmarshall.addException( new JClass( "IOException" ) );
        unmarshall.addException( new JClass( "DocumentException" ) );

        JSourceCode sc = unmarshall.getSourceCode();

        sc.add( "SAXReader saxreader = new SAXReader( false );" ); // validating set to false.
        sc.add( "Document doc = saxreader.read( reader );" );
        sc.add( "Element root = doc.getRootElement();" );

        sc.add( "" );

        sc.add( "return parse" + root.getName() + "( \"" + getTagName( root ) + "\", root );" );

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

        if ( tagName != null )
        {
            return tagName;
        }

        return uncapitalise( root.getName() );
    }

    private String typeConversion( String type, String value )
    {
        if ( "boolean".equals( type ) )
        {
            return "getBooleanValue( " + value + " )";
        }
        else if ( "char".equals( type ) )
        {
            return "getCharacterValue( " + value + " )";
        }
        else if ( "double".equals( type ) )
        {
            return "getDoubleValue( " + value + " )";
        }
        else if ( "float".equals( type ) )
        {
            return "getFloatValue( " + value + " )";
        }
        else if ( "int".equals( type ) )
        {
            return "getIntegerValue( " + value + " )";
        }
        else if ( "long".equals( type ) )
        {
            return "getLongValue( " + value + " )";
        }
        else if ( "short".equals( type ) )
        {
            return "getShortValue( " + value + " )";
        }
        else if ( "Date".equals( type ) )
        {
            return "getDateValue( " + value + " )";
        }
        else if ( "String".equals( type ) || "Boolean".equals( type ) )
        {
            return value;
        }
        else if ( "DOM".equals( type ) )
        {
            throw new IllegalArgumentException( "Unhandled type: DOM." );
        }
        else
        {
            throw new IllegalArgumentException( "Unknown type: " + type );
        }
    }

    private void writeAllClassesParser( Model objectModel, JClass jClass )
    {
        for ( Iterator i = objectModel.getClasses( getGeneratedVersion() ).iterator(); i.hasNext(); )
        {
            ModelClass clazz = (ModelClass) i.next();

            writeClassParser( clazz, jClass );
        }
    }

    private void writeClassParser( ModelClass modelClass, JClass jClass )
    {
        String className = modelClass.getName();

        String capClassName = capitalise( className );

        String uncapClassName = uncapitalise( className );

        JMethod unmarshall = new JMethod( new JClass( className ), "parse" + capClassName );

        unmarshall.addParameter( new JParameter( new JClass( "String" ), "tagName" ) );

        unmarshall.addParameter( new JParameter( new JClass( "Element" ), "elem" ) );

        unmarshall.addException( new JClass( "IOException" ) );
        unmarshall.addException( new JClass( "DocumentException" ) );

        unmarshall.getModifiers().makePrivate();

        JSourceCode sc = unmarshall.getSourceCode();

        sc.add( "if( !elem.getName().equals(tagName) )" );
        sc.add( "{" );
        sc.indent();
        sc.add(
            "throw new DocumentException(\"Expected <\" + tagName + \"> in place of <\" + elem.getName() + \">\");" );
        sc.unindent();
        sc.add( "}" );

        sc.add( className + " " + uncapClassName + " = new " + className + "();" );

        /* Add Handlers for Class Fields. */
        sc.add( "" );
        breadcrumb( sc, "Attribute Handler" );

        for ( Iterator i = modelClass.getAllFields( getGeneratedVersion(), true ).iterator(); i.hasNext(); )
        {
            ModelField field = (ModelField) i.next();

            XmlFieldMetadata fieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

            if ( fieldMetadata.isAttribute() )
            {
                writePrimitiveField( field, field.getType(), uncapClassName, "set" + capitalise( field.getName() ),
                                     sc );
            }
        }

        sc.add( "" );

        breadcrumb( sc, "Sub Fields" );
        sc.add( "org.dom4j.Element subelem;" );

        for ( Iterator i = modelClass.getAllFields( getGeneratedVersion(), true ).iterator(); i.hasNext(); )
        {
            ModelField field = (ModelField) i.next();

            XmlFieldMetadata fieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

            if ( fieldMetadata.isAttribute() )
            {
                continue;
            }

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

            if ( field instanceof ModelAssociation )
            {
                breadcrumb( sc, "field is model association" );
                ModelAssociation association = (ModelAssociation) field;

                String associationName = association.getName();

                if ( ModelAssociation.ONE_MULTIPLICITY.equals( association.getMultiplicity() ) )
                {
                    breadcrumb( sc, "one multiplicity " );
                    sc.add( "subelem = elem.element( \"" + tagName + "\" );" );
                    sc.add( "if ( subelem != null )" );

                    sc.add( "{" );

                    sc.indent();

                    sc.add( uncapClassName + ".set" + capFieldName + "( parse" + association.getTo() + "( \"" +
                            tagName + "\", subelem ) );" );

                    sc.unindent();

                    sc.add( "}" );
                }
                else
                {
                    //MANY_MULTIPLICITY
                    breadcrumb( sc, "many multiplicity" );

                    String type = association.getType();

                    if ( ModelDefault.LIST.equals( type ) || ModelDefault.SET.equals( type ) )
                    {
                        String elemname;

                        if ( wrappedList )
                        {
                            breadcrumb( sc, "list: wrapped" );
                            sc.add( "subelem = elem.element( \"" + tagName + "\" );" );
                            sc.add( "if ( subelem != null )" );

                            sc.add( "{" );

                            sc.indent();

                            sc.add( type + " " + associationName + " = " + association.getDefaultValue() + ";" );

                            sc.add( uncapClassName + ".set" + capFieldName + "( " + associationName + " );" );

                            sc.add( "" );
                            sc.add( "java.util.List elems = subelem.elements( \"" + singularTagName + "\" );" );
                            sc.add( "java.util.Iterator it = elems.iterator();" );
                            sc.add( "while ( it.hasNext() )" );
                            sc.add( "{" );

                            sc.indent();

                            sc.add( "org.dom4j.Element itelem = ( org.dom4j.Element ) it.next();" );
                            elemname = "itelem";
                        }
                        else
                        {
                            breadcrumb( sc, "list: not wrapped" );
                            sc.add( "subelem = elem.element( \"" + singularTagName + "\" );" );
                            sc.add( "if ( subelem != null )" );

                            sc.add( "{" );

                            sc.indent();

                            sc.add( type + " " + associationName + " = " +
                                    uncapClassName + ".get" + capFieldName + "();" );

                            sc.add( "if ( " + associationName + " == null )" );

                            sc.add( "{" );

                            sc.indent();

                            sc.add( associationName + " = " + association.getDefaultValue() + ";" );

                            sc.add( uncapClassName + ".set" + capFieldName + "( " + associationName + " );" );

                            sc.unindent();

                            sc.add( "}" );
                            elemname = "subelem";
                        }

                        if ( isClassInModel( association.getTo(), modelClass.getModel() ) )
                        {
                            breadcrumb( sc, "class '" + association.getTo() + "' is in model" );
                            sc.add( associationName + ".add( parse" + association.getTo() + "( \"" + singularTagName +
                                    "\", " + elemname + " ) );" );
                        }
                        else
                        {
                            breadcrumb( sc, "class '" + association.getTo() + "' is NOT in model" );

                            String gettext = conditionalTrim( association, elemname, "getText" ) + "()";
                            sc.add(
                                associationName + ".add( " + typeConversion( association.getTo(), gettext ) + " );" );
                        }

                        if ( wrappedList )
                        {
                            sc.unindent();
                            sc.add( "}" );

                        }

                        sc.unindent();

                        sc.add( "}" );

                    }
                    else
                    {
                        breadcrumb( sc, "Map or Properties" );
                        // Map or Properties

                        sc.add( "subelem = elem.element( \"" + tagName + "\" );" );
                        sc.add( "if ( subelem != null )" );

                        sc.add( "{" );

                        sc.indent();

                        XmlAssociationMetadata xmlAssociationMetadata =
                            (XmlAssociationMetadata) association.getAssociationMetadata( XmlAssociationMetadata.ID );

                        if ( XmlAssociationMetadata.EXPLODE_MODE.equals( xmlAssociationMetadata.getMapStyle() ) )
                        {
                            String trimMode = "";
                            if ( fieldMetadata.isTrim() )
                            {
                                trimMode = "Trim";
                            }

                            breadcrumb( sc, "Map Style: Explode Mode" );
                            sc.add( "java.util.List elems = subelem.elements( \"" + singularTagName + "\" );" );
                            sc.add( "java.util.Iterator it = elems.iterator();" );
                            sc.add( "while ( it.hasNext() )" );
                            sc.add( "{" );

                            sc.indent();

                            sc.add( "// " + xmlAssociationMetadata.getMapStyle() + " mode." );
                            sc.add( "org.dom4j.Element itelem = ( org.dom4j.Element ) it.next();" );
                            sc.add( "String key = itelem.elementText( \"key\" );" );
                            sc.add( "String value = itelem.elementText" + trimMode + "( \"value\" );" );
                            sc.add( uncapClassName + ".add" + capitalise( singularName ) + "( key, value );" );

                            sc.unindent();

                            sc.add( "}" );
                        }
                        else
                        {
                            breadcrumb( sc, "Map Style: Inline Mode" );
                            //INLINE Mode

                            sc.add( "java.util.List elems = subelem.elements();" );
                            sc.add( "java.util.Iterator it = elems.iterator();" );
                            sc.add( "while ( it.hasNext() )" );
                            sc.add( "{" );

                            sc.indent();

                            sc.add( "// " + xmlAssociationMetadata.getMapStyle() + " mode." );
                            sc.add( "org.dom4j.Element itelem = ( org.dom4j.Element ) it.next();" );
                            sc.add( "String key = itelem.getName();" );

                            if ( fieldMetadata.isTrim() )
                            {
                                sc.add( "String value = itelem.getTextTrim();" );
                            }
                            else
                            {
                                sc.add( "String value = itelem.getText();" );
                            }

                            sc.add( uncapClassName + ".add" + capitalise( singularName ) + "( key, value );" );

                            sc.unindent();

                            sc.add( "}" );
                        }

                        sc.unindent();

                        sc.add( "}" );
                    }
                }
            } // end of model association.
            else
            {
                sc.add( "subelem = elem.element( \"" + tagName + "\" );" );
                sc.add( "if ( subelem != null )" );
                sc.add( "{" );
                sc.indent();
                sc.add( uncapClassName + ".set" + capitalise( field.getName() ) + "( " +
                        typeConversion( field.getType(),
                                        conditionalTrim( field, "subelem", "getText" ) + "()" ) + " );" );
                // writePrimitiveField( field, field.getType(), uncapClassName, "set" + capitalise( field.getName() ), sc );
                sc.unindent();
                sc.add( "}" );
            }
        }

        /* return populated class */

        sc.add( "return " + uncapClassName + ";" );

        jClass.addMethod( unmarshall );
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

        method = new JMethod( new JClass( "String" ), "getRequiredAttributeValue" );
        method.addException( new JClass( "DocumentException" ) );

        // TODO: add include name of element that attribute belongs to, so exception makes sense.
        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s == null )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "throw new DocumentException( \"Missing required value for attribute '\" + attribute + \"'\" );" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "return s;" );

        jClass.addMethod( method );

        method = new JMethod( JType.Boolean, "getBooleanValue" );

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );

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

        sc = method.getSourceCode();

        sc.add( "if ( s != null )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "return Integer.valueOf( s ).intValue();" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "return 0;" );

        jClass.addMethod( method );

        method = new JMethod( JType.Short, "getShortValue" );

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s != null )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "return Short.valueOf( s ).shortValue();" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "return 0;" );

        jClass.addMethod( method );

        method = new JMethod( JType.Long, "getLongValue" );

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s != null )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "return Long.valueOf( s ).longValue();" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "return 0;" );

        jClass.addMethod( method );

        method = new JMethod( JType.Float, "getFloatValue" );

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s != null )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "return Float.valueOf( s ).floatValue();" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "return 0;" );

        jClass.addMethod( method );

        method = new JMethod( JType.Double, "getDoubleValue" );

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s != null )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "return Double.valueOf( s ).doubleValue();" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "return 0;" );

        jClass.addMethod( method );

        method = new JMethod( new JClass( "java.util.Date" ), "getDateValue" );

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s != null )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "DateFormat dateParser = DateFormat.getDateTimeInstance( DateFormat.FULL, DateFormat.FULL );" );

        sc.add( "return dateParser.parse( s, new ParsePosition( 0 ) );" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "return null;" );

        jClass.addMethod( method );
    }

    private void writePrimitiveField( ModelField field, String type, String objectName, String setterName,
                                      JSourceCode sc )
    {
        XmlFieldMetadata fieldMetaData = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

        String tagName = fieldMetaData.getTagName();

        String parserGetter;

        if ( tagName == null )
        {
            tagName = field.getName();
        }

        if ( fieldMetaData.isAttribute() )
        {
            parserGetter = "elem.attributeValue( \"" + tagName + "\" )";
            if ( fieldMetaData.isTrim() )
            {
                parserGetter = "getTrimmedValue( " + parserGetter + " )";
            }
        }
        else
        {
            parserGetter = conditionalTrim( field, "elem", "elementText" ) + "( \"" + tagName + "\" )";
        }

/* TODO: this and a default
        if ( fieldMetaData.isRequired() )
        {
            parserGetter = "getRequiredAttributeValue( " + parserGetter + ", \"" + tagName + "\", parser )";
        }
*/

        sc.add( objectName + "." + setterName + "( " + typeConversion( type, parserGetter ) + " );" );
    }
}
