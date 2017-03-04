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
import org.codehaus.modello.plugin.java.metadata.JavaFieldMetadata;
import org.codehaus.modello.plugin.model.ModelClassMetadata;
import org.codehaus.modello.plugins.xml.AbstractXmlJavaGenerator;
import org.codehaus.modello.plugins.xml.metadata.XmlAssociationMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlFieldMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlModelMetadata;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * <p>
 * Generate a writer that uses Dom4J.
 * </p>
 * TODO: chunks are lifted from xpp3, including the tests. Can we abstract it in some way?
 *
 * @author <a href="mailto:brett@codehaus.org">Brett Porter</a>
 */
public class Dom4jWriterGenerator
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

        String packageName = objectModel.getDefaultPackageName( isPackageWithVersion(), getGeneratedVersion() )
            + ".io.dom4j";

        String marshallerName = getFileName( "Dom4jWriter" );

        JSourceWriter sourceWriter = newJSourceWriter( packageName, marshallerName );

        JClass jClass = new JClass( packageName + '.' + marshallerName );
        initHeader( jClass );
        suppressAllWarnings( objectModel, jClass );

        jClass.addImport( "java.io.OutputStream" );
        jClass.addImport( "java.io.Writer" );
        jClass.addImport( "java.util.Arrays" );
        jClass.addImport( "java.util.Iterator" );
        jClass.addImport( "java.util.Locale" );
        jClass.addImport( "java.text.DateFormat" );
        jClass.addImport( "org.dom4j.Document" );
        jClass.addImport( "org.dom4j.DocumentException" );
        jClass.addImport( "org.dom4j.DocumentFactory" );
        jClass.addImport( "org.dom4j.Element" );
        jClass.addImport( "org.dom4j.io.OutputFormat" );
        jClass.addImport( "org.dom4j.io.XMLWriter" );

        addModelImports( jClass, null );

        String root = objectModel.getRoot( getGeneratedVersion() );

        ModelClass rootClass = objectModel.getClass( root, getGeneratedVersion() );

        String rootElement = resolveTagName( rootClass );
        String variableName = uncapitalise( root );

        // ----------------------------------------------------------------------
        // Write the write( Reader, Model ) method which will do the unmarshalling.
        // ----------------------------------------------------------------------

        JMethod marshall = new JMethod( "write" );

        marshall.addParameter( new JParameter( new JClass( "Writer" ), "writer" ) );
        marshall.addParameter( new JParameter( new JClass( root ), variableName ) );

        marshall.addException( new JClass( "java.io.IOException" ) );

        JSourceCode sc = marshall.getSourceCode();

        sc.add( "Document document = new DocumentFactory().createDocument();" );

        sc.add( "write" + root + "( " + variableName + ", \"" + rootElement + "\", document );" );

        // TODO: pretty printing optional
        sc.add( "OutputFormat format = OutputFormat.createPrettyPrint();" );
        sc.add( "format.setLineSeparator( System.getProperty( \"line.separator\" ) );" );
        sc.add( "XMLWriter serializer = new XMLWriter( writer, format );" );

        sc.add( "serializer.write( document );" );

        jClass.addMethod( marshall );

        // ----------------------------------------------------------------------
        // Write the write( OutputStream, Model ) method which will do the unmarshalling.
        // ----------------------------------------------------------------------

        marshall = new JMethod( "write" );

        marshall.addParameter( new JParameter( new JClass( "OutputStream" ), "stream" ) );
        marshall.addParameter( new JParameter( new JClass( root ), variableName ) );

        marshall.addException( new JClass( "java.io.IOException" ) );

        sc = marshall.getSourceCode();

        sc.add( "Document document = new DocumentFactory().createDocument();" );

        sc.add( "write" + root + "( " + variableName + ", \"" + rootElement + "\", document );" );

        // TODO: pretty printing optional
        sc.add( "OutputFormat format = OutputFormat.createPrettyPrint();" );
        sc.add( "format.setLineSeparator( System.getProperty( \"line.separator\" ) );" );
        sc.add( "format.setEncoding( " + variableName + ".getModelEncoding() );" );
        sc.add( "XMLWriter serializer = new XMLWriter( stream, format );" );

        sc.add( "serializer.write( document );" );

        jClass.addMethod( marshall );

        writeAllClasses( objectModel, jClass );

        if ( requiresDomSupport )
        {
            jClass.addImport( "org.codehaus.plexus.util.xml.Xpp3Dom" );
            writeDomHelpers( jClass );
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

        ModelClassMetadata classMetadata = (ModelClassMetadata) modelClass.getMetadata( ModelClassMetadata.ID );

        marshall.addParameter( new JParameter( new JClass( "org.dom4j.Branch" ), "parentElement" ) );

        marshall.addException( new JClass( "java.io.IOException" ) );

        JSourceCode sc = marshall.getSourceCode();

        sc.add( "if ( " + uncapClassName + " != null )" );

        sc.add( "{" );
        sc.indent();

        XmlModelMetadata xmlModelMetadata = (XmlModelMetadata) modelClass.getModel().getMetadata( XmlModelMetadata.ID );

        // add namespace information for root element only
        if ( classMetadata.isRootElement() && ( xmlModelMetadata.getNamespace() != null ) )
        {
            String namespace = xmlModelMetadata.getNamespace( getGeneratedVersion() );
            sc.add( "Element element = parentElement.addElement( tagName, \"" + namespace + "\" );" );

            if ( xmlModelMetadata.getSchemaLocation() != null )
            {
                String url = xmlModelMetadata.getSchemaLocation( getGeneratedVersion() );

                sc.add( "element.addAttribute( \"xmlns:xsi\", \"http://www.w3.org/2001/XMLSchema-instance\" );" );
                sc.add(
                    "element.addAttribute( \"xsi:schemaLocation\", \"" + namespace + " " + url + "\" );" );
            }
        }
        else
        {
            sc.add( "Element element = parentElement.addElement( tagName );" );
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
                sc.addIndented( "element.addAttribute( \"" + fieldTagName + "\", "
                                + getValue( field.getType(), value, xmlFieldMetadata ) + " );" );
                sc.add( "}" );
            }
        }

        if ( contentField != null )
        {
            XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) contentField.getMetadata( XmlFieldMetadata.ID );
            sc.add( "element.setText( " + getValue( contentField.getType(), contentValue, xmlFieldMetadata ) + " );" );
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

            if ( !xmlFieldMetadata.isAttribute() )
            {
                processField( field, xmlFieldMetadata, uncapClassName, sc, modelClass, jClass );
            }
        }

        sc.unindent();
        sc.add( "}" );

        jClass.addMethod( marshall );
    }

    private void processField( ModelField field, XmlFieldMetadata xmlFieldMetadata, String uncapClassName,
                               JSourceCode sc, ModelClass modelClass, JClass jClass )
        throws ModelloException
    {
        JavaFieldMetadata javaFieldMetadata = (JavaFieldMetadata) field.getMetadata( JavaFieldMetadata.ID );

        String fieldTagName = resolveTagName( field, xmlFieldMetadata );

        String type = field.getType();

        String value = uncapClassName + "." + getPrefix( javaFieldMetadata ) + capitalise( field.getName() ) + "()";

        if ( field instanceof ModelAssociation )
        {
            ModelAssociation association = (ModelAssociation) field;

            String associationName = association.getName();

            if ( association.isOneMultiplicity() )
            {
                sc.add( getValueChecker( type, value, association ) );

                sc.add( "{" );
                sc.addIndented( "write" + association.getTo() + "( " + value + ", \"" + fieldTagName + "\", element );" );
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

                    sc.add( "Element listElement = element;" );

                    if ( wrappedItems )
                    {
                        sc.add( "listElement = element.addElement( \"" + fieldTagName + "\" );" );
                    }

                    sc.add( "for ( Iterator iter = " + value + ".iterator(); iter.hasNext(); )" );

                    sc.add( "{" );
                    sc.indent();

                    if ( isClassInModel( association.getTo(), modelClass.getModel() ) )
                    {
                        sc.add( toType + " o = (" + toType + ") iter.next();" );

                        sc.add( "write" + toType + "( o, \"" + valuesTagName + "\", listElement );" );
                    }
                    else
                    {
                        sc.add( toType + " " + singular( uncapitalise( field.getName() ) ) + " = (" + toType
                            + ") iter.next();" );

                        sc.add( "listElement.addElement( \"" + valuesTagName + "\" ).setText( "
                            + singular( uncapitalise( field.getName() ) ) + " );" );
                    }

                    sc.unindent();
                    sc.add( "}" );

                    sc.unindent();
                    sc.add( "}" );
                }
                else
                {
                    //Map or Properties

                    sc.add( getValueChecker( type, value, field ) );

                    sc.add( "{" );
                    sc.indent();

                    sc.add( "Element listElement = element;" );

                    if ( wrappedItems )
                    {
                        sc.add( "listElement = element.addElement( \"" + fieldTagName + "\" );" );
                    }

                    sc.add( "for ( Iterator iter = " + value + ".keySet().iterator(); iter.hasNext(); )" );

                    sc.add( "{" );
                    sc.indent();

                    sc.add( "String key = (String) iter.next();" );

                    sc.add( "String value = (String) " + value + ".get( key );" );

                    if ( xmlAssociationMetadata.isMapExplode() )
                    {
                        sc.add( "Element assocElement = listElement.addElement( \"" + singular( associationName )
                            + "\" );" );
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

                requiresDomSupport = true;
            }
            else
            {
                sc.add( "element.addElement( \"" + fieldTagName + "\" ).setText( "
                    + getValue( field.getType(), value, xmlFieldMetadata ) + " );" );
            }

            sc.unindent();
            sc.add( "}" );
        }
    }

    private void writeDomHelpers( JClass jClass )
    {
        JMethod method = new JMethod( "writeXpp3DomToElement" );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "Xpp3Dom" ), "xpp3Dom" ) );
        method.addParameter( new JParameter( new JClass( "Element" ), "parentElement" ) );

        JSourceCode sc = method.getSourceCode();

        sc.add( "Element element = parentElement.addElement( xpp3Dom.getName() );" );

        sc.add( "if ( xpp3Dom.getValue() != null )" );
        sc.add( "{" );
        sc.addIndented( "element.setText( xpp3Dom.getValue() );" );
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
