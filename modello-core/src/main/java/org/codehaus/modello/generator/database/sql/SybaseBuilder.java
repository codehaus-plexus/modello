package org.codehaus.modello.generator.database.sql;

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

import java.io.IOException;
import java.util.Iterator;

import org.codehaus.modello.generator.database.model.Column;
import org.codehaus.modello.generator.database.model.Table;

/**
 * An SQL Builder for Sybase
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision$
 */
public class SybaseBuilder extends SqlBuilder
{

    public SybaseBuilder()
    {
        setForeignKeyConstraintsNamed( true );
    }

    public void dropTable( Table table ) throws IOException
    {
        String tableName = table.getName();

        // drop the foreign key contraints
        int counter = 1;
        for ( Iterator iter = table.getForeignKeys().iterator(); iter.hasNext(); )
        {
//            ForeignKey key = (ForeignKey) iter.next();

            String constraintName = tableName + "_FK_" + counter;
            println( "IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name=''"
                     + constraintName + "')" );
            printIndent();
            print( "ALTER TABLE " + tableName + " DROP CONSTRAINT " + constraintName );
            printEndOfStatement();
        }

        // now drop the table
        println( "IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = '"
                 + tableName + "')" );
        println( "BEGIN" );
        printIndent();
        println( "DROP TABLE " + tableName );
        print( "END" );
        printEndOfStatement();
    }

    protected void printComment( String text ) throws IOException
    {
        print( "/* " );
        print( text );
        println( " */" );
    }

    protected void printAutoIncrementColumn( Table table, Column column ) throws IOException
    {
        //print( "AUTO_INCREMENT" );
    }
}
