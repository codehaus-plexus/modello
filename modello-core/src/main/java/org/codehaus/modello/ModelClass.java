package org.codehaus.modello;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 *
 * @version $Id$
 */
public class ModelClass
    extends BaseElement
{
    private String superClass;

    private List fields;

    private List associations;

    private Map fieldMap;

    private Map associationMap;

    private List codeSegments;

    private Model model;

    public ModelClass()
    {
        fieldMap = new HashMap();

        associationMap = new HashMap();
    }

    public Model getModel()
    {
        return model;
    }

    public String getSuperClass()
    {
        return superClass;
    }

    public List getFields()
    {
        if ( fields == null )
        {
            return Collections.EMPTY_LIST;
        }

        return fields;
    }

    public List getAssociations()
    {
        if ( associations == null )
        {
            return Collections.EMPTY_LIST;
        }

        return associations;
    }

    public List getCodeSegments()
    {
        if ( codeSegments == null )
        {
            return Collections.EMPTY_LIST;
        }

        return codeSegments;
    }

    public boolean hasSuperClass()
    {
        return ( superClass != null );
    }

    public ModelField getField( String fieldName )
    {
        ModelField field = (ModelField) fieldMap.get( fieldName );

        if ( field == null )
        {
            throw new ModelloRuntimeException( "No such field: '" + fieldName + "'." );
        }

        return field;
    }

    public ModelAssociation getAssociation( String associationName )
    {
        ModelAssociation association = (ModelAssociation) associationMap.get( associationName );

        if ( association == null )
        {
            throw new ModelloRuntimeException( "No such association: '" + associationName + "'." );
        }

        return association;
    }

    public void initialize( Model model )
    {
        this.model = model;

        for ( Iterator it = getFields().iterator(); it.hasNext(); )
        {
            ModelField modelField = (ModelField) it.next();

            modelField.initialize( this );

            fieldMap.put( modelField.getName(), modelField );
        }

        for ( Iterator it = getAssociations().iterator(); it.hasNext(); )
        {
            ModelAssociation modelAssociation = (ModelAssociation) it.next();

            modelAssociation.initialize( this );

            associationMap.put( modelAssociation.getName(), modelAssociation );
        }
    }

    public void validate()
    {
    }
}
