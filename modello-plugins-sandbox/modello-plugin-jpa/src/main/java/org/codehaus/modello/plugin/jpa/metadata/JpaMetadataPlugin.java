package org.codehaus.modello.plugin.jpa.metadata;

/**
 * Copyright 2007 Rahul Thakur
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.metadata.AbstractMetadataPlugin;
import org.codehaus.modello.metadata.AssociationMetadata;
import org.codehaus.modello.metadata.ClassMetadata;
import org.codehaus.modello.metadata.FieldMetadata;
import org.codehaus.modello.metadata.MetadataPlugin;
import org.codehaus.modello.metadata.ModelMetadata;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.plugin.metadata.processor.ClassMetadataProcessorMetadata;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;

/**
 * A {@link MetadataPlugin} extension that processes JPA specific metadata.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.0.0
 * @plexus.component role="org.codehaus.modello.metadata.MetadataPlugin"
 *                   role-hint="jpa"
 */
public class JpaMetadataPlugin
    extends AbstractMetadataPlugin
    implements Contextualizable
{

    /**
     * Prefix that identifies the attributes meant for consumption by the
     * Modello JPA Plugin.
     */
    private static final String PLUGIN_PREFIX = "jpa.";

    /**
     * For Metdata lookups.
     */
    private PlexusContainer container;

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable#contextualize(org.codehaus.plexus.context.Context)
     */
    public void contextualize( Context context )
        throws ContextException
    {
        this.container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.modello.metadata.MetadataPlugin#getAssociationMetadata(org.codehaus.modello.model.ModelAssociation,
     *      java.util.Map)
     */
    public AssociationMetadata getAssociationMetadata( ModelAssociation modelAssociation, Map data )
        throws ModelloException
    {
        // TODO Auto-generated method stub
        return new JpaAssociationLevelMetadata();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.modello.metadata.MetadataPlugin#getClassMetadata(org.codehaus.modello.model.ModelClass,
     *      java.util.Map)
     */
    public ClassMetadata getClassMetadata( ModelClass modelClass, Map data )
        throws ModelloException
    {
        JpaClassLevelMetadata metadata = new JpaClassLevelMetadata();

        // obtain all keys in the data passed in
        Set attrs = data.keySet();

        // iterate and identify ones that a JPA Modello Plugin specific
        for ( Iterator it = attrs.iterator(); it.hasNext(); )
        {
            String attribute = (String) it.next();
            if ( attribute.startsWith( PLUGIN_PREFIX ) )
            {
                String hint = attribute.substring( PLUGIN_PREFIX.length() );
                getLogger().info(
                                  "Looking up ProcessorMetadata for PLUGIN_PREFIX : " + PLUGIN_PREFIX + " , hint: "
                                      + hint );
                try
                {
                    ClassMetadataProcessorMetadata processorMetadata = (ClassMetadataProcessorMetadata) container
                        .lookup( ClassMetadataProcessorMetadata.ROLE, hint );
                    metadata.add( processorMetadata );
                }
                catch ( ComponentLookupException e )
                {
                    // FIXME Revisit handling.
                    e.printStackTrace();
                }
            }

        }

        // Look up ClassMetadataProcessorMetadata instances

        return metadata;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.modello.metadata.MetadataPlugin#getFieldMetadata(org.codehaus.modello.model.ModelField,
     *      java.util.Map)
     */
    public FieldMetadata getFieldMetadata( ModelField model, Map data )
        throws ModelloException
    {
        // TODO Auto-generated method stub
        return new JpaFieldLevelMetdata();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.modello.metadata.MetadataPlugin#getModelMetadata(org.codehaus.modello.model.Model,
     *      java.util.Map)
     */
    public ModelMetadata getModelMetadata( Model model, Map data )
        throws ModelloException
    {
        return new JpaModelMetadata();
    }

}
