package org.codehaus.modello.generator.database.sql;

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

import java.io.IOException;

import org.codehaus.modello.generator.database.model.Column;
import org.codehaus.modello.generator.database.model.Table;

/**
 * An SQL Builder for the <a href="http://axion.tigris.org/">Axion</a> JDBC database.
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision$
 */
public class AxionBuilder extends SqlBuilder
{

    public AxionBuilder()
    {
        setForeignKeysEmbedded( true );
    }

    protected String getSqlType( Column column )
    {
        // Axion doesn't support text width specification
        return getNativeType( column );
    }

    protected void writePrimaryKeys( Table table ) throws IOException
    {
        // disable primary key constraints
    }

    protected void writeForeignKeys( Table table ) throws IOException
    {
        // disable foreign key constraints
    }

    protected void printAutoIncrementColumn( Table table, Column column ) throws IOException
    {
        //print( "IDENTITY" );
    }

    protected void printNotNullable() throws IOException
    {
        //print("NOT NULL");
    }

    protected void printNullable() throws IOException
    {
        //print("NULL");
    }

    protected String getNativeType( Column column )
    {
        if ( column.getTypeCode() == java.sql.Types.DECIMAL )
        {
            return "FLOAT";
        }
        else
        {
            return super.getNativeType( column );
        }
    }

    /**
     * Outputs the DDL to add a column to a table. Axion
     * does not support default values so we are removing
     * default from the Axion column builder.
     */
    public void createColumn( Table table, Column column ) throws IOException
    {
        print( column.getName() );
        print( " " );
        print( getSqlType( column ) );
        print( " " );

        if ( column.isRequired() )
        {
            printNotNullable();
        }
        else
        {
            printNullable();
        }
        print( " " );
        if ( column.isAutoIncrement() )
        {
            printAutoIncrementColumn( table, column );
        }
    }
}
