package org.codehaus.modello.plugin;

/**
 * Copyright 2007-2008 The Apache Software Foundation.
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

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.model.Model;

import java.util.Properties;

/**
 * Generates the an ORM (Object Relational Mapping) from the Modello model
 * source.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.0.0
 */
public class OpenJpaOrmMappingModelloGenerator extends AbstractModelloGenerator
{

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.modello.plugin.ModelloGenerator#generate(org.codehaus.modello.model.Model,
     *      java.util.Properties)
     */
    public void generate( Model model, Properties properties ) throws ModelloException
    {
        // TODO Implement!
    }

}
