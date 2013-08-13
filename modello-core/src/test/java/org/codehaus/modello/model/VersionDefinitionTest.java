package org.codehaus.modello.model;

import junit.framework.TestCase;

public class VersionDefinitionTest
    extends TestCase
{

    public void testFieldType()
    {
        VersionDefinition def = new VersionDefinition();
        def.setType( "field" );
        assertTrue( def.isFieldType() );
        assertFalse( def.isNamespaceType() );
    }
    
    public void testNamespaceType()
    {
        VersionDefinition def = new VersionDefinition();
        def.setType( "namespace" );
        assertTrue( def.isNamespaceType() );
        assertFalse( def.isFieldType() );
    }

    public void testFieldAndNamespaceType()
    {
        VersionDefinition def = new VersionDefinition();
        def.setType( "field+namespace" );
        assertTrue( def.isFieldType() );
        assertTrue( def.isNamespaceType() );
    }

    
}
