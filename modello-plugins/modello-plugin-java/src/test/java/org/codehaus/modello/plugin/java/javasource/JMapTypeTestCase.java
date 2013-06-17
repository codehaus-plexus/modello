package org.codehaus.modello.plugin.java.javasource;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:simonetripodi@apache.org">Simone Tripodi</a>
 * @since 1.8
 */
public final class JMapTypeTestCase
    extends TestCase
{

    public void testJava5PropertiesType()
    {
        JMapType mapType = new JMapType( "java.util.Properties", new JClass( "String" ), true );

        assertEquals( "java.util.Properties", mapType.toString() );
    }

    public void testJava4PropertiesType()
    {
        JMapType mapType = new JMapType( "java.util.Properties", new JClass( "String" ), false );

        assertEquals( "java.util.Properties", mapType.toString() );
    }

    public void testJava5MapType()
    {
        JMapType mapType = new JMapType( "java.util.Map", new JClass( "String" ), true );

        assertEquals( "java.util.Map<Object, String>", mapType.toString() );
    }

    public void testJava5MapInitialization()
    {
        JMapType mapType = new JMapType( "java.util.Map", "new java.util.HashMap()", new JClass( "String" ), true );

        assertEquals( "new java.util.HashMap<Object, String>()", mapType.getInstanceName() );
    }

    public void testJava4MapType()
    {
        JMapType mapType = new JMapType( "java.util.Map", new JClass( "String" ), false );

        assertEquals( "java.util.Map/*<Object, String>*/", mapType.toString() );
    }

    public void testJava4MapInitialization()
    {
        JMapType mapType = new JMapType( "java.util.Map", "new java.util.HashMap()", new JClass( "String" ), false );

        assertEquals( "new java.util.HashMap/*<Object, String>*/()", mapType.getInstanceName() );
    }

}
