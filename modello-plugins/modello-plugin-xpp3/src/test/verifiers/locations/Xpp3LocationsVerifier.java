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

import junit.framework.Assert;
import org.codehaus.modello.test.locations.Item;
import org.codehaus.modello.test.locations.Location;
import org.codehaus.modello.test.locations.Model;
import org.codehaus.modello.test.locations.io.xpp3.LocationsTestXpp3ReaderEx;
import org.codehaus.modello.verifier.Verifier;

/**
 * @author Benjamin Bentmann
 */
public class Xpp3LocationsVerifier
    extends Verifier
{

    public void verify()
        throws Exception
    {
        LocationsTestXpp3ReaderEx reader = new LocationsTestXpp3ReaderEx();

        Model model = reader.read( getClass().getResourceAsStream( "/locations.xml" ), true );

        assertLocation( model.getLocation( "" ), -1, -1 );
        assertLocation( model.getLocation( "string" ), 4, 11 );

        assertLocation( model.getLocation( "flatListStrings" ), -1, -1 );
        assertLocation( model.getLocation( "flatListStrings" ).getLocation( new Integer( 0 ) ), 6, 19 );
        assertLocation( model.getLocation( "flatListStrings" ).getLocation( new Integer( 1 ) ), 7, 19 );
        assertLocation( model.getLocation( "flatListStrings" ).getLocation( new Integer( 2 ) ), 8, 19 );

        assertLocation( model.getLocation( "flatSetStrings" ), -1, -1 );
        assertLocation( model.getLocation( "flatSetStrings" ).getLocation( "a" ), 10, 18 );
        assertLocation( model.getLocation( "flatSetStrings" ).getLocation( "b" ), 11, 18 );
        assertLocation( model.getLocation( "flatSetStrings" ).getLocation( "c" ), 12, 18 );

        assertLocation( model.getLocation( "wrappedListStrings" ), -1, -1 );
        assertLocation( model.getLocation( "wrappedListStrings" ).getLocation( new Integer( 0 ) ), 15, 24 );
        assertLocation( model.getLocation( "wrappedListStrings" ).getLocation( new Integer( 1 ) ), 16, 24 );
        assertLocation( model.getLocation( "wrappedListStrings" ).getLocation( new Integer( 2 ) ), 17, 24 );

        assertLocation( model.getLocation( "wrappedSetStrings" ), -1, -1 );
        assertLocation( model.getLocation( "wrappedSetStrings" ).getLocation( "a" ), 21, 23 );
        assertLocation( model.getLocation( "wrappedSetStrings" ).getLocation( "b" ), 22, 23 );
        assertLocation( model.getLocation( "wrappedSetStrings" ).getLocation( "c" ), 23, 23 );

        assertLocation( model.getLocation( "inlinedProperties" ), -1, -1 );
        assertLocation( model.getLocation( "inlinedProperties" ).getLocation( "a" ), 27, 8 );
        assertLocation( model.getLocation( "inlinedProperties" ).getLocation( "b" ), 28, 8 );

        assertLocation( model.getLocation( "explodedProperties" ), -1, -1 );
        assertLocation( model.getLocation( "explodedProperties" ).getLocation( "a" ), 34, 14 );
        assertLocation( model.getLocation( "explodedProperties" ).getLocation( "b" ), 38, 14 );

        Item item = model.getItems().get( 0 );
        assertNotNull( item );

        assertLocation( item.getLocation( "" ), -1, -1 );
        assertLocation( item.getLocation( "string" ), 45, 15 );
    }

    private void assertLocation( Location location, int line, int column )
        throws Exception
    {
        assertNotNull( location );

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
