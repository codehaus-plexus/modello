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

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.model.BaseElement;
import org.codehaus.modello.model.CodeSegment;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelDefault;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.ModelInterface;
import org.codehaus.modello.model.VersionDefinition;
import org.codehaus.modello.model.VersionRange;
import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 */
public class ModelReader
{
    private Map<String, String> modelAttributes = new HashMap<String, String>();

    private Map<String, Map<String, String>> classAttributes = new HashMap<String, Map<String, String>>();

    private Map<String, Map<String, String>> interfaceAttributes = new HashMap<String, Map<String, String>>();

    private Map<String, Map<String, String>> fieldAttributes = new HashMap<String, Map<String, String>>();

    private Map<String, Map<String, String>> associationAttributes = new HashMap<String, Map<String, String>>();

    public Map<String, String> getAttributesForModel()
    {
        return modelAttributes;
    }

    public Map<String, String> getAttributesForClass( ModelClass modelClass )
    {
        return classAttributes.get( modelClass.getName() );
    }

    public Map<String, String> getAttributesForInterface( ModelInterface modelInterface )
    {
        return interfaceAttributes.get( modelInterface.getName() );
    }

    public Map<String, String> getAttributesForField( ModelField modelField )
    {
        return fieldAttributes.get( modelField.getModelClass().getName() + ':' + modelField.getName() + ':'
            + modelField.getVersionRange() );
    }

    public Map<String, String> getAttributesForAssociation( ModelAssociation modelAssociation )
    {
        return associationAttributes.get( modelAssociation.getModelClass().getName() + ':' + modelAssociation.getName()
            + ':' + modelAssociation.getVersionRange() );
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
                else if ( "id".equals( parser.getName() ) )
                {
                    model.setId( parser.nextText() );
                }
                else if ( "defaults".equals( parser.getName() ) )
                {
                    parseDefaults( model, parser );
                }
                else if ( "versionDefinition".equals( parser.getName() ) )
                {
                    parseVersionDefinition( model, parser );
                }
                else if ( "interfaces".equals( parser.getName() ) )
                {
                    parseInterfaces( model, parser );
                }
                else if ( "classes".equals( parser.getName() ) )
                {
                    parseClasses( model, parser );
                }
                else if ( "model".equals( parser.getName() ) )
                {
                    modelAttributes = getAttributes( parser );
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
            if ( "default".equals( parser.getName() ) )
            {
                ModelDefault modelDefault = new ModelDefault();

                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( "key".equals( parser.getName() ) )
                    {
                        modelDefault.setKey( parser.nextText() );
                    }
                    else if ( "value".equals( parser.getName() ) )
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

    private void parseVersionDefinition( Model model, XmlPullParser parser )
        throws XmlPullParserException, IOException
    {
        if ( "versionDefinition".equals( parser.getName() ) )
        {
            VersionDefinition versionDefinition = new VersionDefinition();

            while ( parser.nextTag() == XmlPullParser.START_TAG )
            {
                if ( "type".equals( parser.getName() ) )
                {
                    versionDefinition.setType( parser.nextText() );
                }
                else if ( "value".equals( parser.getName() ) )
                {
                    versionDefinition.setValue( parser.nextText() );
                }
                else
                {
                    parser.nextText();
                }
            }

            model.setVersionDefinition( versionDefinition );
        }
    }

    private void parseInterfaces( Model model, XmlPullParser parser )
        throws XmlPullParserException, IOException
    {
        while ( parser.nextTag() == XmlPullParser.START_TAG )
        {
            if ( "interface".equals( parser.getName() ) )
            {
                ModelInterface modelInterface = new ModelInterface();

                Map<String, String> attributes = getAttributes( parser );

                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( parseBaseElement( modelInterface, parser ) )
                    {
                    }
                    else if ( "superInterface".equals( parser.getName() ) )
                    {
                        modelInterface.setSuperInterface( parser.nextText() );
                    }
                    else if ( "packageName".equals( parser.getName() ) )
                    {
                        modelInterface.setPackageName( parser.nextText() );
                    }
                    else if ( "codeSegments".equals( parser.getName() ) )
                    {
                        parseCodeSegment( modelInterface, parser );
                    }
                    else
                    {
                        parser.nextText();
                    }
                }

                model.addInterface( modelInterface );
                interfaceAttributes.put( modelInterface.getName(), attributes );
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
            if ( "class".equals( parser.getName() ) )
            {
                ModelClass modelClass = new ModelClass();

                Map<String, String> attributes = getAttributes( parser );

                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( parseBaseElement( modelClass, parser ) )
                    {
                    }
                    else if ( "interfaces".equals( parser.getName() ) )
                    {
                        parseClassInterfaces( modelClass, parser );
                    }
                    else if ( "superClass".equals( parser.getName() ) )
                    {
                        modelClass.setSuperClass( parser.nextText() );
                    }
                    else if ( "packageName".equals( parser.getName() ) )
                    {
                        modelClass.setPackageName( parser.nextText() );
                    }
                    else if ( "fields".equals( parser.getName() ) )
                    {
                        parseFields( modelClass, parser );
                    }
                    else if ( "codeSegments".equals( parser.getName() ) )
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
        while ( parser.nextTag() == XmlPullParser.START_TAG )
        {
            if ( "interface".equals( parser.getName() ) )
            {
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
            if ( "field".equals( parser.getName() ) )
            {
                ModelField modelField = new ModelField();

                ModelAssociation modelAssociation = null;

                Map<String, String> fAttributes = getAttributes( parser );

                Map<String, String> aAttributes = new HashMap<String, String>();

                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( parseBaseElement( modelField, parser ) )
                    {
                    }
                    else if ( "association".equals( parser.getName() ) )
                    {
                        aAttributes = getAttributes( parser );

                        modelAssociation = parseAssociation( parser );
                    }
                    else if ( "alias".equals( parser.getName() ) )
                    {
                        modelField.setAlias( parser.nextText() );
                    }
                    else if ( "type".equals( parser.getName() ) )
                    {
                        modelField.setType( parser.nextText() );
                    }
                    else if ( "defaultValue".equals( parser.getName() ) )
                    {
                        modelField.setDefaultValue( parser.nextText() );
                    }
                    else if ( "typeValidator".equals( parser.getName() ) )
                    {
                        modelField.setTypeValidator( parser.nextText() );
                    }
                    else if ( "required".equals( parser.getName() ) )
                    {
                        modelField.setRequired( Boolean.valueOf( parser.nextText() ) );
                    }
                    else if ( "identifier".equals( parser.getName() ) )
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
                    fieldAttributes.put(
                        modelClass.getName() + ":" + modelField.getName() + ":" + modelField.getVersionRange(),
                        fAttributes );
                }

                if ( modelAssociation != null )
                {
                    // Base element
                    modelAssociation.setName( modelField.getName() );

                    modelAssociation.setDescription( modelField.getDescription() );

                    modelAssociation.setVersionRange( modelField.getVersionRange() );

                    modelAssociation.setComment( modelField.getComment() );

                    modelAssociation.setAnnotations( modelField.getAnnotations() );

                    // model field fields
                    modelAssociation.setType( modelField.getType() );

                    modelAssociation.setAlias( modelField.getAlias() );

                    modelAssociation.setDefaultValue( modelField.getDefaultValue() );

                    modelAssociation.setTypeValidator( modelField.getTypeValidator() );

                    modelAssociation.setRequired( modelField.isRequired() );

                    modelAssociation.setIdentifier( modelField.isIdentifier() );

                    if ( modelAssociation.getName() != null )
                    {
                        associationAttributes.put( modelClass.getName() + ":" + modelAssociation.getName() + ":"
                                                   + modelAssociation.getVersionRange(), aAttributes );
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
            else if ( "type".equals( parser.getName() ) )
            {
                modelAssociation.setTo( parser.nextText() );
            }
            else if ( "multiplicity".equals( parser.getName() ) )
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
            if ( "codeSegment".equals( parser.getName() ) )
            {
                CodeSegment codeSegment = new CodeSegment();

                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( parseBaseElement( codeSegment, parser ) )
                    {
                    }
                    else if ( "code".equals( parser.getName() ) )
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
            if ( "codeSegment".equals( parser.getName() ) )
            {
                CodeSegment codeSegment = new CodeSegment();

                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( parseBaseElement( codeSegment, parser ) )
                    {
                    }
                    else if ( "code".equals( parser.getName() ) )
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
        if ( "name".equals( parser.getName() ) )
        {
            element.setName( parser.nextText() );
        }
        else if ( "description".equals( parser.getName() ) )
        {
            element.setDescription( parser.nextText() );
        }
        else if ( "version".equals( parser.getName() ) )
        {
            element.setVersionRange( new VersionRange( parser.nextText() ) );
        }
        else if ( "comment".equals( parser.getName() ) )
        {
            element.setComment( parser.nextText() );
        }
        else if ( "annotations".equals( parser.getName() ) )
        {
            List<String> annotationsList = new ArrayList<String>();
            while ( parser.nextTag() == XmlPullParser.START_TAG )
            {
                if ( "annotation".equals( parser.getName() ) )
                {
                    annotationsList.add( parser.nextText() );
                }
            }
            element.setAnnotations( annotationsList );
        }
        else
        {
            return false;
        }

        return true;
    }

    private Map<String, String> getAttributes( XmlPullParser parser )
    {
        Map<String, String> attributes = new HashMap<String, String>();

        for ( int i = 0; i < parser.getAttributeCount(); i++ )
        {
            String name = parser.getAttributeName( i );

            String value = parser.getAttributeValue( i );

            attributes.put( name, value );
        }

        return attributes;
    }
}
