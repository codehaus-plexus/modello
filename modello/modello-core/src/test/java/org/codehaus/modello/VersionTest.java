package org.codehaus.modello;

/*
 * LICENSE
 */

import junit.framework.TestCase;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class VersionTest
    extends TestCase
{
    // TODO: Add testing for multidigit version numbers
    // TODO: Add tests for invalid version strings
    public void testVersionParsing()
    {
        Version version = new Version( "1.2.3" );

        assertEquals( 1, version.getMajor() );

        assertEquals( 2, version.getMinor() );

        assertEquals( 3, version.getMicro() );
    }

    public void testVersionRange()
    {
        VersionRange range = new VersionRange( "2.0.0+" );

        assertFalse( new Version( "1.0.0" ).inside( range ) );

        assertTrue( new Version( "2.0.0" ).inside( range ) );

        assertTrue( new Version( "3.0.0" ).inside( range ) );
    }

    public void testGreaterThanWhenFooIsLessThanBar()
    {
        assertNotGreaterThan( "1.0.0", "2.9.9" );
        assertNotGreaterThan( "1.9.9", "2.0.0" );
        assertNotGreaterThan( "0.1.0", "0.2.9" );
        assertNotGreaterThan( "0.1.1", "0.2.0" );
        assertNotGreaterThan( "0.0.1", "0.0.1" );
    }

    public void testGreaterThanWhenFooIsEqualBar()
    {
        assertNotGreaterThan( "1.2.3", "1.2.3" );
    }

    public void testGreaterThanWhenFooIsGreaterThanBar()
    {
        assertGreaterThan( "2.0.0", "1.9.9" );
        assertGreaterThan( "2.9.9", "1.0.0" );
        assertGreaterThan( "0.2.9", "0.1.0" );
        assertGreaterThan( "0.2.0", "0.1.9" );
        assertGreaterThan( "0.0.2", "0.0.1" );
    }

    private void assertGreaterThan( String foo, String bar )
    {
        assertTrue( new Version( foo ).greaterThan( new Version( bar ) ) );
    }

    private void assertNotGreaterThan( String foo, String bar )
    {
        assertFalse( new Version( foo ).greaterThan( new Version( bar ) ) );
    }
}
