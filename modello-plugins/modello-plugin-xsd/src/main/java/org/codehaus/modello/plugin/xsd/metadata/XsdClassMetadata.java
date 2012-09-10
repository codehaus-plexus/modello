package org.codehaus.modello.plugin.xsd.metadata;

/*
 * Copyright 2001-2007 The Codehaus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.codehaus.modello.metadata.ClassMetadata;

/**
 * XsdClassMetadata
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 */
public class XsdClassMetadata implements ClassMetadata
{
    public static final String ID = XsdClassMetadata.class.getName();

    public static final String COMPOSITOR_ALL = "all";

    public static final String COMPOSITOR_SEQUENCE = "sequence";

    private String compositor = COMPOSITOR_ALL;

    public String getCompositor()
    {
        return compositor;
    }

    public void setCompositor( String compositor )
    {
        if ( COMPOSITOR_ALL.equals( compositor ) || COMPOSITOR_SEQUENCE.equals( compositor ) )
        {
            this.compositor = compositor;
        }
        else
        {
            // default
            this.compositor = COMPOSITOR_ALL;
        }
    }

}
