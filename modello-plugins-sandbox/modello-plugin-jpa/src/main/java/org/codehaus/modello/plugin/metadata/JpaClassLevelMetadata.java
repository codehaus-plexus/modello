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

package org.codehaus.modello.plugin.metadata;

import org.codehaus.modello.metadata.ClassMetadata;

/**
 * Wraps the <b>Class</b> level JPA metadata.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.0.0
 */
public class JpaClassLevelMetadata implements ClassMetadata
{

    public static final String ID = JpaClassLevelMetadata.class.getName();

    /**
     * Determines if a Class defined in the Model is a PersistableEntity.
     * <p>
     * This is <code>true</code> if the class is persistable else
     * <code>false</code>.
     */
    private boolean isEntity;

    private boolean isEmbedded;

    private String tableName;

}
