package org.codehaus.modello.core.io;

/*
 * LICENSE
 */

import java.io.FileReader;
import java.util.List;

import org.codehaus.modello.Model;
import org.codehaus.modello.ModelAssociation;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelField;
import org.codehaus.modello.ModelloTest;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ModelReaderTest
    extends ModelloTest
{
    public void testBasic()
        throws Exception
    {
        ModelReader reader = new ModelReader();

        Model model = reader.loadModel( new FileReader( getTestPath( "src/test/resources/models/simple.mdo" ) ) );

        assertNotNull( model );

        assertEquals( "simple", model.getId() );

        assertEquals( "Simple Modello Test Model", model.getName() );

        assertEquals( "foo.bar", model.getPackageName() );

        assertEquals( "Boy", model.getRoot() );

        List classes = model.getClasses();

        assertNotNull( classes );

        assertEquals( 2, classes.size() );

        assertBoy( classes.get( 0 ) );

        assertBoy( model.getClass( "Boy" ) );

        assertGirl( model.getClass( "Girl" ) );
    }

    public void testAssociationDefaultValues()
        throws Exception
    {
//        ModelBuilder builder = getModelBuilder();

//        Model model = builder.getModel( getTestFile( "src/test/resources/models/association.mdo" ) );

        Model model = getModelloCore().loadModel( new FileReader( getTestPath( "src/test/resources/models/association.mdo" ) ) );

        ModelAssociation association = model.getClass( "Foo" ).getAssociation( "bars" );

        assertEquals( "bars", association.getName() );

        assertEquals( "Foo", association.getFromClass().getName() );

        assertEquals( "Bar", association.getToClass().getName() );

        assertEquals( "bars", association.getFromRole() );

        assertEquals( "foo", association.getToRole() );

        assertEquals( "1", association.getFromMultiplicity() );

        assertEquals( "*", association.getToMultiplicity() );
    }

    private void assertBoy( Object boyObject )
    {
        assertTrue( boyObject instanceof ModelClass );

        ModelClass boy = (ModelClass)boyObject;

        assertNotNull( boy );

        assertEquals( "Boy", boy.getName() );

        assertEquals( "1.0.0", boy.getVersion() );

        List fields = boy.getFields();

        assertEquals( 1, fields.size() );

        assertBoyName( fields.get( 0 ) );

        assertBoyName( boy.getField( "name" ) );
    }

    private void assertBoyName( Object nameObject )
    {
        assertTrue( nameObject instanceof ModelField );

        ModelField name = (ModelField) nameObject;

        assertEquals( "name", name.getName() );

        assertEquals( "1.0.0", name.getVersion() );

        assertEquals( "String", name.getType() );
    }

    private void assertGirl( Object girlObject )
    {
        assertTrue( girlObject instanceof ModelClass );

        ModelClass girl = (ModelClass)girlObject;

        assertNotNull( girl );

        assertEquals( "Girl", girl.getName() );

        assertEquals( "1.0.0", girl.getVersion() );

        List fields = girl.getFields();

        assertEquals( 1, fields.size() );

        assertGirlAge( fields.get( 0 ) );

        assertGirlAge( girl.getField( "age" ) );
    }

    private void assertGirlAge( Object ageObject )
    {
        assertTrue( ageObject instanceof ModelField );

        ModelField age = (ModelField) ageObject;

        assertEquals( "age", age.getName() );

        assertEquals( "1.0.0+", age.getVersion() );

        assertEquals( "int", age.getType() );
    }

    private void assertAssociation( Object associationObject )
    {
        assertTrue( associationObject instanceof ModelAssociation );

        ModelAssociation association = (ModelAssociation) associationObject;

        assertEquals( "girlfriends", association.getName() );

        assertEquals( "Boy", association.getFromClass().getName() );

        assertBoy( association.getFromClass() );

        assertEquals( "Girl", association.getToClass().getName() );

        assertGirl( association.getToClass() );

        assertEquals( "girlfriends", association.getFromRole() );

        assertEquals( "boyfriend", association.getToRole() );

        assertEquals( "1", association.getFromMultiplicity() );

        assertEquals( "*", association.getToMultiplicity() );
    }
}
