package org.codehaus.modello;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.modello.metadata.ClassMetadata;

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

    private List codeSegments;

    private transient Model model;

    private transient Map fieldMap = new HashMap();

    private transient Map associationMap = new HashMap();

    private transient Map codeSegmentMap = new HashMap();

    public ModelClass()
    {
        super( true );
    }

    public ModelClass( Model model, String name )
    {
        super( true, name );

        this.model = model;
    }

    public String getSuperClass()
    {
        return superClass;
    }

    public void setSuperClass( String superClass )
    {
        this.superClass = superClass;
    }

    public Model getModel()
    {
        return model;
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
            fields = new ArrayList();
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

    public void addField( ModelField field )
    {
        if ( fieldMap.containsKey( field.getName() ) )
        {
            throw new ModelloRuntimeException( "Duplicate field in " + getName() + ": " + field.getName() + "." );
        }

        getFields().add( field );

        fieldMap.put( field.getName(), field );
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
            associations = new ArrayList();
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

    public void addAssociation( ModelAssociation association )
    {
        if ( associationMap.containsKey( association.getName() ) )
        {
            throw new ModelloRuntimeException( "Duplicate association in " + getName() + ": " + association.getName() + "." );
        }

        getAssociations().add( association );

        associationMap.put( association.getName(), association );
    }

    public List getCodeSegments()
    {
        if ( codeSegments == null )
        {
            codeSegments = new ArrayList();
        }

        return codeSegments;
    }

    public void addCodeSegment( CodeSegment codeSegment )
    {
        getCodeSegments().add( codeSegment );

        codeSegmentMap.put( codeSegment.getName(), codeSegment );
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

    public ClassMetadata getMetaData( String key )
    {
        return (ClassMetadata) getMetadata( ClassMetadata.class, key );
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

    public void validateElement()
        throws ModelValidationException
    {
    }
}
