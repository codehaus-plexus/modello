package org.codehaus.modello.generator.database.model;

/*
 * Copyright (c) 2004, Jason van Zyl
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Models a table.
 *
 * @author John Marshall/Connectria
 * @author Matthew Hawthorne
 * @version $Id$
 */

public class Table
{
    private String catalog = null;

    private String name = null;

    private String schema = null;

    private String remarks = null;

    private String type = null;

    private List columns = new ArrayList();

    private List foreignKeys = new ArrayList();

    private List indexes = new ArrayList();

    public Table()
    {
    }

    public String getCatalog()
    {
        return this.catalog;
    }

    public void setCatalog( String catalog )
    {
        this.catalog = catalog;
    }

    public String getRemarks()
    {
        return this.remarks;
    }

    public void setRemarks( String remarks )
    {
        this.remarks = remarks;
    }

    public String getSchema()
    {
        return this.schema;
    }

    public void setSchema( String schema )
    {
        this.schema = schema;
    }

    public String getType()
    {
        return ( type == null ) ? "(null)" : type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public void addColumn( Column column )
    {
        columns.add( column );
    }

    public void addAll( List columns )
    {
        if ( columns != null &&
            columns.size() > 0 )
        {
            int columnsSize = columns.size();
            for ( int i = 0; i < columnsSize; i++ )
            {
                Column column = (Column) columns.get( i );
                if ( column != null )
                {
                    this.addColumn( column );
                }
            }
        }
    }

    public List getColumns()
    {
        return columns;
    }

    public void addForeignKey( ForeignKey foreignKey )
    {
        foreignKeys.add( foreignKey );
    }

    public List getForeignKeys()
    {
        return foreignKeys;
    }

    public Column getColumn( int index )
    {
        return (Column) columns.get( index );
    }

    public ForeignKey getForeignKey( int index )
    {
        return (ForeignKey) foreignKeys.get( index );
    }

    public void addIndex( Index index )
    {
        indexes.add( index );
    }

    public List getIndexes()
    {
        return indexes;
    }

    public Index getIndex( int index )
    {
        return (Index) indexes.get( index );
    }

//take this out of Unique is annoying
//this is in here to support <unique> in the xml
    /**
     * Add a unique index to this table
     *
     * @param index The unique index
     */
    public void addUnique( Unique index )
    {
        addIndex( index );
    }


    // Helper methods
    //-------------------------------------------------------------------------

    /**
     * @return true if there is at least one primary key column
     *         on this table
     */
    public boolean hasPrimaryKey()
    {
        for ( Iterator iter = getColumns().iterator(); iter.hasNext(); )
        {
            Column column = (Column) iter.next();
            if ( column.isPrimaryKey() )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the table with the specified name, using case insensitive matching.
     * Note that this method is not called getColumn(String) to avoid introspection
     * problems.
     */
    public Column findColumn( String name )
    {
        for ( Iterator iter = getColumns().iterator(); iter.hasNext(); )
        {
            Column column = (Column) iter.next();

            // column names are typically case insensitive
            if ( column.getName().equalsIgnoreCase( name ) )
            {
                return column;
            }
        }
        return null;
    }

    /**
     * Finds the index with the specified name, using case insensitive matching.
     * Note that this method is not called getIndex(String) to avoid introspection
     * problems.
     */
    public Index findIndex( String name )
    {
        for ( Iterator iter = getIndexes().iterator(); iter.hasNext(); )
        {
            Index index = (Index) iter.next();

            // column names are typically case insensitive
            if ( index.getName().equalsIgnoreCase( name ) )
            {
                return index;
            }
        }
        return null;
    }

    /**
     * @return a List of primary key columns or an empty list if there are no
     *         primary key columns for this Table
     */
    public List getPrimaryKeyColumns()
    {
        List answer = new ArrayList();
        for ( Iterator iter = getColumns().iterator(); iter.hasNext(); )
        {
            Column column = (Column) iter.next();
            if ( column.isPrimaryKey() )
            {
                answer.add( column );
            }
        }
        return answer;
    }

    /**
     * @return the auto increment column, if there is one, otherwise null is returned
     */
    public Column getAutoIncrementColumn()
    {
        for ( Iterator iter = getColumns().iterator(); iter.hasNext(); )
        {
            Column column = (Column) iter.next();
            if ( column.isAutoIncrement() )
            {
                return column;
            }
        }
        return null;
    }
}
