package org.codehaus.modello.plugin;

/*
 * LICENSE
 */

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class FooTestPlugin
    implements TestPlugin
{
    public String ping()
    {
        return "Hello World!";
    }
}
