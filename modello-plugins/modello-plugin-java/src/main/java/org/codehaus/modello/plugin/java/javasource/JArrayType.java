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
 * JType sub-class for Arrays.
 *
 * @author Werner Guttman
 * @version $Revision$ $Date$
 * @since 1.0.4
 */
public final class JArrayType extends JComponentizedType {
    // --------------------------------------------------------------------------

    /**
     * Creates an instance of a array type, of type 'name'.
     *
     * @param componentType Component type.
     */
    public JArrayType(final JType componentType) {
        super(componentType.getName(), componentType);
    }

    // --------------------------------------------------------------------------

    /**
     * Returns the String representation of this JType, which is simply the name of this type.
     *
     * @return The String representation of this JType.
     */
    public String toString() {
        return getComponentType().toString() + "[]";
    }

    // --------------------------------------------------------------------------
}
