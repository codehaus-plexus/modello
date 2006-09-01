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
    public static final String DEPENDENT = "jpox.dependent";

    public static final String DETACHABLE = "jpox.detachable";

    public static final String FETCH_GROUP_NAMES = "jpox.fetchGroupNames";

    public static final String JOIN = "jpox.join";

    public static final String MAPPED_BY = "jpox.mappedBy";

    public static final String NULL_VALUE = "jpox.nullValue";

    public static final String TABLE = "jpox.table";

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

        metadata.setDetachable( getBoolean( data, DETACHABLE, true ) );

        String table = (String) data.get( TABLE );

        if ( !StringUtils.isEmpty( table ) )
        {
            metadata.setTable( table );
        }

        return metadata;
    }

    public FieldMetadata getFieldMetadata( ModelField field, Map data )
        throws ModelloException
    {
        JPoxFieldMetadata metadata = new JPoxFieldMetadata();

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
