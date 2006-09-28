package org.codehaus.modello.plugin.store.metadata;

/*
 * Copyright (c) 2005, Codehaus.org
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
import org.codehaus.modello.metadata.ModelMetadata;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.ModelloException;
import org.codehaus.plexus.util.StringUtils;

import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class StoreMetadataPlugin
    extends AbstractMetadataPlugin
{
    public final static String PART = "stash.part";

    public final static String KEY_TYPE = "stash.keyType";

    // ----------------------------------------------------------------------
    // Map to Metadata
    // ----------------------------------------------------------------------

    public ModelMetadata getModelMetadata( Model model, Map data )
    {
        return new StoreModelMetadata();
    }

    public ClassMetadata getClassMetadata( ModelClass clazz, Map data )
        throws ModelloException
    {
        StoreClassMetadata metadata = new StoreClassMetadata();

        String storable = (String) data.get( "stash.storable" );

        if ( storable != null && storable.equals( "true" ) )
        {
//            JavaClassMetadata jcm = (JavaClassMetadata) clazz.getMetadata( JavaClassMetadata.ID );
//
//            if ( jcm.isAbstract() )
//            {
//                throw new ModelloException( "A storable class can't be abstract. " +
//                                            "Class name '" + clazz.getName() + "'." );
//            }
//
            metadata.setStorable( true );
        }

        return metadata;
    }

    public FieldMetadata getFieldMetadata( ModelField field, Map data )
        throws ModelloException
    {
        StoreFieldMetadata metadata = new StoreFieldMetadata();

        // ----------------------------------------------------------------------
        // Fields are per default storable as the fields can't be persisted
        // unless the class itself is storable.
        // ----------------------------------------------------------------------

        metadata.setStorable( getBoolean( data, "stash.storable", true ) );

        String maxSize = (String) data.get( "stash.maxSize" );

        if ( !StringUtils.isEmpty( maxSize ) )
        {
            if ( !field.getType().equals( "String" ) )
            {
                throw new ModelloException( "When specifying max size on a field the type must be String. " +
                                            "Class: '" + field.getModelClass().getName() + "', " +
                                            "field : '" + field.getName() + "'." );
            }

            try
            {
                metadata.setMaxSize( Integer.parseInt( maxSize ) );
            }
            catch ( NumberFormatException e )
            {
                throw new ModelloException( "Max size on a field the type must be String. " +
                                            "Class: '" + field.getModelClass().getName() + "', " +
                                            "field : '" + field.getName() + "'." );
            }
        }

        return metadata;
    }

    public AssociationMetadata getAssociationMetadata( ModelAssociation association, Map data )
        throws ModelloException
    {
        StoreAssociationMetadata metadata = new StoreAssociationMetadata();

        // ----------------------------------------------------------------------
        // Associations are per default storable as the fields can't be persisted
        // unless the class itself is storable.
        // ----------------------------------------------------------------------

        metadata.setStorable( getBoolean( data, "stash.storable", true ) );

        if ( data.containsKey( PART ) )
        {
            metadata.setPart( Boolean.valueOf( (String) data.get( PART ) ) );
        }

        String keyType = (String) data.get( KEY_TYPE );

        if ( association.getType() != null && association.getType().equals( "Map" ) )
        {
            if ( StringUtils.isEmpty( keyType ) )
            {
                throw new ModelloException( "When the association is a java.util.Map key type has to be specified." +
                                            "Class: '" + association.getModelClass().getName() + "', " +
                                            "field : '" + association.getName() + "'." );
            }

            // TODO: assert the key type

            metadata.setKeyType( keyType );
        }

        return metadata;
    }

    // ----------------------------------------------------------------------
    // Metadata to Map
    // ----------------------------------------------------------------------

    public Map getFieldMap( ModelField field, FieldMetadata metadata )
    {
        return Collections.EMPTY_MAP;
    }
}
