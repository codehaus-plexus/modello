package org.codehaus.modello.core.io;

/*
 * LICENSE
 */

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.modello.BaseElement;
import org.codehaus.modello.CodeSegment;
import org.codehaus.modello.Model;
import org.codehaus.modello.ModelAssociation;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelField;
import org.codehaus.modello.ModelloException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ModelReader
{
    private Map modelAttributes = new HashMap();

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
        return (Map) associationAttributes.get( modelAssociation.getFromClass().getName() + ":" + modelAssociation.getName() );
    }

    public Model loadModel( Reader reader )
        throws ModelloException
    {
        try
        {
            Model model = new Model();
    
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
    
            XmlPullParser parser = factory.newPullParser();
    
            parser.setInput( reader );

            parseModel( model, parser );

            return model;
        }
        catch( IOException ex )
        {
            throw new ModelloException( "Error parsing the model.", ex );
        }
        catch( XmlPullParserException ex )
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
                else if ( parser.getName().equals( "packageName" ) )
                {
                    model.setPackageName( parser.nextText() );
                }
                else if ( parser.getName().equals( "root" ) )
                {
                    model.setRoot( parser.nextText() );
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

    private void parseClasses( Model model, XmlPullParser parser )
        throws XmlPullParserException, IOException
    {
        while ( parser.nextTag() == XmlPullParser.START_TAG )
        {
            if ( parser.getName().equals( "class" ) )
            {
                ModelClass modelClass = new ModelClass();

                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( parseBaseElement( modelClass, parser ) )
                    {
                    }
                    else if ( parser.getName().equals( "superClass" ) )
                    {
                        modelClass.setSuperClass( parser.nextText() );
                    }
                    else if ( parser.getName().equals( "fields" ) )
                    {
                        parseFields( modelClass, parser );
                    }
                    else if ( parser.getName().equals( "associations" ) )
                    {
                        parseAssociations( modelClass, parser );
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
            }
            else
            {
                parser.next();
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

                Map attributes = getAttributes( parser );

                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {

                    if ( parseBaseElement( modelField, parser ) )
                    {
                    }
                    else if ( parser.getName().equals( "type" ) )
                    {
                        modelField.setType( parser.nextText() );
                    }
                    else if ( parser.getName().equals( "specification" ) )
                    {
                        modelField.setSpecifiaction( parser.nextText() );
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
                    else
                    {
                        parser.nextText();
                    }
                }

                if ( modelField.getName() != null )
                {
                    fieldAttributes.put( modelClass.getName() + ":" + modelField.getName(), attributes );
                }

                modelClass.addField( modelField );
            }
            else
            {
                parser.next();
            }
        }
    }

    private void parseAssociations( ModelClass modelClass, XmlPullParser parser )
        throws XmlPullParserException, IOException
    {
        while ( parser.nextTag() == XmlPullParser.START_TAG )
        {
            if ( parser.getName().equals( "association" ) )
            {
                ModelAssociation modelAssociation = new ModelAssociation();

                Map attributes = getAttributes( parser );

                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( parseBaseElement( modelAssociation, parser ) )
                    {
                    }
                    else if ( parser.getName().equals( "to" ) )
                    {
                        modelAssociation.setTo( parser.nextText() );
                    }
                    else if ( parser.getName().equals( "fromRole" ) )
                    {
                        modelAssociation.setFromRole( parser.nextText() );
                    }
                    else if ( parser.getName().equals( "toRole" ) )
                    {
                        modelAssociation.setToRole( parser.nextText() );
                    }
                    else if ( parser.getName().equals( "fromMultiplicity" ) )
                    {
                        modelAssociation.setFromMultiplicity( parser.nextText() );
                    }
                    else if ( parser.getName().equals( "toMultiplicity" ) )
                    {
                        modelAssociation.setToMultiplicity( parser.nextText() );
                    }
                    else
                    {
                        parser.nextText();
                    }
                }

                if ( modelAssociation.getName() != null )
                {
                    associationAttributes.put( modelClass.getName() + ":" + modelAssociation.getName(), attributes );
                }

                modelClass.addAssociation( modelAssociation );
            }
            else
            {
                parser.next();
            }
        }
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
            element.setVersion( parser.nextText() );
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

        for( int i = 0; i < parser.getAttributeCount(); i++ )
        {
            String name = parser.getAttributeName( i );

            String value = parser.getAttributeValue( i );

            attributes.put( name, value );
        }

        return attributes;
    }
}
