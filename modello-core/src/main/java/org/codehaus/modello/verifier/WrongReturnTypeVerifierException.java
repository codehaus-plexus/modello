package org.codehaus.modello.verifier;

/*
 * LICENSE
 */

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class WrongReturnTypeVerifierException
    extends RuntimeException
{
    private String className;

    private String fieldName;

    public WrongReturnTypeVerifierException( String className, String fieldName, Object expectedValue, Object actualValue )
    {
        super( "Field not equal. Field: " + className + "." + fieldName + ". Expected: " + encode( expectedValue ) + ". Actual: " + encode( actualValue ) );

        this.className = className;

        this.fieldName = fieldName;
    }

    /**
     * @return Returns the className.
     */
    public String getClassName()
    {
        return className;
    }

    /**
     * @return Returns the fieldName.
     */
    public String getFieldName()
    {
        return fieldName;
    }

    private static String encode( Object value )
    {
        if ( value == null )
        {
            return "<null>";
        }

        return "'" + value + "'";
    }
}
