package org.codehaus.modello.core;

/*
 * LICENSE
 */

import java.io.FileReader;

import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.ModelloTest;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultModelloCoreTest
    extends ModelloTest
{
    public void testModelWithDuplicateClasses()
        throws Exception
    {
        ModelloCore modello = getModelloCore();

        try
        {
            modello.loadModel( new FileReader( getTestFile( "src/test/resources/models/duplicate-classes.mdo" ) ) );

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
        ModelloCore modello = getModelloCore();

        try
        {
            modello.loadModel( new FileReader( getTestFile( "src/test/resources/models/duplicate-fields.mdo" ) ) );

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
        ModelloCore modello = getModelloCore();

        try
        {
            modello.loadModel( new FileReader( getTestFile( "src/test/resources/models/duplicate-associations.mdo" ) ) );

            fail( "Expected ModelloRuntimeException." );
        }
        catch( ModelloRuntimeException ex )
        {
            assertEquals( "Duplicate association in MyClass: MyAssociation.", ex.getMessage() );
        }
    }
}
