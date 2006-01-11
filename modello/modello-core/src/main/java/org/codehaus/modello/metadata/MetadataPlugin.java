package org.codehaus.modello.metadata;

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

import java.util.Map;

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface MetadataPlugin
{
    String ROLE = MetadataPlugin.class.getName();

    ModelMetadata getModelMetadata( Model model, Map data )
        throws ModelloException;

    ClassMetadata getClassMetadata( ModelClass clazz, Map data )
        throws ModelloException;

    FieldMetadata getFieldMetadata( ModelField field, Map data )
        throws ModelloException;

    AssociationMetadata getAssociationMetadata( ModelAssociation association, Map data )
        throws ModelloException;

    Map getModelMap( Model model, ModelMetadata metadata )
        throws ModelloException;

    Map getClassMap( ModelClass clazz, ClassMetadata metadata )
        throws ModelloException;

    Map getFieldMap( ModelField field, FieldMetadata metadata )
        throws ModelloException;

    Map getAssociationMap( ModelAssociation association, AssociationMetadata metadata )
        throws ModelloException;
}
