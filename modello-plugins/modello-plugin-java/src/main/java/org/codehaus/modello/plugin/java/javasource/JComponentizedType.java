/*
 * Copyright 2006 Werner Guttmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.codehaus.modello.plugin.java.javasource;

/**
 * JType sub-class for componentized types, such as array as collections.
 *
 * @author Werner Guttman
 * @version $Revision$ $Date$
 * @since 1.0.4
 */
public class JComponentizedType extends JType {
    // --------------------------------------------------------------------------

    /** Indicates the data type contained in this collection. */
    private JType _componentType;

    // --------------------------------------------------------------------------

    /**
     * Creates an instance of a componentized type, of type 'name'.
     *
     * @param name Type name for this componentized type.
     * @param componentType Component type.
     */
    protected JComponentizedType(final String name, final JType componentType) {
        super(name);

        _componentType = componentType;
    }

    // --------------------------------------------------------------------------

    /**
     * Returns the component type.
     *
     * @return The component type.
     */
    public final JType getComponentType() {
        return _componentType;
    }

    // --------------------------------------------------------------------------
}
