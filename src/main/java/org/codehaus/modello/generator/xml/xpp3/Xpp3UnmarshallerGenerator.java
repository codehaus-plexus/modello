package org.codehaus.modello.generator.xml.xpp3;

import org.codehaus.modello.Model;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelField;
import org.codehaus.modello.generator.AbstractGenerator;
import org.codehaus.modello.generator.java.javasource.JClass;
import org.codehaus.modello.generator.java.javasource.JMethod;
import org.codehaus.modello.generator.java.javasource.JParameter;
import org.codehaus.modello.generator.java.javasource.JSourceCode;
import org.codehaus.modello.generator.java.javasource.JSourceWriter;

import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;

/**
 *
 *
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 *
 * @version $Id$
 */
public class Xpp3UnmarshallerGenerator
    extends AbstractGenerator
{
    public Xpp3UnmarshallerGenerator( String model, String outputDirectory )
    {
        super( model, outputDirectory );
    }

    public void generate()
        throws Exception
    {
        Model objectModel = getModel();

        String packageName = objectModel.getPackageName();

        String directory = packageName.replace( '.', '/' );

        String unmarshallerName = objectModel.getName() + "Xpp3Unmarshaller";

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

        // Add imports for classes within the model we need to unmarshall.

        for ( Iterator i = objectModel.getClasses().iterator(); i.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) i.next();

            jClass.addImport( packageName + "." + modelClass.getName() );
        }

        // Write the parse method which will do the unmarshalling.

        JMethod unmarshall = new JMethod( new JClass( "Object" ), "unmarshall" );

        unmarshall.addParameter( new JParameter( new JClass( "Reader" ), "reader" ) );

        JSourceCode sc = unmarshall.getSourceCode();

        sc.add( "Model model = new Model();" );

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

        writeClassParsing( (ModelClass) objectModel.getClasses().get( 0 ), sc, objectModel );

        sc.unindent();

        sc.add( "}" );

        sc.add( "eventType = parser.next();" );

        sc.unindent();

        sc.add( "}" );

        jClass.addMethod( unmarshall );

        jClass.print( sourceWriter );

        writer.flush();

        writer.close();
    }

    private void writeClassParsing( ModelClass modelClass, JSourceCode sc, Model objectModel )
    {
        int size = modelClass.getFields().size();

        String statement;

        sc.add( "while ( parser.nextTag() == XmlPullParser.START_TAG )" );

        sc.add( "{" );

        sc.indent();

        for ( int i = 0; i < size; i++ )
        {
            ModelField field = (ModelField) modelClass.getFields().get( i );

            if ( i == 0 )
            {
                statement = "if";
            }
            else
            {
                statement = "else if";
            }

            writeFieldParsing( modelClass, field, sc, statement, objectModel );
        }

        writeCatchAll( sc );

        sc.unindent();

        sc.add( "}" );
    }

    private void writeFieldParsing( ModelClass modelClass, ModelField field, JSourceCode sc, String statement, Model objectModel )
    {
        String type = field.getType();

        String name = field.getName();

        String className = capitalise( field.getName() );

        String modelClassName = uncapitalise( modelClass.getName() );

        sc.add( statement + " ( parser.getName().equals( \"" + field.getName() + "\" ) )" );

        sc.add( "{" );

        sc.indent();

        if ( isClassInModel( type, objectModel ) )
        {
            sc.add( type + " " + name + " = new " + type + "();" );

            sc.add( modelClassName + ".set" + className + "( " + name + " );" );

            writeClassParsing( objectModel.getClass( field.getType() ), sc, objectModel );
        }
        else if ( isCollection( type ) )
        {
            writeCollectionParsing( modelClassName, name, sc, objectModel );
        }
        else
        {
            sc.add( modelClassName + ".set" + className + "( parser.nextText() );" );
        }

        sc.unindent();

        sc.add( "}" );
    }

    private void writeCollectionParsing( String modelClassName, String fieldName, JSourceCode sc, Model objectModel )
    {
        // We have a collection but we need to know what is in the collection.
        String collectionClass = capitalise( singular( fieldName ) );

        sc.add( "if ( parser.getName().equals( \"" + singular( fieldName ) + "\" ) )" );

        sc.add( "{" );

        sc.indent();

        sc.add( collectionClass + " " + singular( fieldName ) + " = new " + collectionClass + "();" );

        sc.add( modelClassName + ".add" + collectionClass + "( " + singular( fieldName ) + " );" );

        if ( isClassInModel( collectionClass, objectModel ) )
        {
            writeClassParsing( objectModel.getClass( collectionClass ), sc, objectModel );
        }

        sc.unindent();

        sc.add( "}" );

        writeCatchAll( sc );
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
