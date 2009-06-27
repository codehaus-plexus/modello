package test;

import java.util.Date;

import org.codehaus.plexus.util.xml.Xpp3Dom;

import junit.framework.TestCase;

public class CloneTest
    extends TestCase
{

    public void testNullSafe()
        throws Exception
    {
        Thing orig = new Thing();

        Thing copy = (Thing) orig.clone();

        assertNotNull( copy );
        assertNotSame( orig, copy );
    }

    public void testClone()
        throws Exception
    {
        Thing orig = new Thing();
        orig.setSomeBoolean( true );
        orig.setSomeChar( 'X' );
        orig.setSomeByte( (byte) 7 );
        orig.setSomeShort( (short) 11 );
        orig.setSomeInt( 13 );
        orig.setSomeLong( 17 );
        orig.setSomeFloat( -2.5f );
        orig.setSomeDouble( 3.14 );
        orig.setSomeString( "test" );
        orig.setSomeDate( new Date() );
        orig.setSomeDom( new Xpp3Dom( "test" ) );
        orig.addSomeStringList( "string" );
        orig.addSomeStringSet( "string" );
        orig.setDeepThingy( new Thingy() );
        orig.addDeepThingyList( new Thingy() );
        orig.addDeepThingySet( new Thingy() );
        orig.setShallowThingy( new Thingy() );
        orig.addShallowThingyList( new Thingy() );
        orig.addShallowThingySet( new Thingy() );
        orig.addSomeProperty( "key", "value" );
        orig.customProperties.setProperty( "key", "value" );

        Thing copy = (Thing) orig.clone();

        assertNotNull( copy );
        assertNotSame( orig, copy );

        assertEquals( orig.isSomeBoolean(), copy.isSomeBoolean() );
        assertEquals( orig.getSomeChar(), copy.getSomeChar() );
        assertEquals( orig.getSomeByte(), copy.getSomeByte() );
        assertEquals( orig.getSomeShort(), copy.getSomeShort() );
        assertEquals( orig.getSomeInt(), copy.getSomeInt() );
        assertEquals( orig.getSomeLong(), copy.getSomeLong() );
        assertEquals( orig.getSomeFloat(), copy.getSomeFloat(), 0.1 );
        assertEquals( orig.getSomeDouble(), copy.getSomeDouble(), 0.1 );
        assertEquals( orig.getSomeString(), copy.getSomeString() );

        assertEquals( orig.getSomeDate(), copy.getSomeDate() );
        assertNotSame( orig.getSomeDate(), copy.getSomeDate() );
        assertEquals( orig.getSomeDom(), copy.getSomeDom() );
        assertNotSame( orig.getSomeDom(), copy.getSomeDom() );

        assertEquals( orig.getSomeStringList(), copy.getSomeStringList() );
        assertNotSame( orig.getSomeStringList(), copy.getSomeStringList() );
        assertEquals( orig.getSomeStringSet(), copy.getSomeStringSet() );
        assertNotSame( orig.getSomeStringSet(), copy.getSomeStringSet() );

        assertNotSame( orig.getDeepThingy(), copy.getDeepThingy() );
        assertNotSame( orig.getDeepThingyList(), copy.getDeepThingyList() );
        assertNotSame( orig.getDeepThingyList().iterator().next(), copy.getDeepThingyList().iterator().next() );
        assertNotSame( orig.getDeepThingySet(), copy.getDeepThingySet() );
        assertNotSame( orig.getDeepThingySet().iterator().next(), copy.getDeepThingySet().iterator().next() );

        assertSame( orig.getShallowThingy(), copy.getShallowThingy() );
        assertNotSame( orig.getShallowThingyList(), copy.getShallowThingyList() );
        assertSame( orig.getShallowThingyList().iterator().next(), copy.getShallowThingyList().iterator().next() );
        assertNotSame( orig.getShallowThingySet(), copy.getShallowThingySet() );
        assertSame( orig.getShallowThingySet().iterator().next(), copy.getShallowThingySet().iterator().next() );

        assertEquals( orig.customProperties, copy.customProperties );
        assertNotSame( orig.customProperties, copy.customProperties );
    }

}
