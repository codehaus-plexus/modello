package org.codehaus.modello.generator;

public abstract class AbstractVerifier
{
    protected void assertEquals( String text, int expected, int actual )
    {
        assertEquals( text, new Integer( expected ), new Integer( actual ) );
    }

    protected void assertEquals( String text, Object expected, Object actual)
    {
        if ( expected == null )
        {
            if ( actual == null )
            {
                return;
            }

            throw new RuntimeException( "Assertion error: " + text + " expected '" + format( expected ) + "', actual: '" + format( actual ) + "'." );
        }

        if ( expected.equals( actual ) )
        {
            return;
        }

        throw new RuntimeException( "Assertion error: " + text + " expected '" + format( expected ) + "', actual: '" + format( actual ) + "'." );
    }

    protected void assertNotNull( String text, Object object )
    {
        if ( object != null )
        {
            return;
        }

        throw new RuntimeException( "Not null assertion failed. " + text );
    }

    protected void assertInstanceOf( String text, Class expected, Class actual )
    {
        if ( expected.getName().equals( actual.getName() ) )
        {
            return;
        }

        throw new RuntimeException( "instanceof assertion failed. " + text + " expected '" + format( expected ) + "', actual: '" + format( actual ) + "'." );
    }

    private String format( Object object )
    {
        if ( object == null )
        {
            return "<null>";
        }

        return object.toString();
    }
}
