package org.codehaus.modello.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionDefinitionTest {

    @Test
    void fieldType() {
        VersionDefinition def = new VersionDefinition();
        def.setType("field");
        assertTrue(def.isFieldType());
        assertFalse(def.isNamespaceType());
    }

    @Test
    void namespaceType() {
        VersionDefinition def = new VersionDefinition();
        def.setType("namespace");
        assertTrue(def.isNamespaceType());
        assertFalse(def.isFieldType());
    }

    @Test
    void fieldAndNamespaceType() {
        VersionDefinition def = new VersionDefinition();
        def.setType("field+namespace");
        assertTrue(def.isFieldType());
        assertTrue(def.isNamespaceType());
    }
}
