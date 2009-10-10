import junit.framework.Assert;

import org.codehaus.modello.ifaceassociation.package1.IPerson;
import org.codehaus.modello.ifaceassociation.package1.Person;
import org.codehaus.modello.ifaceassociation.package1.Location;
import org.codehaus.modello.verifier.Verifier;

import java.util.List;
import java.util.ArrayList;

public class InterfaceAssociationVerifier
    extends Verifier
{
    public void verify()
    {
        Location location = new Location();

        IPerson person = new Person();

        location.addPerson( person );

        List<IPerson> persons = location.getPersons();

        location.setPersons( new ArrayList<IPerson>( persons ) );
        
        location.removePerson( person );
    }
}
