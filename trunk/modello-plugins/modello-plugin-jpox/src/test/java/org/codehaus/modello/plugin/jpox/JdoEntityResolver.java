package org.codehaus.modello.plugin.jpox;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * JdoEntityResolver 
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 */
public class JdoEntityResolver
    implements EntityResolver
{
    private static final Map PUBLICID_TO_RESOURCE_MAP;

    static
    {
        PUBLICID_TO_RESOURCE_MAP = new HashMap();

        PUBLICID_TO_RESOURCE_MAP.put( "-//Sun Microsystems, Inc.//DTD Java Data Objects Metadata 1.0//EN",
                                      "/jdo_1_0.dtd" );
        PUBLICID_TO_RESOURCE_MAP.put( "-//Sun Microsystems, Inc.//DTD Java Data Objects Metadata 2.0//EN",
                                      "/jdo_2_0.dtd" );
    }

    public InputSource resolveEntity( String publicId, String systemId )
        throws SAXException, IOException
    {
        if ( PUBLICID_TO_RESOURCE_MAP.containsKey( publicId ) )
        {
            URL url = this.getClass().getResource( (String) PUBLICID_TO_RESOURCE_MAP.get( publicId ) );
            return new InputSource( url.openStream() );
        }

        return null;
    }

}
