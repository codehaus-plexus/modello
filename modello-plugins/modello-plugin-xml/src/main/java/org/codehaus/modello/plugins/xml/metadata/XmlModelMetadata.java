package org.codehaus.modello.plugins.xml.metadata;

/*
 * Copyright (c) 2004, Codehaus.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import org.codehaus.modello.metadata.ModelMetadata;
import org.codehaus.modello.model.Version;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class XmlModelMetadata
    implements ModelMetadata
{
    public static final String ID = XmlModelMetadata.class.getName();

    private String namespace;

    private String schemaLocation;

    public String getNamespace()
    {
        return namespace;
    }

    public void setNamespace( String namespace )
    {
        this.namespace = namespace;
    }

    public String getSchemaLocation()
    {
        return schemaLocation;
    }

    public void setSchemaLocation( String schemaLocation )
    {
        this.schemaLocation = schemaLocation;
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

    public String getSchemaLocation( Version version )
    {
        String schemaLocation = this.schemaLocation;

        if ( version != null )
        {
            schemaLocation = StringUtils.replace( schemaLocation, "${version}", version.toString() );
        }

        return schemaLocation;
    }
}
