package org.codehaus.modello;

/*
 * LICENSE
 */

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ModelAssociation
    extends BaseElement
{
    // ----------------------------------------------------------------------
    // Configuration
    // ----------------------------------------------------------------------

    private String name;

    private String to;

    private String fromRole;

    private String toRole;

    private String fromMultiplicity;

    private String toMultiplicity;

    /**
     * This class is used as a mapping when the association is a many-to-many 
     * relation.
     */
    private String associationClass;

    // Adding this now is overkill for the moment.
//    private String aggregationType;

    // ----------------------------------------------------------------------
    // Other
    // ----------------------------------------------------------------------

    private ModelClass fromClass;

    private ModelClass toClass;

    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return Returns the to.
     *//*
    public String getTo()
    {
        return to;
    }*/

    /**
     * @return Returns the fromRole.
     */
    public String getFromRole()
    {
        return fromRole;
    }

    /**
     * @return Returns the toRole.
     */
    public String getToRole()
    {
        return toRole;
    }

    /**
     * @return Returns the fromMultiplicity.
     */
    public String getFromMultiplicity()
    {
        return fromMultiplicity;
    }

    /**
     * @return Returns the toMultiplicity.
     */
    public String getToMultiplicity()
    {
        return toMultiplicity;
    }

    /**
     * @return Returns the associationClass.
     */
    public String getAssociationClass()
    {
        return associationClass;
    }

    /**
     * @return Returns the toClass.
     */
    public ModelClass getToClass()
    {
        return toClass;
    }

    /**
     * @return Returns the fromClass.
     */
    public ModelClass getFromClass()
    {
        return fromClass;
    }

    public void initialize( ModelClass modelClass )
    {
        this.fromClass = modelClass;
    }

    public void validate()
        throws ModelValidationException
    {
        validateFieldNotEmpty( "Association", "name", getName() );

        validateFieldNotEmpty( "Association '" + getName() + "'", "to", to );

        if ( !to.equals( "String" ) )
        {
            toClass = fromClass.getModel().getClass( to );
    
            if ( toClass == null )
            {
                throw new ModelValidationException( "Association '" + getName() + "': Could not find to class." );
            }
        }

        if ( isEmpty( fromRole ) )
        {
            fromRole = name;
        }

        if ( isEmpty( fromMultiplicity ) )
        {
            fromMultiplicity = "1";
        }

        if ( isEmpty( toMultiplicity ) )
        {
            toMultiplicity = "*";
        }

        // TODO: assert the multiplicities
    }
}
