package org.codehaus.modello;

/*
 * Copyright (c) 2004, Jason van Zyl
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
