package org.codehaus.modello.plugin.xpp3;

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
import org.codehaus.modello.generator.java.javasource.JClass;
import org.codehaus.modello.generator.java.javasource.JField;
import org.codehaus.modello.generator.java.javasource.JMethod;
import org.codehaus.modello.generator.java.javasource.JParameter;
import org.codehaus.modello.generator.java.javasource.JSourceCode;
import org.codehaus.modello.generator.java.javasource.JSourceWriter;
import org.codehaus.modello.generator.java.javasource.JType;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelDefault;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.plugins.xml.XmlAssociationMetadata;
import org.codehaus.modello.plugins.xml.XmlFieldMetadata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
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
        catch ( IOException ex )
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

        jClass.addImport( "org.codehaus.plexus.util.xml.pull.*" );

        jClass.addImport( "java.io.Reader" );

        jClass.addImport( "java.util.ArrayList" );

        jClass.addImport( "java.util.List" );

        addModelImports( jClass, null );

        // ----------------------------------------------------------------------
        // Write option setters
        // ----------------------------------------------------------------------

        // The Field
        JField addDefaultEntities = new JField( JType.Boolean, "addDefaultEntities" );

        addDefaultEntities.setComment( "If set the parser till be loaded with all single characters from the XHTML specification.\n" +
                                       "The entities used:\n" + "<ul>\n" + "<li>http://www.w3.org/TR/xhtml1/DTD/xhtml-lat1.ent</li>\n" +
                                       "<li>http://www.w3.org/TR/xhtml1/DTD/xhtml-special.ent</li>\n" +
                                       "<li>http://www.w3.org/TR/xhtml1/DTD/xhtml-symbol.ent</li>\n" + "</ul>\n" );

        addDefaultEntities.setInitString( "true" );

        jClass.addField( addDefaultEntities );

        // The setter
        JMethod addDefaultEntitiesSetter = new JMethod( null, "setAddDefaultEntities" );

        addDefaultEntitiesSetter.addParameter( new JParameter( JType.Boolean, "addDefaultEntities" ) );

        addDefaultEntitiesSetter.setSourceCode( "this.addDefaultEntities = addDefaultEntities;" );

        addDefaultEntitiesSetter.setComment( "Sets the state of the \"add default entities\" flag." );

        jClass.addMethod( addDefaultEntitiesSetter );

        // The getter
        JMethod addDefaultEntitiesGetter = new JMethod( JType.Boolean, "getAddDefaultEntities" );

        addDefaultEntitiesSetter.setComment( "Returns the state of the \"add default entities\" flag." );

        addDefaultEntitiesGetter.setSourceCode( "return addDefaultEntities;" );

        jClass.addMethod( addDefaultEntitiesGetter );

        // ----------------------------------------------------------------------
        // Write the parse method which will do the unmarshalling.
        // ----------------------------------------------------------------------

        ModelClass root = objectModel.getClass( objectModel.getRoot( getGeneratedVersion() ), getGeneratedVersion() );

        String rootElement = uncapitalise( root.getName() );

        JMethod unmarshall = new JMethod( new JClass( root.getName() ), "read" );

        unmarshall.addParameter( new JParameter( new JClass( "Reader" ), "reader" ) );

        unmarshall.addException( new JClass( "Exception" ) );

        JSourceCode sc = unmarshall.getSourceCode();

        sc.add( "XmlPullParser parser = new MXParser();" );

        sc.add( "parser.setInput( reader );" );

        sc.add( "" );

        writeParserInitialization( sc );

        sc.add( "" );

        sc.add( "return parse" + root.getName() + "( \"" + rootElement + "\", parser );" );

        jClass.addMethod( unmarshall );

        // ----------------------------------------------------------------------
        // Write the class parsers
        // ----------------------------------------------------------------------

        writeAllClassesParser( objectModel, jClass );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

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

        sc.add( "{" );

        sc.indent();

        for ( Iterator i = modelClass.getAllFields( getGeneratedVersion(), true ).iterator(); i.hasNext(); )
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

        for ( Iterator i = modelClass.getAllFields( getGeneratedVersion(), true ).iterator(); i.hasNext(); )
        {
            ModelField field = (ModelField) i.next();

            XmlFieldMetadata fieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

            if ( fieldMetadata.isAttribute() )
            {
                continue;
            }

            String tagName = fieldMetadata.getTagName();

            if ( tagName == null )
            {
                tagName = field.getName();
            }

            String singularTagName = fieldMetadata.getAssociationTagName();

            if ( singularTagName == null )
            {
                singularTagName = singular( tagName );
            }

            boolean wrappedList = XmlFieldMetadata.LIST_STYLE_WRAPPED.equals( fieldMetadata.getListStyle() );

            String capFieldName = capitalise( field.getName() );

            String singularName = singular( field.getName() );

            if ( field instanceof ModelAssociation )
            {
                ModelAssociation association = (ModelAssociation) field;

                String associationName = association.getName();

                if ( ModelAssociation.ONE_MULTIPLICITY.equals( association.getMultiplicity() ) )
                {
                    sc.add( statement + " ( parser.getName().equals( \"" + tagName + "\" ) )" );

                    sc.add( "{" );

                    sc.indent();

                    sc.add( uncapClassName + ".set" + capFieldName + "( parse" + association.getTo() + "( \"" +
                            tagName +
                            "\", parser ) );" );

                    sc.unindent();

                    sc.add( "}" );
                }
                else
                {
                    //MANY_MULTIPLICITY

                    String type = association.getType();

                    if ( ModelDefault.LIST.equals( type ) || ModelDefault.SET.equals( type ) )
                    {
                        if ( wrappedList )
                        {
                            sc.add( statement + " ( parser.getName().equals( \"" + tagName + "\" ) )" );

                            sc.add( "{" );

                            sc.indent();

                            sc.add( type + " " + associationName + " = " + association.getDefaultValue() + ";" );

                            sc.add( uncapClassName + ".set" + capFieldName + "( " + associationName + " );" );

                            sc.add( "while ( parser.nextTag() == XmlPullParser.START_TAG )" );

                            sc.add( "{" );

                            sc.indent();

                            sc.add( "if ( parser.getName().equals( \"" + singularTagName + "\" ) )" );

                            sc.add( "{" );

                            sc.indent();
                        }
                        else
                        {
                            sc.add( statement + " ( parser.getName().equals( \"" + singularTagName + "\" ) )" );

                            sc.add( "{" );

                            sc.indent();

                            sc.add(
                                type + " " + associationName + " = " + uncapClassName + ".get" + capFieldName + "();" );

                            sc.add( "if ( " + associationName + " == null )" );

                            sc.add( "{" );

                            sc.indent();

                            sc.add( associationName + " = " + association.getDefaultValue() + ";" );

                            sc.add( uncapClassName + ".set" + capFieldName + "( " + associationName + " );" );

                            sc.unindent();

                            sc.add( "}" );
                        }

                        if ( isClassInModel( association.getTo(), modelClass.getModel() ) )
                        {
                            sc.add( associationName + ".add( parse" + association.getTo() + "( \"" + singularTagName +
                                    "\", parser ) );" );
                        }
                        else
                        {
                            writePrimitiveField( association, association.getTo(), associationName, "add", sc );
                        }

                        if ( wrappedList )
                        {
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

                            sc.unindent();

                            sc.add( "}" );
                        }
                        else
                        {

                            sc.unindent();

                            sc.add( "}" );
                        }
                    }
                    else
                    {
                        //Map or Properties

                        sc.add( statement + " ( parser.getName().equals( \"" + tagName + "\" ) )" );

                        sc.add( "{" );

                        sc.indent();

                        XmlAssociationMetadata xmlAssociationMetadata = (XmlAssociationMetadata) association.getAssociationMetadata(
                            XmlAssociationMetadata.ID );

                        if ( XmlAssociationMetadata.EXPLODE_MODE.equals( xmlAssociationMetadata.getMapStyle() ) )
                        {
                            sc.add( "while ( parser.nextTag() == XmlPullParser.START_TAG )" );

                            sc.add( "{" );

                            sc.indent();

                            sc.add( "if ( parser.getName().equals( \"" + singularTagName + "\" ) )" );

                            sc.add( "{" );

                            sc.indent();

                            sc.add( "String key = null;" );

                            sc.add( "String value = null;" );

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

                            sc.add( "value = parser.nextText()" );

                            if ( fieldMetadata.isTrim() )
                            {
                                sc.add( ".trim()" );
                            }

                            sc.add( ";" );

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

                            sc.add( uncapClassName + ".add" + capitalise( singularName ) + "( key, value );" );

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

                            sc.add( "String value = parser.nextText()" );

                            if ( fieldMetadata.isTrim() )
                            {
                                sc.add( ".trim()" );
                            }

                            sc.add( ";" );

                            sc.add( uncapClassName + ".add" + capitalise( singularName ) + "( key, value );" );

                            sc.unindent();

                            sc.add( "}" );
                        }

                        sc.unindent();

                        sc.add( "}" );
                    }
                }
            }
            else
            {
                sc.add( statement + " ( parser.getName().equals( \"" + tagName + "\" ) )" );

                sc.add( "{" );

                sc.indent();

                //ModelField
                writePrimitiveField( field, field.getType(), uncapClassName, "set" + capitalise( field.getName() ), sc );

                sc.unindent();

                sc.add( "}" );
            }

            statement = "else if";
        }
        if ( !rootElement )
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

    private void writePrimitiveField( ModelField field, String type, String objectName, String setterName,
                                      JSourceCode sc )
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

            if ( fieldMetaData.isTrim() )
            {
                parserGetter += ".trim()";
            }
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

    private void writeParserInitialization( JSourceCode sc )
    {
        sc.add( "if ( addDefaultEntities ) " );

        sc.add( "{" );

        sc.indent();

        sc.add( "// ----------------------------------------------------------------------" );
        sc.add( "// Latin 1 entities" );
        sc.add( "// ----------------------------------------------------------------------" );
        sc.add( "" );
        sc.add( "parser.defineEntityReplacementText( \"nbsp\", \"\\u00a0\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"iexcl\", \"\\u00a1\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"cent\", \"\\u00a2\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"pound\", \"\\u00a3\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"curren\", \"\\u00a4\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"yen\", \"\\u00a5\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"brvbar\", \"\\u00a6\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"sect\", \"\\u00a7\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"uml\", \"\\u00a8\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"copy\", \"\\u00a9\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"ordf\", \"\\u00aa\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"laquo\", \"\\u00ab\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"not\", \"\\u00ac\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"shy\", \"\\u00ad\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"reg\", \"\\u00ae\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"macr\", \"\\u00af\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"deg\", \"\\u00b0\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"plusmn\", \"\\u00b1\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"sup2\", \"\\u00b2\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"sup3\", \"\\u00b3\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"acute\", \"\\u00b4\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"micro\", \"\\u00b5\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"para\", \"\\u00b6\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"middot\", \"\\u00b7\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"cedil\", \"\\u00b8\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"sup1\", \"\\u00b9\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"ordm\", \"\\u00ba\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"raquo\", \"\\u00bb\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"frac14\", \"\\u00bc\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"frac12\", \"\\u00bd\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"frac34\", \"\\u00be\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"iquest\", \"\\u00bf\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Agrave\", \"\\u00c0\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Aacute\", \"\\u00c1\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Acirc\", \"\\u00c2\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Atilde\", \"\\u00c3\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Auml\", \"\\u00c4\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Aring\", \"\\u00c5\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"AElig\", \"\\u00c6\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Ccedil\", \"\\u00c7\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Egrave\", \"\\u00c8\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Eacute\", \"\\u00c9\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Ecirc\", \"\\u00ca\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Euml\", \"\\u00cb\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Igrave\", \"\\u00cc\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Iacute\", \"\\u00cd\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Icirc\", \"\\u00ce\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Iuml\", \"\\u00cf\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"ETH\", \"\\u00d0\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Ntilde\", \"\\u00d1\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Ograve\", \"\\u00d2\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Oacute\", \"\\u00d3\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Ocirc\", \"\\u00d4\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Otilde\", \"\\u00d5\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Ouml\", \"\\u00d6\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"times\", \"\\u00d7\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Oslash\", \"\\u00d8\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Ugrave\", \"\\u00d9\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Uacute\", \"\\u00da\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Ucirc\", \"\\u00db\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Uuml\", \"\\u00dc\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Yacute\", \"\\u00dd\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"THORN\", \"\\u00de\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"szlig\", \"\\u00df\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"agrave\", \"\\u00e0\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"aacute\", \"\\u00e1\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"acirc\", \"\\u00e2\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"atilde\", \"\\u00e3\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"auml\", \"\\u00e4\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"aring\", \"\\u00e5\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"aelig\", \"\\u00e6\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"ccedil\", \"\\u00e7\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"egrave\", \"\\u00e8\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"eacute\", \"\\u00e9\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"ecirc\", \"\\u00ea\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"euml\", \"\\u00eb\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"igrave\", \"\\u00ec\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"iacute\", \"\\u00ed\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"icirc\", \"\\u00ee\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"iuml\", \"\\u00ef\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"eth\", \"\\u00f0\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"ntilde\", \"\\u00f1\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"ograve\", \"\\u00f2\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"oacute\", \"\\u00f3\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"ocirc\", \"\\u00f4\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"otilde\", \"\\u00f5\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"ouml\", \"\\u00f6\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"divide\", \"\\u00f7\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"oslash\", \"\\u00f8\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"ugrave\", \"\\u00f9\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"uacute\", \"\\u00fa\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"ucirc\", \"\\u00fb\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"uuml\", \"\\u00fc\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"yacute\", \"\\u00fd\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"thorn\", \"\\u00fe\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"yuml\", \"\\u00ff\" ); " );
        sc.add( "" );
        sc.add( "// ----------------------------------------------------------------------" );
        sc.add( "// Special entities" );
        sc.add( "// ----------------------------------------------------------------------" );
        sc.add( "" );
        // These are required to be handled by the parser by the XML specification
//        sc.add( "parser.defineEntityReplacementText( \"quot\", \"\\u0022\" ); " );
//        sc.add( "parser.defineEntityReplacementText( \"amp\", \"\\u0026\" ); " );
//        sc.add( "parser.defineEntityReplacementText( \"lt\", \"\\u003c\" ); " );
//        sc.add( "parser.defineEntityReplacementText( \"gt\", \"\\u003e\" ); " );
//        sc.add( "parser.defineEntityReplacementText( \"apos\", \"\\u0027\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"OElig\", \"\\u0152\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"oelig\", \"\\u0153\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Scaron\", \"\\u0160\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"scaron\", \"\\u0161\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Yuml\", \"\\u0178\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"circ\", \"\\u02c6\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"tilde\", \"\\u02dc\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"ensp\", \"\\u2002\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"emsp\", \"\\u2003\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"thinsp\", \"\\u2009\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"zwnj\", \"\\u200c\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"zwj\", \"\\u200d\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"lrm\", \"\\u200e\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"rlm\", \"\\u200f\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"ndash\", \"\\u2013\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"mdash\", \"\\u2014\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"lsquo\", \"\\u2018\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"rsquo\", \"\\u2019\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"sbquo\", \"\\u201a\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"ldquo\", \"\\u201c\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"rdquo\", \"\\u201d\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"bdquo\", \"\\u201e\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"dagger\", \"\\u2020\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Dagger\", \"\\u2021\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"permil\", \"\\u2030\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"lsaquo\", \"\\u2039\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"rsaquo\", \"\\u203a\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"euro\", \"\\u20ac\" ); " );
        sc.add( "" );
        sc.add( "// ----------------------------------------------------------------------" );
        sc.add( "// Symbol entities" );
        sc.add( "// ----------------------------------------------------------------------" );
        sc.add( "" );
        sc.add( "parser.defineEntityReplacementText( \"fnof\", \"\\u0192\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Alpha\", \"\\u0391\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Beta\", \"\\u0392\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Gamma\", \"\\u0393\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Delta\", \"\\u0394\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Epsilon\", \"\\u0395\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Zeta\", \"\\u0396\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Eta\", \"\\u0397\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Theta\", \"\\u0398\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Iota\", \"\\u0399\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Kappa\", \"\\u039a\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Lambda\", \"\\u039b\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Mu\", \"\\u039c\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Nu\", \"\\u039d\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Xi\", \"\\u039e\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Omicron\", \"\\u039f\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Pi\", \"\\u03a0\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Rho\", \"\\u03a1\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Sigma\", \"\\u03a3\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Tau\", \"\\u03a4\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Upsilon\", \"\\u03a5\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Phi\", \"\\u03a6\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Chi\", \"\\u03a7\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Psi\", \"\\u03a8\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Omega\", \"\\u03a9\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"alpha\", \"\\u03b1\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"beta\", \"\\u03b2\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"gamma\", \"\\u03b3\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"delta\", \"\\u03b4\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"epsilon\", \"\\u03b5\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"zeta\", \"\\u03b6\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"eta\", \"\\u03b7\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"theta\", \"\\u03b8\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"iota\", \"\\u03b9\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"kappa\", \"\\u03ba\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"lambda\", \"\\u03bb\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"mu\", \"\\u03bc\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"nu\", \"\\u03bd\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"xi\", \"\\u03be\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"omicron\", \"\\u03bf\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"pi\", \"\\u03c0\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"rho\", \"\\u03c1\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"sigmaf\", \"\\u03c2\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"sigma\", \"\\u03c3\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"tau\", \"\\u03c4\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"upsilon\", \"\\u03c5\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"phi\", \"\\u03c6\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"chi\", \"\\u03c7\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"psi\", \"\\u03c8\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"omega\", \"\\u03c9\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"thetasym\", \"\\u03d1\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"upsih\", \"\\u03d2\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"piv\", \"\\u03d6\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"bull\", \"\\u2022\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"hellip\", \"\\u2026\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"prime\", \"\\u2032\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"Prime\", \"\\u2033\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"oline\", \"\\u203e\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"frasl\", \"\\u2044\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"weierp\", \"\\u2118\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"image\", \"\\u2111\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"real\", \"\\u211c\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"trade\", \"\\u2122\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"alefsym\", \"\\u2135\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"larr\", \"\\u2190\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"uarr\", \"\\u2191\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"rarr\", \"\\u2192\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"darr\", \"\\u2193\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"harr\", \"\\u2194\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"crarr\", \"\\u21b5\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"lArr\", \"\\u21d0\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"uArr\", \"\\u21d1\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"rArr\", \"\\u21d2\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"dArr\", \"\\u21d3\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"hArr\", \"\\u21d4\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"forall\", \"\\u2200\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"part\", \"\\u2202\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"exist\", \"\\u2203\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"empty\", \"\\u2205\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"nabla\", \"\\u2207\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"isin\", \"\\u2208\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"notin\", \"\\u2209\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"ni\", \"\\u220b\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"prod\", \"\\u220f\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"sum\", \"\\u2211\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"minus\", \"\\u2212\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"lowast\", \"\\u2217\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"radic\", \"\\u221a\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"prop\", \"\\u221d\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"infin\", \"\\u221e\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"ang\", \"\\u2220\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"and\", \"\\u2227\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"or\", \"\\u2228\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"cap\", \"\\u2229\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"cup\", \"\\u222a\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"int\", \"\\u222b\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"there4\", \"\\u2234\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"sim\", \"\\u223c\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"cong\", \"\\u2245\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"asymp\", \"\\u2248\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"ne\", \"\\u2260\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"equiv\", \"\\u2261\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"le\", \"\\u2264\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"ge\", \"\\u2265\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"sub\", \"\\u2282\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"sup\", \"\\u2283\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"nsub\", \"\\u2284\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"sube\", \"\\u2286\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"supe\", \"\\u2287\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"oplus\", \"\\u2295\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"otimes\", \"\\u2297\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"perp\", \"\\u22a5\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"sdot\", \"\\u22c5\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"lceil\", \"\\u2308\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"rceil\", \"\\u2309\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"lfloor\", \"\\u230a\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"rfloor\", \"\\u230b\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"lang\", \"\\u2329\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"rang\", \"\\u232a\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"loz\", \"\\u25ca\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"spades\", \"\\u2660\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"clubs\", \"\\u2663\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"hearts\", \"\\u2665\" ); " );
        sc.add( "parser.defineEntityReplacementText( \"diams\", \"\\u2666\" ); " );
        sc.add( "" );

        sc.unindent();

        sc.add( "}" );
    }
}
