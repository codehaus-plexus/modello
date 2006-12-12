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
import org.codehaus.modello.model.*;
import org.codehaus.modello.plugin.java.JavaFieldMetadata;
import org.codehaus.modello.plugin.java.javasource.*;
import org.codehaus.modello.plugin.model.ModelClassMetadata;
import org.codehaus.modello.plugins.xml.XmlAssociationMetadata;
import org.codehaus.modello.plugins.xml.XmlClassMetadata;
import org.codehaus.modello.plugins.xml.XmlFieldMetadata;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl </a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse </a>
 * @version $Id: StaxWriterGenerator.java 675 2006-11-16 10:58:59Z brett $
 */
public class StaxWriterGenerator
    extends AbstractStaxGenerator
{
    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

        try
        {
            generateStaxWriter();
        }
        catch ( IOException ex )
        {
            throw new ModelloException( "Exception while generating StAX Writer.", ex );
        }
    }

    private void generateStaxWriter()
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

        packageName += ".io.stax";

        String directory = packageName.replace( '.', '/' );

        String marshallerName = getFileName( "StaxWriter" );

        File f = new File( new File( getOutputDirectory(), directory ), marshallerName + ".java" );

        if ( !f.getParentFile().exists() )
        {
            f.getParentFile().mkdirs();
        }

        FileWriter writer = new FileWriter( f );

        JSourceWriter sourceWriter = new JSourceWriter( writer );

        JClass jClass = new JClass( marshallerName );

        jClass.setPackageName( packageName );

        jClass.addImport( "java.io.Writer" );

        jClass.addImport( "java.text.DateFormat" );

        jClass.addImport( "java.util.Iterator" );

        jClass.addImport( "java.util.Locale" );

        jClass.addImport( "javax.xml.stream.*" );

        jClass.addImport( "javanet.staxutils.IndentingXMLStreamWriter" );

        jClass.addImport( "org.codehaus.plexus.util.xml.Xpp3Dom" );

        addModelImports( jClass, null );

        jClass.addField( new JField( JType.Int, "curId" ) );
        jClass.addField( new JField( new JType( "java.util.Map" ), "idMap" ) );
        JConstructor constructor = new JConstructor( jClass );
        constructor.getSourceCode().add( "idMap = new java.util.HashMap();" );
        jClass.addConstructor( constructor );

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

        String rootElementParameterName = uncapitalise( root );

        marshall.addParameter( new JParameter( new JClass( root ), rootElementParameterName ) );

        marshall.addException( new JClass( "java.io.IOException" ) );

        marshall.addException( new JClass( "XMLStreamException" ) );

        JSourceCode sc = marshall.getSourceCode();

        sc.add( "XMLOutputFactory factory = XMLOutputFactory.newInstance();" );

        sc.add(
            "IndentingXMLStreamWriter serializer = new IndentingXMLStreamWriter( factory.createXMLStreamWriter( writer ) );" );

        // TODO: re-enable when StAX and/or Woodstox support writing windows line endings in their current versions
//        sc.add( "serializer.setNewLine( serializer.getLineSeparator() );" );

        sc.add( "serializer.writeStartDocument( " + rootElementParameterName + ".getModelEncoding(), \"1.0\" );" );

        sc.add( "write" + root + "( " + rootElementParameterName + ", \"" + rootElement + "\", serializer );" );

        sc.add( "serializer.writeEndDocument();" );

        jClass.addMethod( marshall );

        createWriteDomMethod( jClass );

        writeAllClasses( objectModel, jClass );

        jClass.print( sourceWriter );

        writer.flush();

        writer.close();
    }

    private void writeAllClasses( Model objectModel, JClass jClass )
        throws ModelloException
    {
        for ( Iterator i = objectModel.getClasses( getGeneratedVersion() ).iterator(); i.hasNext(); )
        {
            ModelClass clazz = (ModelClass) i.next();

            writeClass( clazz, jClass );
        }
    }

    private void writeClass( ModelClass modelClass, JClass jClass )
        throws ModelloException
    {
        String className = modelClass.getName();

        String uncapClassName = uncapitalise( className );

        JMethod marshall = new JMethod( null, "write" + className );

        marshall.addParameter( new JParameter( new JClass( className ), uncapClassName ) );

        marshall.addParameter( new JParameter( new JClass( "String" ), "tagName" ) );

        marshall.addParameter( new JParameter( new JClass( "XMLStreamWriter" ), "serializer" ) );

        marshall.addException( new JClass( "java.io.IOException" ) );

        marshall.addException( new JClass( "XMLStreamException" ) );

        marshall.getModifiers().makePrivate();

        JSourceCode sc = marshall.getSourceCode();

        sc.add( "if ( " + uncapClassName + " != null )" );

        sc.add( "{" );

        sc.indent();

        XmlClassMetadata metadata = (XmlClassMetadata) modelClass.getMetadata( XmlClassMetadata.ID );
        String namespace = null;
        if ( metadata.getNamespace() != null )
        {
            namespace = StringUtils.replace( metadata.getNamespace(), "${version}", getGeneratedVersion().toString() );
            sc.add( "serializer.setDefaultNamespace( \"" + namespace + "\" );" );
        }

        sc.add( "serializer.writeStartElement( tagName );" );

        if ( namespace != null )
        {
            sc.add( "serializer.writeDefaultNamespace( \"" + namespace + "\" );" );

            if ( metadata.getSchemaLocation() != null )
            {
                String url =
                    StringUtils.replace( metadata.getSchemaLocation(), "${version}", getGeneratedVersion().toString() );

                sc.add( "serializer.setPrefix( \"xsi\", \"http://www.w3.org/2001/XMLSchema-instance\" );" );
                sc.add( "serializer.writeNamespace( \"xsi\", \"http://www.w3.org/2001/XMLSchema-instance\" );" );
                sc.add(
                    "serializer.writeAttribute( \"http://www.w3.org/2001/XMLSchema-instance\", \"schemaLocation\", \"" +
                        namespace + " " + url + "\" );" );
            }
        }

        if ( isAssociationPartToClass( modelClass ) )
        {
            if ( modelClass.getIdentifierFields( getGeneratedVersion() ).size() != 1 )
            {
                writeIdMapCheck( sc, uncapClassName, "modello.id" );
            }
        }

        // XML attributes
        for ( Iterator i = modelClass.getAllFields( getGeneratedVersion(), true ).iterator(); i.hasNext(); )
        {
            ModelField field = (ModelField) i.next();

            XmlFieldMetadata fieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

            String fieldTagName = fieldMetadata.getTagName();

            if ( fieldTagName == null )
            {
                fieldTagName = field.getName();
            }

            String type = field.getType();

            String value = getFieldValue( uncapClassName, field );

            if ( fieldMetadata.isAttribute() )
            {
                sc.add( getValueChecker( type, value, field ) );

                sc.add( "{" );

                sc.indent();

                sc.add( "serializer.writeAttribute( \"" + fieldTagName + "\", " +
                    getValue( field.getType(), value, fieldMetadata ) + " );" );

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

            String value = getFieldValue( uncapClassName, field );

            if ( fieldMetadata.isAttribute() )
            {
                continue;
            }

            if ( field instanceof ModelAssociation )
            {
                ModelAssociation association = (ModelAssociation) field;

                String associationName = association.getName();

                ModelField referenceIdentifierField = getReferenceIdentifierField( association );

                if ( ModelAssociation.ONE_MULTIPLICITY.equals( association.getMultiplicity() ) )
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
                        sc.add( "write" + association.getTo() + "( (" + association.getTo() + ") " + value + ", \"" +
                            fieldTagName + "\", serializer );" );
                    }

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

                        if ( wrappedList )
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
                                sc.add( "serializer.writeStartElement( \"" + singularTagName + "\" );" );

                                writeElementAttribute( sc, referenceIdentifierField, "o" );

                                sc.add( "serializer.writeEndElement();" );
                            }
                            else
                            {
                                sc.add( "write" + toType + "( o, \"" + singularTagName + "\", serializer );" );
                            }
                        }
                        else
                        {
                            sc.add( toType + " " + singular( uncapitalise( field.getName() ) ) + " = (" + toType +
                                ") iter.next();" );

                            sc.add( "serializer.writeStartElement( " + "\"" + singularTagName + "\" );" );
                            sc.add(
                                "serializer.writeCharacters( " + singular( uncapitalise( field.getName() ) ) + " );" );
                            sc.add( "serializer.writeEndElement();" );
                        }

                        sc.unindent();

                        sc.add( "}" );

                        if ( wrappedList )
                        {
                            sc.add( "serializer.writeEndElement();" );
                        }

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

                        if ( wrappedList )
                        {
                            sc.add( "serializer.writeStartElement( " + "\"" + fieldTagName + "\" );" );
                        }

                        sc.add( "for ( Iterator iter = " + value + ".keySet().iterator(); iter.hasNext(); )" );

                        sc.add( "{" );

                        sc.indent();

                        sc.add( "String key = (String) iter.next();" );

                        sc.add( "String value = (String) " + value + ".get( key );" );

                        if ( XmlAssociationMetadata.EXPLODE_MODE.equals( xmlAssociationMetadata.getMapStyle() ) )
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

                        if ( wrappedList )
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
                    sc.add( "writeDom( (Xpp3Dom) " + value + ", serializer );" );
                }
                else
                {
                    sc.add( "serializer.writeStartElement( " + "\"" + fieldTagName + "\" );" );
                    sc.add(
                        "serializer.writeCharacters( " + getValue( field.getType(), value, fieldMetadata ) + " );" );
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
        sc.indent();

        sc.add( "serializer.writeAttribute( \"" + attributeName + "\", (String) idMap.get( " + value + " ) );" );

        sc.unindent();
        sc.add( "}" );
    }

    private String getFieldValue( String uncapClassName, ModelField field )
    {
        JavaFieldMetadata javaFieldMetadata = (JavaFieldMetadata) field.getMetadata( JavaFieldMetadata.ID );

        return uncapClassName + "." + getPrefix( javaFieldMetadata ) + capitalise( field.getName() ) + "()";
    }

    private void createWriteDomMethod( JClass jClass )
    {
        JMethod method = new JMethod( "writeDom" );

        method.addParameter( new JParameter( new JType( "Xpp3Dom" ), "dom" ) );
        method.addParameter( new JParameter( new JType( "XMLStreamWriter" ), "serializer" ) );

        method.addException( new JClass( "XMLStreamException" ) );

        JSourceCode sc = method.getSourceCode();

        sc.add( "serializer.writeStartElement( dom.getName() );" );

        sc.add( "String[] attributeNames = dom.getAttributeNames();" );
        sc.add( "for ( int i = 0; i < attributeNames.length; i++ )" );
        sc.add( "{" );

        sc.indent();
        sc.add( "String attributeName = attributeNames[i];" );
        sc.add( "serializer.writeAttribute( attributeName, dom.getAttribute( attributeName ) );" );
        sc.unindent();

        sc.add( "}" );
        sc.add( "Xpp3Dom[] children = dom.getChildren();" );
        sc.add( "for ( int i = 0; i < children.length; i++ )" );
        sc.add( "{" );

        sc.indent();
        sc.add( "writeDom( children[i], serializer );" );
        sc.unindent();

        sc.add( "}" );

        sc.add( "String value = dom.getValue();" );
        sc.add( "if ( value != null )" );
        sc.add( "{" );

        sc.indent();
        sc.add( "serializer.writeCharacters( value );" );
        sc.unindent();

        sc.add( "}" );

        sc.add( "serializer.writeEndElement();" );

        jClass.addMethod( method );
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
}
