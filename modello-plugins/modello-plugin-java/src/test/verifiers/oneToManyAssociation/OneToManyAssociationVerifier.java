import org.codehaus.modello.association.package1.ListSetMapProperties;
import org.codehaus.modello.association.package1.Person;
import org.codehaus.modello.association.package2.Location;
import org.codehaus.modello.verifier.Verifier;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;

public class OneToManyAssociationVerifier
    extends Verifier
{
    public void verify()
    {
        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        Person person = new Person();

        Location location = new Location();

        location.getPersons().add( person );

        Assertions.assertNotNull( location.getPersons() , "Location.persons == null");

        Assertions.assertEquals( 1, location.getPersons().size() , "Location.persons.length != 1");

        Assertions.assertEquals( person, location.getPersons().get( 0 ) , "Location.persons[0]");

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        person = new Person();

        location = new Location();

        person.setLocation( location );

        Assertions.assertNotNull( location.getPersons() , "Location.persons == null");

        Assertions.assertEquals( 1, location.getPersons().size() , "Location.persons.length != 1");

        Assertions.assertEquals( person, location.getPersons().get( 0 ) , "Location.persons[0]");

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        testList();

        testMap();

        testProperty();

        testSet();
    }

    private void testList()
    {
        ListSetMapProperties foo = new ListSetMapProperties();

        Person person1 = new Person();

        Person person2 = new Person();

        foo.addList( person1 );

        List list = foo.getList();

        Assertions.assertEquals( 1, list.size() , "list.size");

        Assertions.assertEquals( person1, list.get( 0 ) , "list[0]");

        foo.removeList( person1 );

        Assertions.assertEquals( 0, list.size() , "list.size");

        list = new ArrayList();

        list.add( person1 );

        list.add( person2 );

        foo.setList( list );

        Assertions.assertEquals( 2, list.size() , "list.size");

        Assertions.assertEquals( person1, list.get( 0 ) , "list[0]");

        Assertions.assertEquals( person2, list.get( 1 ) , "list[1]");
    }

    private void testMap()
    {
        ListSetMapProperties foo = new ListSetMapProperties();

        Integer i1 = new Integer( 1 );

        Integer i2 = new Integer( 2 );

        Person person1 = new Person();

        Person person2 = new Person();

        foo.addMap( i1, person1 );

        Assertions.assertEquals( 1, foo.getMap().size() , "map.size");

        foo.addMap( i1, person1 );

        Assertions.assertEquals( 1, foo.getMap().size() , "map.size");

        foo.addMap( i2, person2 );

        Assertions.assertEquals( 2, foo.getMap().size() , "map.size");
    }

    private void testProperty()
    {
        ListSetMapProperties foo = new ListSetMapProperties();

        String i1 = "1";

        String i2 = "2";

        Person person1 = new Person();

        Person person2 = new Person();

        foo.addProperty( i1, person1 );

        Assertions.assertEquals( 1, foo.getProperties().size() , "properties.size");

        foo.addProperty( i1, person1 );

        Assertions.assertEquals( 1, foo.getProperties().size() , "properties.size");

        foo.addProperty( i2, person2 );

        Assertions.assertEquals( 2, foo.getProperties().size() , "properties.size");
    }

    private void testSet()
    {
        ListSetMapProperties foo = new ListSetMapProperties();

        Person person1 = new Person();

        Person person2 = new Person();

        foo.addSet( person1 );

        Assertions.assertEquals( 1, foo.getSet().size() , "set.size");

        foo.addSet( person1 );

        Assertions.assertEquals( 1, foo.getSet().size() , "set.size");

        foo.addSet( person2 );

        Assertions.assertEquals( 2, foo.getSet().size() , "set.size");
    }
}
