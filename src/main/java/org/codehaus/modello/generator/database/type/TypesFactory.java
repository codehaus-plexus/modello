package org.codehaus.modello.generator.database.type;

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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;


/**
 * Factory for constructing {@link Types} from database meta data
 *
 * @author <a href="mailto:tima@intalio.com">Tim Anderson</a>
 * @version 1.1 2003/02/05 08:08:37
 */
public class TypesFactory
{
    /**
     * Construct a new <code>Types</code>, using meta-data obtained from
     * a database connection
     *
     * @param connection the database connection to obtain meta-data from
     * @throws SQLException if meta-data cannot be accessed
     */
    public static Types create( Connection connection ) throws SQLException
    {
        Types types = new Types();
        HashSet parameterSet = new HashSet();

        DatabaseMetaData metaData = connection.getMetaData();

        // determine the types supported by the database
        populateTypes( metaData, types, parameterSet );

        // determine the mappings from standard JDBC types
        populateMappings( metaData, types, parameterSet );

        return types;
    }

    private static void populateTypes( DatabaseMetaData metaData,
                                       Types types,
                                       HashSet parameterSet )
        throws SQLException
    {

        ResultSet set = null;

        try
        {
            set = metaData.getTypeInfo();
            while ( set.next() )
            {
                String sqlName = set.getString( "TYPE_NAME" );
//                int typeCode = set.getInt( "DATA_TYPE" );
                long precision = set.getLong( "PRECISION" );
                short minScale = set.getShort( "MINIMUM_SCALE" );
                short maxScale = set.getShort( "MAXIMUM_SCALE" );
                String createParams = set.getString( "CREATE_PARAMS" );

                Type type = types.getType( sqlName );
                if ( type == null || precision > type.getSize() )
                {
                    type = new Type( sqlName, precision, minScale, maxScale );
                    types.addType( type );
                }

                // determine if the type can take parameters.
                if ( createParams != null && createParams.length() != 0 )
                {
                    parameterSet.add( sqlName );
                }
            }
        }
        finally
        {
            if ( set != null )
            {
                set.close();
            }
        }
    }

    private static void populateMappings( DatabaseMetaData metaData,
                                          Types types, HashSet parameterSet )
        throws SQLException
    {

        ResultSet set = null;

        try
        {
            set = metaData.getTypeInfo();
            while ( set.next() )
            {
                String sqlName = set.getString( "TYPE_NAME" );
                int typeCode = set.getInt( "DATA_TYPE" );
                long precision = set.getLong( "PRECISION" );
//                short scale = set.getShort( "MAXIMUM_SCALE" );
                boolean autoIncrement = set.getBoolean( "AUTO_INCREMENT" );

                String name = TypeMap.getName( typeCode );
                if ( name != null )
                {
                    String format = null;
                    if ( parameterSet.contains( sqlName ) )
                    {
                        // type takes parameters, so determine its format
                        format = getFormat( typeCode, precision );
                    }

                    Mapping mapping = new Mapping( name, sqlName, format );
                    types.addMapping( mapping );

                    if ( autoIncrement )
                    {
                        types.addAutoIncrementMapping( mapping );
                    }
                }
            }
        }
        finally
        {
            if ( set != null )
            {
                set.close();
            }
        }
    }

    private static String getFormat( int typeCode, long size )
    {
        String format = null;

        switch ( typeCode )
        {
            case java.sql.Types.CHAR:
            case java.sql.Types.VARCHAR:
            case java.sql.Types.LONGVARCHAR:
            case java.sql.Types.BLOB:
            case java.sql.Types.CLOB:
            case java.sql.Types.VARBINARY:
            case java.sql.Types.LONGVARBINARY:
                format = Mapping.SIZE_FORMAT;
                break;
            case java.sql.Types.BIT:
            case java.sql.Types.TINYINT:
            case java.sql.Types.SMALLINT:
            case java.sql.Types.INTEGER:
            case java.sql.Types.BIGINT:
                format = "(" + size + ")";
                break;
            case java.sql.Types.REAL:
            case java.sql.Types.FLOAT:
            case java.sql.Types.DOUBLE:
            case java.sql.Types.NUMERIC:
            case java.sql.Types.DECIMAL:
                format = Mapping.SIZE_SCALE_FORMAT;
                break;
            case java.sql.Types.DATE:
            case java.sql.Types.TIME:
            case java.sql.Types.TIMESTAMP:
                format = Mapping.SIZE_FORMAT;
                break;
        }

        return format;
    }

}
