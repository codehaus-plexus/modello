package org.codehaus.modello.plugin.java.javasource;

/*
 * Copyright (c) 2013, Codehaus.org
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

import org.codehaus.modello.model.ModelDefault;

/**
 * JType sub-class for maps.
 *
 * @author <a href="mailto:simonetripodi@apache.org">Simone Tripodi</a>
 * @since 1.8
 */
public final class JMapType
    extends JComponentizedType
{
    // --------------------------------------------------------------------------

    /** Name of the actual map instance to be used, e.g. java.util.ArrayList. */
    private String _instanceName;

    // --------------------------------------------------------------------------

    /**
     * Creates an instance of a map type, of type 'mapName'.
     *
     * @param typeName Name of the map type interface.
     * @param componentType Component type.
     * @param useJava50 True if Java 5.0 should be used.
     */
    public JMapType( final String typeName, final JType componentType, final boolean useJava50 )
    {
        super( typeName, componentType, useJava50 );
    }

    /**
     * Creates an instance of a map type, of type 'mapName'.
     *
     * @param typeName Name of the map type interface.
     * @param instanceName Name of the actual map type instance.
     * @param componentType Component type.
     * @param useJava50 True if Java 5.0 should be used.
     */
    public JMapType( final String typeName, final String instanceName, final JType componentType,
                            final boolean useJava50 )
    {
        super( typeName, componentType, useJava50 );
        _instanceName = instanceName;
    }

    // --------------------------------------------------------------------------

    /**
     * Returns the instance name of this map type.
     *
     * @return The instance name of this map type.
     */
    public String getInstanceName()
    {
        if ( ModelDefault.PROPERTIES.equals( getName() ) )
        {
            return _instanceName;
        }

        if ( _instanceName != null )
        {
            String instance;

            int separator = _instanceName.indexOf( "()" );
            if ( separator != -1 )
            {
                instance = _instanceName.substring( 0, separator );
            }
            else
            {
                instance = _instanceName;
            }

            if ( isUseJava50() )
            {
                return instance + "<Object, " + getComponentType().toString() + ">()";
            }

            return instance + "/*<Object, " + getComponentType().toString() + ">*/()";
        }

        return toString();
    }

    /**
     * @return the String representation of this JType.
     */
    public String toString()
    {
        if ( ModelDefault.PROPERTIES.equals( getName() ) )
        {
            return getName();
        }

        if ( isUseJava50() )
        {
            return getName() + "<Object, " + getComponentType().toString() + ">";
        }

        return getName() + "/*<Object, " + getComponentType().toString() + ">*/";
    }

    // --------------------------------------------------------------------------
}
