import java.util.*;
import org.codehaus.modello.association.package1.Person;
import org.codehaus.modello.association.package1.ListSetMapProperties;
import org.codehaus.modello.association.package2.Location;
import org.codehaus.modello.generator.*;
import org.codehaus.modello.verifier.*;

public class OneToManyAssociationVerifier
    extends Verifier
{
    public void verify()
        throws Exception
    {
        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        Person person = new Person();

        Location location = new Location();

        person.setLocation( location );

        assertEquals( "Location.persons[0]", person, location.getPersons().get( 0 ) );

        testList();

        testMap();

        testProperty();

        testSet();
    }

    private void testList()
        throws Exception
    {
        ListSetMapProperties foo = new ListSetMapProperties();

        Person person1 = new Person();

        Person person2 = new Person();

        foo.addList( person1 );

        List list = foo.getList();

        assertEquals( "list.size", 1, list.size() );

        assertEquals( "list[0]", person1, list.get( 0 ) );

        foo.removeList( person1 );

        assertEquals( "list.size", 0, list.size() );

        list = new ArrayList();

        list.add( person1 );

        list.add( person2 );

        foo.setList( list );

        assertEquals( "list.size", 2, list.size() );

        assertEquals( "list[0]", person1, list.get( 0 ) );

        assertEquals( "list[1]", person2, list.get( 1 ) );
    }

    private void testMap()
    {
        ListSetMapProperties foo = new ListSetMapProperties();

        Integer i1 = new Integer( 1 );

        Integer i2 = new Integer( 2 );

        Person person1 = new Person();

        Person person2 = new Person();

        foo.addMap( i1, person1 );

        assertEquals( "map.size", 1, foo.getMap().size() );

        foo.addMap( i1, person1 );

        assertEquals( "map.size", 1, foo.getMap().size() );

        foo.addMap( i2, person2 );

        assertEquals( "map.size", 2, foo.getMap().size() );
    }

    private void testProperty()
    {
        ListSetMapProperties foo = new ListSetMapProperties();

        String i1 = "1";

        String i2 = "2";

        Person person1 = new Person();

        Person person2 = new Person();

        foo.addProperty( i1, person1 );

        assertEquals( "properties.size", 1, foo.getProperties().size() );

        foo.addProperty( i1, person1 );

        assertEquals( "properties.size", 1, foo.getProperties().size() );

        foo.addProperty( i2, person2 );

        assertEquals( "properties.size", 2, foo.getProperties().size() );
    }

    private void testSet()
    {
        ListSetMapProperties foo = new ListSetMapProperties();

        String i1 = "1";

        String i2 = "2";

        Person person1 = new Person();

        Person person2 = new Person();

        foo.addSet( person1 );

        assertEquals( "set.size", 1, foo.getSet().size() );

        foo.addSet( person1 );

        assertEquals( "set.size", 1, foo.getSet().size() );

        foo.addSet( person2 );

        assertEquals( "set.size", 2, foo.getSet().size() );
    }
}
