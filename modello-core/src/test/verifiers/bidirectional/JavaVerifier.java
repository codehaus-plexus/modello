import org.codehaus.modello.plugin.java.Role;
import org.codehaus.modello.tests.bidiroverride.BiRole;
import org.codehaus.modello.verifier.Verifier;

import junit.framework.Assert;

import java.util.List;

public class JavaVerifier
    extends Verifier
{
    public void verify()
    {
        Role parent = new BiRole();
        parent.setName( "parent" );
        
        Role child = new BiRole();
        child.setName( "child" );
        
        parent.addRole( child );
        Assert.assertEquals( 1, parent.getRoles().size() );
        
        List roles = parent.getRoles();
        Assert.assertTrue( "Collection element should be of type BiRole.", (roles.get(0) instanceof BiRole) );
        
        parent.removeRole( child );
        Assert.assertEquals( 0, parent.getRoles().size() );
        
        BiRole birole = (BiRole) parent;
        
        birole.setPrincipal( 22 );
        Assert.assertEquals( 22, birole.getPrincipal() );
    }
}
