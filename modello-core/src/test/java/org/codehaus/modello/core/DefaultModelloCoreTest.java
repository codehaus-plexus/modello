package org.codehaus.modello.core;

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

import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.plexus.PlexusTestCase;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class DefaultModelloCoreTest
    extends PlexusTestCase
{
    public void testModelWithDuplicateClasses()
        throws Exception
    {
        ModelloCore modello = (ModelloCore) lookup( ModelloCore.ROLE );

        try
        {
            modello.loadModel( getTestFile( "src/test/resources/models/duplicate-classes.mdo" ) );

            fail( "Expected ModelloRuntimeException." );
        }
        catch( ModelloRuntimeException ex )
        {
            assertEquals( "Duplicate class: MyClass.", ex.getMessage() );
        }
    }

    public void testModelWithDuplicateFields()
        throws Exception
    {
        ModelloCore modello = (ModelloCore) lookup( ModelloCore.ROLE );

        try
        {
            modello.loadModel( getTestFile( "src/test/resources/models/duplicate-fields.mdo" ) );

            fail( "Expected ModelloRuntimeException." );
        }
        catch( ModelloRuntimeException ex )
        {
            assertEquals( "Duplicate field in MyClass: MyField.", ex.getMessage() );
        }
    }

    public void testModelWithDuplicateAssociations()
        throws Exception
    {
        ModelloCore modello = (ModelloCore) lookup( ModelloCore.ROLE );

        try
        {
            modello.loadModel( getTestFile( "src/test/resources/models/duplicate-associations.mdo" ) );

            fail( "Expected ModelloRuntimeException." );
        }
        catch( ModelloRuntimeException ex )
        {
            assertEquals( "Duplicate field in MyClass: MyAssociation.", ex.getMessage() );
        }
    }
    
    public void testRecursion() throws Exception
    {
        ModelloCore modello = (ModelloCore) lookup( ModelloCore.ROLE );
        
        modello.loadModel( getTestFile( "src/test/resources/models/recursion.mdo" ) ); 
    }
}
