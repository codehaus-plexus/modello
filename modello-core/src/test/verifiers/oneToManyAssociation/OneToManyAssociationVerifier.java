import java.util.*;
import org.codehaus.modello.association.package1.Person;
import org.codehaus.modello.association.package2.Location;
import org.codehaus.modello.generator.*;
import org.codehaus.modello.verifier.*;

public class OneToManyAssociationVerifier
    extends Verifier
{
    public void verify()
    {
        Person person = new Person();

        Location location = new Location();

        try
        {
            person.setLocation( location );
        }
        catch(Exception e)
        {
            e.printStackTrace();
            fail( e.getMessage() );
        }

        assertEquals( "Location.persons", person, location.getPersons().get( 0 ) );
    }
}
