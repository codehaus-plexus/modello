package org.codehaus.modello.model;

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

/**
 * Default values for a model, that can be overrided with <code>defaults</code> element of the model descriptor.
 *
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 */
public class ModelDefault
{
    public static final String CHECK_DEPRECATION = "checkDeprecation";

    public static final String CHECK_DEPRECATION_VALUE = "false";

    public static final String PACKAGE = "package";

    public static final String PACKAGE_VALUE = "model";

    public static final String LIST = "java.util.List";

    public static final String LIST_VALUE = "new java.util.ArrayList<?>()";

    public static final String MAP = "java.util.Map";

    public static final String MAP_VALUE = "new java.util.HashMap()";

    public static final String PROPERTIES = "java.util.Properties";

    public static final String PROPERTIES_VALUE = "new java.util.Properties()";

    public static final String SET = "java.util.Set";

    public static final String SET_VALUE = "new java.util.HashSet<?>()";

    public static final String STRICT_XML_ATTRIBUTES = "strictXmlAttributes";

    public static final String STRICT_XML_ATTRIBUTES_VALUE = "true";

    private String key;

    private String value;

    public static ModelDefault getDefault( String key )
        throws ModelValidationException
    {
        validateKey( key );

        ModelDefault modelDefault = new ModelDefault();

        modelDefault.setKey( key );

        if ( CHECK_DEPRECATION.equalsIgnoreCase( key ) )
        {
            modelDefault.setValue( CHECK_DEPRECATION_VALUE );
        }
        else if ( PACKAGE.equalsIgnoreCase( key ) )
        {
            modelDefault.setValue( PACKAGE_VALUE );
        }
        else if ( LIST.equalsIgnoreCase( key ) )
        {
            modelDefault.setValue( LIST_VALUE );
        }
        else if ( MAP.equalsIgnoreCase( key ) )
        {
            modelDefault.setValue( MAP_VALUE );
        }
        else if ( PROPERTIES.equalsIgnoreCase( key ) )
        {
            modelDefault.setValue( PROPERTIES_VALUE );
        }
        else if ( SET.equalsIgnoreCase( key ) )
        {
            modelDefault.setValue( SET_VALUE );
        }
        else if ( STRICT_XML_ATTRIBUTES.equalsIgnoreCase( key ) )
        {
            modelDefault.setValue( STRICT_XML_ATTRIBUTES_VALUE );
        }

        return modelDefault;
    }

    public void setKey( String key )
    {
        this.key = key;
    }

    public String getKey()
    {
        return key;
    }

    public void setValue( String value )
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }

    public boolean getBoolean()
    {
        return Boolean.valueOf( value ).booleanValue();
    }

    public void validateElement()
        throws ModelValidationException
    {
        if ( isEmpty( key ) )
        {
            throw new ModelValidationException( "You must define the key of default element." );
        }

        if ( isEmpty( value ) )
        {
            throw new ModelValidationException( "You must define the value of default element." );
        }

        validateKey( key );
    }

    private static void validateKey( String key )
        throws ModelValidationException
    {
        if ( ! SET.equalsIgnoreCase( key )
             && ! LIST.equalsIgnoreCase( key )
             && ! MAP.equalsIgnoreCase( key )
             && ! PROPERTIES.equalsIgnoreCase( key )
             && ! CHECK_DEPRECATION.equalsIgnoreCase( key )
             && ! PACKAGE.equalsIgnoreCase( key )
             && ! STRICT_XML_ATTRIBUTES.equalsIgnoreCase( key ) )
        {
            throw new ModelValidationException( "The key of default element must be ' " + SET + "', '" + LIST + "', '"
                + MAP + "', '" + PROPERTIES + "', '" + CHECK_DEPRECATION + "', '" + PACKAGE + "' or '"
                + STRICT_XML_ATTRIBUTES + "', was '" + key + "'." );
        }
    }

    protected boolean isEmpty( String string )
    {
        return string == null || string.trim().length() == 0;
    }
}
