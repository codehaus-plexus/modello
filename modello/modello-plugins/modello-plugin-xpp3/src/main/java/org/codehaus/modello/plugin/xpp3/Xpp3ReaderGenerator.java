package org.codehaus.modello.plugin.xpp3;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.codehaus.modello.Model;
import org.codehaus.modello.ModelAssociation;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelField;
import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.generator.java.javasource.JClass;
import org.codehaus.modello.generator.java.javasource.JMethod;
import org.codehaus.modello.generator.java.javasource.JParameter;
import org.codehaus.modello.generator.java.javasource.JSourceCode;
import org.codehaus.modello.generator.java.javasource.JSourceWriter;
import org.codehaus.modello.plugins.xml.XmlMetadata;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 * @version $Id$
 */
public class Xpp3ReaderGenerator
    extends AbstractXpp3Generator
{
    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

        try
        {
            generateXpp3Reader();
        }
        catch( IOException ex )
        {
            throw new ModelloException( "Exception while generating XDoc.", ex );
        }
    }

    private void generateXpp3Reader()
        throws ModelloException, IOException
    {
        Model objectModel = getModel();

        String packageName = getBasePackageName() + ".io.xpp3";

        String directory = packageName.replace( '.', '/' );

        String unmarshallerName = getFileName( "Xpp3Reader" );

        File f = new File( new File( getOutputDirectory(), directory ), unmarshallerName + ".java" );

        if ( !f.getParentFile().exists() )
        {
            f.getParentFile().mkdirs();
        }

        FileWriter writer = new FileWriter( f );

        JSourceWriter sourceWriter = new JSourceWriter( writer );

        JClass jClass = new JClass( unmarshallerName );

        jClass.setPackageName( packageName );

        jClass.addImport( "org.xmlpull.v1.XmlPullParser" );

        jClass.addImport( "org.xmlpull.v1.XmlPullParserException" );

        jClass.addImport( "org.xmlpull.v1.XmlPullParserFactory" );

        jClass.addImport( "java.io.Reader" );

        addModelImports( jClass );

        // Write the parse method which will do the unmarshalling.
        String root = objectModel.getRoot();

        String rootElement = uncapitalise( root );

        JMethod unmarshall = new JMethod( new JClass( root ), "read" );

        unmarshall.addParameter( new JParameter( new JClass( "Reader" ), "reader" ) );

        unmarshall.addException( new JClass( "Exception" ) );

        JSourceCode sc = unmarshall.getSourceCode();

        sc.add( root + " " + rootElement + " = new " + root + "();" );

        sc.add( "XmlPullParserFactory factory = XmlPullParserFactory.newInstance();" );

        sc.add( "XmlPullParser parser = factory.newPullParser();" );

        sc.add( "parser.setInput( reader );" );

        sc.add( "int eventType = parser.getEventType();" );

        sc.add( "while ( eventType != XmlPullParser.END_DOCUMENT )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "if ( eventType == XmlPullParser.START_TAG )" );

        sc.add( "{" );

        sc.indent();

        writeClassParsing( (ModelClass) objectModel.getClasses().get( 0 ), sc, objectModel, false );

        sc.unindent();

        sc.add( "}" );

        sc.add( "eventType = parser.next();" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "return " + rootElement + ";" );

        jClass.addMethod( unmarshall );

        jClass.print( sourceWriter );

        writer.flush();

        writer.close();
    }

    private void writeClassParsing( ModelClass modelClass, JSourceCode sc, Model objectModel, boolean withLoop )
        throws IOException
    {
        if ( !outputElement( modelClass ) )
        {
            return;
        }

        if ( withLoop )
        {
            sc.add( "while ( parser.nextTag() == XmlPullParser.START_TAG )" );

            sc.add( "{" );

            sc.indent();
        }

        String statement;

        List fields = modelClass.getAllFields();

        int fieldCount = fields.size();

        boolean firstStatement = true;

        for ( int i = 0; i < fieldCount; i++ )
        {
            ModelField field = (ModelField) fields.get( i );

            XmlMetadata xmlMetadata = (XmlMetadata)field.getMetadata( XmlMetadata.ID );

            if ( !xmlMetadata.isAttribute() )
            {
                continue;
            }

            if ( outputElement( field ) )
            {
                if ( firstStatement )
                {
                    sc.add( "// Reading attributes" );

                    sc.add( "if ( parser.getName().equals( \"" + uncapitalise( modelClass.getName() ) + "\") )" );

                    sc.add( "{" );

                    sc.indent();

                    statement = "if";

                    firstStatement = false;
                }
                else
                {
                    statement = "else if";
                }

                writeFieldParsing( modelClass, field, sc, statement, objectModel, true );
            }
        }

        if ( !firstStatement )
        {
            sc.unindent();

            sc.add( "}" );
        }

        firstStatement = true;

        for ( int i = 0; i < fieldCount; i++ )
        {
            ModelField field = (ModelField) fields.get( i );

            XmlMetadata xmlMetadata = (XmlMetadata)field.getMetadata( XmlMetadata.ID );

            if ( xmlMetadata.isAttribute() )
            {
                continue;
            }

            if ( outputElement( field ) )
            {
                if ( firstStatement )
                {
                    statement = "if";

                    firstStatement = false;
                }
                else
                {
                    statement = "else if";
                }

                writeFieldParsing( modelClass, field, sc, statement, objectModel, false );
            }
        }

        List associations = modelClass.getAllAssociations();

        int associationCount = associations.size();

        for ( int i = 0; i < associationCount; i++ )
        {
            ModelAssociation association = (ModelAssociation) associations.get( i );

            if ( outputElement( association ) )
            {
                if ( firstStatement )
                {
                    statement = "if";

                    firstStatement = false;
                }
                else
                {
                    statement = "else if";
                }

                writeAssociationParsing( modelClass, association, sc, statement, objectModel );
            }
        }

        if ( withLoop )
        {
            // TODO: replace with !firstStatement
            if ( !firstStatement )
            {
                writeCatchAll( sc );
            }

            sc.unindent();

            sc.add( "}" );
        }
    }

    private void writeAssociationParsing( ModelClass modelClass, ModelAssociation association, JSourceCode sc, String statement, Model objectModel )
        throws IOException
    {
        XmlMetadata xmlMetadata = new XmlMetadata();

        writeFieldParsing( modelClass, association.getFromRole(), "java.util.List", sc, statement, objectModel, xmlMetadata, false, true );
    }

    private void writeFieldParsing( ModelClass modelClass, ModelField field, JSourceCode sc, String statement, Model objectModel, boolean attribute )
        throws IOException
    {
        XmlMetadata xmlMetadata = (XmlMetadata)field.getMetadata( XmlMetadata.ID );

        writeFieldParsing( modelClass, field.getName(), field.getType(), sc, statement, objectModel, xmlMetadata, attribute, false );
    }

    private void writeFieldParsing( ModelClass modelClass, String name, String type, JSourceCode sc, String statement, Model objectModel, XmlMetadata xmlMetadata, boolean attribute, boolean association )
        throws IOException
    {
        String className = capitalise( name );

        String modelClassName = uncapitalise( modelClass.getName() );

        String tagName = xmlMetadata.getTagName();

        if ( tagName == null )
        {
            tagName = name;
        }

        if ( attribute )
        {
            if ( !type.equals( "String" ) )
            {
                throw new ModelloRuntimeException( "A xml attribute field must be a java.lang.String. Field name: " + name );
            }

            sc.add( modelClassName + ".set" + className + "( parser.getAttributeValue( \"\", \"" + tagName + "\" ) );" );

            return;
        }
        else
        {
            sc.add( statement + " ( parser.getName().equals( \"" + tagName + "\" ) )" );
        }

        sc.add( "{" );

        sc.indent();

        if ( isClassInModel( type, objectModel ) )
        {
            if ( attribute )
            {
                throw new ModelloRuntimeException( "A class cannot be a serialized as a attribute." );
            }

            sc.add( type + " " + name + " = new " + type + "();" );

            sc.add( modelClassName + ".set" + className + "( " + name + " );" );

            writeClassParsing( objectModel.getClass( type ), sc, objectModel, true );
        }
        else if ( association )
        {
            if ( attribute )
            {
                throw new ModelloRuntimeException( "A class cannot be a serialized as a attribute." );
            }

            writeCollectionParsing( modelClassName, name, tagName, sc, objectModel );
        }
/*
        else if ( isMap( type ) )
        {
            if ( attribute )
            {
                throw new ModelloRuntimeException( "A class cannot be a serialized as a attribute." );
            }

            // These are properties for now.
            writePropertiesParsing( modelClassName, name, sc, objectModel );
        }
*/
        else
        {
            sc.add( modelClassName + ".set" + className + "( parser.nextText() );" );
        }

        sc.unindent();

        sc.add( "}" );
    }

    private void writeCollectionParsing( String modelClassName, String fieldName, String tagName, JSourceCode sc, Model objectModel )
        throws IOException
    {
        // We have a collection but we need to know what is in the collection.
        String collectionClass = capitalise( singular( fieldName ) );

        sc.add( "while ( parser.nextTag() == XmlPullParser.START_TAG )" );

        sc.add( "{" );

        sc.indent();

        if ( tagName != null )
        {
            sc.add( "if ( parser.getName().equals( \"" + singular( tagName ) + "\" ) )" );
        }
        else
        {
            sc.add( "if ( parser.getName().equals( \"" + singular( fieldName ) + "\" ) )" );
        }

        sc.add( "{" );

        sc.indent();

        if ( isClassInModel( collectionClass, objectModel ) )
        {
            sc.add( collectionClass + " " + singular( fieldName ) + " = new " + collectionClass + "();" );

            sc.add( modelClassName + ".add" + collectionClass + "( " + singular( fieldName ) + " );" );

            writeClassParsing( objectModel.getClass( collectionClass ), sc, objectModel, true );
        }
        else
        {
            sc.add( modelClassName + ".add" + collectionClass + "( parser.nextText() );" );
        }

        sc.unindent();

        sc.add( "}" );

        writeCatchAll( sc );

        sc.unindent();

        sc.add( "}" );

    }

    private void writePropertiesParsing( String modelClassName, String fieldName, JSourceCode sc, Model objectModel )
    {
        // We have a collection but we need to know what is in the collection.
        String collectionClass = capitalise( singular( fieldName ) );

        sc.add( "while ( parser.nextTag() == XmlPullParser.START_TAG )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "String key = parser.getName();" );

        sc.add( "String value = parser.nextText();" );

        sc.add( modelClassName + ".add" + collectionClass + "( key, value );" );

        sc.unindent();

        sc.add( "}" );
    }

    private void writeCatchAll( JSourceCode sc )
    {
        // Add the catchall

        sc.add( "else" );

        sc.add( "{" );

        sc.indent();

        sc.add( "parser.nextText();" );

        sc.unindent();

        sc.add( "}" );
    }
}
