package org.codehaus.modello.verifier;

/*
 * LICENSE
 */

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class WrongObjectTypeVerifierException
    extends RuntimeException
{
    private String className;

    private String fieldName;

    public WrongObjectTypeVerifierException( String className, String fieldName, Object expectedClass, Object actualClass )
    {
        super( "Field not equal. Field: " + className + "." + fieldName + ". Expected: " + encode( expectedClass ) + ". Actual: " + encode( actualClass ) );

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
