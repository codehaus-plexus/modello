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

import org.codehaus.modello.generator.database.model.Column;
import org.codehaus.modello.generator.database.model.Table;

/**
 * An SQL Builder for Oracle
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision$
 */
public class OracleBuilder extends SqlBuilder
{

    public OracleBuilder()
    {
        setPrimaryKeyEmbedded( false );
        setForeignKeyConstraintsNamed( true );
    }

    public void dropTable( Table table ) throws IOException
    {
        print( "drop table " );
        print( table.getName() );
        print( " CASCADE CONSTRAINTS" );
        printEndOfStatement();
    }

    // there's no real need to print comments like this, just preserving
    // backwards compatibility with the old Torque Velocity scripts
    protected void printComment( String text ) throws IOException
    {
        print( "--" );
        if ( !text.startsWith( "-" ) )
        {
            print( " " );
        }
        println( text );
    }

    public void createTable( Table table ) throws IOException
    {
        // lets create any sequences
        Column column = table.getAutoIncrementColumn();
        if ( column != null )
        {
            createSequence( table, column );
        }
        super.createTable( table );
        if ( column != null )
        {
            createSequenceTrigger( table, column );
        }
    }


    protected void printAutoIncrementColumn( Table table, Column column ) throws IOException
    {
        //print( "AUTO_INCREMENT" );
    }

    /**
     * Creates a sequence so that values can be auto incremented
     */
    protected void createSequence( Table table, Column column ) throws IOException
    {
        print( "create sequence " );
        print( table.getName() );
        print( "_seq" );
        printEndOfStatement();
    }

    /**
     * Creates a trigger to auto-increment values
     */
    protected void createSequenceTrigger( Table table, Column column ) throws IOException
    {
        print( "create or replace trigger " );
        print( table.getName() );
        print( "_trg before insert on " );
        println( table.getName() );
        println( "for each row" );
        println( "begin" );
        print( "select " );
        print( table.getName() );
        print( "_seq.nextval into :new." );
        print( column.getName() );
        println( " from dual;" );
        print( "end" );
        printEndOfStatement();
    }


    /**
     * @return the full SQL type string, including size where appropriate.
     *         Where necessary, translate for Oracle specific DDL requirements.
     */
    protected String getSqlType( Column column )
    {
        switch ( column.getTypeCode() )
        {
            case java.sql.Types.INTEGER:
                return "INTEGER";
            case java.sql.Types.DATE:
            case java.sql.Types.TIME:
            case java.sql.Types.TIMESTAMP:
                return "DATE";
            default:
                return column.getType();
        }
    }
}
