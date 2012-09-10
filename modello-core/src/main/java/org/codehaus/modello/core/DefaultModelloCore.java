package org.codehaus.modello.core;

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
import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.core.io.ModelReader;
import org.codehaus.modello.metadata.AssociationMetadata;
import org.codehaus.modello.metadata.ClassMetadata;
import org.codehaus.modello.metadata.FieldMetadata;
import org.codehaus.modello.metadata.InterfaceMetadata;
import org.codehaus.modello.metadata.MetadataPlugin;
import org.codehaus.modello.metadata.ModelMetadata;
import org.codehaus.modello.model.CodeSegment;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelDefault;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.ModelInterface;
import org.codehaus.modello.model.ModelValidationException;
import org.codehaus.modello.plugin.ModelloGenerator;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 */
public class DefaultModelloCore
    extends AbstractModelloCore
{
    /**
     * @requirement
     */
    private MetadataPluginManager metadataPluginManager;

    /**
     * @requirement
     */
    private GeneratorPluginManager generatorPluginManager;

    public MetadataPluginManager getMetadataPluginManager()
    {
        return metadataPluginManager;
    }

    public Model loadModel( File file )
        throws IOException, ModelloException, ModelValidationException
    {
        Reader reader = null;

        try
        {
            reader = ReaderFactory.newXmlReader( file );
            return loadModel( reader );
        }
        finally
        {
            IOUtil.close( reader );
        }
    }

    private void upgradeModifiedAttribute( String name, Map<String, String> from, String newName,
                                           Map<String, String> to, String warn )
    {
        String value = from.remove( name );

        if ( value != null )
        {
            getLogger().warn( warn );

            to.put( newName, value );
        }
    }

    private void upgradeModifiedAttribute( String name, Map<String, String> from, Map<String, String> to, String warn )
    {
        upgradeModifiedAttribute( name, from, name, to, warn );
    }

    private void upgradeModelloModel( ModelReader modelReader, Model model )
    {
        Map<String, String> modelAttributes = modelReader.getAttributesForModel();

        upgradeModifiedAttribute( "xsd.target-namespace", modelAttributes, "xsd.targetNamespace", modelAttributes,
                                  "attribute 'xsd.target-namespace' for model element is deprecated: "
                                  + "it has been renamed to 'xsd.targetNamespace'" );

        for ( ModelClass clazz : model.getAllClasses() )
        {
            Map<String, String> classAttributes = modelReader.getAttributesForClass( clazz );

            // attributes moved from root class to model
            upgradeModifiedAttribute( "xml.namespace", classAttributes, modelAttributes,
                "attribute 'xml.namespace' for class element is deprecated: it should be moved to model element" );

            upgradeModifiedAttribute( "xml.schemaLocation", classAttributes, modelAttributes,
                "attribute 'xml.schemaLocation' for class element is deprecated: it should be moved to model element" );

            for ( ModelField field : clazz.getAllFields() )
            {
                if ( field instanceof ModelAssociation )
                {
                    Map<String, String> fieldAttributes = modelReader.getAttributesForField( field );
                    Map<String, String> associationAttributes = modelReader.getAttributesForAssociation( (ModelAssociation)field );

                    upgradeModifiedAttribute( "java.adder", fieldAttributes, associationAttributes,
                        "attribute 'java.adder' for field element is deprecated: it should be moved to association" );

                    upgradeModifiedAttribute( "java.generate-create", associationAttributes,
                        "java.bidi", associationAttributes, "attribute 'java.generate-create' for association "
                        + "element is deprecated: it has been renamed to 'java.bidi'" );

                    upgradeModifiedAttribute( "java.generate-break", associationAttributes,
                        "java.bidi", associationAttributes, "attribute 'java.generate-break' for association "
                        + "element is deprecated: it has been renamed to 'java.bidi'" );

                    upgradeModifiedAttribute( "java.use-interface", associationAttributes,
                        "java.useInterface", associationAttributes, "attribute 'xml.use-interface' for association "
                        + "element is deprecated: it has been renamed to 'xml.useInterface'" );

                    upgradeModifiedAttribute( "xml.associationTagName", fieldAttributes,
                        "xml.tagName", associationAttributes, "attribute 'xml.associationTagName' for field element is "
                        + "deprecated: use 'xml.tagName' in association instead" );

                    upgradeModifiedAttribute( "xml.listStyle", fieldAttributes,
                        "xml.itemsStyle", associationAttributes, "attribute 'xml.listStyle' for field element is "
                        + "deprecated: use 'xml.itemsStyle' in association instead" );
                }

                if ( "Content".equals( field.getType() ) )
                {
                    getLogger().warn( "'Content' type is deprecated: use 'String' type and add xml.content='true' to the field" );
                    field.setType( "String" );
                    Map<String, String> fieldAttributes = modelReader.getAttributesForField( field );
                    fieldAttributes.put( "xml.content", "true" );
                }
            }
        }
    }

    public Model loadModel( Reader reader )
        throws ModelloException, ModelValidationException
    {
        ModelReader modelReader = new ModelReader();
        Model model = modelReader.loadModel( reader );

        model.initialize();

        // keep backward compatibility with Modello attributes model changes
        upgradeModelloModel( modelReader, model );

        handlePluginsMetadata( modelReader, model );

        validate( model );

        return model;
    }

    /**
     * Handle Plugins Metadata.
     *
     * @throws ModelloException
     */
    private void handlePluginsMetadata( ModelReader modelReader, Model model )
        throws ModelloException
    {
        Collection<MetadataPlugin> plugins = metadataPluginManager.getPlugins().values();

        for ( MetadataPlugin plugin : plugins )
        {
            Map<String, String> attributes = modelReader.getAttributesForModel();

            attributes = Collections.unmodifiableMap( attributes );

            ModelMetadata metadata = plugin.getModelMetadata( model, attributes );

            if ( metadata == null )
            {
                throw new ModelloException( "A meta data plugin must not return null." );
            }

            model.addMetadata( metadata );
        }

        for ( ModelClass clazz : model.getAllClasses() )
        {
            Map<String, String> attributes = modelReader.getAttributesForClass( clazz );

            attributes = Collections.unmodifiableMap( attributes );

            for ( MetadataPlugin plugin : plugins )
            {
                ClassMetadata metadata = plugin.getClassMetadata( clazz, attributes );

                if ( metadata == null )
                {
                    throw new ModelloException( "A meta data plugin must not return null." );
                }

                clazz.addMetadata( metadata );
            }

            for ( ModelField field : clazz.getAllFields() )
            {
                if ( field instanceof ModelAssociation )
                {
                    ModelAssociation modelAssociation = (ModelAssociation) field;

                    Map<String, String> fieldAttributes = modelReader.getAttributesForField( modelAssociation );

                    fieldAttributes = Collections.unmodifiableMap( fieldAttributes );

                    Map<String, String> associationAttributes = modelReader.getAttributesForAssociation( modelAssociation );

                    associationAttributes = Collections.unmodifiableMap( associationAttributes );

                    for ( MetadataPlugin plugin : plugins )
                    {
                        FieldMetadata fieldMetadata = plugin.getFieldMetadata( modelAssociation, fieldAttributes );

                        if ( fieldMetadata == null )
                        {
                            throw new ModelloException( "A meta data plugin must not return null." );
                        }

                        modelAssociation.addMetadata( fieldMetadata );

                        AssociationMetadata associationMetadata = plugin.getAssociationMetadata( modelAssociation, associationAttributes );

                        if ( associationMetadata == null )
                        {
                            throw new ModelloException( "A meta data plugin must not return null." );
                        }

                        modelAssociation.addMetadata( associationMetadata );
                    }
                }
                else
                {
                    attributes = modelReader.getAttributesForField( field );

                    attributes = Collections.unmodifiableMap( attributes );

                    for ( MetadataPlugin plugin : plugins )
                    {
                        FieldMetadata metadata = plugin.getFieldMetadata( field, attributes );

                        if ( metadata == null )
                        {
                            throw new ModelloException( "A meta data plugin must not return null." );
                        }

                        field.addMetadata( metadata );
                    }
                }
            }
        }

        for ( ModelInterface iface : model.getAllInterfaces() )
        {
            Map<String, String> attributes = modelReader.getAttributesForInterface( iface );

            attributes = Collections.unmodifiableMap( attributes );

            for ( MetadataPlugin plugin : plugins )
            {
                InterfaceMetadata metadata = plugin.getInterfaceMetadata( iface, attributes );

                if ( metadata == null )
                {
                    throw new ModelloException( "A meta data plugin must not return null." );
                }

                iface.addMetadata( metadata );
            }
        }
    }

    /**
     * Validate the entire model.
     *
     * @throws ModelValidationException
     */
    private void validate( Model model )
        throws ModelValidationException
    {
        model.validate();

        for ( ModelDefault modelDefault : model.getDefaults() )
        {
            modelDefault.validateElement();
        }

        for ( ModelClass modelClass : model.getAllClasses() )
        {
            modelClass.validate();
        }

        for ( ModelClass modelClass : model.getAllClasses() )
        {
            for ( ModelField field : modelClass.getAllFields() )
            {
                field.validate();
            }

            for ( CodeSegment codeSegment : modelClass.getAllCodeSegments() )
            {
                codeSegment.validate();
            }
        }
    }

    public void saveModel( Model model, Writer writer )
        throws ModelloException
    {
        throw new ModelloRuntimeException( "Not implemented." );
    }

    public Model translate( Reader reader, String inputType, Properties parameters )
        throws ModelloException
    {
        throw new ModelloRuntimeException( "Not implemented." );
    }

    public void generate( Model model, String outputType, Properties parameters )
        throws ModelloException
    {
        if ( model == null )
        {
            throw new ModelloRuntimeException( "Illegal argument: model == null." );
        }

        if ( outputType == null )
        {
            throw new ModelloRuntimeException( "Illegal argument: outputType == null." );
        }

        if ( parameters == null )
        {
            parameters = new Properties();
        }

        ModelloGenerator generator = generatorPluginManager.getGeneratorPlugin( outputType );

        generator.generate( model, parameters );
    }
}
