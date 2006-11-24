package org.codehaus.modello.plugin.jpox;

/*
 * Copyright (c) 2006, Codehaus.org
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

import java.util.Date;
import java.util.Properties;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import junit.framework.Assert;
import org.apache.log4j.*;
import org.codehaus.modello.verifier.Verifier;
import org.codehaus.plexus.security.authorization.rbac.jdo.*;

/**
 * @version $Id: Xpp3Verifier.java 675 2006-11-16 10:58:59Z brett $
 */
public class JpoxVerifierDeleteModel
    extends Verifier
{
    public void verify()
        throws Exception
    {
        Properties properties = new Properties();
        properties.setProperty( "javax.jdo.PersistenceManagerFactoryClass", "org.jpox.PersistenceManagerFactoryImpl" );
        properties.setProperty( "javax.jdo.option.ConnectionDriverName", "org.apache.derby.jdbc.EmbeddedDriver" );
        properties.setProperty( "javax.jdo.option.ConnectionURL", "jdbc:derby:target/jpox-delete-model/database;create=true" );
        properties.setProperty( "javax.jdo.option.ConnectionUserName", "sa" );
        properties.setProperty( "javax.jdo.option.ConnectionPassword", "" );
        properties.setProperty( "org.jpox.autoCreateSchema", "true" );
        properties.setProperty( "org.jpox.validateTables", "false" );
        properties.setProperty( "org.jpox.validateColumns", "false" );
        properties.setProperty( "org.jpox.validateConstraints", "false" );

        PropertyConfigurator.configure( getClass().getResource( "/log4j.properties" ) );
        // Logger.getLogger( "JPOX" ).setLevel( Level.DEBUG );

        PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory( properties );

        RbacJdoModelJPoxStore store = new RbacJdoModelJPoxStore( pmf );

        createDatabase( store );

        // Check the permissions, etc. are retrieved
        JdoRole role = store.getJdoRole( "name", true );
        Assert.assertNotNull( role );
        Assert.assertEquals( 3, role.getPermissions().size() );
        Assert.assertEquals( "pDesc1", ( (JdoPermission) role.getPermissions().get( 0 ) ).getDescription() );
        Assert.assertEquals( "pDesc2", ( (JdoPermission) role.getPermissions().get( 1 ) ).getDescription() );
        Assert.assertEquals( "pDesc3", ( (JdoPermission) role.getPermissions().get( 2 ) ).getDescription() );

        // These aren't detached. Something to monitor when using the class - careful use of fetch groups is required
        // Assert.assertEquals( "oDesc1", ( (JdoPermission) role.getPermissions().get( 0 ) ).getOperation().getDescription() );
        // Assert.assertEquals( "rId1", ( (JdoPermission) role.getPermissions().get( 0 ) ).getResource().getIdentifier() );

        // Check the totals
        Assert.assertEquals( 3, store.getJdoRoleCollection( true, null, null ).size() );
        Assert.assertEquals( 3, store.getJdoPermissionCollection( true, null, null ).size() );
        Assert.assertEquals( 2, store.getJdoOperationCollection( true, null, null ).size() );
        Assert.assertEquals( 2, store.getJdoResourceCollection( true, null, null ).size() );
        Assert.assertEquals( 3, store.getJdoUserAssignmentCollection( true, null, null ).size() );

        RbacJdoModelModelloMetadata metadata = store.getRbacJdoModelModelloMetadata( true );
        Assert.assertEquals( "1.0.0", metadata.getModelVersion() );

        store.eraseModelFromDatabase();

        // Check it is all gone
        Assert.assertEquals( 0, store.getJdoRoleCollection( true, null, null ).size() );
        Assert.assertEquals( 0, store.getJdoPermissionCollection( true, null, null ).size() );
        Assert.assertEquals( 0, store.getJdoOperationCollection( true, null, null ).size() );
        Assert.assertEquals( 0, store.getJdoResourceCollection( true, null, null ).size() );
        Assert.assertEquals( 0, store.getJdoUserAssignmentCollection( true, null, null ).size() );

        metadata = store.getRbacJdoModelModelloMetadata( true );
        Assert.assertNull( metadata );
    }

    private void createDatabase( RbacJdoModelJPoxStore store )
    {
        JdoOperation operation1 = new JdoOperation();
        operation1.setName( "oName1" );
        operation1.setDescription( "oDesc1" );
        operation1 = store.getJdoOperationByJdoId( store.addJdoOperation( operation1 ), true );

        JdoOperation operation2 = new JdoOperation();
        operation2.setName( "oName2" );
        operation2.setDescription( "oDesc2" );
        operation2 = store.getJdoOperationByJdoId( store.addJdoOperation( operation2 ), true );

        JdoResource resource1 = new JdoResource();
        resource1.setIdentifier( "rId1" );
        resource1 = store.getJdoResourceByJdoId( store.addJdoResource( resource1 ), true );

        JdoResource resource2 = new JdoResource();
        resource2.setIdentifier( "rId2" );
        resource2 = store.getJdoResourceByJdoId( store.addJdoResource( resource2 ), true );

        JdoPermission permission1 = new JdoPermission();
        permission1.setName( "pName1" );
        permission1.setDescription( "pDesc1" );
        permission1.setOperation( operation1 );
        permission1.setResource( resource1 );
        permission1 = store.getJdoPermissionByJdoId( store.addJdoPermission( permission1 ), true );

        JdoPermission permission2 = new JdoPermission();
        permission2.setName( "pName2" );
        permission2.setDescription( "pDesc2" );
        permission2.setOperation( operation1 );
        permission2.setResource( resource2 );
        permission2 = store.getJdoPermissionByJdoId( store.addJdoPermission( permission2 ), true );

        JdoPermission permission3 = new JdoPermission();
        permission3.setName( "pName3" );
        permission3.setDescription( "pDesc3" );
        permission3.setOperation( operation2 );
        permission3.setResource( resource1 );
        permission3 = store.getJdoPermissionByJdoId( store.addJdoPermission( permission3 ), true );

        JdoRole childRole1 = new JdoRole();
        childRole1.setName( "role1" );
        childRole1.setDescription( "description1" );
        childRole1.addPermission( permission1 );
        store.addJdoRole( childRole1 );

        JdoRole childRole2 = new JdoRole();
        childRole2.setName( "role2" );
        childRole2.setDescription( "description2" );
        childRole2.addPermission( permission1 );
        childRole2.addPermission( permission3 );
        store.addJdoRole( childRole2 );

        JdoRole role = new JdoRole();
        role.setName( "name" );
        role.setDescription( "description" );
        role.addChildRoleName( "role1" );
        role.addChildRoleName( "role2" );
        role.addPermission( permission1 );
        role.addPermission( permission2 );
        role.addPermission( permission3 );
        store.addJdoRole( role );

        JdoUserAssignment userAssignment1 = new JdoUserAssignment();
        userAssignment1.setPrincipal( "principal1" );
        userAssignment1.setTimestamp( new Date() );
        userAssignment1.getRoleNames().add( "role1" );
        store.addJdoUserAssignment( userAssignment1 );

        JdoUserAssignment userAssignment2 = new JdoUserAssignment();
        userAssignment2.setPrincipal( "principal2" );
        userAssignment2.setTimestamp( new Date() );
        userAssignment2.getRoleNames().add( "name" );
        store.addJdoUserAssignment( userAssignment2 );

        JdoUserAssignment userAssignment3 = new JdoUserAssignment();
        userAssignment3.setPrincipal( "principal3" );
        userAssignment3.setTimestamp( new Date() );
        userAssignment3.getRoleNames().add( "role1" );
        userAssignment3.getRoleNames().add( "role2" );
        store.addJdoUserAssignment( userAssignment3 );    

        RbacJdoModelModelloMetadata metadata = new RbacJdoModelModelloMetadata();
        metadata.setModelVersion( "1.0.0" );
        store.storeRbacJdoModelModelloMetadata( metadata );
    }
}
