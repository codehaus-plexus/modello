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
 * Models a database.
 *
 * @author John Marshall/Connectria
 * @author Matthew Hawthorne
 * @version $Id$
 */
public class Database
{
    private String name;

    private String idMethod;

    /**
     * Database version id
     */
    private String version;

    private List tables = new ArrayList();

    public Database()
    {
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String ver )
    {
        version = ver;
    }


    public void setIdMethod( String idMethod )
    {
        this.idMethod = idMethod;
    }


    public void addTable( Table table )
    {
        tables.add( table );
    }

    public List getTables()
    {
        return tables;
    }

    // Helper methods

    /**
     * Finds the table with the specified name, using case insensitive matching.
     * Note that this method is not called getTable(String) to avoid introspection
     * problems.
     */
    public Table findTable( String name )
    {
        for ( Iterator iter = tables.iterator(); iter.hasNext(); )
        {
            Table table = (Table) iter.next();

            // table names are typically case insensitive
            if ( table.getName().equalsIgnoreCase( name ) )
            {
                return table;
            }
        }
        return null;
    }


    // Additions for PropertyUtils

    public void setTable( int index, Table table )
    {
        addTable( table );
    }

    public Table getTable( int index )
    {
        return (Table) tables.get( index );
    }


    public String toString()
    {
        return super.toString() + "[name=" + name + ";tableCount=" + tables.size() + "]";
    }
}
