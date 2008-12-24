package org.codehaus.modello.plugin.xdoc.metadata;

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

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.metadata.AbstractMetadataPlugin;
import org.codehaus.modello.metadata.AssociationMetadata;
import org.codehaus.modello.metadata.ClassMetadata;
import org.codehaus.modello.metadata.FieldMetadata;
import org.codehaus.modello.metadata.MetadataPlugin;
import org.codehaus.modello.metadata.ModelMetadata;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;

import java.util.Map;

/**
 * @author Hervé Boutemy
 * @version $Id$
 */
public class XdocMetadataPlugin
    extends AbstractMetadataPlugin
    implements MetadataPlugin
{
    public ClassMetadata getClassMetadata( ModelClass clazz, Map data )
    {
        return new XdocClassMetadata();
    }

    public AssociationMetadata getAssociationMetadata( ModelAssociation association, Map data )
        throws ModelloException
    {
        return new XdocAssociationMetadata();
    }

    public FieldMetadata getFieldMetadata( ModelField field, Map data )
        throws ModelloException
    {
        XdocFieldMetadata metadata = new XdocFieldMetadata();

        metadata.setSeparator( (String) data.get( "xdoc.separator" ) );

        return metadata;
    }

    public ModelMetadata getModelMetadata( Model model, Map data )
        throws ModelloException
    {
        return new XdocModelMetadata();
    }
}
