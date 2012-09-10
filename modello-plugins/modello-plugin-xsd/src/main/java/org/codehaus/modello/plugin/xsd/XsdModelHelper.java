package org.codehaus.modello.plugin.xsd;

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.Version;
import org.codehaus.modello.plugin.xsd.metadata.XsdModelMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlModelMetadata;
import org.codehaus.plexus.util.StringUtils;

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

/**
 * Helper methods to deal with XML schema representation of the model.
 *
 * @author <a href="mailto:hboutemy@codehaus.org">Hervé Boutemy</a>
 */
public class XsdModelHelper
{
    public static String getNamespace( Model model, Version version )
        throws ModelloException
    {
        XmlModelMetadata xmlModelMetadata = (XmlModelMetadata) model.getMetadata( XmlModelMetadata.ID );

        XsdModelMetadata xsdModelMetadata = (XsdModelMetadata) model.getMetadata( XsdModelMetadata.ID );

        String namespace;
        if ( StringUtils.isNotEmpty( xsdModelMetadata.getNamespace() ) )
        {
            namespace = xsdModelMetadata.getNamespace( version );
        }
        else
        {
            // xsd.namespace is not set, try using xml.namespace
            if ( StringUtils.isEmpty( xmlModelMetadata.getNamespace() ) )
            {
                throw new ModelloException( "Cannot generate xsd without xmlns specification:"
                                            + " <model xml.namespace='...'> or <model xsd.namespace='...'>" );
            }

            namespace = xmlModelMetadata.getNamespace( version );
        }

        return namespace;
    }

    public static String getTargetNamespace( Model model, Version version, String namespace )
    {
        XsdModelMetadata xsdModelMetadata = (XsdModelMetadata) model.getMetadata( XsdModelMetadata.ID );

        String targetNamespace;
        if ( xsdModelMetadata.getTargetNamespace() == null )
        {
            // xsd.target-namespace not set, using namespace
            targetNamespace = namespace;
        }
        else
        {
            targetNamespace = xsdModelMetadata.getTargetNamespace( version );
        }
        return targetNamespace;
    }

    public static String getTargetNamespace( Model model, Version version )
        throws ModelloException
    {
        return getTargetNamespace( model, version, getNamespace( model, version ) );
    }
}
