package org.codehaus.modello.generator.java;

import org.codehaus.modello.plugin.java.Role;
import org.codehaus.modello.tests.bidiroverride.BiRole;
import org.codehaus.modello.verifier.Verifier;

import org.junit.jupiter.api.Assertions;

import java.util.List;

public class BidirectionalJavaVerifier
    extends Verifier
{
    public void verify()
    {
        Role parent = new BiRole();
        parent.setName( "parent" );
        
        Role child = new BiRole();
        child.setName( "child" );
        
        parent.addRole( child );
        Assertions.assertEquals( 1, parent.getRoles().size() );
        
        List roles = parent.getRoles();
        Assertions.assertTrue( (roles.get(0) instanceof BiRole) , "Collection element should be of type BiRole.");
        
        parent.removeRole( child );
        Assertions.assertEquals( 0, parent.getRoles().size() );
        
        BiRole birole = (BiRole) parent;
        
        birole.setPrincipal( 22 );
        Assertions.assertEquals( 22, birole.getPrincipal() );
    }
}
