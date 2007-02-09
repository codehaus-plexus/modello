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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.modello.metadata.ClassMetadata;
import org.codehaus.modello.plugin.metadata.processor.MetadataProcessor;
import org.codehaus.modello.plugin.metadata.processor.ProcessorMetadata;

/**
 * Wraps the <b>Class</b> level JPA metadata.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.0.0
 */
public class JpaClassLevelMetadata
    implements ClassMetadata
{

    public static final String ID = JpaClassLevelMetadata.class.getName();

    private List processorMetadata= new ArrayList();

    /**
     * Adds the Class level {@link ProcessorMetadata} to the list wrapped within
     * the extensions.
     * 
     * @param metadata
     */
    public void add( ProcessorMetadata metadata )
    {
        this.processorMetadata.add( metadata );
    }

    /**
     * Returns a list of {@link ProcessorMetadata} instances wrapped by the
     * extensions.
     * <p>
     * These are inturn used to obtain the associated {@link MetadataProcessor}
     * to handle the {@link ProcessorMetadata} instance.
     * 
     * @return list of {@link ProcessorMetadata}
     */
    public List getProcessorMetadata()
    {
        return this.processorMetadata;
    }

}
