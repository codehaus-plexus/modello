package org.codehaus.modello.generator.xml.xpp3;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.codehaus.modello.test.locationssrc.Item;
import org.codehaus.modello.test.locationssrc.Location;
import org.codehaus.modello.test.locationssrc.Model;
import org.codehaus.modello.test.locationssrc.Source;
import org.codehaus.modello.test.locationssrc.io.xpp3.LocationsSourceTestXpp3ReaderEx;
import org.codehaus.modello.verifier.Verifier;

/**
 * @author Benjamin Bentmann
 */
public class Xpp3LocationsSourceVerifier
    extends Verifier
{

    public void verify()
        throws Exception
    {
        Source source = new Source();

        LocationsSourceTestXpp3ReaderEx reader = new LocationsSourceTestXpp3ReaderEx();

        Model model = reader.read( getClass().getResourceAsStream( "/locations.xml" ), true, source );

        assertLocation( model.getLocation( "" ), -1, -1, source );
        assertLocation( model.getLocation( "string" ), 4, 11, source );

        assertLocation( model.getLocation( "flatListStrings" ), -1, -1, source );
        assertLocation( model.getLocation( "flatListStrings" ).getLocation( new Integer( 0 ) ), 6, 19, source );
        assertLocation( model.getLocation( "flatListStrings" ).getLocation( new Integer( 1 ) ), 7, 19, source );
        assertLocation( model.getLocation( "flatListStrings" ).getLocation( new Integer( 2 ) ), 8, 19, source );

        assertLocation( model.getLocation( "flatSetStrings" ), -1, -1, source );
        assertLocation( model.getLocation( "flatSetStrings" ).getLocation( "a" ), 10, 18, source );
        assertLocation( model.getLocation( "flatSetStrings" ).getLocation( "b" ), 11, 18, source );
        assertLocation( model.getLocation( "flatSetStrings" ).getLocation( "c" ), 12, 18, source );

        assertLocation( model.getLocation( "wrappedListStrings" ), -1, -1, source );
        assertLocation( model.getLocation( "wrappedListStrings" ).getLocation( new Integer( 0 ) ), 15, 24, source );
        assertLocation( model.getLocation( "wrappedListStrings" ).getLocation( new Integer( 1 ) ), 16, 24, source );
        assertLocation( model.getLocation( "wrappedListStrings" ).getLocation( new Integer( 2 ) ), 17, 24, source );

        assertLocation( model.getLocation( "wrappedSetStrings" ), -1, -1, source );
        assertLocation( model.getLocation( "wrappedSetStrings" ).getLocation( "a" ), 21, 23, source );
        assertLocation( model.getLocation( "wrappedSetStrings" ).getLocation( "b" ), 22, 23, source );
        assertLocation( model.getLocation( "wrappedSetStrings" ).getLocation( "c" ), 23, 23, source );

        assertLocation( model.getLocation( "inlinedProperties" ), -1, -1, source );
        assertLocation( model.getLocation( "inlinedProperties" ).getLocation( "a" ), 27, 8, source );
        assertLocation( model.getLocation( "inlinedProperties" ).getLocation( "b" ), 28, 8, source );

        assertLocation( model.getLocation( "explodedProperties" ), -1, -1, source );
        assertLocation( model.getLocation( "explodedProperties" ).getLocation( "a" ), 34, 14, source );
        assertLocation( model.getLocation( "explodedProperties" ).getLocation( "b" ), 38, 14, source );

        Item item = model.getItems().get( 0 );
        assertNotNull( item );

        assertLocation( item.getLocation( "" ), -1, -1, source );
        assertLocation( item.getLocation( "string" ), 45, 15, source );
    }

    private void assertLocation( Location location, int line, int column, Source src )
        throws Exception
    {
        assertNotNull( location );

        assertSame( src, location.getSource() );

        if ( line >= 0 )
        {
            assertEquals( line, location.getLineNumber() );
        }

        if ( column >= 0 )
        {
            assertEquals( column, location.getColumnNumber() );
        }
    }

}
