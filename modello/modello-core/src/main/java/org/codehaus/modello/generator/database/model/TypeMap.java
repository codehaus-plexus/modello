package org.codehaus.modello.generator.database.model;


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

import java.sql.Types;
import java.util.Hashtable;

// I don't know if the peer system deals

/**
 * A class that maps SQL type names to their JDBC type ID found in
 * {@link java.sql.Types}.
 * <p/>
 * <pre>
 * -------------------------------------------------------
 * SQL Type      | Java Type            | Peer Type
 * -------------------------------------------------------
 * CHAR          | String               | String
 * VARCHAR       | String               | String
 * LONGVARCHAR   | String               | String
 * NUMERIC       | java.math.BigDecimal | java.math.BigDecimal
 * DECIMAL       | java.math.BigDecimal | java.math.BigDecimal
 * BIT           | boolean OR Boolean   | Boolean
 * TINYINT       | byte OR Byte         | Byte
 * SMALLINT      | short OR Short       | Short
 * INTEGER       | int OR Integer       | Integer
 * BIGINT        | long OR Long         | Long
 * REAL          | float OR Float       | Float
 * FLOAT         | double OR Double     | Double
 * DOUBLE        | double OR Double     | Double
 * BINARY        | byte[]               | ?
 * VARBINARY     | byte[]               | ?
 * LONGVARBINARY | byte[]               | ?
 * DATE          | java.sql.Date        | java.util.Date
 * TIME          | java.sql.Time        | java.util.Date
 * TIMESTAMP     | java.sql.Timestamp   | java.util.Date
 * <p/>
 * -------------------------------------------------------
 * A couple variations have been introduced to cover cases
 * that may arise, but are not covered above
 * BOOLEANCHAR   | boolean OR Boolean   | String
 * BOOLEANINT    | boolean OR Boolean   | Integer
 * </pre>
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision$
 */
public class TypeMap
{
    public static final String CHAR = "CHAR";
    public static final String VARCHAR = "VARCHAR";
    public static final String LONGVARCHAR = "LONGVARCHAR";
    public static final String CLOB = "CLOB";
    public static final String NUMERIC = "NUMERIC";
    public static final String DECIMAL = "DECIMAL";
    public static final String BIT = "BIT";
    public static final String TINYINT = "TINYINT";
    public static final String SMALLINT = "SMALLINT";
    public static final String INTEGER = "INTEGER";
    public static final String BIGINT = "BIGINT";
    public static final String REAL = "REAL";
    public static final String FLOAT = "FLOAT";
    public static final String DOUBLE = "DOUBLE";
    public static final String BINARY = "BINARY";
    public static final String VARBINARY = "VARBINARY";
    public static final String LONGVARBINARY = "LONGVARBINARY";
    public static final String BLOB = "BLOB";
    public static final String DATE = "DATE";
    public static final String TIME = "TIME";
    public static final String TIMESTAMP = "TIMESTAMP";
    public static final String BOOLEANCHAR = "BOOLEANCHAR";
    public static final String BOOLEANINT = "BOOLEANINT";

    private static final String[] TEXT_TYPES =
        {
            CHAR, VARCHAR, LONGVARCHAR, CLOB, DATE, TIME, TIMESTAMP, BOOLEANCHAR
        };
    private static final String[] DECIMAL_TYPES =
        {
            NUMERIC, DECIMAL, REAL, FLOAT, DOUBLE
        };

    private static Hashtable sqlTypeNameToTypeID = new Hashtable();
    private static Hashtable typeIdToSqlTypeName = new Hashtable();


    static
    {

        /*
         * Map the Strings to JDBC type IDs
         */
        registerSqlTypeID( new Integer( Types.CHAR ), CHAR );
        registerSqlTypeID( new Integer( Types.VARCHAR ), VARCHAR );
        registerSqlTypeID( new Integer( Types.LONGVARCHAR ), LONGVARCHAR );
        registerSqlTypeID( new Integer( Types.CLOB ), CLOB );
        registerSqlTypeID( new Integer( Types.NUMERIC ), NUMERIC );
        registerSqlTypeID( new Integer( Types.DECIMAL ), DECIMAL );
        registerSqlTypeID( new Integer( Types.BIT ), BIT );
        registerSqlTypeID( new Integer( Types.TINYINT ), TINYINT );
        registerSqlTypeID( new Integer( Types.SMALLINT ), SMALLINT );
        registerSqlTypeID( new Integer( Types.INTEGER ), INTEGER );
        registerSqlTypeID( new Integer( Types.BIGINT ), BIGINT );
        registerSqlTypeID( new Integer( Types.REAL ), REAL );
        registerSqlTypeID( new Integer( Types.FLOAT ), FLOAT );
        registerSqlTypeID( new Integer( Types.DOUBLE ), DOUBLE );
        registerSqlTypeID( new Integer( Types.BINARY ), BINARY );
        registerSqlTypeID( new Integer( Types.VARBINARY ), VARBINARY );
        registerSqlTypeID( new Integer( Types.LONGVARBINARY ), LONGVARBINARY );
        registerSqlTypeID( new Integer( Types.BLOB ), BLOB );
        registerSqlTypeID( new Integer( Types.DATE ), DATE );
        registerSqlTypeID( new Integer( Types.TIME ), TIME );
        registerSqlTypeID( new Integer( Types.TIMESTAMP ), TIMESTAMP );
    }

    /**
     * Returns the JDBC type name which maps to {@link java.sql.Types}
     * for the given SQL name of type
     */
    public static int getJdbcTypeCode( String typeName )
    {
        Integer answer = (Integer) sqlTypeNameToTypeID.get( typeName.toUpperCase() );
        if ( answer != null )
        {
            return answer.intValue();
        }
        return Types.OTHER;
    }

    /**
     * Returns the name which maps to the given {@link java.sql.Types}
     * type code
     */
    public static String getJdbcTypeName( int typeCode )
    {
        String answer = (String) typeIdToSqlTypeName.get( new Integer( typeCode ) );
        if ( answer == null )
        {
            System.out.println( "Couldn't find JDBC Name for typeCode: " + typeCode );
            answer = "UNKNOWN";
        }
        return answer;
    }

    /**
     * Returns true if values for the type need to be quoted.
     *
     * @param type The type to check.
     */
    public static final boolean isTextType( int type )
    {
        return isTextType( getJdbcTypeName( type ) );
    }

    /**
     * Returns true if values for the type need to be quoted.
     *
     * @param type The type to check.
     */
    public static final boolean isTextType( String type )
    {
        for ( int i = 0; i < TEXT_TYPES.length; i++ )
        {
            if ( type.equalsIgnoreCase( TEXT_TYPES[i] ) )
            {
                return true;
            }
        }

        // If we get this far, there were no matches.
        return false;
    }

    /**
     * Returns true if values for the type need have size and scale measurements
     *
     * @param type The type to check.
     */
    public static final boolean isDecimalType( int type )
    {
        return isDecimalType( getJdbcTypeName( type ) );
    }

    /**
     * Returns true if values for the type need have size and scale measurements
     *
     * @param type The type to check.
     */
    public static final boolean isDecimalType( String type )
    {
        for ( int i = 0; i < DECIMAL_TYPES.length; i++ )
        {
            if ( type.equalsIgnoreCase( DECIMAL_TYPES[i] ) )
            {
                return true;
            }
        }

        // If we get this far, there were no matches.
        return false;
    }


    /**
     * Registers the fact that the given Integer SQL ID maps to the given SQL name
     */
    protected static void registerSqlTypeID( Integer sqlTypeID, String name )
    {
        sqlTypeNameToTypeID.put( name, sqlTypeID );
        typeIdToSqlTypeName.put( sqlTypeID, name );
    }
}

