package org.codehaus.modello.plugin.jpox.metadata;

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

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.metadata.AbstractMetadataPlugin;
import org.codehaus.modello.metadata.AssociationMetadata;
import org.codehaus.modello.metadata.ClassMetadata;
import org.codehaus.modello.metadata.FieldMetadata;
import org.codehaus.modello.metadata.ModelMetadata;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;
import org.codehaus.plexus.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class JPoxMetadataPlugin
    extends AbstractMetadataPlugin
{
    public static final String ENABLED = "jpox.enabled";
    
    public static final String DEPENDENT = "jpox.dependent";

    public static final String DETACHABLE = "jpox.detachable";

    public static final String FETCH_GROUP_NAMES = "jpox.fetchGroupNames";
    
    public static final String NOT_PERSISTED_FIELDS = "jpox.not-persisted-fields"; 

    public static final String JOIN = "jpox.join";

    public static final String MAPPED_BY = "jpox.mappedBy";

    public static final String NULL_VALUE = "jpox.nullValue";

    public static final String TABLE = "jpox.table";
    
    public static final String COLUMN = "jpox.column";
    
    public static final String JOIN_TABLE = "jpox.join-table";
    
    public static final String INDEXED = "jpox.indexed";
    
    public static final String PRIMARY_KEY = "jpox.primary-key";
    
    public static final String VALUE_STRATEGY = "jpox.value-strategy";
    
    public static final String PERSISTENCE_MODIFIER = "jpox.persistence-modifier";
    
    public static final String IDENTITY_TYPE = "jpox.identity-type";
    
    public static final String IDENTITY_CLASS = "jpox.identity-class";
    
    public static final String USE_IDENTIFIERS = "jpox.use-identifiers-as-primary-key";

    // ----------------------------------------------------------------------
    // Map to Metadata
    // ----------------------------------------------------------------------

    public ModelMetadata getModelMetadata( Model model, Map data )
    {
        return new JPoxModelMetadata();
    }

    public ClassMetadata getClassMetadata( ModelClass clazz, Map data )
        throws ModelloException
    {
        JPoxClassMetadata metadata = new JPoxClassMetadata();

        metadata.setEnabled( getBoolean( data, ENABLED, true ) );
        metadata.setDetachable( getBoolean( data, DETACHABLE, true ) );
        
        String notPersistedFields = (String) data.get( NOT_PERSISTED_FIELDS );

        if ( !StringUtils.isEmpty( notPersistedFields ) )
        {
            List ignoredFields = Arrays.asList( StringUtils.split( notPersistedFields ) );

            metadata.setNotPersisted( ignoredFields );
        }

        String table = (String) data.get( TABLE );

        if ( !StringUtils.isEmpty( table ) )
        {
            metadata.setTable( table );
        }
        
        String identityType = (String) data.get( IDENTITY_TYPE );

        if ( StringUtils.isNotEmpty( identityType ) )
        {
            metadata.setIdentityType( identityType );
        }

        String identityClass = (String) data.get( IDENTITY_CLASS );

        if ( StringUtils.isNotEmpty( identityClass ) )
        {
            metadata.setIdentityClass( identityClass );
        }
        
        metadata.setUseIdentifiersAsPrimaryKey( getBoolean( data, USE_IDENTIFIERS, true ) );
        
        return metadata;
    }

    public FieldMetadata getFieldMetadata( ModelField field, Map data )
        throws ModelloException
    {
        JPoxFieldMetadata metadata = new JPoxFieldMetadata();
        
        JPoxClassMetadata classMetadata = (JPoxClassMetadata) field.getModelClass().getMetadata( JPoxClassMetadata.ID );

        boolean useIdentifiersAsPrimaryKey = classMetadata.useIdentifiersAsPrimaryKey();

        metadata.setPrimaryKey( getBoolean( data, PRIMARY_KEY, ( field.isIdentifier() && useIdentifiersAsPrimaryKey ) ) );

        String fetchGroupNames = (String) data.get( FETCH_GROUP_NAMES );

        if ( !StringUtils.isEmpty( fetchGroupNames ) )
        {
            List fetchGroups = Arrays.asList( StringUtils.split( fetchGroupNames ) );

            metadata.setFetchGroupNames( fetchGroups );
        }

        String mappedBy = (String) data.get( MAPPED_BY );

        if ( !StringUtils.isEmpty( mappedBy ) )
        {
            metadata.setMappedBy( mappedBy );
        }

        String nullValue = (String) data.get( NULL_VALUE );

        if ( !StringUtils.isEmpty( nullValue ) )
        {
            metadata.setNullValue( nullValue );
        }
        
        String column = (String) data.get( COLUMN );

        if ( StringUtils.isNotEmpty( column ) )
        {
            metadata.setColumnName( column );
        }
        
        String joinTable = (String) data.get( JOIN_TABLE );
        
        if ( StringUtils.isNotEmpty( joinTable ) )
        {
            metadata.setJoinTableName( joinTable );
        }
        
        String indexed = (String) data.get( INDEXED );
        
        if ( StringUtils.isNotEmpty( indexed ) )
        {
            metadata.setIndexed( indexed );
        }
        
        String persistenceModifier = (String) data.get( PERSISTENCE_MODIFIER );

        if ( StringUtils.isNotEmpty( persistenceModifier ) )
        {
            metadata.setPersistenceModifier( persistenceModifier );
        }

        // According to http://www.jpox.org/docs/1_1/identity_generation.html the default value for
        // this should be 'native', however this is untrue in jpox-1.1.1
        metadata.setValueStrategy( "native" );
        
        if ( StringUtils.isNotEmpty( (String) data.get( VALUE_STRATEGY ) ) )
        {
            String valueStrategy = (String) data.get( VALUE_STRATEGY );

            if ( StringUtils.equals( valueStrategy, "off" ) )
            {
                metadata.setValueStrategy( null );
            }
            else
            {
                metadata.setValueStrategy( valueStrategy );
            }
        }
        
        return metadata;
    }

    public AssociationMetadata getAssociationMetadata( ModelAssociation association, Map data )
        throws ModelloException
    {
        JPoxAssociationMetadata metadata = new JPoxAssociationMetadata();

        metadata.setDependent( getBoolean( data, DEPENDENT, true ) );

        metadata.setJoin( getBoolean( data, JOIN, true ) );

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
