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

import java.util.Properties;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import junit.framework.Assert;
import org.apache.log4j.*;
import org.codehaus.modello.verifier.Verifier;
import org.codehaus.plexus.security.authorization.rbac.jdo.RbacJdoModelModelloMetadata;
import org.codehaus.plexus.security.authorization.rbac.jdo.RbacJdoModelJPoxStore;

/**
 * @version $Id: Xpp3Verifier.java 675 2006-11-16 10:58:59Z brett $
 */
public class JpoxVerifierVersion
    extends Verifier
{
    public void verify()
        throws Exception
    {
        Properties properties = new Properties();
        properties.setProperty( "javax.jdo.PersistenceManagerFactoryClass", "org.jpox.PersistenceManagerFactoryImpl" );
        properties.setProperty( "javax.jdo.option.ConnectionDriverName", "org.apache.derby.jdbc.EmbeddedDriver" );
        properties.setProperty( "javax.jdo.option.ConnectionURL", "jdbc:derby:target/jpox-version/database;create=true" );
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

        RbacJdoModelModelloMetadata metadata = store.getRbacJdoModelModelloMetadata( true );

        Assert.assertNull( metadata );

        metadata = new RbacJdoModelModelloMetadata();
        metadata.setModelVersion( "1.0.0" );
        store.storeRbacJdoModelModelloMetadata( metadata );

        metadata = store.getRbacJdoModelModelloMetadata( true );
        Assert.assertEquals( "1.0.0", metadata.getModelVersion() );
    }
}
