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

import org.codehaus.modello.metadata.ModelMetadata;
import org.codehaus.modello.model.Version;
import org.codehaus.plexus.util.StringUtils;

/**
 * XsdModelMetadata
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 */
public class XsdModelMetadata implements ModelMetadata
{
    public static final String ID = XsdModelMetadata.class.getName();

    private String namespace;

    private String targetNamespace;

    public String getNamespace()
    {
        return namespace;
    }

    public void setNamespace( String namespace )
    {
        this.namespace = namespace;
    }

    public String getTargetNamespace()
    {
        return targetNamespace;
    }

    public void setTargetNamespace( String targetNamespace )
    {
        this.targetNamespace = targetNamespace;
    }

    public String getNamespace( Version version )
    {
        String namespace = this.namespace;

        if ( version != null )
        {
            namespace = StringUtils.replace( namespace, "${version}", version.toString() );
        }

        return namespace;
    }

    public String getTargetNamespace( Version version )
    {
        String targetNamespace = this.targetNamespace;

        if ( version != null )
        {
            targetNamespace = StringUtils.replace( targetNamespace, "${version}", version.toString() );
        }

        return targetNamespace;
    }
}
