package org.codehaus.modello.plugin;

/*
 * LICENSE
 */

import java.util.Iterator;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PluginManagerTest
    extends TestCase
{
    public void testPluginManager()
        throws Exception
    {
        TestPluginManager manager = new TestPluginManager();

        manager.initialize();

        Iterator it = manager.getPlugins();

        assertTrue( it.hasNext() );

        Object o = it.next();

        assertTrue( o instanceof TestPlugin );

        assertTrue( o instanceof FooTestPlugin );

        FooTestPlugin plugin = (FooTestPlugin) o;

        assertEquals( "Hello World!", plugin.ping() );

        assertFalse( it.hasNext() );
    }
}
