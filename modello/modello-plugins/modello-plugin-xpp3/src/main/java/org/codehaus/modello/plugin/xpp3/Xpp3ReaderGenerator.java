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
import java.util.Iterator;
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
            throw new ModelloException( "Exception while generating XPP3 Reader.", ex );
        }
    }

    private void generateXpp3Reader()
        throws ModelloException, IOException
    {
        Model objectModel = getModel();

        String packageName;

        if ( isPackageWithVersion() )
        {
            packageName = objectModel.getPackageName( true, getGeneratedVersion() );
        }
        else
        {
            packageName = objectModel.getPackageName( false, null );
        }

        packageName += ".io.xpp3";

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

        sc.add( "XmlPullParserFactory factory = XmlPullParserFactory.newInstance();" );

        sc.add( "XmlPullParser parser = factory.newPullParser();" );

        sc.add( "parser.setInput( reader );" );

        sc.add( "return parse" + root.getName() + "( \"" + rootElement + "\", parser );" );

        jClass.addMethod( unmarshall );

        writeAllClassesParser( objectModel, jClass );

        jClass.print( sourceWriter );

        writer.flush();

        writer.close();
    }

    private void writeAllClassesParser( Model objectModel, JClass jClass )
    {
        ModelClass root = objectModel.getClass( objectModel.getRoot( getGeneratedVersion() ), getGeneratedVersion() );

        for ( Iterator i = objectModel.getClasses( getGeneratedVersion() ).iterator(); i.hasNext(); )
        {
            ModelClass clazz = (ModelClass) i.next();

            if ( root.getName().equals( clazz.getName() ) )
            {
                writeClassParser( clazz, jClass, true );
            }
            else
            {
                writeClassParser( clazz, jClass, false );
            }
        }
    }

    private void writeClassParser( ModelClass modelClass, JClass jClass, boolean rootElement )
    {
        String className = modelClass.getName();

        String capClassName = capitalise( className );

        String uncapClassName = uncapitalise( className );

        String statement = "if";

        JMethod unmarshall = new JMethod( new JClass( className ), "parse" + capClassName );

        unmarshall.addParameter( new JParameter( new JClass( "String" ), "tagName" ) );

        unmarshall.addParameter( new JParameter( new JClass( "XmlPullParser" ), "parser" ) );

        unmarshall.addException( new JClass( "Exception" ) );

        unmarshall.getModifiers().makePrivate();

        JSourceCode sc = unmarshall.getSourceCode();

        sc.add( className + " " + uncapClassName + " = new " + className + "();" );

        if ( rootElement )
        {
            sc.add( "int eventType = parser.getEventType();" );

            sc.add( "while ( eventType != XmlPullParser.END_DOCUMENT )" );

            sc.add( "{" );

            sc.indent();

            sc.add( "if ( eventType == XmlPullParser.START_TAG )" );
        }
        else
        {
            sc.add( "while ( parser.nextTag() == XmlPullParser.START_TAG )" );
        }

        sc.add( "{" );

        sc.indent();

        //Write xml attribute

        sc.add( "if ( parser.getName().equals( tagName ) )" );

        sc.add( "{" ) ;

        sc.indent();

        for (Iterator i = modelClass.getAllFields( getGeneratedVersion(), true ).iterator(); i.hasNext(); )
        {
            ModelField field = (ModelField) i.next();

            XmlFieldMetadata fieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

            if ( fieldMetadata.isAttribute() )
            {
                writePrimitiveField( field, field.getType(), uncapClassName, "set" + capitalise( field.getName() ), sc );

                continue;
            }
        }

        sc.unindent();

        sc.add( "}" );

        //Write other fields

        for (Iterator i = modelClass.getAllFields( getGeneratedVersion(), true ).iterator(); i.hasNext(); )
        {
            ModelField field = (ModelField) i.next();

            XmlFieldMetadata fieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

            String tagName = fieldMetadata.getTagName();

            if ( fieldMetadata.isAttribute() )
            {
                continue;
            }
            if (tagName == null)
            {
                tagName = field.getName();
            }

            String capFieldName = capitalise( field.getName() );

            String uncapFieldName = uncapitalise( field.getName() );

            String singularName = singular( field.getName() );

            String singularTagName = singular( field.getName() );

            sc.add( statement + " ( parser.getName().equals( \"" + tagName + "\" ) )" );

            sc.add( "{" );

            sc.indent();

            if ( field instanceof ModelAssociation )
            {
                ModelAssociation association = (ModelAssociation) field;

                String associationName = association.getName();

                if ( ModelAssociation.ONE_MULTIPLICITY.equals( association.getMultiplicity() ) )
                {
                    sc.add( uncapClassName + ".set" + capFieldName + "( parse" + association.getTo() + "( \"" + tagName + "\", parser ) );" );
                }
                else
                {
                    //MANY_MULTIPLICITY

                    String type = association.getType();

                    if ( ModelDefault.LIST.equals( type )
                        || ModelDefault.SET.equals( type ) )
                    {
                        sc.add( type + " " + associationName + " = " + association.getDefaultValue() + ";" );

                        sc.add( "while ( parser.nextTag() == XmlPullParser.START_TAG )" );

                        sc.add( "{" );

                        sc.indent();

                        sc.add( "if ( parser.getName().equals( \"" + singularTagName + "\" ) )" );

                        sc.add( "{" );

                        sc.indent();

                        if ( isClassInModel( association.getTo(), modelClass.getModel() ) )
                        {
                            sc.add( associationName + ".add( parse" + association.getTo() + "( \"" + tagName + "\", parser ) );" );
                        }
                        else
                        {
                            writePrimitiveField( association, association.getTo(), associationName, "add", sc );
                        }

                        sc.unindent();

                        sc.add( "}" );

                        sc.add( "else" );

                        sc.add( "{" );

                        sc.indent();

                        sc.add( "parser.nextText();" );

                        sc.unindent();

                        sc.add( "}" );

                        sc.unindent();

                        sc.add( "}" );

                        sc.add( uncapClassName + ".set" + capFieldName + "( " + associationName + " );" );
                    }
                    else
                    {
                        //Map or Properties

                        XmlAssociationMetadata xmlAssociationMetadata = (XmlAssociationMetadata)association.getAssociationMetadata( XmlAssociationMetadata.ID );

                        if ( XmlAssociationMetadata.EXPLODE_MODE.equals( xmlAssociationMetadata.getMapStyle() ) )
                        {
                            sc.add( "while ( parser.nextTag() == XmlPullParser.START_TAG )" );

                            sc.add( "{" );

                            sc.indent();

                            sc.add( "if ( parser.getName().equals( \"" + singularTagName + "\" ) )" );

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

                            sc.add( "else" );

                            sc.add( "{" );

                            sc.indent();

                            sc.add( "parser.nextText();" );

                            sc.unindent();

                            sc.add( "}" );

                            sc.unindent();

                            sc.add( "}" );

                            sc.add( uncapClassName + ".add" + capitalise( singularName ) + "( key, value );");

                            sc.unindent();

                            sc.add( "}" );

                            sc.add( "parser.next();" );

                            sc.unindent();

                            sc.add( "}" );
                        }
                        else
                        {
                            //INLINE Mode

                            sc.add( "while ( parser.nextTag() == XmlPullParser.START_TAG )" );

                            sc.add( "{" );

                            sc.indent();

                            sc.add( "String key = parser.getName();" );

                            sc.add( "String value = parser.nextText();" );

                            sc.add( uncapClassName + ".add" + capitalise( singularName ) + "( key, value );");

                            sc.unindent();

                            sc.add( "}" );
                        }

                    }
                }
            }
            else
            {
                //ModelField
                writePrimitiveField( field, field.getType(), uncapClassName, "set" + capitalise( field.getName() ), sc );
            }

            sc.unindent();

            sc.add( "}" );

            statement = "else if";
        }
        if ( ! rootElement )
        {
            if ( modelClass.getFields( getGeneratedVersion() ).size() > 0 )
            {
                sc.add( "else" );

                sc.add( "{" );

                sc.indent();

                sc.add( "parser.nextText();" );

                sc.unindent();

                sc.add( "}" );
            }
        }
        else
        {
            sc.unindent();

            sc.add( "}" );

            sc.add( "eventType = parser.next();" );
        }

        sc.unindent();

        sc.add( "}" );

        sc.add( "return " + uncapClassName + ";" );

        jClass.addMethod( unmarshall );
    }

    private void writePrimitiveField( ModelField field, String type, String objectName, String setterName, JSourceCode sc )
    {
        XmlFieldMetadata fieldMetaData = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

        String tagName = fieldMetaData.getTagName();

        String parserGetter;

        if ( tagName == null )
        {
            tagName = field.getName();
        }

        if ( fieldMetaData.isAttribute() )
        {
            parserGetter = "parser.getAttributeValue( \"\", \"" + tagName + "\" )";
        }
        else
        {
            parserGetter = "parser.nextText()";
        }

        if ( "boolean".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( (new Boolean( " + parserGetter + " ) ).booleanValue() );" );
        }
        else if ( "char".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( (new Character( " + parserGetter + " ) ).charValue() );" );
        }
        else if ( "double".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( (new Double( " + parserGetter + " ) ).doubleValue() );" );
        }
        else if ( "float".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( (new Float( " + parserGetter + " ) ).floatValue() );" );
        }
        else if ( "int".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( (new Integer( " + parserGetter + " ) ).intValue() );" );
        }
        else if ( "long".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( (new Long( " + parserGetter + " ) ).longValue() );" );
        }
        else if ( "short".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( (new Short( " + parserGetter + " ) ).shortValue() );" );
        }
        else if ( "String".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( " + parserGetter + " );" );
        }
    }
}
