package org.codehaus.modello.plugin.xpp3;

/*
 * Copyright (c) 2004, Jason van Zyl
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.generator.java.javasource.JClass;
import org.codehaus.modello.generator.java.javasource.JMethod;
import org.codehaus.modello.generator.java.javasource.JParameter;
import org.codehaus.modello.generator.java.javasource.JSourceCode;
import org.codehaus.modello.generator.java.javasource.JSourceWriter;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelDefault;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.plugins.xml.XmlAssociationMetadata;
import org.codehaus.modello.plugins.xml.XmlFieldMetadata;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 *
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

        jClass.addImport( "java.util.ArrayList" );

        jClass.addImport( "java.util.List" );

        addModelImports( jClass );

        // Write the parse method which will do the unmarshalling.
        ModelClass root = objectModel.getClass( objectModel.getRoot( getGeneratedVersion() ), getGeneratedVersion() );

        String rootElement = uncapitalise( root.getName() );

        JMethod unmarshall = new JMethod( new JClass( root.getName() ), "read" );

        unmarshall.addParameter( new JParameter( new JClass( "Reader" ), "reader" ) );

        unmarshall.addException( new JClass( "Exception" ) );

        JSourceCode sc = unmarshall.getSourceCode();

        sc.add( root.getName() + " " + rootElement + " = new " + root.getName() + "();" );

        sc.add( "XmlPullParserFactory factory = XmlPullParserFactory.newInstance();" );

        sc.add( "XmlPullParser parser = factory.newPullParser();" );

        sc.add( "parser.setInput( reader );" );

        sc.add( "int eventType = parser.getEventType();" );

        sc.add( "while ( eventType != XmlPullParser.END_DOCUMENT )" );

        sc.add( "{" );

        sc.indent();

        writeClassParsing( root, sc, false, true );

        sc.unindent();

        sc.add( "}" );

        sc.add( "return " + rootElement + ";" );

        jClass.addMethod( unmarshall );

        jClass.print( sourceWriter );

        writer.flush();

        writer.close();
    }

    private void writeClassParsing( ModelClass modelClass, JSourceCode sc, boolean withLoop, boolean rootElement )
        throws IOException
    {
        writeClassParsing( modelClass, null, sc, withLoop, rootElement );
    }

    private void writeClassParsing( ModelClass modelClass, String objectName, JSourceCode sc, boolean withLoop, boolean rootElement )
        throws IOException
    {
        String modelClassName = modelClass.getName();

        String objName = objectName;

        if ( objName == null )
        {
            objName = uncapitalise( modelClassName );
        }

        if ( withLoop )
        {
            sc.add( "while ( parser.nextTag() == XmlPullParser.START_TAG )" );

            sc.add( "{" );

            sc.indent();

            sc.add( "if ( parser.getName().equals( \"" + uncapitalise( modelClassName ) + "\" ) )" );

            sc.add( "{" );

            sc.indent();

            sc.add( modelClassName + " " + uncapitalise( modelClassName ) + " = new " + modelClassName + "();" );
            
            sc.add( "while ( parser.nextTag() == XmlPullParser.START_TAG )" );

            sc.add( "{" );

            sc.indent();

            objName = uncapitalise( modelClassName );
        }
        else
        {
            if ( rootElement)
            {
                sc.add( "if ( eventType == XmlPullParser.START_TAG )" );
            }
            else
            {
                sc.add( "while ( parser.nextTag() == XmlPullParser.START_TAG )" );
            }

            sc.add( "{" );

            sc.indent();
        }

        writeAttributes( modelClass, objName, sc );

        sc.add( "// Reading tags" );

        writeFields( modelClass, objName, sc );

        if ( withLoop )
        {
            writeCatchAll( sc );

            sc.unindent();

            sc.add( "}" );

            sc.add( objectName + ".add( " + uncapitalise( modelClassName ) + " );" );

            sc.unindent();

            sc.add( "}" );

            writeCatchAll( sc );

            sc.unindent();

            sc.add( "}" );
        }
        else
        {
            sc.unindent();

            sc.add( "}" );

            if ( rootElement )
            {
                sc.add( "eventType = parser.next();" );
            }
        }
    }

    private void writeAttributes( ModelClass modelClass, String objectName, JSourceCode sc )
        throws IOException
    {
        List fields = modelClass.getAllFields( getGeneratedVersion(), true );

        boolean firstStatement = true;

        for ( int i = 0; i < fields.size(); i++ )
        {
            ModelField field = (ModelField) fields.get( i );

            XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata)field.getMetadata( XmlFieldMetadata.ID );

            String tagName = xmlFieldMetadata.getTagName();

            if ( tagName == null )
            {
                tagName = field.getName();
            }

            if ( !xmlFieldMetadata.isAttribute() || ( field instanceof ModelAssociation ) )
            {
                continue;
            }

            if ( firstStatement )
            {
                sc.add( "// Reading attributes" );

                sc.add( "if ( parser.getName().equals( \"" + uncapitalise( modelClass.getName() ) + "\") )" );

                sc.add( "{" );

                sc.indent();

                firstStatement = false;
            }

            sc.add( objectName + ".set" + capitalise( field.getName() ) + "( parser.getAttributeValue( \"\", \"" + tagName + "\" ) );" );
        }
        
        if ( ! firstStatement )
        {
            sc.unindent();

            sc.add( "}" );
        }
    }

    private void writeFields( ModelClass modelClass, String objectName, JSourceCode sc )
        throws IOException
    {
        List fields = modelClass.getAllFields( getGeneratedVersion(), true );

        String statement = "if";

        for ( int i = 0; i < fields.size(); i++ )
        {
            ModelField field = (ModelField) fields.get( i );

            XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata)field.getMetadata( XmlFieldMetadata.ID );

            String tagName = xmlFieldMetadata.getTagName();

            if ( tagName == null )
            {
                tagName = field.getName();
            }

            if ( xmlFieldMetadata.isAttribute() )
            {
                continue;
            }

            sc.add( statement + " ( parser.getName().equals( \"" + tagName + "\" ) )" );

            sc.add( "{" );

            sc.indent();

            if ( field instanceof ModelAssociation &&
                ModelAssociation.MANY_MULTIPLICITY.equals( ( (ModelAssociation) field ).getMultiplicity() ) )
            {
                writeAssociation( modelClass, (ModelAssociation) field, objectName, sc );
            }
            else
            {
                writeField( modelClass, field, objectName, sc );
            }

            sc.unindent();

            sc.add( "}" );

            statement = "else if";
        }
    }

    private void writeField( ModelClass modelClass, ModelField field, String objectName, JSourceCode sc )
        throws IOException
    {
        String type = field.getType();
        
        XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata)field.getMetadata( XmlFieldMetadata.ID );

        String tagName = xmlFieldMetadata.getTagName();

        if ( tagName == null )
        {
            tagName = field.getName();
        }

        if ( isClassInModel( type, modelClass.getModel() ) )
        {
            ModelClass fieldClass = modelClass.getModel().getClass( type, getGeneratedVersion() );

            sc.add( type + " " + field.getName() + " = new " + type + "();" );

            writeClassParsing( fieldClass, field.getName(), sc, false, false );

            sc.add( objectName + ".set" + type + "( " + field.getName() + ");" );
        }
        else
        {
            writePrimitiveField( field, objectName, sc );
        }
    }

    private void writeAssociation( ModelClass modelClass, ModelAssociation field, String objectName, JSourceCode sc )
        throws IOException
    {
        String type = field.getTo();

        String singularName = singular( field.getName() );

        XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata)field.getMetadata( XmlFieldMetadata.ID );

        XmlAssociationMetadata xmlAssociationMetadata = (XmlAssociationMetadata)field.getAssociationMetadata( XmlAssociationMetadata.ID );

        String tagName = xmlFieldMetadata.getTagName();

        if ( tagName == null )
        {
            tagName = singularName;
        }

        if ( isClassInModel( type, modelClass.getModel() ) )
        {
            ModelClass fieldClass = modelClass.getModel().getClass( type, getGeneratedVersion() );

            sc.add( "List " + field.getName() + " = new ArrayList();" );

            writeClassParsing( fieldClass, field.getName(), sc, true, false );

            sc.add( objectName + ".set" + capitalise( field.getName() ) + "( " + field.getName() + ");" );
        }
        else
        {
            if ( ModelDefault.MAP.equals( field.getType() )
                || ModelDefault.PROPERTIES.equals( field.getType() ) )
            {
                sc.add( "//" + field.getType() ) ;

                if ( XmlAssociationMetadata.EXPLODE_MODE.equals( xmlAssociationMetadata.getMapStyle() ) )
                {
                    sc.add( "while ( parser.nextTag() == XmlPullParser.START_TAG )" );

                    sc.add( "{" );

                    sc.indent();

                    sc.add( "if ( parser.getName().equals( \"" + singularName + "\" ) )" );

                    sc.add( "{" );

                    sc.indent();

                    sc.add( "String key = null;" );

                    sc.add( "String value = null;");

                    sc.add( "//" + xmlAssociationMetadata.getMapStyle() + " mode." );

                    sc.add( "while ( parser.nextTag() == XmlPullParser.START_TAG )" );

                    sc.add( "{" );

                    sc.indent();

                    sc.add( "if ( parser.getName().equals( \"key\" ) )" );

                    sc.add( "{" );

                    sc.indent();

                    sc.add( "key = parser.nextText();" );

                    sc.unindent();

                    sc.add( "}" );

                    sc.add( "else if ( parser.getName().equals( \"value\" ) )" );

                    sc.add( "{" );

                    sc.indent();

                    sc.add( "value = parser.nextText();" );

                    sc.unindent();

                    sc.add( "}" );

                    writeCatchAll( sc );

                    sc.unindent();

                    sc.add( "}" );

                    sc.add( objectName + ".add" + capitalise( singularName ) + "( key, value );");
                }
                else
                {
                    sc.add( "while ( parser.nextTag() == XmlPullParser.START_TAG )" );

                    sc.add( "{" );

                    sc.indent();

                    sc.add( "String key = parser.getName();" );

                    sc.add( "String value = parser.nextText();" );

                    sc.add( objectName + ".add" + capitalise( singularName ) + "( key, value );");

                    sc.unindent();

                    sc.add( "}" );
                }

                sc.unindent();

                sc.add( "}" );

                sc.add( "parser.next();" );

                sc.unindent();

                sc.add( "}" );
            }
            else if ( ModelDefault.LIST.equals( field.getType() )
                || ModelDefault.SET.equals( field.getType() ) )
            {
                sc.add( "//LIST of STRING" ) ;

                    sc.add( "while ( parser.nextTag() == XmlPullParser.START_TAG )" );

                    sc.add( "{" );

                    sc.indent();

                    sc.add( "if ( parser.getName().equals( \"" + singularName + "\" ) )" );

                    sc.add( "{" );

                    sc.indent();

                    sc.add( objectName + ".add" + capitalise( singularName ) + "( parser.nextText() );");

                    sc.unindent();

                    sc.add( "}" );

                    writeCatchAll( sc );

                    sc.unindent();

                    sc.add( "}" );
            }
            else
            {
                sc.add( "if ( parser.getName().equals( \"" + singularName + "\" ) )" );

                sc.add( "{" );

                sc.indent();

                writePrimitiveField( field, field.getName(), sc );

                sc.unindent();

                sc.add( "}" );
            }
        }
    }

    private void writePrimitiveField( ModelField field, String objectName, JSourceCode sc )
    {
        String type = field.getType();
        
        String setterName = "set" + capitalise( field.getName() );

        if ( "boolean".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( (new Boolean( parser.nextText() ) ).booleanValue() );" );
        }
        if ( "char".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( (new Character( parser.nextText() ) ).charValue() );" );
        }
        if ( "double".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( (new Double( parser.nextText() ) ).doubleValue() );" );
        }
        if ( "float".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( (new Float( parser.nextText() ) ).floatValue() );" );
        }
        if ( "int".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( (new Integer( parser.nextText() ) ).intValue() );" );
        }
        if ( "long".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( (new Long( parser.nextText() ) ).longValue() );" );
        }
        if ( "short".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( (new Short( parser.nextText() ) ).shortValue() );" );
        }
        else if ( "String".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( parser.nextText() );" );
        }
    }

    private void writeCatchAll( JSourceCode sc )
    {
        sc.add( "else" );

        sc.add( "{" );

        sc.indent();

        sc.add( "parser.nextText();" );

        sc.unindent();

        sc.add( "}" );
    }
}
