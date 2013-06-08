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
import org.codehaus.modello.metadata.InterfaceMetadata;
import org.codehaus.modello.metadata.MetadataPlugin;
import org.codehaus.modello.metadata.ModelMetadata;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.ModelInterface;

import java.util.Map;

/**
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse </a>
 */
public class JavaMetadataPlugin
    extends AbstractMetadataPlugin
    implements MetadataPlugin
{
    public static final String JAVA_ABSTRACT = "java.abstract";

    public static final String JAVA_ADDER = "java.adder";

    public static final String JAVA_BIDI = "java.bidi";

    public static final String JAVA_ENABLED = "java.enabled";

    public static final String JAVA_GETTER = "java.getter";

    public static final String JAVA_INIT = "java.init";

    public static final String JAVA_SETTER = "java.setter";

    public static final String JAVA_USE_INTERFACE = "java.useInterface";

    public static final String JAVA_CLONE = "java.clone";

    public static final String JAVA_CLONE_HOOK = "java.clone.hook";

    /**
     * @since 1.8
     */
    public static final String JAVA_GENERATE_TOSTRING = "java.toString";

    public static final String JAVA_SUPPRESS_ALL_WARNINGS = "java.suppressAllWarnings";

    /**
     * @since 1.8
     */
    public static final String JAVA_GENERATE_BUILDER = "java.builder";

    /**
     * @since 1.8
     */
    public static final String JAVA_GENERATE_STATIC_CREATORS = "java.staticCreator";

    // ----------------------------------------------------------------------
    // Map to Metadata
    // ----------------------------------------------------------------------

    public ModelMetadata getModelMetadata( Model model, Map<String, String> data )
    {
        JavaModelMetadata metadata = new JavaModelMetadata();

        metadata.setSuppressAllWarnings( getBoolean( data, JAVA_SUPPRESS_ALL_WARNINGS, true ) );

        return metadata;
    }

    public ClassMetadata getClassMetadata( ModelClass clazz, Map<String, String> data )
    {
        JavaClassMetadata metadata = new JavaClassMetadata();

        metadata.setEnabled( getBoolean( data, JAVA_ENABLED, true ) );

        metadata.setAbstract( getBoolean( data, JAVA_ABSTRACT, false ) );

        metadata.setCloneMode( getString( data, JAVA_CLONE ) );

        metadata.setCloneHook( getString( data, JAVA_CLONE_HOOK ) );

        metadata.setGenerateToString( getBoolean( data, JAVA_GENERATE_TOSTRING, false ) );

        metadata.setGenerateBuilder( getBoolean( data, JAVA_GENERATE_BUILDER, false ) );

        metadata.setGenerateStaticCreators( getBoolean( data, JAVA_GENERATE_STATIC_CREATORS, false ) );

        return metadata;
    }

    public InterfaceMetadata getInterfaceMetadata( ModelInterface iface, Map<String, String> data )
    {
        return new JavaInterfaceMetadata();
    }

    public FieldMetadata getFieldMetadata( ModelField field, Map<String, String> data )
    {
        JavaFieldMetadata metadata = new JavaFieldMetadata();

        metadata.setGetter( getBoolean( data, JAVA_GETTER, true ) );

        String fieldType = field.getType();
        metadata.setBooleanGetter( ( fieldType != null ) && fieldType.endsWith( "oolean" ) );

        metadata.setSetter( getBoolean( data, JAVA_SETTER, true ) );

        return metadata;
    }

    public AssociationMetadata getAssociationMetadata( ModelAssociation association, Map<String, String> data )
    {
        JavaAssociationMetadata metadata = new JavaAssociationMetadata();

        metadata.setAdder( getBoolean( data, JAVA_ADDER, true ) );

        metadata.setBidi( getBoolean( data, JAVA_BIDI, true ) );

        metadata.setInterfaceName( getString( data, JAVA_USE_INTERFACE ) );

        metadata.setInitializationMode( getString( data, JAVA_INIT ) );

        metadata.setCloneMode( getString( data, JAVA_CLONE ) );

        return metadata;
    }
}
