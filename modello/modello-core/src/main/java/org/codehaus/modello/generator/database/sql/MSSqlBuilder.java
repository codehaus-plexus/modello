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
 * An SQL Builder for MS SQL
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision$
 */
public class MSSqlBuilder extends SqlBuilder
{

    public MSSqlBuilder()
    {
        setForeignKeyConstraintsNamed( true );
    }

    public void dropTable( Table table ) throws IOException
    {
        // this method is one example that might be a bit simpler if implemented in Velocity...

        String tableName = table.getName();

        // drop the foreign key contraints
        int counter = 1;
        for ( Iterator iter = table.getForeignKeys().iterator(); iter.hasNext(); )
        {
//            ForeignKey key = (ForeignKey) iter.next();

            String constraintName = tableName + "_FK_" + counter;
            println( "IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='"
                     + constraintName + "'" );
            printIndent();
            print( "ALTER TABLE " + tableName + " DROP CONSTRAINT " + constraintName );
            printEndOfStatement();
        }

        // now drop the table
        println( "IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = '" + tableName + "')" );
        println( "BEGIN" );
        println( "     DECLARE @reftable nvarchar(60), @constraintname nvarchar(60)" );
        println( "     DECLARE refcursor CURSOR FOR" );
        println( "     select reftables.name tablename, cons.name constraitname" );
        println( "      from sysobjects tables," );
        println( "           sysobjects reftables," );
        println( "           sysobjects cons," );
        println( "           sysreferences ref" );
        println( "       where tables.id = ref.rkeyid" );
        println( "         and cons.id = ref.constid" );
        println( "         and reftables.id = ref.fkeyid" );
        println( "         and tables.name = '" + tableName + "'" );
        println( "     OPEN refcursor" );
        println( "     FETCH NEXT from refcursor into @reftable, @constraintname" );
        println( "     while @@FETCH_STATUS = 0" );
        println( "     BEGIN" );
        println( "       exec ('alter table '+@reftable+' drop constraint '+@constraintname)" );
        println( "       FETCH NEXT from refcursor into @reftable, @constraintname" );
        println( "     END" );
        println( "     CLOSE refcursor" );
        println( "     DEALLOCATE refcursor" );
        println( "     DROP TABLE " + tableName );
        print( "END" );
        printEndOfStatement();
    }

    protected void printComment( String text ) throws IOException
    {
        print( "# " );
        println( text );
    }

    protected void printAutoIncrementColumn( Table table, Column column ) throws IOException
    {
        print( "IDENTITY (1,1) " );
    }
}
