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

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.metadata.AbstractMetadataPlugin;
import org.codehaus.modello.metadata.AssociationMetadata;
import org.codehaus.modello.metadata.ClassMetadata;
import org.codehaus.modello.metadata.FieldMetadata;
import org.codehaus.modello.metadata.InterfaceMetadata;
import org.codehaus.modello.metadata.ModelMetadata;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.ModelInterface;

import java.util.Map;

/**
 * XsdMetadataPlugin
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 */
public class XsdMetadataPlugin
    extends AbstractMetadataPlugin
{
    public static final String NAMESPACE = "xsd.namespace";

    public static final String TARGET_NAMESPACE = "xsd.targetNamespace";

    public static final String COMPOSITOR = "xsd.compositor";

    public AssociationMetadata getAssociationMetadata( ModelAssociation association, Map<String, String> data )
        throws ModelloException
    {
        return new XsdAssociationMetadata();
    }

    public ClassMetadata getClassMetadata( ModelClass clazz, Map<String, String> data )
        throws ModelloException
    {
        XsdClassMetadata metadata = new XsdClassMetadata();

        metadata.setCompositor( getString( data, COMPOSITOR ) );

        return metadata;
    }

    public InterfaceMetadata getInterfaceMetadata( ModelInterface iface, Map<String, String> data )
        throws ModelloException
    {
        return new XsdInterfaceMetadata();
    }

    public FieldMetadata getFieldMetadata( ModelField field, Map<String, String> data )
        throws ModelloException
    {
        return new XsdFieldMetadata();
    }

    public ModelMetadata getModelMetadata( Model model, Map<String, String> data )
        throws ModelloException
    {
        XsdModelMetadata metadata = new XsdModelMetadata();

        metadata.setNamespace( getString( data, NAMESPACE ) );

        metadata.setTargetNamespace( getString( data, TARGET_NAMESPACE ) );

        return metadata;
    }
}
