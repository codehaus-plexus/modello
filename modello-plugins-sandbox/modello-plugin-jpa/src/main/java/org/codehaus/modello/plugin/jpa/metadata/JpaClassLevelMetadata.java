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

import org.codehaus.modello.metadata.ClassMetadata;

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

    /**
     * Determines if a Class defined in the Model is a PersistableEntity.
     * <p>
     * This is <code>true</code> if the class is persistable, else
     * <code>false</code>.
     */
    private boolean isEntity;

    /**
     * Determines if a Class defined in the Data Model is a Embeddable.
     * <p>
     * This is <code>true</code> if embeddable, else <code>false</code>.
     */
    private boolean isEmbeddable;

    /**
     * Table name that the Class defined in the Data Model maps to in the
     * database.
     */
    private String table;

    /**
     * @return the isEntity
     */
    public boolean isEntity()
    {
        return isEntity;
    }

    /**
     * @param isEntity the isEntity to set
     */
    public void setEntity( boolean isEntity )
    {
        this.isEntity = isEntity;
    }

    /**
     * @return the isEmbeddable
     */
    public boolean isEmbeddable()
    {
        return isEmbeddable;
    }

    /**
     * @param isEmbeddable the isEmbeddable to set
     */
    public void setEmbeddable( boolean isEmbeddable )
    {
        this.isEmbeddable = isEmbeddable;
    }

    /**
     * @return the tableName
     */
    public String getTable()
    {
        return table;
    }

    /**
     * @param tableName the tableName to set
     */
    public void setTable( String tableName )
    {
        this.table = tableName;
    }

}
