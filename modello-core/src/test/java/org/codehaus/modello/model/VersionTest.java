package org.codehaus.modello.model;

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

import junit.framework.TestCase;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
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

        assertTrue( new Version( "2.0.0" ).equals( range.getFromVersion() ) );

        assertTrue( Version.INFINITE.equals( range.getToVersion() ) );

        range = new VersionRange( "1.0.0" );

        assertTrue( new Version( "1.0.0" ).equals( range.getFromVersion() ) );

        assertTrue( new Version( "1.0.0" ).equals( range.getToVersion() ) );

        range = new VersionRange( "1.0.0/3.0.0" );

        assertTrue( new Version( "1.0.0" ).equals( range.getFromVersion() ) );

        assertTrue( new Version( "3.0.0" ).equals( range.getToVersion() ) );
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
