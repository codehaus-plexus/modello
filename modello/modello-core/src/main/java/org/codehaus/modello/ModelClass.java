package org.codehaus.modello;

import java.util.ArrayList;
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

    /**
     * Returns the list of all fields in this class. 
     * 
     * It does not include the fields of super classes.
     * 
     * @return Returns the list of all fields in this class. It does not include the 
     *         fields of super classes.
     */
    public List getFields()
    {
        if ( fields == null )
        {
            return Collections.EMPTY_LIST;
        }

        return fields;
    }

    /**
     * Returns all the associations in this class and all super classes.
     * 
     * @return Returns all the associations in this class and all super classes.
     */
    public List getAllFields()
    {
        List fields = new ArrayList( getFields() );

        ModelClass c = this;

        while ( c.getSuperClass() != null )
        {
            ModelClass parent = model.getClass( c.getSuperClass() );

            fields.addAll( parent.getFields() );

            c = parent;
        }

        return fields;
    }

    /**
     * Returns the list of all associations in this class. 
     * 
     * It does not include the associations of super classes.
     * 
     * @return Returns the list of all associations in this class. It does not include the 
     *         associations of super classes.
     */
    public List getAssociations()
    {
        if ( associations == null )
        {
            return Collections.EMPTY_LIST;
        }

        return associations;
    }

    /**
     * Returns all the associations in this class and all super classes.
     * 
     * @return Returns all the associations in this class and all super classes.
     */
    public List getAllAssociations()
    {
        List associations = new ArrayList( getAssociations() );

        ModelClass c = this;

        while ( c.getSuperClass() != null )
        {
            ModelClass parent = model.getClass( c.getSuperClass() );

            associations.addAll( parent.getAssociations() );

            c = parent;
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
