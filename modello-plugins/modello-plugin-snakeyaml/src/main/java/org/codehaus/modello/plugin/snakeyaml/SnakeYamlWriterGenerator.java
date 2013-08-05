package org.codehaus.modello.plugin.snakeyaml;

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
public class SnakeYamlWriterGenerator
    extends AbstractSnakeYamlGenerator
{

    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

        try
        {
            generateSnakeYamlWriter();
        }
        catch ( IOException ex )
        {
            throw new ModelloException( "Exception while generating SnakeYaml Writer.", ex );
        }
    }

    private void generateSnakeYamlWriter()
        throws ModelloException, IOException
    {
        Model objectModel = getModel();

        String packageName = objectModel.getDefaultPackageName( isPackageWithVersion(), getGeneratedVersion() )
            + ".io.snakeyaml";

        String marshallerName = getFileName( "SnakeYamlWriter" );

        JSourceWriter sourceWriter = newJSourceWriter( packageName, marshallerName );

        JClass jClass = new JClass( packageName + '.' + marshallerName );
        initHeader( jClass );

        jClass.addImport( "org.yaml.snakeyaml.DumperOptions" );
        jClass.addImport( "org.yaml.snakeyaml.DumperOptions.Version" );
        jClass.addImport( "org.yaml.snakeyaml.emitter.Emitable" );
        jClass.addImport( "org.yaml.snakeyaml.emitter.Emitter" );
        jClass.addImport( "org.yaml.snakeyaml.events.DocumentEndEvent" );
        jClass.addImport( "org.yaml.snakeyaml.events.DocumentStartEvent" );
        jClass.addImport( "org.yaml.snakeyaml.events.ImplicitTuple" );
        jClass.addImport( "org.yaml.snakeyaml.events.MappingEndEvent" );
        jClass.addImport( "org.yaml.snakeyaml.events.MappingStartEvent" );
        jClass.addImport( "org.yaml.snakeyaml.events.ScalarEvent" );
        jClass.addImport( "org.yaml.snakeyaml.events.SequenceEndEvent" );
        jClass.addImport( "org.yaml.snakeyaml.events.SequenceStartEvent" );
        jClass.addImport( "org.yaml.snakeyaml.events.StreamEndEvent" );
        jClass.addImport( "org.yaml.snakeyaml.events.StreamStartEvent" );
        jClass.addImport( "java.io.IOException" );
        jClass.addImport( "java.io.OutputStream" );
        jClass.addImport( "java.io.OutputStreamWriter" );
        jClass.addImport( "java.io.Writer" );

        addModelImports( jClass, null );

        JField factoryField = new JField( new JClass( "DumperOptions" ), "dumperOptions" );
        factoryField.getModifiers().setFinal( true );
        factoryField.setInitString( "new DumperOptions()" );
        jClass.addField( factoryField );

        JConstructor jacksonWriterConstructor = new JConstructor( jClass );
        JSourceCode sc = jacksonWriterConstructor.getSourceCode();
        sc.add( "dumperOptions.setAllowUnicode( true );" );
        sc.add( "dumperOptions.setPrettyFlow( true );" );
        sc.add( "dumperOptions.setVersion( Version.V1_1 );" );

        jClass.addConstructor( jacksonWriterConstructor );

        String root = objectModel.getRoot( getGeneratedVersion() );

        // ----------------------------------------------------------------------
        // Write the write( Writer, Model ) method which will do the unmarshalling.
        // ----------------------------------------------------------------------

        JMethod marshall = new JMethod( "write" );

        String rootElementParameterName = uncapitalise( root );
        marshall.addParameter( new JParameter( new JClass( "Writer" ), "writer" ) );
        marshall.addParameter( new JParameter( new JClass( root ), rootElementParameterName ) );

        marshall.addException( new JClass( "IOException" ) );

        sc = marshall.getSourceCode();

        sc.add( "Emitable generator = new Emitter( writer, dumperOptions );" );

        sc.add( "generator.emit( new StreamStartEvent( null, null ) );" );

        sc.add( "generator.emit( new DocumentStartEvent( null, null, dumperOptions.isExplicitStart(), dumperOptions.getVersion(), dumperOptions.getTags() ) );" );

        sc.add( "write" + root + "( " + rootElementParameterName + ", generator );" );

        sc.add( "generator.emit( new DocumentEndEvent( null, null, dumperOptions.isExplicitEnd() ) );" );

        sc.add( "generator.emit( new StreamEndEvent( null, null ) );" );

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

        writeAllClasses( objectModel, jClass );

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
        marshall.addParameter( new JParameter( new JClass( "Emitable" ), "generator" ) );

        marshall.addException( new JClass( "IOException" ) );

        marshall.getModifiers().makePrivate();

        JSourceCode sc = marshall.getSourceCode();

        sc.add( "generator.emit( new MappingStartEvent( null, null, true, null, null, false ) );" );

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
                sc.indent();

                writeScalarKey( sc, fieldTagName );
                writeScalar( sc, getValue( field.getType(), value, xmlFieldMetadata ) );

                sc.unindent();
                sc.add( "}" );
            }

        }

        if ( contentField != null )
        {
            XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) contentField.getMetadata( XmlFieldMetadata.ID );
            writeScalar( sc, getValue( contentField.getType(), contentValue, xmlFieldMetadata ) );
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

                if ( association.isOneMultiplicity() )
                {
                    sc.add( getValueChecker( type, value, association ) );

                    sc.add( "{" );
                    sc.indent();

                    writeScalarKey( sc, fieldTagName );
                    sc.add( "write" + association.getTo() + "( (" + association.getTo() + ") " + value + ", generator );" );

                    sc.unindent();
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

                        writeScalarKey( sc, fieldTagName );
                        sc.add( "generator.emit( new SequenceStartEvent( null, null, true, null, null, false ) );" );

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
                            writeScalar( sc, "o" );
                        }

                        sc.unindent();
                        sc.add( "}" );

                        sc.add( "generator.emit( new SequenceEndEvent( null, null ) );" );

                        sc.unindent();
                        sc.add( "}" );
                    }
                    else
                    {
                        //Map or Properties

                        sc.add( getValueChecker( type, value, field ) );

                        sc.add( "{" );
                        sc.indent();

                        writeScalarKey( sc, fieldTagName );

                        if ( xmlAssociationMetadata.isMapExplode() )
                        {
                            sc.add( "generator.emit( new SequenceStartEvent( null, null, true, null, null, false ) );" );
                        }
                        else
                        {
                            sc.add( "generator.emit( new MappingStartEvent( null, null, true, null, null, false ) );" );
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
                            sc.add( "generator.emit( new MappingStartEvent( null, null, true, null, null, false ) );" );
                            writeScalarKey( sc, "key" );
                            writeScalar( sc, "key" );
                            writeScalarKey( sc, "value" );
                            writeScalar( sc, "value" );
                            sc.add( "generator.emit( new MappingEndEvent( null, null ) );" );
                        }
                        else
                        {
                            writeScalar( sc, "key" );
                            writeScalar( sc, "value" );
                        }

                        sc.unindent();
                        sc.add( "}" );

                        if ( xmlAssociationMetadata.isMapExplode() )
                        {
                            sc.add( "generator.emit( new SequenceEndEvent( null, null ) );" );
                        }
                        else
                        {
                            sc.add( "generator.emit( new MappingEndEvent( null, null ) );" );
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

                writeScalarKey( sc, fieldTagName );
                writeScalar( sc, getValue( field.getType(), value, xmlFieldMetadata ) );

                sc.unindent();
                sc.add( "}" );
            }
        }

        sc.add( "generator.emit( new MappingEndEvent( null, null ) );" );

        jClass.addMethod( marshall );
    }

    private void writeScalarKey( JSourceCode sc, String key )
    {
        writeScalar( sc, "\"" + key + "\"" );
    }

    private void writeScalar( JSourceCode sc, String value )
    {
        sc.add( "generator.emit( new ScalarEvent( null, null, new ImplicitTuple( true, true ), "
                + value
                + ", null, null, ' ' ) );" );
    }

}
