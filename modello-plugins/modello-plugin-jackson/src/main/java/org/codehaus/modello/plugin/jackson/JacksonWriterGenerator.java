package org.codehaus.modello.plugin.jackson;

/*
 * Copyright (c) 2004-2013, Codehaus.org
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
import org.codehaus.modello.plugin.java.metadata.JavaFieldMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlAssociationMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlFieldMetadata;

/**
 * @author <a href="mailto:simonetripodi@apache.org">Simone Tripodi</a>
 */
public class JacksonWriterGenerator
    extends AbstractJacksonGenerator
{

    private boolean requiresDomSupport;

    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

        requiresDomSupport = true;

        try
        {
            generateJacksonWriter();
        }
        catch ( IOException ex )
        {
            throw new ModelloException( "Exception while generating JSON Jackson Writer.", ex );
        }
    }

    private void generateJacksonWriter()
        throws ModelloException, IOException
    {
        Model objectModel = getModel();

        String packageName = objectModel.getDefaultPackageName( isPackageWithVersion(), getGeneratedVersion() )
            + ".io.jackson";

        String marshallerName = getFileName( "JacksonWriter" );

        JSourceWriter sourceWriter = newJSourceWriter( packageName, marshallerName );

        JClass jClass = new JClass( packageName + '.' + marshallerName );
        initHeader( jClass );

        jClass.addImport( "com.fasterxml.jackson.core.JsonFactory" );
        jClass.addImport( "com.fasterxml.jackson.core.JsonGenerator" );
        jClass.addImport( "com.fasterxml.jackson.core.JsonGenerator.Feature" );
        jClass.addImport( "java.io.IOException" );
        jClass.addImport( "java.io.OutputStream" );
        jClass.addImport( "java.io.OutputStreamWriter" );
        jClass.addImport( "java.io.Writer" );

        addModelImports( jClass, null );

        JField factoryField = new JField( new JClass( "JsonFactory" ), "factory" );
        factoryField.getModifiers().setFinal( true );
        factoryField.setInitString( "new JsonFactory()" );
        jClass.addField( factoryField );

        String root = objectModel.getRoot( getGeneratedVersion() );

        JConstructor jacksonWriterConstructor = new JConstructor( jClass );
        JSourceCode sc = jacksonWriterConstructor.getSourceCode();
        sc.add( "factory.enable( Feature.AUTO_CLOSE_JSON_CONTENT );" );
        sc.add( "factory.enable( Feature.AUTO_CLOSE_TARGET );" );
        sc.add( "factory.enable( Feature.ESCAPE_NON_ASCII );" );
        sc.add( "factory.enable( Feature.FLUSH_PASSED_TO_STREAM );" );
        sc.add( "factory.enable( Feature.QUOTE_FIELD_NAMES );" );
        sc.add( "factory.enable( Feature.QUOTE_NON_NUMERIC_NUMBERS );" );
        sc.add( "factory.disable( Feature.WRITE_NUMBERS_AS_STRINGS );" );

        jClass.addConstructor( jacksonWriterConstructor );

        writeAllClasses( objectModel, jClass );

        // ----------------------------------------------------------------------
        // DOM support
        // ----------------------------------------------------------------------

        if ( requiresDomSupport )
        {
            getLogger().warn( "Jackson DOM support requires auxiliary com.fasterxml.jackson.core:jackson-databind module!" );

            jClass.addImport( "com.fasterxml.jackson.databind.ObjectMapper" );

            sc.add( "factory.setCodec( new ObjectMapper() );" );
        }

        // ----------------------------------------------------------------------
        // Write the write( Writer, Model ) method which will do the unmarshalling.
        // ----------------------------------------------------------------------

        JMethod marshall = new JMethod( "write" );

        String rootElementParameterName = uncapitalise( root );
        marshall.addParameter( new JParameter( new JClass( "Writer" ), "writer" ) );
        marshall.addParameter( new JParameter( new JClass( root ), rootElementParameterName ) );

        marshall.addException( new JClass( "IOException" ) );

        sc = marshall.getSourceCode();

        sc.add( "JsonGenerator generator = factory.createGenerator( writer );" );

        sc.add( "generator.useDefaultPrettyPrinter();" );

        sc.add( "write" + root + "( " + rootElementParameterName + ", generator );" );

        sc.add( "generator.close();" );

        jClass.addMethod( marshall );

        // ----------------------------------------------------------------------
        // Write the write( OutputStream, Model ) method which will do the unmarshalling.
        // ----------------------------------------------------------------------

        marshall = new JMethod( "write" );

        marshall.addParameter( new JParameter( new JClass( "OutputStream" ), "stream" ) );
        marshall.addParameter( new JParameter( new JClass( root ), rootElementParameterName ) );

        marshall.addException( new JClass( "IOException" ) );

        sc = marshall.getSourceCode();

        sc.add( "write( new OutputStreamWriter( stream, "
                        + rootElementParameterName
                        + ".getModelEncoding() ), "
                        + rootElementParameterName
                        + " );" );

        jClass.addMethod( marshall );

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
        marshall.addParameter( new JParameter( new JClass( "JsonGenerator" ), "generator" ) );

        marshall.addException( new JClass( "IOException" ) );

        marshall.getModifiers().makePrivate();

        JSourceCode sc = marshall.getSourceCode();

        sc.add( "generator.writeStartObject();" );

        List<ModelField> modelFields = getFieldsForXml( modelClass, getGeneratedVersion() );

        // XML tags
        for ( ModelField field : modelFields )
        {
            XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

            JavaFieldMetadata javaFieldMetadata = (JavaFieldMetadata) field.getMetadata( JavaFieldMetadata.ID );

            String fieldTagName = resolveTagName( field, xmlFieldMetadata );

            String type = field.getType();

            String value = uncapClassName + "." + getPrefix( javaFieldMetadata ) + capitalise( field.getName() ) + "()";

            if ( field instanceof ModelAssociation )
            {
                ModelAssociation association = (ModelAssociation) field;

                if ( association.isOneMultiplicity() )
                {
                    sc.add( getValueChecker( type, value, association ) );

                    sc.add( "{" );
                    sc.addIndented( "generator.writeFieldName( \"" + fieldTagName + "\" );" );
                    sc.addIndented( "write" + association.getTo() + "( (" + association.getTo() + ") " + value + ", generator );" );
                    sc.add( "}" );
                }
                else
                {
                    //MANY_MULTIPLICITY

                    XmlAssociationMetadata xmlAssociationMetadata =
                        (XmlAssociationMetadata) association.getAssociationMetadata( XmlAssociationMetadata.ID );

                    type = association.getType();
                    String toType = association.getTo();

                    if ( ModelDefault.LIST.equals( type ) || ModelDefault.SET.equals( type ) )
                    {
                        sc.add( getValueChecker( type, value, association ) );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "generator.writeArrayFieldStart( \"" + fieldTagName + "\" );" );

                        if ( useJava5 )
                        {
                            sc.add( "for ( " + toType + " o : " + value + " )" );
                        }
                        else
                        {
                            sc.add( "for ( java.util.Iterator it = " + value + ".iterator(); it.hasNext(); )" );
                        }

                        sc.add( "{" );
                        sc.indent();

                        if ( !useJava5 )
                        {
                            sc.add( toType + " o = (" + toType + " ) it.next();" );
                        }

                        if ( isClassInModel( association.getTo(), modelClass.getModel() ) )
                        {
                            sc.add( "write" + toType + "( o, generator );" );
                        }
                        else
                        {
                            sc.add( "generator.writeObject( o ); " );
                        }

                        sc.unindent();
                        sc.add( "}" );

                        sc.add( "generator.writeEndArray();" );

                        sc.unindent();
                        sc.add( "}" );
                    }
                    else
                    {
                        //Map or Properties

                        sc.add( getValueChecker( type, value, field ) );

                        sc.add( "{" );
                        sc.indent();

                        if ( xmlAssociationMetadata.isMapExplode() )
                        {
                            sc.add( "generator.writeArrayFieldStart( \"" + fieldTagName + "\" );" );
                        }
                        else
                        {
                            sc.add( "generator.writeObjectFieldStart( \"" + fieldTagName + "\" );" );
                        }

                        StringBuilder entryTypeBuilder = new StringBuilder( "java.util.Map.Entry" );

                        if ( useJava5 )
                        {
                            entryTypeBuilder.append( '<' );

                            if ( association.getType().equals( ModelDefault.PROPERTIES ) )
                            {
                                entryTypeBuilder.append( "Object, Object" );
                            }
                            else
                            {
                                entryTypeBuilder.append( "String, " ).append( association.getTo() );
                            }

                            entryTypeBuilder.append( '>' );
                        }

                        if ( useJava5 )
                        {
                            sc.add( "for ( " + entryTypeBuilder + " entry : " + value + ".entrySet() )" );
                        }
                        else
                        {
                            sc.add( "for ( java.util.Iterator it = " + value + ".entrySet().iterator(); it.hasNext(); )" );
                        }

                        sc.add( "{" );
                        sc.indent();

                        if ( !useJava5 )
                        {
                            sc.add( entryTypeBuilder + " entry = (" + entryTypeBuilder + ") it.next();" );
                        }

                        sc.add( "final String key = String.valueOf( entry.getKey() );" );
                        sc.add( "final String value = String.valueOf( entry.getValue() );" );

                        if ( xmlAssociationMetadata.isMapExplode() )
                        {
                            sc.add( "generator.writeStartObject();" );
                            sc.add( "generator.writeStringField( \"key\", key );" );
                            sc.add( "generator.writeStringField( \"value\", value );" );
                            sc.add( "generator.writeEndObject();" );
                        }
                        else
                        {
                            sc.add( "generator.writeStringField( key, value );" );
                        }

                        sc.unindent();
                        sc.add( "}" );

                        if ( xmlAssociationMetadata.isMapExplode() )
                        {
                            sc.add( "generator.writeEndArray();" );
                        }
                        else
                        {
                            sc.add( "generator.writeEndObject();" );
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
                        getLogger().warn( "Xpp3Dom not supported for "
                                          + modelClass.getName()
                                          + "#"
                                          + field.getName()
                                          + ", it will be treated as a regular Java Object." );
                    }

                    requiresDomSupport = true;

                    sc.addIndented( "generator.writeObjectField( \"" + fieldTagName + "\", " + value + " );" );
                }
                else
                {
                    sc.addIndented( "generator.writeObjectField( \"" + fieldTagName + "\", " + value + " );" );
                }
                sc.add( "}" );
            }
        }

        sc.add( "generator.writeEndObject();" );

        jClass.addMethod( marshall );
    }

}
