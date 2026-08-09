import org.junit.jupiter.api.Assertions;

import org.codehaus.modello.ifaceassociation.package1.IPerson;
import org.codehaus.modello.ifaceassociation.package1.Person;
import org.codehaus.modello.ifaceassociation.package1.Location;
import org.codehaus.modello.verifier.Verifier;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;

public class InterfaceAssociationVerifier
    extends Verifier
{
    public void verify()
        throws Exception
    {
        Location location = new Location();

        IPerson person = new Person();

        // check List<IPerson> persons attribute getters/setters
        location.addPerson( person );

        List<IPerson> persons = location.getPersons();

        location.setPersons( new ArrayList<IPerson>( persons ) );

        location.removePerson( person );

        // check Set<IPerson> relatives attribute getters/setters
        location.addRelative( person );

        Set<IPerson> relatives = location.getRelatives();

        location.setRelatives( new HashSet<IPerson>( relatives ) );

        location.removeRelative( person );

        IPerson mother = new Person();

        location.setMother( mother );

        Assertions.assertNotNull( location.getMother() );

        location.setMother( null );

        Assertions.assertNull( location.getMother() );
    }
}
