package org.codehaus.modello.plugin.java.javasource;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:simonetripodi@apache.org">Simone Tripodi</a>
 * @since 1.8
 */
public final class JMapTypeTestCase extends TestCase {

    public void testJavaPropertiesType() {
        JMapType mapType = new JMapType("java.util.Properties", new JClass("String"));

        assertEquals("java.util.Properties", mapType.toString());
    }

    public void testJavaMapType() {
        JMapType mapType = new JMapType("java.util.Map", new JClass("String"));

        assertEquals("java.util.Map<Object, String>", mapType.toString());
    }

    public void testJavaMapInitialization() {
        JMapType mapType = new JMapType("java.util.Map", "new java.util.HashMap()", new JClass("String"));

        assertEquals("new java.util.HashMap<Object, String>()", mapType.getInstanceName());
    }
}
