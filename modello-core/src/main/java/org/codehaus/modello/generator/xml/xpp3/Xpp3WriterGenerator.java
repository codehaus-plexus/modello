package org.codehaus.modello.generator.xml.xpp3;

import org.codehaus.modello.Model;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelField;
import org.codehaus.modello.generator.java.JavaGenerator;
import org.codehaus.modello.generator.java.javasource.JClass;
import org.codehaus.modello.generator.java.javasource.JField;
import org.codehaus.modello.generator.java.javasource.JMethod;
import org.codehaus.modello.generator.java.javasource.JParameter;
import org.codehaus.modello.generator.java.javasource.JSourceCode;
import org.codehaus.modello.generator.java.javasource.JSourceWriter;

import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 * @version $Id$
 */
public class Xpp3WriterGenerator
    extends JavaGenerator
{
    public Xpp3WriterGenerator( String model, String outputDirectory, String modelVersion )
    {
        super( model, outputDirectory, modelVersion );
    }

    public void generate()
        throws Exception
    {
        Model objectModel = getModel();

        String packageName = getBasePackageName( objectModel ) + ".io.xpp3";

        String directory = packageName.replace( '.', '/' );

        String marshallerName = objectModel.getName() + "Xpp3Writer";

        File f = new File( new File( getOutputDirectory(), directory ), marshallerName + ".java" );

        if ( !f.getParentFile().exists() )
        {
            f.getParentFile().mkdirs();
        }

        FileWriter writer = new FileWriter( f );

        JSourceWriter sourceWriter = new JSourceWriter( writer );

        JClass jClass = new JClass( marshallerName );

        jClass.setPackageName( packageName );

        jClass.addImport( "org.xmlpull.v1.XmlPullParser" );

        //jClass.addImport( "org.xmlpull.v1.XmlPullParserException" );

        jClass.addImport( "org.xmlpull.v1.XmlPullParserFactory" );

        jClass.addImport( "org.xmlpull.v1.XmlSerializer" );

        jClass.addImport( "java.io.Writer" );

        jClass.addField( new JField( new JClass( "org.xmlpull.v1.XmlSerializer" ), "serializer" ) );

        jClass.addField( new JField( new JClass( "String" ), "NAMESPACE" ) );

        /*

        There doesn't seem to be a way to add exceptions to constructors, we'll have to tweak javasource.

        JConstructor constructor = new JConstructor( jClass );

        constructor.getSourceCode().add( "XmlPullParserFactory factory = XmlPullParserFactory.newInstance( System.getProperty( XmlPullParserFactory.PROPERTY_NAME ), null );" );

        constructor.getSourceCode().add( "serializer = factory.newSerializer();" );

        constructor.getSourceCode().add( "serializer.setProperty( \"http://xmlpull.org/v1/doc/properties.html#serializer-indentation\", \"  \" );" );

        constructor.getSourceCode().add( "serializer.setProperty( \"http://xmlpull.org/v1/doc/properties.html#serializer-line-separator\", \"\\n\" );" );

        jClass.addConstructor( constructor );
        */

        // Add imports for classes within the model we need to marshall.

        for ( Iterator i = objectModel.getClasses().iterator(); i.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) i.next();

            if ( outputElement( modelClass.getVersion(), modelClass.getName() ) )
            {
                jClass.addImport( getBasePackageName( objectModel ) + "." + modelClass.getName() );
            }
        }

        String root = objectModel.getRoot();

        String rootElement = uncapitalise( root );

        // Write the parse method which will do the unmarshalling.

        JMethod marshall = new JMethod( null, "write" );

        marshall.addParameter( new JParameter( new JClass( "Writer" ), "writer" ) );

        marshall.addParameter( new JParameter( new JClass( root ), rootElement ) );

        marshall.addException( new JClass( "Exception" ) );

        JSourceCode sc = marshall.getSourceCode();

        sc.add( "XmlPullParserFactory factory = XmlPullParserFactory.newInstance( System.getProperty( XmlPullParserFactory.PROPERTY_NAME ), null );" );

        sc.add( "serializer = factory.newSerializer();" );

        sc.add( "serializer.setProperty( \"http://xmlpull.org/v1/doc/properties.html#serializer-indentation\", \"  \" );" );

        sc.add( "serializer.setProperty( \"http://xmlpull.org/v1/doc/properties.html#serializer-line-separator\", \"\\n\" );" );

        sc.add( "serializer.setOutput( writer );" );

        sc.add( "serializer.startTag( NAMESPACE, \"" + rootElement + "\" );" );

        writeClassParsing( (ModelClass) objectModel.getClasses().get( 0 ), sc, objectModel );

        sc.add( "serializer.endTag( NAMESPACE, \"" + rootElement + "\" );" );

        jClass.addMethod( marshall );

        jClass.print( sourceWriter );

        writer.flush();

        writer.close();
    }

    private void writeClassParsing( ModelClass modelClass, JSourceCode sc, Model objectModel )
    {
        if ( outputElement( modelClass.getVersion(), modelClass.getName() ) )
        {
            List allFields = objectModel.getAllFields( modelClass );

            int size = allFields.size();

            for ( int i = 0; i < size; i++ )
            {
                ModelField field = (ModelField) allFields.get( i );

                writeFieldMarshalling( modelClass, field, sc, objectModel );
            }
        }
    }

    private void writeFieldMarshalling( ModelClass modelClass, ModelField field, JSourceCode sc, Model objectModel )
    {
        if ( outputElement( field.getVersion(), modelClass.getName() + "." + field.getName() ) )
        {
            String type;

            String fieldName;

            String className;

            String modelClassName;

            type = field.getType();

            fieldName = field.getName();

            className = capitalise( field.getName() );

            modelClassName = uncapitalise( modelClass.getName() );

            if ( isClassInModel( type, objectModel ) )
            {
                sc.add( "serializer.startTag( NAMESPACE, " + "\"" + fieldName + "\" );" );

                sc.add( className + " " + fieldName + " = " + modelClassName + ".get" + className + "();" );

                sc.add( "if ( " + fieldName + " != null )" );

                sc.add( "{" );

                sc.indent();

                writeClassParsing( objectModel.getClass( type ), sc, objectModel );

                sc.unindent();

                sc.add( "}" );

                sc.add( "serializer.endTag( NAMESPACE, " + "\"" + fieldName + "\" );" );

            }
            else if ( isCollection( type ) )
            {
                writeCollectionMarshalling( modelClassName, fieldName, sc, objectModel );
            }
            else if ( isMap( type ) )
            {
                // These are properties for now.
                writePropertiesMarshalling( modelClassName, fieldName, sc, objectModel );
            }
            else
            {
                sc.add( "if ( " + modelClassName + ".get" + className + "() != null )" );

                sc.add( "{" );

                sc.indent();

                sc.add( "serializer.startTag( NAMESPACE, " + "\"" + fieldName + "\" ).text( " +
                        modelClassName + ".get" + className + "() ).endTag( NAMESPACE, " + "\"" + fieldName + "\" );" );

                sc.unindent();

                sc.add( "}" );
            }
        }
    }

    private void writeCollectionMarshalling( String modelClassName, String fieldName, JSourceCode sc, Model objectModel )
    {
        sc.add( "\n" );

        // We have a collection but we need to know what is in the collection.

        String getterName = capitalise( fieldName );

        String collectionClass = singular( getterName );

        String singular = singular( fieldName );

        if ( isClassInModel( collectionClass, objectModel ) )
        {
            sc.add( "serializer.startTag( NAMESPACE, " + "\"" + fieldName + "\" );" );

            String size = modelClassName + ".get" + getterName + "().size()";

            String index = getIndex();

            sc.add( "for ( int " + index + "= 0; " + index + " < " + size + "; " + index + "++ )" );

            sc.add( "{" );

            sc.indent();

            sc.add( collectionClass + " " + singular +
                    " = (" + collectionClass + ") " + modelClassName + ".get" + getterName + "().get( " + index + " );" );

            sc.add( "serializer.startTag( NAMESPACE, " + "\"" + singular + "\" );" );

            writeClassParsing( objectModel.getClass( singular( collectionClass ) ), sc, objectModel );

            sc.add( "serializer.endTag( NAMESPACE, " + "\"" + singular + "\" );" );

            sc.unindent();

            sc.add( "}" );

            sc.add( "serializer.endTag( NAMESPACE, " + "\"" + fieldName + "\" );" );
        }
        else
        {
            sc.add( "serializer.startTag( NAMESPACE, " + "\"" + fieldName + "\" );" );

            String size = modelClassName + ".get" + getterName + "().size()";

            String index = getIndex();

            sc.add( "for ( int " + index + "= 0; " + index + " < " + size + "; " + index + "++ )" );

            sc.add( "{" );

            sc.indent();

            sc.add( "String s = (String) " + modelClassName + ".get" + getterName + "().get( " + index + " );" );

            sc.add( "serializer.startTag( NAMESPACE, " + "\"" + singular + "\" ).text( s ).endTag( NAMESPACE, " + "\"" + singular + "\" );" );

            sc.unindent();

            sc.add( "}" );

            sc.add( "serializer.endTag( NAMESPACE, " + "\"" + fieldName + "\" );" );
        }
    }

    int index;

    private String getIndex()
    {
        index++;
        return "i" + Integer.toString( index );
    }

    private void writePropertiesMarshalling( String modelClassName, String fieldName, JSourceCode sc, Model objectModel )
    {
    }
}
