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

import static org.junit.Assert.assertTrue;

import org.codehaus.modello.test.locations.Item;
import org.codehaus.modello.test.locations.Location;
import org.codehaus.modello.test.locations.LocationTracker;
import org.codehaus.modello.test.locations.Model;
import org.codehaus.modello.verifier.Verifier;

/**
 * @author Benjamin Bentmann
 */
public class JavaLocationsVerifier
    extends Verifier
{

    public void verify()
        throws Exception
    {
        assertTrue( LocationTracker.class.isAssignableFrom( Model.class ) );
        assertTrue( LocationTracker.class.isAssignableFrom( Item.class ) );
        assertTrue( LocationTracker.class.isAssignableFrom( Location.class ) );
    }

}
