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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Types;
import java.util.HashMap;


/**
 * This class is a helper class for converting from string values to their
 * corresponding {@link java.sql.Types}
 * (Exists only at the moment to overcome some deficiencies in
 * {@link org.codehaus.modello.generator.database.model.TypeMap}
 *
 * @author <a href="mailto:tima@intalio.com">Tim Anderson</a>
 * @version 1.1 2003/02/05 08:08:37
 */
class TypeMap extends org.codehaus.modello.generator.database.model.TypeMap
{

    public static final String[] VARCHARS = {VARCHAR, LONGVARCHAR};

    public static final String[] EXACT_NUMERICS = {
        TINYINT, SMALLINT, INTEGER, BIGINT, NUMERIC, DECIMAL};

    public static final String[] APPROX_NUMERICS = {
        REAL, FLOAT, DOUBLE};

    /**
     * A map of type identifiers to their names
     */
    private static final HashMap TYPE_MAP;

    /**
     * A map of names to their corresponding type identifiers
     */
    private static final HashMap NAME_MAP;

    /**
     * Returns the type identifier for a type name
     *
     * @param name the type name
     * @return the type identifier corresponding <code>name</code> or
     *         <code>null</code> if there is no corresponding identifier
     *         i
     */
    public static Integer getType( String name )
    {
        return (Integer) NAME_MAP.get( name );
    }

    /**
     * Returns the type name for a type identifier
     *
     * @param type the type identifier
     * @return the type name corresponding <code>id</code> or
     *         <code>null</code> if there is no corresponding name
     *         i
     */
    public static String getName( int type )
    {
        return (String) TYPE_MAP.get( new Integer( type ) );
    }

    public static boolean isVarChar( String name )
    {
        return isType( name, VARCHARS );
    }

    public static boolean isExactNumeric( String name )
    {
        return isType( name, EXACT_NUMERICS );
    }

    public static boolean isApproxNumeric( String name )
    {
        return isType( name, APPROX_NUMERICS );
    }

    private static boolean isType( String name, String[] types )
    {
        boolean result = false;
        for ( int i = 0; i < types.length; ++i )
        {
            if ( types[i].equals( name ) )
            {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Initialise the maps
     */
    static
    {
        TYPE_MAP = new HashMap();
        NAME_MAP = new HashMap();
        try
        {
            Field[] fields = Types.class.getFields();
            for ( int i = 0; i < fields.length; ++i )
            {
                Field field = fields[i];
                if ( Modifier.isStatic( field.getModifiers() ) )
                {
                    Integer type = (Integer) field.get( null );
                    String name = field.getName().toUpperCase();
                    NAME_MAP.put( name, type );
                    TYPE_MAP.put( type, name );
                }
            }
        }
        catch ( IllegalAccessException exception )
        {
            throw new RuntimeException( exception.getMessage() );
        }
    }

}
