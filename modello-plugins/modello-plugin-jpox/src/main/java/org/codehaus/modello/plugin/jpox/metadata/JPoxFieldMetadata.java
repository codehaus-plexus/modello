package org.codehaus.modello.plugin.jpox.metadata;

/*
 * Copyright (c) 2005, Codehaus.org
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

import org.codehaus.modello.metadata.FieldMetadata;
import org.codehaus.plexus.util.StringUtils;

import java.util.List;

/**
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class JPoxFieldMetadata implements FieldMetadata
{
    public static final String ID = JPoxFieldMetadata.class.getName();

    public static final String[] FOREIGN_KEY_ACTIONS = new String[] { "cascade", "restrict", "null", "default" };

    public static final String[] BOOLEANS = new String[] { "true", "false" };

    private List fetchGroupNames;

    private String mappedBy;

    private String nullValue;

    private String columnName;

    private boolean primaryKey;

    private String persistenceModifier;

    private String valueStrategy;

    private String joinTableName;

    private String indexed;

    private boolean unique;

    private boolean foreignKey;

    private String foreignKeyDeferred;

    private String foreignKeyDeleteAction;

    private String foreignKeyUpdateAction;

    public List getFetchGroupNames()
    {
        return fetchGroupNames;
    }

    public void setFetchGroupNames( List fetchGroupNames )
    {
        this.fetchGroupNames = fetchGroupNames;
    }

    public String getMappedBy()
    {
        return mappedBy;
    }

    public void setMappedBy( String mappedBy )
    {
        this.mappedBy = mappedBy;
    }

    public String getNullValue()
    {
        return nullValue;
    }

    public void setNullValue( String nullValue )
    {
        this.nullValue = nullValue;
    }

    public String getColumnName()
    {
        return columnName;
    }

    public void setColumnName( String columnName )
    {
        this.columnName = columnName;
    }

    public boolean isPrimaryKey()
    {
        return primaryKey;
    }

    public void setPrimaryKey( boolean primaryKey )
    {
        this.primaryKey = primaryKey;
    }

    public String getPersistenceModifier()
    {
        return persistenceModifier;
    }

    public void setPersistenceModifier( String persistenceModifier )
    {
        this.persistenceModifier = persistenceModifier;
    }

    public String getValueStrategy()
    {
        return valueStrategy;
    }

    public void setValueStrategy( String valueStrategy )
    {
        this.valueStrategy = valueStrategy;
    }

    public String getJoinTableName()
    {
        return joinTableName;
    }

    public void setJoinTableName( String joinTableName )
    {
        this.joinTableName = joinTableName;
    }

    public String getIndexed()
    {
        return indexed;
    }

    public void setIndexed( String indexed )
    {
        this.indexed = indexed;
    }

    public String getForeignKeyDeferred()
    {
        return foreignKeyDeferred;
    }

    public void setForeignKeyDeferred( String foreignKeyDeferred )
    {
        this.foreignKeyDeferred = foreignKeyDeferred;

        if ( StringUtils.isNotEmpty( this.foreignKeyDeferred ) )
        {
            this.foreignKey = true;
        }
    }

    public String getForeignKeyDeleteAction()
    {
        return foreignKeyDeleteAction;
    }

    public void setForeignKeyDeleteAction( String foreignKeyDeleteAction )
    {
        this.foreignKeyDeleteAction = foreignKeyDeleteAction;

        if ( StringUtils.isNotEmpty( this.foreignKeyDeleteAction ) )
        {
            this.foreignKey = true;
        }
    }

    public String getForeignKeyUpdateAction()
    {
        return foreignKeyUpdateAction;
    }

    public void setForeignKeyUpdateAction( String foreignKeyUpdateAction )
    {
        this.foreignKeyUpdateAction = foreignKeyUpdateAction;

        if ( StringUtils.isNotEmpty( this.foreignKeyUpdateAction ) )
        {
            this.foreignKey = true;
        }
    }

    public boolean isUnique()
    {
        return unique;
    }

    public void setUnique( boolean unique )
    {
        this.unique = unique;
    }

    public boolean isForeignKey()
    {
        return foreignKey;
    }

    public void setForeignKey( boolean foreignKey )
    {
        this.foreignKey = foreignKey;
    }
}
