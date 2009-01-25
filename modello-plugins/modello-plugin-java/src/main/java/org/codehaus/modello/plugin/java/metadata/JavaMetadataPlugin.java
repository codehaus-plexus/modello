package org.codehaus.modello.plugin.java.metadata;

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
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse </a>
 * @version $Id$
 */
public class JavaMetadataPlugin
    extends AbstractMetadataPlugin
    implements MetadataPlugin
{
    public static final String JAVA_ABSTRACT = "java.abstract";

    public static final String JAVA_ADDER = "java.adder";

    public static final String JAVA_ENABLED = "java.enabled";

    public static final String JAVA_GENERATE_BREAK = "java.generate-break";

    public static final String JAVA_GENERATE_CREATE = "java.generate-create";

    public static final String JAVA_GETTER = "java.getter";

    public static final String JAVA_INIT = "java.init";

    public static final String JAVA_SETTER = "java.setter";

    public static final String JAVA_USE_INTERFACE = "java.useInterface";

    // ----------------------------------------------------------------------
    // Map to Metadata
    // ----------------------------------------------------------------------

    public ModelMetadata getModelMetadata( Model model, Map data )
    {
        return new JavaModelMetadata();
    }

    public ClassMetadata getClassMetadata( ModelClass clazz, Map data )
    {
        JavaClassMetadata metadata = new JavaClassMetadata();

        metadata.setEnabled( getBoolean( data, JAVA_ENABLED, true ) );

        metadata.setAbstract( getBoolean( data, JAVA_ABSTRACT, false ) );

        return metadata;
    }

    public FieldMetadata getFieldMetadata( ModelField field, Map data )
    {
        JavaFieldMetadata metadata = new JavaFieldMetadata();

        metadata.setSetter( getBoolean( data, JAVA_GETTER, true ) );

        String fieldType = field.getType();
        metadata.setBooleanGetter( ( fieldType != null ) && fieldType.endsWith( "oolean" ) );

        metadata.setSetter( getBoolean( data, JAVA_SETTER, true ) );

        return metadata;
    }

    public AssociationMetadata getAssociationMetadata( ModelAssociation association, Map data )
    {
        JavaAssociationMetadata metadata = new JavaAssociationMetadata();

        metadata.setAdder( getBoolean( data, JAVA_ADDER, true ) );

        metadata.setGenerateBreak( getBoolean( data, JAVA_GENERATE_BREAK, true ) );
        metadata.setGenerateCreate( getBoolean( data, JAVA_GENERATE_CREATE, true ) );

        metadata.setInterfaceName( getString( data, JAVA_USE_INTERFACE ) );

        metadata.setInitializationMode( getString( data, JAVA_INIT ) );

        return metadata;
    }
}
