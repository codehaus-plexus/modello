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

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public ModelAssociation()
    {
        super( true );
    }

    /**
     * @param to The to to set.
     */
    public void setTo( String to )
    {
        this.to = to;
    }

    /**
     * @return Returns the fromRole.
     */
    public String getFromRole()
    {
        return fromRole;
    }

    /**
     * @param fromRole The fromRole to set.
     */
    public void setFromRole( String fromRole )
    {
        this.fromRole = fromRole;
    }

    /**
     * @return Returns the toRole.
     */
    public String getToRole()
    {
        return toRole;
    }

    /**
     * @param toRole The toRole to set.
     */
    public void setToRole( String toRole )
    {
        this.toRole = toRole;
    }

    /**
     * @return Returns the fromMultiplicity.
     */
    public String getFromMultiplicity()
    {
        return fromMultiplicity;
    }

    /**
     * @param fromMultiplicity The fromMultiplicity to set.
     */
    public void setFromMultiplicity( String fromMultiplicity )
    {
        this.fromMultiplicity = fromMultiplicity;
    }

    /**
     * @return Returns the toMultiplicity.
     */
    public String getToMultiplicity()
    {
        return toMultiplicity;
    }

    /**
     * @param toMultiplicity The toMultiplicity to set.
     */
    public void setToMultiplicity( String toMultiplicity )
    {
        this.toMultiplicity = toMultiplicity;
    }

    /**
     * @return Returns the associationClass.
     */
    public String getAssociationClass()
    {
        return associationClass;
    }

    /**
     * @param associationClass The associationClass to set.
     */
    public void setAssociationClass( String associationClass )
    {
        this.associationClass = associationClass;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /**
     * @return Returns the fromClass.
     */
    public ModelClass getFromClass()
    {
        return fromClass;
    }

    /**
     * @return Returns the toClass.
     */
    public ModelClass getToClass()
    {
        return toClass;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void initialize( ModelClass modelClass )
    {
        this.fromClass = modelClass;
    }

    public void validateElement()
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
            fromRole = getName();
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
