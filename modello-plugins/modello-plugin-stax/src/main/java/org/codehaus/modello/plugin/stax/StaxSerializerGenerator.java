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
import org.codehaus.modello.plugin.java.javasource.JClass;
import org.codehaus.modello.plugin.java.javasource.JConstructor;
import org.codehaus.modello.plugin.java.javasource.JField;
import org.codehaus.modello.plugin.java.javasource.JMethod;
import org.codehaus.modello.plugin.java.javasource.JParameter;
import org.codehaus.modello.plugin.java.javasource.JSourceCode;
import org.codehaus.modello.plugin.java.javasource.JSourceWriter;
import org.codehaus.modello.plugin.java.javasource.JType;
import org.codehaus.plexus.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Generates the IndentingXMLStreamWriter used by the writer for pretty printing.
 * 
 * @author Benjamin Bentmann
 */
public class StaxSerializerGenerator
    extends AbstractStaxGenerator
{

    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

        try
        {
            generateStaxSerializer();
        }
        catch ( IOException ex )
        {
            throw new ModelloException( "Exception while generating StAX serializer.", ex );
        }
    }

    private void generateStaxSerializer()
        throws ModelloException, IOException
    {
        Model objectModel = getModel();

        String packageName =
            objectModel.getDefaultPackageName( isPackageWithVersion(), getGeneratedVersion() ) + ".io.stax";

        String className = "IndentingXMLStreamWriter";

        JSourceWriter sourceWriter = newJSourceWriter( packageName, className );

        JClass jClass = new JClass( packageName + '.' + className );
        jClass.getModifiers().makePackage();
        jClass.addInterface( "XMLStreamWriter" );
        initHeader( jClass );
        suppressAllWarnings( objectModel, jClass );

        jClass.addImport( "javax.xml.namespace.NamespaceContext" );
        jClass.addImport( "javax.xml.stream.XMLStreamException" );
        jClass.addImport( "javax.xml.stream.XMLStreamWriter" );

        addField( jClass, "XMLStreamWriter", "out", null, false );
        addField( jClass, "String", "NEW_LINE", "\"\\n\"", true );
        addField( jClass, "String", "newLine", "NEW_LINE", false );
        addField( jClass, "String", "indent", "\"  \"", false );
        addField( jClass, "char[]", "linePrefix", "\"                        \".toCharArray()", false );
        addField( jClass, "int", "depth", null, false );
        addField( jClass, "byte[]", "states", "{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }", false );
        addField( jClass, "int", "ELEMENT_HAS_DATA", "0x1", true );
        addField( jClass, "int", "ELEMENT_HAS_MARKUP", "0x2", true );

        JConstructor constructor = jClass.createConstructor();
        constructor.addParameter( new JParameter( new JType( "XMLStreamWriter" ), "out" ) );
        constructor.getSourceCode().add( "this.out = out;" );

        JMethod jMethod;
        JSourceCode sc;

        jMethod = new JMethod( "setNewLine" );
        jMethod.addParameter( new JParameter( new JType( "String" ), "newLine" ) );
        jMethod.getSourceCode().add( "this.newLine = newLine;" );
        jClass.addMethod( jMethod );

        jMethod = new JMethod( "getLineSeparator", new JType( "String" ), null );
        sc = jMethod.getSourceCode();
        sc.add( "try" );
        sc.add( "{" );
        sc.addIndented( "return System.getProperty( \"line.separator\", NEW_LINE );" );
        sc.add( "}" );
        sc.add( "catch ( Exception e )" );
        sc.add( "{" );
        sc.addIndented( "return NEW_LINE;" );
        sc.add( "}" );
        jClass.addMethod( jMethod );

        jMethod = new JMethod( "beforeMarkup" );
        jMethod.getModifiers().makePrivate();
        sc = jMethod.getSourceCode();
        sc.add( "int state = states[depth];" );
        sc.add( "if ( ( state & ELEMENT_HAS_DATA ) == 0 && ( depth > 0 || state != 0 ) )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "newLine( depth );" );
        sc.add( "if ( depth > 0 && indent.length() > 0 )" );
        sc.add( "{" );
        sc.addIndented( "afterMarkup();" );
        sc.add( "}" );
        sc.unindent();
        sc.add( "}" );
        jClass.addMethod( jMethod );

        jMethod = new JMethod( "afterMarkup" );
        jMethod.getModifiers().makePrivate();
        jMethod.getSourceCode().add( "states[depth] |= ELEMENT_HAS_MARKUP;" );
        jClass.addMethod( jMethod );

        jMethod = new JMethod( "beforeStartElement" );
        jMethod.getModifiers().makePrivate();
        sc = jMethod.getSourceCode();
        sc.add( "beforeMarkup();" );
        sc.add( "if ( states.length <= depth + 1 )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "byte[] tmp = new byte[states.length * 2];" );
        sc.add( "System.arraycopy( states, 0, tmp, 0, states.length );" );
        sc.add( "states = tmp;" );
        sc.unindent();
        sc.add( "}" );
        sc.add( "states[depth + 1] = 0;" );
        jClass.addMethod( jMethod );

        jMethod = new JMethod( "afterStartElement" );
        jMethod.getModifiers().makePrivate();
        sc = jMethod.getSourceCode();
        sc.add( "afterMarkup();" );
        sc.add( "depth++;" );
        jClass.addMethod( jMethod );

        jMethod = new JMethod( "beforeEndElement" );
        jMethod.getModifiers().makePrivate();
        sc = jMethod.getSourceCode();
        sc.add( "if ( depth > 0 && states[depth] == ELEMENT_HAS_MARKUP )" );
        sc.add( "{" );
        sc.addIndented( "newLine( depth - 1 );" );
        sc.add( "}" );
        jClass.addMethod( jMethod );

        jMethod = new JMethod( "afterEndElement" );
        jMethod.getModifiers().makePrivate();
        sc = jMethod.getSourceCode();
        sc.add( "if ( depth > 0 )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "depth--;" );
        sc.add( "if ( depth <= 0 )" );
        sc.add( "{" );
        sc.addIndented( "newLine( 0 );" );
        sc.add( "}" );
        sc.unindent();
        sc.add( "}" );
        jClass.addMethod( jMethod );

        jMethod = new JMethod( "afterData" );
        jMethod.getModifiers().makePrivate();
        jMethod.getSourceCode().add( "states[depth] |= ELEMENT_HAS_DATA;" );
        jClass.addMethod( jMethod );

        jMethod = new JMethod( "newLine" );
        jMethod.addParameter( new JParameter( JType.INT, "depth" ) );
        jMethod.getModifiers().makePrivate();
        sc = jMethod.getSourceCode();
        sc.add( "try" );
        sc.add( "{" );
        sc.indent();
        sc.add( "out.writeCharacters( newLine );" );
        sc.add( "int prefixLength = depth * indent.length();" );
        sc.add( "while ( linePrefix.length < prefixLength )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "char[] tmp = new char[linePrefix.length * 2];" );
        sc.add( "System.arraycopy( linePrefix, 0, tmp, 0, linePrefix.length );" );
        sc.add( "System.arraycopy( linePrefix, 0, tmp, linePrefix.length, linePrefix.length );" );
        sc.add( "linePrefix = tmp;" );
        sc.unindent();
        sc.add( "}" );
        sc.add( "out.writeCharacters( linePrefix, 0, prefixLength );" );
        sc.unindent();
        sc.add( "}" );
        sc.add( "catch ( Exception e )" );
        sc.add( "{" );
        sc.add( "}" );
        jClass.addMethod( jMethod );

        jMethod = new JMethod( "close" );
        jMethod.addException( new JClass( "XMLStreamException" ) );
        jMethod.getSourceCode().add( "out.close();" );
        jClass.addMethod( jMethod );

        jMethod = new JMethod( "flush" );
        jMethod.addException( new JClass( "XMLStreamException" ) );
        jMethod.getSourceCode().add( "out.flush();" );
        jClass.addMethod( jMethod );

        jMethod = new JMethod( "getNamespaceContext", new JType( "NamespaceContext" ), null );
        jMethod.getSourceCode().add( "return out.getNamespaceContext();" );
        jClass.addMethod( jMethod );

        jMethod = new JMethod( "getPrefix", new JType( "String" ), null );
        jMethod.addException( new JClass( "XMLStreamException" ) );
        jMethod.addParameter( param( "String", "uri" ) );
        jMethod.getSourceCode().add( "return out.getPrefix( uri );" );
        jClass.addMethod( jMethod );

        jMethod = new JMethod( "getProperty", new JType( "Object" ), null );
        jMethod.addException( new JClass( "IllegalArgumentException" ) );
        jMethod.addParameter( param( "String", "name" ) );
        jMethod.getSourceCode().add( "return out.getProperty( name );" );
        jClass.addMethod( jMethod );

        jMethod = new JMethod( "setDefaultNamespace" );
        jMethod.addException( new JClass( "XMLStreamException" ) );
        jMethod.addParameter( param( "String", "uri" ) );
        jMethod.getSourceCode().add( "out.setDefaultNamespace( uri );" );
        jClass.addMethod( jMethod );

        jMethod = new JMethod( "setNamespaceContext" );
        jMethod.addException( new JClass( "XMLStreamException" ) );
        jMethod.addParameter( param( "NamespaceContext", "context" ) );
        jMethod.getSourceCode().add( "out.setNamespaceContext( context );" );
        jClass.addMethod( jMethod );

        jMethod = new JMethod( "setPrefix" );
        jMethod.addException( new JClass( "XMLStreamException" ) );
        jMethod.addParameter( param( "String", "prefix" ) );
        jMethod.addParameter( param( "String", "uri" ) );
        jMethod.getSourceCode().add( "out.setPrefix( prefix, uri );" );
        jClass.addMethod( jMethod );

        add( jClass, "Attribute", null, null, param( "String", "localName" ), param( "String", "value" ) );
        add( jClass, "Attribute", null, null, param( "String", "namespaceURI" ), param( "String", "localName" ),
             param( "String", "value" ) );
        add( jClass, "Attribute", null, null, param( "String", "prefix" ), param( "String", "namespaceURI" ),
             param( "String", "localName" ), param( "String", "value" ) );

        add( jClass, "CData", null, "Data", param( "String", "data" ) );

        add( jClass, "Characters", null, "Data", param( "String", "text" ) );
        add( jClass, "Characters", null, "Data", param( "char[]", "text" ), param( "int", "start" ), param( "int",
                                                                                                            "len" ) );

        add( jClass, "Comment", "Markup", "Markup", param( "String", "data" ) );

        add( jClass, "DTD", "Markup", "Markup", param( "String", "dtd" ) );

        add( jClass, "DefaultNamespace", null, null, param( "String", "namespaceURI" ) );

        add( jClass, "EmptyElement", "Markup", "Markup", param( "String", "localName" ) );
        add( jClass, "EmptyElement", "Markup", "Markup", param( "String", "namespaceURI" ), param( "String",
                                                                                                   "localName" ) );
        add( jClass, "EmptyElement", "Markup", "Markup", param( "String", "prefix" ),
             param( "String", "namespaceURI" ), param( "String", "localName" ) );

        add( jClass, "EndDocument", null, null );

        add( jClass, "EndElement", "EndElement", "EndElement" );

        add( jClass, "EntityRef", null, "Data", param( "String", "name" ) );

        add( jClass, "Namespace", null, null, param( "String", "prefix" ), param( "String", "namespaceURI" ) );

        add( jClass, "ProcessingInstruction", "Markup", "Markup", param( "String", "target" ) );
        add( jClass, "ProcessingInstruction", "Markup", "Markup", param( "String", "target" ), param( "String", "data" ) );

        add( jClass, "StartDocument", "Markup", "Markup" );
        add( jClass, "StartDocument", "Markup", "Markup", param( "String", "version" ) );
        add( jClass, "StartDocument", "Markup", "Markup", param( "String", "encoding" ), param( "String", "version" ) );

        add( jClass, "StartElement", "StartElement", "StartElement", param( "String", "localName" ) );
        add( jClass, "StartElement", "StartElement", "StartElement", param( "String", "namespaceURI" ),
             param( "String", "localName" ) );
        add( jClass, "StartElement", "StartElement", "StartElement", param( "String", "prefix" ), param( "String",
                                                                                                         "localName" ),
             param( "String", "namespaceURI" ) );

        jClass.print( sourceWriter );

        sourceWriter.close();
    }

    private void addField( JClass jClass, String fieldType, String fieldName, String initializer, boolean constant )
    {
        JField jField = new JField( new JType( fieldType ), fieldName );
        jField.setInitString( initializer );
        if ( constant )
        {
            jField.getModifiers().setFinal( true );
            jField.getModifiers().setStatic( true );
        }
        jClass.addField( jField );
    }

    private void add( JClass jClass, String name, String before, String after, JParameter... params )
    {
        List<String> names = new ArrayList<String>();

        JMethod jMethod = new JMethod( "write" + name );
        jMethod.addException( new JClass( "XMLStreamException" ) );

        for ( JParameter param : params )
        {
            jMethod.addParameter( param );
            names.add( param.getName() );
        }

        JSourceCode sc = jMethod.getSourceCode();
        if ( before != null )
        {
            sc.add( "before" + before + "();" );
        }

        sc.add( "out.write" + name + "( " + StringUtils.join( names.iterator(), ", " ) + " );" );

        if ( after != null )
        {
            sc.add( "after" + after + "();" );
        }

        jClass.addMethod( jMethod );
    }

    private static JParameter param( String type, String name )
    {
        return new JParameter( new JType( type ), name );
    }

}
