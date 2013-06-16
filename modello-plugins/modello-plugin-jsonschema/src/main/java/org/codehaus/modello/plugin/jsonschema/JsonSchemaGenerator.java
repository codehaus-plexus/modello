package org.codehaus.modello.plugin.jsonschema;

/*
 * Copyright (c) 2013, Codehaus.org
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
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelDefault;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.plugins.xml.AbstractXmlJavaGenerator;
import org.codehaus.modello.plugins.xml.metadata.XmlAssociationMetadata;
import org.codehaus.plexus.util.StringUtils;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerator.Feature;

/**
 * @author <a href="mailto:simonetripodi@apache.org">Simone Tripodi</a>
 * @since 1.8
 */
public final class JsonSchemaGenerator
    extends AbstractXmlJavaGenerator
{

    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

        try
        {
            generateJsonSchema( parameters );
        }
        catch ( IOException ioe )
        {
            throw new ModelloException( "Exception while generating JSON Schema.", ioe );
        }
    }

    private void generateJsonSchema( Properties parameters )
        throws IOException, ModelloException
    {
        Model objectModel = getModel();

        File directory = getOutputDirectory();

        if ( isPackageWithVersion() )
        {
            directory = new File( directory, getGeneratedVersion().toString() );
        }

        if ( !directory.exists() )
        {
            directory.mkdirs();
        }

        // we assume parameters not null
        String schemaFileName = parameters.getProperty( ModelloParameterConstants.OUTPUT_JSONSCHEMA_FILE_NAME );

        File schemaFile;

        if ( schemaFileName != null )
        {
            schemaFile = new File( directory, schemaFileName );
        }
        else
        {
            schemaFile = new File( directory, objectModel.getId() + "-" + getGeneratedVersion() + ".schema.json" );
        }

        JsonGenerator generator = new JsonFactory()
                                  .enable( Feature.AUTO_CLOSE_JSON_CONTENT )
                                  .enable( Feature.AUTO_CLOSE_TARGET )
                                  .enable( Feature.ESCAPE_NON_ASCII )
                                  .enable( Feature.FLUSH_PASSED_TO_STREAM )
                                  .enable( Feature.QUOTE_FIELD_NAMES )
                                  .enable( Feature.QUOTE_NON_NUMERIC_NUMBERS )
                                  .disable( Feature.WRITE_NUMBERS_AS_STRINGS )
                                  .createGenerator( schemaFile, JsonEncoding.UTF8 );

        generator.useDefaultPrettyPrinter();

        ModelClass root = objectModel.getClass( objectModel.getRoot( getGeneratedVersion() ),
                                                getGeneratedVersion() );

        try
        {
            generator.writeStartObject();
            generator.writeStringField( "$schema", "http://json-schema.org/draft-04/schema#" );

            writeClassDocumentation( generator, root, true );

            generator.writeObjectFieldStart( "definitions" );

            for ( ModelClass current : objectModel.getClasses( getGeneratedVersion() ) )
            {
                if ( !root.equals( current ) )
                {
                    writeClassDocumentation( generator, current, false );
                }
            }

            // end "definitions"
            generator.writeEndObject();

            // end main object
            generator.writeEndObject();
        }
        finally
        {
            generator.close();
        }
    }

    private void writeClassDocumentation( JsonGenerator generator, ModelClass modelClass, boolean isRoot )
        throws IOException
    {
        if ( !isRoot )
        {
            generator.writeObjectFieldStart( modelClass.getName() );
        }

        generator.writeStringField( "id", modelClass.getName() + '#' );
        writeDescriptionField( generator, modelClass.getDescription() );
        writeTypeField( generator, "object" );

        generator.writeObjectFieldStart( "properties" );

        List<String> required = new LinkedList<String>();

        ModelClass reference = modelClass;
        // traverse the whole modelClass hierarchy to create the nested Builder instance
        while ( reference != null )
        {
            // collect parameters and set them in the instance object
            for ( ModelField modelField : reference.getFields( getGeneratedVersion() ) )
            {
                if ( modelField.isRequired() )
                {
                    required.add( modelField.getName() );
                }

                // each field is represented as object
                generator.writeObjectFieldStart( modelField.getName() );

                writeDescriptionField( generator, modelField.getDescription() );

                if ( modelField instanceof ModelAssociation )
                {
                    ModelAssociation modelAssociation = (ModelAssociation) modelField;

                    if ( modelAssociation.isOneMultiplicity() )
                    {
                        writeTypeField( generator, modelAssociation.getType() );
                    }
                    else
                    {
                        // MANY_MULTIPLICITY
                        writeTypeField( generator, "array" );

                        generator.writeObjectFieldStart( "items" );

                        String type = modelAssociation.getType();
                        String toType = modelAssociation.getTo();

                        if ( ModelDefault.LIST.equals( type ) || ModelDefault.SET.equals( type ) )
                        {
                            writeTypeField( generator, toType );
                        }
                        else
                        {
                            // Map or Properties

                            writeTypeField( generator, "object" );

                            generator.writeObjectFieldStart( "properties" );

                            XmlAssociationMetadata xmlAssociationMetadata =
                                (XmlAssociationMetadata) modelAssociation.getAssociationMetadata( XmlAssociationMetadata.ID );

                            if ( xmlAssociationMetadata.isMapExplode() )
                            {
                                // key
                                generator.writeObjectFieldStart( "key" );
                                writeTypeField( generator, "string" );
                                generator.writeEndObject();

                                // value
                                generator.writeObjectFieldStart( "value" );
                                writeTypeField( generator, toType );
                                generator.writeEndObject();

                                // properties
                                generator.writeEndObject();

                                // required field
                                generator.writeArrayFieldStart( "required" );
                                generator.writeString( "key" );
                                generator.writeString( "value" );
                                generator.writeEndArray();
                            }
                            else
                            {
                                generator.writeObjectFieldStart( "*" );
                                writeTypeField( generator, toType );
                                generator.writeEndObject();
                            }
                        }

                        // items
                        generator.writeEndObject();
                    }
                }
                else
                {
                    writeTypeField( generator, modelField.getType() );
                }

                generator.writeEndObject();
            }

            if ( reference.hasSuperClass() )
            {
                reference = reference.getModel().getClass( reference.getSuperClass(), getGeneratedVersion() );
            }
            else
            {
                reference = null;
            }
        }

        // end of `properties` element
        generator.writeEndObject();

        // write `required` sequence
        if ( !required.isEmpty() )
        {
            generator.writeArrayFieldStart( "required" );

            for ( String requiredField : required )
            {
                generator.writeString( requiredField );
            }

            generator.writeEndArray();
        }

        // end definition
        if ( !isRoot )
        {
            generator.writeEndObject();
        }
    }

    private static void writeDescriptionField( JsonGenerator generator, String description )
        throws IOException
    {
        if ( !StringUtils.isEmpty( description ) )
        {
            generator.writeStringField( "description", description );
        }
    }

    private void writeTypeField( JsonGenerator generator, String type )
        throws IOException
    {
        if ( isClassInModel( type, getModel() ) )
        {
            generator.writeStringField( "$ref", "#/definitions/" + type );
            return;
        }

        // try to make the input type compliant, as much as possible, to JSON Schema primitive types
        // see http://json-schema.org/latest/json-schema-core.html#anchor8
        if ( "boolean".equals( type ) || "Boolean".equals( type ) )
        {
            type = "boolean";
        }
        else if ( "int".equals( type ) || "Integer".equals( type ) )
        {
            type = "integer";
        }
        else if ( "short".equals( type ) || "Short".equals( type )
                  || "long".equals( type ) || "Long".equals( type )
                  || "double".equals( type ) || "Double".equals( type )
                  || "float".equals( type ) || "Float".equals( type ) )
        {
            type = "number";
        }
        else if ( "String".equals( type ) )
        {
            type = "string";
        }

        // keep as it is otherwise

        generator.writeStringField( "type", type );
    }

}
