package org.codehaus.modello.plugins.xml;

import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.plugin.AbstractModelloGenerator;
import org.codehaus.modello.plugins.xml.metadata.XmlClassMetadata;

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
 * Helper methods to deal with XML representation of the model.
 *
 * @author <a href="mailto:hboutemy@codehaus.org">Hervé Boutemy</a>
 * @version $Id$
 */
class XmlModelHelpers
{
    static String getTagName( ModelClass modelClass )
    {
        XmlClassMetadata metadata = (XmlClassMetadata) modelClass.getMetadata( XmlClassMetadata.ID );

        String tagName;
        if ( metadata == null || metadata.getTagName() == null )
        {
            tagName = AbstractModelloGenerator.uncapitalise( modelClass.getName() );
        }
        else
        {
            tagName = metadata.getTagName();
        }
        return tagName;
    }

}
