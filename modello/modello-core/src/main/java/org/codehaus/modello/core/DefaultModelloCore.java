package org.codehaus.modello.core;

/*
 * LICENSE
 */

import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.codehaus.modello.CodeSegment;
import org.codehaus.modello.Model;
import org.codehaus.modello.ModelAssociation;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelField;
import org.codehaus.modello.ModelValidationException;
import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.core.io.ModelReader;
import org.codehaus.modello.metadata.AssociationMetadata;
import org.codehaus.modello.metadata.ClassMetadata;
import org.codehaus.modello.metadata.FieldMetadata;
import org.codehaus.modello.metadata.MetadataPlugin;
import org.codehaus.modello.metadata.ModelMetadata;
import org.codehaus.modello.plugin.ModelloGenerator;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
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

    public Model loadModel( Reader reader )
        throws ModelloException, ModelValidationException
    {
        Model model;

        ModelReader modelReader = new ModelReader();

        model = modelReader.loadModel( reader );

        model.initialize();

        // ----------------------------------------------------------------------
        // Handle Metadata
        // ----------------------------------------------------------------------

        for( Iterator plugins = metadataPluginManager.getPlugins(); plugins.hasNext(); )
        {
            MetadataPlugin plugin = (MetadataPlugin) plugins.next();

            Map attributes = Collections.EMPTY_MAP;

            attributes = Collections.unmodifiableMap( attributes );

            ModelMetadata metadata = plugin.getModelMetadata( model, attributes );

            if ( metadata == null )
            {
                throw new ModelloException( "A meta data plugin must not return null." );
            }

            model.addMetadata( metadata );
        }

        for( Iterator classes = model.getClasses().iterator(); classes.hasNext(); )
        {
            ModelClass clazz = (ModelClass) classes.next();

            Map attributes = Collections.unmodifiableMap( Collections.EMPTY_MAP );

            attributes = Collections.unmodifiableMap( attributes );

            for( Iterator plugins = metadataPluginManager.getPlugins(); plugins.hasNext(); )
            {
                MetadataPlugin plugin = (MetadataPlugin) plugins.next();

                ClassMetadata metadata = plugin.getClassMetadata( clazz, attributes );

                if ( metadata == null )
                {
                    throw new ModelloException( "A meta data plugin must not return null." );
                }

                clazz.addMetadata( metadata );
            }

            for( Iterator fields = clazz.getFields().iterator(); fields.hasNext(); )
            {
                ModelField field = (ModelField) fields.next();

                attributes = modelReader.getAttributesForField( field.getName() );

                attributes = Collections.unmodifiableMap( attributes );

                for( Iterator plugins = metadataPluginManager.getPlugins(); plugins.hasNext(); )
                {
                    MetadataPlugin plugin = (MetadataPlugin) plugins.next();

                    FieldMetadata metadata = plugin.getFieldMetadata( field, attributes );

                    if ( metadata == null )
                    {
                        throw new ModelloException( "A meta data plugin must not return null." );
                    }

                    field.addMetadata( metadata );
                }
            }

            for( Iterator associations = clazz.getAssociations().iterator(); associations.hasNext(); )
            {
                ModelAssociation association = (ModelAssociation) associations.next();

                attributes = modelReader.getAttributesForAssociation( association.getName() );

                attributes = Collections.unmodifiableMap( attributes );

                for( Iterator plugins = metadataPluginManager.getPlugins(); plugins.hasNext(); )
                {
                    MetadataPlugin plugin = (MetadataPlugin) plugins.next();

                    AssociationMetadata metadata = plugin.getAssociationMetadata( association, attributes );

                    if ( metadata == null )
                    {
                        throw new ModelloException( "A meta data plugin must not return null." );
                    }

                    association.addMetadata( metadata );
                }
            }
        }

        // ----------------------------------------------------------------------
        // Validate the entire model
        // ----------------------------------------------------------------------

        model.validate();

        for( Iterator classes = model.getClasses().iterator(); classes.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) classes.next();

            modelClass.validate();
        }

        for( Iterator classes = model.getClasses().iterator(); classes.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) classes.next();

            for( Iterator fields = modelClass.getFields().iterator(); fields.hasNext(); )
            {
                ModelField modelField = (ModelField) fields.next();

                modelField.validate();
            }

            for( Iterator associations = modelClass.getAssociations().iterator(); associations.hasNext(); )
            {
                ModelAssociation modelAssociation = (ModelAssociation) associations.next();

                modelAssociation.validate();
            }

            for( Iterator codeSegments = modelClass.getCodeSegments().iterator(); codeSegments.hasNext(); )
            {
                CodeSegment codeSegment = (CodeSegment) codeSegments.next();

                codeSegment.validate();
            }
        }

        return model;
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
