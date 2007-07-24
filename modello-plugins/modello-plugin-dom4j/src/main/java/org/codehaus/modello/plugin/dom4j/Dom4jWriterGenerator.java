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
import org.codehaus.modello.plugin.java.JavaFieldMetadata;
import org.codehaus.modello.plugin.java.javasource.JClass;
import org.codehaus.modello.plugin.java.javasource.JMethod;
import org.codehaus.modello.plugin.java.javasource.JParameter;
import org.codehaus.modello.plugin.java.javasource.JSourceCode;
import org.codehaus.modello.plugin.java.javasource.JSourceWriter;
import org.codehaus.modello.plugin.model.ModelClassMetadata;
import org.codehaus.modello.plugins.xml.XmlAssociationMetadata;
import org.codehaus.modello.plugins.xml.XmlFieldMetadata;
import org.codehaus.plexus.util.WriterFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Properties;

/**
 * Generate a writer that uses Dom4J.
 * <p/>
 * TODO: chunks are lifted from xpp3, including the tests. Can we abstract it in some way?
 *
 * @author <a href="mailto:brett@codehaus.org">Brett Porter</a>
 */
public class Dom4jWriterGenerator
    extends AbstractModelloGenerator
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
            throw new ModelloException( "Exception while generating Dom4j Writer.", ex );
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

        Writer writer = WriterFactory.newPlatformWriter( f );

        JSourceWriter sourceWriter = new JSourceWriter( writer );

        JClass jClass = new JClass( marshallerName );

        jClass.setPackageName( packageName );

        jClass.addImport( "java.io.Writer" );

        jClass.addImport( "java.util.Arrays" );

        jClass.addImport( "java.util.Iterator" );

        jClass.addImport( "java.util.Locale" );

        jClass.addImport( "java.text.DateFormat" );

        jClass.addImport( "org.codehaus.plexus.util.xml.Xpp3Dom" );

        jClass.addImport( "org.dom4j.Document" );

        jClass.addImport( "org.dom4j.DocumentException" );

        jClass.addImport( "org.dom4j.DocumentFactory" );

        jClass.addImport( "org.dom4j.Element" );

        jClass.addImport( "org.dom4j.io.OutputFormat" );

        jClass.addImport( "org.dom4j.io.XMLWriter" );

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

        sc.add( "Document document = new DocumentFactory().createDocument();" );

        sc.add( "write" + root + "( " + rootElement + ", \"" + rootElement + "\", document );" );

        // TODO: pretty printing optional
        sc.add( "OutputFormat format = OutputFormat.createPrettyPrint();" );
        sc.add( "format.setLineSeparator( System.getProperty( \"line.separator\" ) );" );
        sc.add( "XMLWriter serializer = new XMLWriter( writer, format );" );

        sc.add( "serializer.write( document );" );

        jClass.addMethod( marshall );

        writeAllClasses( objectModel, jClass );

        writeHelpers( jClass );

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

        JMethod marshall = new JMethod( null, "write" + className );

        marshall.addParameter( new JParameter( new JClass( className ), uncapClassName ) );

        marshall.addParameter( new JParameter( new JClass( "String" ), "tagName" ) );

        ModelClassMetadata metadata = (ModelClassMetadata) modelClass.getMetadata( ModelClassMetadata.ID );

        if ( metadata.isRootElement() )
        {
            marshall.addParameter( new JParameter( new JClass( "Document" ), "parentElement" ) );
        }
        else
        {
            marshall.addParameter( new JParameter( new JClass( "Element" ), "parentElement" ) );
        }

        marshall.addException( new JClass( "java.io.IOException" ) );

        marshall.getModifiers().makePrivate();

        JSourceCode sc = marshall.getSourceCode();

        sc.add( "if ( " + uncapClassName + " != null )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "Element element = parentElement.addElement( tagName );" );

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

                sc.add(
                    "element.addAttribute( \"" + fieldTagName + "\", " + getValue( field.getType(), value ) + " );" );

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

            if ( !fieldMetadata.isAttribute() )
            {
                processField( field, fieldMetadata, uncapClassName, sc, modelClass, jClass );
            }
        }

        sc.unindent();

        sc.add( "}" );

        jClass.addMethod( marshall );
    }

    private void processField( ModelField field, XmlFieldMetadata fieldMetadata, String uncapClassName, JSourceCode sc,
                               ModelClass modelClass, JClass jClass )
    {
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

        if ( field instanceof ModelAssociation )
        {
            ModelAssociation association = (ModelAssociation) field;

            String associationName = association.getName();

            if ( ModelAssociation.ONE_MULTIPLICITY.equals( association.getMultiplicity() ) )
            {
                sc.add( getValueChecker( type, value, association ) );

                sc.add( "{" );

                sc.indent();

                sc.add( "write" + association.getTo() + "( " + value + ", \"" + fieldTagName + "\", element );" );

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

                    sc.add( "Element listElement = element;" );

                    if ( wrappedList )
                    {
                        sc.add( "listElement = element.addElement( \"" + fieldTagName + "\" );" );
                    }

                    sc.add( "for ( Iterator iter = " + value + ".iterator(); iter.hasNext(); )" );

                    sc.add( "{" );

                    sc.indent();

                    if ( isClassInModel( association.getTo(), modelClass.getModel() ) )
                    {
                        sc.add( toType + " o = (" + toType + ") iter.next();" );

                        sc.add( "write" + toType + "( o, \"" + singularTagName + "\", listElement );" );
                    }
                    else
                    {
                        sc.add( toType + " " + singular( uncapitalise( field.getName() ) ) + " = (" + toType +
                            ") iter.next();" );

                        sc.add( "listElement.addElement( \"" + singularTagName + "\" ).setText( " +
                            singular( uncapitalise( field.getName() ) ) + " );" );
                    }

                    sc.unindent();

                    sc.add( "}" );

                    sc.unindent();

                    sc.add( "}" );
                }
                else
                {
                    //Map or Properties

                    XmlAssociationMetadata xmlAssociationMetadata =
                        (XmlAssociationMetadata) association.getAssociationMetadata( XmlAssociationMetadata.ID );

                    sc.add( getValueChecker( type, value, field ) );

                    sc.add( "{" );

                    sc.indent();

                    sc.add( "Element listElement = element;" );

                    if ( wrappedList )
                    {
                        sc.add( "listElement = element.addElement( \"" + fieldTagName + "\" );" );
                    }

                    sc.add( "for ( Iterator iter = " + value + ".keySet().iterator(); iter.hasNext(); )" );

                    sc.add( "{" );

                    sc.indent();

                    sc.add( "String key = (String) iter.next();" );

                    sc.add( "String value = (String) " + value + ".get( key );" );

                    if ( XmlAssociationMetadata.EXPLODE_MODE.equals( xmlAssociationMetadata.getMapStyle() ) )
                    {
                        sc.add( "Element assocElement = listElement.addElement( \"" + singular( associationName ) +
                            "\" );" );
                        sc.add( "assocElement.addElement( \"key\" ).setText( key );" );
                        sc.add( "assocElement.addElement( \"value\" ).setText( value );" );
                    }
                    else
                    {
                        sc.add( "listElement.addElement( key ).setText( value );" );
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
            sc.add( getValueChecker( type, value, field ) );

            sc.add( "{" );

            sc.indent();

            if ( "DOM".equals( field.getType() ) )
            {
                sc.add( "writeXpp3DomToElement( (Xpp3Dom) " + value + ", element );" );
            }
            else
            {
                sc.add( "element.addElement( \"" + fieldTagName + "\" ).setText( " +
                    getValue( field.getType(), value ) + " );" );
            }

            sc.unindent();

            sc.add( "}" );
        }
    }

    protected String getFileName( String suffix )
        throws ModelloException
    {
        String name = getModel().getName();

        return name + suffix;
    }

    private String getValue( String type, String initialValue )
    {
        String textValue = initialValue;

        if ( "Date".equals( type ) )
        {
            textValue = "DateFormat.getDateTimeInstance( DateFormat.FULL, DateFormat.FULL , Locale.US ).format( " +
                textValue + " )";
        }
        else if ( !"String".equals( type ) )
        {
            textValue = "String.valueOf( " + textValue + " )";
        }

        return textValue;
    }

    private String getValueChecker( String type, String value, ModelField field )
    {
        String retVal;
        if ( "boolean".equals( type ) || "double".equals( type ) || "float".equals( type ) || "int".equals( type ) ||
            "long".equals( type ) || "short".equals( type ) )
        {
            retVal = "if ( " + value + " != " + field.getDefaultValue() + " )";
        }
        else if ( "char".equals( type ) )
        {
            retVal = "if ( " + value + " != '" + field.getDefaultValue() + "' )";
        }
        else if ( ModelDefault.LIST.equals( type ) || ModelDefault.SET.equals( type ) ||
            ModelDefault.MAP.equals( type ) || ModelDefault.PROPERTIES.equals( type ) )
        {
            retVal = "if ( " + value + " != null && " + value + ".size() > 0 )";
        }
        else if ( "String".equals( type ) && field.getDefaultValue() != null )
        {
            retVal = "if ( " + value + " != null && !" + value + ".equals( \"" + field.getDefaultValue() + "\" ) )";
        }
        else if ( "Date".equals( type ) && field.getDefaultValue() != null )
        {
            retVal = "if ( " + value + " != null && !" + value + ".equals( \"" + field.getDefaultValue() + "\" ) )";
        }
        else
        {
            retVal = "if ( " + value + " != null )";
        }
        return retVal;
    }

    private void writeHelpers( JClass jClass )
    {
        JMethod method = new JMethod( null, "writeXpp3DomToElement" );

        method.addParameter( new JParameter( new JClass( "Xpp3Dom" ), "xpp3Dom" ) );

        method.addParameter( new JParameter( new JClass( "Element" ), "parentElement" ) );

        JSourceCode sc = method.getSourceCode();

        sc.add( "Element element = parentElement.addElement( xpp3Dom.getName() );" );

        sc.add( "if ( xpp3Dom.getValue() != null )" );
        sc.add( "{" );
        sc.indent();

        sc.add( "element.setText( xpp3Dom.getValue() );" );

        sc.unindent();
        sc.add( "}" );

        sc.add( "for ( Iterator i = Arrays.asList( xpp3Dom.getAttributeNames() ).iterator(); i.hasNext(); )" );
        sc.add( "{" );
        sc.indent();

        sc.add( "String name = (String) i.next();" );
        sc.add( "element.addAttribute( name, xpp3Dom.getAttribute( name ) );" );

        sc.unindent();
        sc.add( "}" );

        sc.add( "for ( Iterator i = Arrays.asList( xpp3Dom.getChildren() ).iterator(); i.hasNext(); )" );
        sc.add( "{" );
        sc.indent();

        sc.add( "Xpp3Dom child = (Xpp3Dom) i.next();" );
        sc.add( "writeXpp3DomToElement( child, element );" );

        sc.unindent();
        sc.add( "}" );

        jClass.addMethod( method );
    }
}
