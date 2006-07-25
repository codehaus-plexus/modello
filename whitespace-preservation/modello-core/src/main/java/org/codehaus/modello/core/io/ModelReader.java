package org.codehaus.modello.core.io;

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

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.model.BaseElement;
import org.codehaus.modello.model.CodeSegment;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelDefault;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.ModelInterface;
import org.codehaus.modello.model.VersionRange;
import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class ModelReader
{
    private Map classAttributes = new HashMap();

    private Map fieldAttributes = new HashMap();

    private Map associationAttributes = new HashMap();

    public Map getAttributesForModel( Model model )
    {
        return (Map) fieldAttributes.get( model.getName() );
    }

    public Map getAttributesForClass( ModelClass modelClass )
    {
        return (Map) classAttributes.get( modelClass.getName() );
    }

    public Map getAttributesForField( ModelField modelField )
    {
        return (Map) fieldAttributes.get( modelField.getModelClass().getName() + ":" + modelField.getName() );
    }

    public Map getAttributesForAssociation( ModelAssociation modelAssociation )
    {
        return (Map) associationAttributes.get(
            modelAssociation.getModelClass().getName() + ":" + modelAssociation.getName() );
    }

    public Model loadModel( Reader reader )
        throws ModelloException
    {
        try
        {
            Model model = new Model();

            XmlPullParser parser = new MXParser();

            parser.setInput( reader );

            parseModel( model, parser );

            return model;
        }
        catch ( IOException ex )
        {
            throw new ModelloException( "Error parsing the model.", ex );
        }
        catch ( XmlPullParserException ex )
        {
            throw new ModelloException( "Error parsing the model.", ex );
        }
    }

    public void parseModel( Model model, XmlPullParser parser )
        throws XmlPullParserException, IOException
    {
        int eventType = parser.getEventType();

        while ( eventType != XmlPullParser.END_DOCUMENT )
        {
            if ( eventType == XmlPullParser.START_TAG )
            {
                if ( parseBaseElement( model, parser ) )
                {
                }
                else if ( parser.getName().equals( "id" ) )
                {
                    model.setId( parser.nextText() );
                }
                else if ( parser.getName().equals( "defaults" ) )
                {
                    parseDefaults( model, parser );
                }
                else if ( parser.getName().equals( "interfaces" ) )
                {
                    parseInterfaces( model, parser );
                }
                else if ( parser.getName().equals( "classes" ) )
                {
                    parseClasses( model, parser );
                }
                else
                {
//                    parser.nextText();
                }
            }
            eventType = parser.next();
        }
    }

    private void parseDefaults( Model model, XmlPullParser parser )
        throws XmlPullParserException, IOException
    {
        while ( parser.nextTag() == XmlPullParser.START_TAG )
        {
            if ( parser.getName().equals( "default" ) )
            {
                ModelDefault modelDefault = new ModelDefault();

                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( parser.getName().equals( "key" ) )
                    {
                        modelDefault.setKey( parser.nextText() );
                    }
                    else if ( parser.getName().equals( "value" ) )
                    {
                        modelDefault.setValue( parser.nextText() );
                    }
                    else
                    {
                        parser.nextText();
                    }
                }

                model.addDefault( modelDefault );
            }
            else
            {
                parser.next();
            }
        }
    }

    private void parseInterfaces( Model model, XmlPullParser parser )
        throws XmlPullParserException, IOException
    {
        while ( parser.nextTag() == XmlPullParser.START_TAG )
        {
            if ( parser.getName().equals( "interface" ) )
            {
                ModelInterface modelInterface = new ModelInterface();

                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( parseBaseElement( modelInterface, parser ) )
                    {
                    }
                    else if ( parser.getName().equals( "superInterface" ) )
                    {
                        modelInterface.setSuperInterface( parser.nextText() );
                    }
                    else if ( parser.getName().equals( "packageName" ) )
                    {
                        modelInterface.setPackageName( parser.nextText() );
                    }
                    else if ( parser.getName().equals( "codeSegments" ) )
                    {
                        parseCodeSegment( modelInterface, parser );
                    }
                    else
                    {
                        parser.nextText();
                    }
                }

                model.addInterface( modelInterface );
            }
            else
            {
                parser.next();
            }
        }
    }

    private void parseClasses( Model model, XmlPullParser parser )
        throws XmlPullParserException, IOException
    {
        while ( parser.nextTag() == XmlPullParser.START_TAG )
        {
            if ( parser.getName().equals( "class" ) )
            {
                ModelClass modelClass = new ModelClass();

                Map attributes = getAttributes( parser );

                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( parseBaseElement( modelClass, parser ) )
                    {
                    }
                    else if ( parser.getName().equals( "interfaces" ) )
                    {
                        parseClassInterfaces( modelClass, parser );
                    }
                    else if ( parser.getName().equals( "superClass" ) )
                    {
                        modelClass.setSuperClass( parser.nextText() );
                    }
                    else if ( parser.getName().equals( "packageName" ) )
                    {
                        modelClass.setPackageName( parser.nextText() );
                    }
                    else if ( parser.getName().equals( "fields" ) )
                    {
                        parseFields( modelClass, parser );
                    }
                    else if ( parser.getName().equals( "codeSegments" ) )
                    {
                        parseCodeSegment( modelClass, parser );
                    }
                    else
                    {
                        parser.nextText();
                    }
                }

                model.addClass( modelClass );
                classAttributes.put( modelClass.getName(), attributes );
            }
            else
            {
                parser.next();
            }
        }
    }

    private void parseClassInterfaces( ModelClass modelClass, XmlPullParser parser )
        throws IOException, XmlPullParserException
    {
        while( parser.nextTag() == XmlPullParser.START_TAG )
        {
            System.out.println( "parser.getName(): " + parser.getName() );
            if ( parser.getName().equals( "interface" ) )
            {
                System.out.println( "parser.getText() = " + parser.getText() );
                modelClass.addInterface( parser.nextText() );
            }
            else
            {
                parser.nextText();
            }
        }
    }

    private void parseFields( ModelClass modelClass, XmlPullParser parser )
        throws XmlPullParserException, IOException
    {
        while ( parser.nextTag() == XmlPullParser.START_TAG )
        {
            if ( parser.getName().equals( "field" ) )
            {
                ModelField modelField = new ModelField();

                ModelAssociation modelAssociation = null;

                Map fAttributes = getAttributes( parser );

                Map aAttributes = new HashMap();

                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( parseBaseElement( modelField, parser ) )
                    {
                    }
                    else if ( parser.getName().equals( "association" ) )
                    {
                        aAttributes = getAttributes( parser );

                        modelAssociation = parseAssociation( parser );
                    }
                    else if ( parser.getName().equals( "alias" ) )
                    {
                        modelField.setAlias( parser.nextText() );
                    }
                    else if ( parser.getName().equals( "type" ) )
                    {
                        modelField.setType( parser.nextText() );
                    }
                    else if ( parser.getName().equals( "defaultValue" ) )
                    {
                        modelField.setDefaultValue( parser.nextText() );
                    }
                    else if ( parser.getName().equals( "typeValidator" ) )
                    {
                        modelField.setTypeValidator( parser.nextText() );
                    }
                    else if ( parser.getName().equals( "required" ) )
                    {
                        modelField.setRequired( Boolean.valueOf( parser.nextText() ).booleanValue() );
                    }
                    else if ( parser.getName().equals( "identifier" ) )
                    {
                        modelField.setIdentifier( Boolean.valueOf( parser.nextText() ).booleanValue() );
                    }
                    else
                    {
                        parser.nextText();
                    }
                }

                if ( modelField.getName() != null )
                {
                    fieldAttributes.put( modelClass.getName() + ":" + modelField.getName(), fAttributes );
                }

                if ( modelAssociation != null )
                {
                    // Base element
                    modelAssociation.setName( modelField.getName() );

                    modelAssociation.setDescription( modelField.getDescription() );

                    modelAssociation.setVersionRange( modelField.getVersionRange() );

                    modelAssociation.setComment( modelField.getComment() );

                    // model field fields
                    modelAssociation.setType( modelField.getType() );

                    modelAssociation.setAlias( modelField.getAlias() );

                    modelAssociation.setDefaultValue( modelField.getDefaultValue() );

                    modelAssociation.setTypeValidator( modelField.getTypeValidator() );

                    modelAssociation.setRequired( modelField.isRequired() );

                    modelAssociation.setIdentifier( modelField.isIdentifier() );

                    if ( modelAssociation.getName() != null )
                    {
                        associationAttributes.put( modelClass.getName() + ":" + modelAssociation.getName(),
                                                   aAttributes );
                    }

                    modelClass.addField( modelAssociation );
                }
                else
                {
                    modelClass.addField( modelField );
                }
            }
            else
            {
                parser.next();
            }
        }
    }

    private ModelAssociation parseAssociation( XmlPullParser parser )
        throws XmlPullParserException, IOException
    {
        ModelAssociation modelAssociation = new ModelAssociation();

        while ( parser.nextTag() == XmlPullParser.START_TAG )
        {
            if ( parseBaseElement( modelAssociation, parser ) )
            {
            }
            else if ( parser.getName().equals( "type" ) )
            {
                modelAssociation.setTo( parser.nextText() );
            }
            else if ( parser.getName().equals( "multiplicity" ) )
            {
                modelAssociation.setMultiplicity( parser.nextText() );
            }
            else
            {
                parser.nextText();
            }
        }

        return modelAssociation;
    }

    private void parseCodeSegment( ModelClass modelClass, XmlPullParser parser )
        throws XmlPullParserException, IOException
    {
        while ( parser.nextTag() == XmlPullParser.START_TAG )
        {
            if ( parser.getName().equals( "codeSegment" ) )
            {
                CodeSegment codeSegment = new CodeSegment();

                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( parseBaseElement( codeSegment, parser ) )
                    {
                    }
                    else if ( parser.getName().equals( "code" ) )
                    {
                        codeSegment.setCode( parser.nextText() );
                    }
                    else
                    {
                        parser.nextText();
                    }
                }

                modelClass.addCodeSegment( codeSegment );
            }
            else
            {
                parser.next();
            }
        }
    }

    private void parseCodeSegment( ModelInterface modelInterface, XmlPullParser parser )
        throws XmlPullParserException, IOException
    {
        while ( parser.nextTag() == XmlPullParser.START_TAG )
        {
            if ( parser.getName().equals( "codeSegment" ) )
            {
                CodeSegment codeSegment = new CodeSegment();

                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( parseBaseElement( codeSegment, parser ) )
                    {
                    }
                    else if ( parser.getName().equals( "code" ) )
                    {
                        codeSegment.setCode( parser.nextText() );
                    }
                    else
                    {
                        parser.nextText();
                    }
                }

                modelInterface.addCodeSegment( codeSegment );
            }
            else
            {
                parser.next();
            }
        }
    }

    private boolean parseBaseElement( BaseElement element, XmlPullParser parser )
        throws XmlPullParserException, IOException
    {
        if ( parser.getName().equals( "name" ) )
        {
            element.setName( parser.nextText() );
        }
        else if ( parser.getName().equals( "description" ) )
        {
            element.setDescription( parser.nextText() );
        }
        else if ( parser.getName().equals( "version" ) )
        {
            element.setVersionRange( new VersionRange( parser.nextText() ) );
        }
        else if ( parser.getName().equals( "comment" ) )
        {
            element.setComment( parser.nextText() );
        }
        else
        {
            return false;
        }

        return true;
    }

    private Map getAttributes( XmlPullParser parser )
    {
        Map attributes = new HashMap();

        for ( int i = 0; i < parser.getAttributeCount(); i++ )
        {
            String name = parser.getAttributeName( i );

            String value = parser.getAttributeValue( i );

            attributes.put( name, value );
        }

        return attributes;
    }
}
