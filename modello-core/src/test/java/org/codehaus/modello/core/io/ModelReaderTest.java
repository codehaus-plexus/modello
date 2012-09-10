package org.codehaus.modello.core.io;

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

import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.Version;
import org.codehaus.modello.model.VersionRange;
import org.codehaus.plexus.PlexusTestCase;

import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 */
public class ModelReaderTest
    extends PlexusTestCase
{
    public void testBasic()
        throws Exception
    {
        ModelloCore modello = (ModelloCore) lookup( ModelloCore.ROLE );

        Model model = modello.loadModel( getTestFile( "src/test/resources/models/simple.mdo" ) );

        assertNotNull( model );

        assertEquals( "field", model.getVersionDefinition().getType() );
        assertEquals( "foo", model.getVersionDefinition().getValue() );

        assertEquals( "simple", model.getId() );

        assertEquals( "Simple Modello Test Model", model.getName() );

        assertEquals( "foo.bar", model.getDefaultPackageName( false, null ) );

        assertEquals( "Boy", model.getRoot( new Version( "1.0.0" ) ) );

        List<ModelClass> classes = model.getAllClasses();

        assertNotNull( classes );

        assertEquals( 2, classes.size() );

        assertEquals( 2, model.getClasses( new Version( "1.0.0" ) ).size() );

        assertBoy( classes.get( 0 ) );

        assertBoy( model.getClass( "Boy", new VersionRange( "1.0.0" ) ) );

        assertGirl( model.getClass( "Girl", new VersionRange( "1.0.0" ) ) );
    }

    public void testAssociationDefaultValues()
        throws Exception
    {
        ModelloCore modello = (ModelloCore) lookup( ModelloCore.ROLE );

        Model model = modello.loadModel( getTestFile( "src/test/resources/models/association.mdo" ) );

        ModelField field = model.getClass( "Foo", new VersionRange( "1.0.0" ) ).getField( "bars", new VersionRange( "1.0.0" ) );

        assertTrue( field instanceof ModelAssociation );

        ModelAssociation association = (ModelAssociation) field;

        assertEquals( "bars", association.getName() );

        assertEquals( "Foo", association.getModelClass().getName() );

        assertEquals( "Bar", association.getTo() );

        assertEquals( "Bar", association.getToClass().getName() );

//        assertEquals( "bars", association.getFromRole() );

//        assertEquals( "foo", association.getToRole() );

//        assertEquals( "1", association.getFromMultiplicity() );

//        assertEquals( "*", association.getToMultiplicity() );
    }

    private void assertBoy( Object boyObject )
    {
        assertTrue( boyObject instanceof ModelClass );

        ModelClass boy = (ModelClass)boyObject;

        assertNotNull( boy );

        assertEquals( "Boy", boy.getName() );

        assertEquals( "1.0.0", boy.getVersionRange().toString() );

        List<ModelField> fields = boy.getFields( new Version( "1.0.0" ) );

        assertEquals( 2, fields.size() );

        assertBoyName( fields.get( 0 ) );

        assertBoyName( boy.getField( "name", new VersionRange( "1.0.0" ) ) );
    }

    private void assertBoyName( Object nameObject )
    {
        assertTrue( nameObject instanceof ModelField );

        ModelField name = (ModelField) nameObject;

        assertEquals( "name", name.getName() );

        assertEquals( "1.0.0", name.getVersionRange().toString() );

        assertEquals( "String", name.getType() );

        assertEquals( "moniker", name.getAlias() );
    }

    private void assertGirl( Object girlObject )
    {
        assertTrue( girlObject instanceof ModelClass );

        ModelClass girl = (ModelClass)girlObject;

        assertNotNull( girl );

        assertEquals( "Girl", girl.getName() );

        assertEquals( "1.0.0", girl.getVersionRange().toString() );

        List<ModelField> fields = girl.getFields( new Version( "1.0.0" ) );

        assertEquals( 1, fields.size() );

        assertGirlAge( fields.get( 0 ) );

        assertGirlAge( girl.getField( "age", new VersionRange( "1.0.0" ) ) );
    }

    private void assertGirlAge( Object ageObject )
    {
        assertTrue( ageObject instanceof ModelField );

        ModelField age = (ModelField) ageObject;

        assertEquals( "age", age.getName() );

        assertEquals( "1.0.0+", age.getVersionRange().toString() );

        assertEquals( "int", age.getType() );
    }
}
