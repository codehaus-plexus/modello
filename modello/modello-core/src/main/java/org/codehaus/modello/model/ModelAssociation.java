package org.codehaus.modello.model;

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

import org.codehaus.modello.metadata.AssociationMetadata;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class ModelAssociation
    extends ModelField
{
    public static final String ONE_MULTIPLICITY = "1";

    public static final String MANY_MULTIPLICITY = "*";

    // ----------------------------------------------------------------------
    // Configuration
    // ----------------------------------------------------------------------

    private String to;

    private String multiplicity;

    private ModelClass toClass;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /**
     * @param to The to to set.
     */
    public void setTo( String to )
    {
        this.to = to;
    }

    /**
     * @return Returns the to.
     */
    public String getTo()
    {
        return to;
    }

    public String getType()
    {
        if ( ONE_MULTIPLICITY.equals( getMultiplicity() ) )
        {
            return getTo();
        }
        else
        {
            return super.getType();
        }
    }

    /**
     * @return Returns the multiplicity.
     */
    public String getMultiplicity()
    {
        return multiplicity;
    }

    /**
     * @param multiplicity The multiplicity to set.
     */
    public void setMultiplicity( String multiplicity )
    {
        this.multiplicity = multiplicity;
    }

    /**
     * @return Returns the to ModelClass.
     */
    public ModelClass getToClass()
    {
        return toClass;
    }

    public AssociationMetadata getAssociationMetadata( String key )
    {
        return (AssociationMetadata) getMetadata( AssociationMetadata.class, key );
    }
    
    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void validateElement()
        throws ModelValidationException
    {
        validateFieldNotEmpty( "Association", "name", getName() );

        validateFieldNotEmpty( "Association '" + getName() + "'", "to", to );

        if ( isEmpty( to ) )
        {
            throw new ModelValidationException( "You must define the type of association." );
        }

        if ( !to.equals( "String" ) )
        {
            toClass = getModelClass().getModel().getClass( to, getVersionRange() );

            if ( toClass == null )
            {
                throw new ModelValidationException( "Association '" + getName() + "': Could not find to class." );
            }
        }

        if ( isEmpty( multiplicity ) )
        {
            multiplicity = "1";
        }

        if ( "n".equals( multiplicity ) )
        {
            multiplicity = "*";
        }

        if ( !ONE_MULTIPLICITY.equals( multiplicity ) && !MANY_MULTIPLICITY.equals( multiplicity ) )
        {
            throw new ModelValidationException(
                "Association multiplicity '" + getName() + "' is incorrect: Autorized values are 1, * or n." );
        }

        if ( isEmpty( getType() ) )
        {
            ModelDefault modelDefault = getModelClass().getModel().getDefault( ModelDefault.LIST );

            setType( modelDefault.getKey() );

            setDefaultValue( modelDefault.getValue() );
        }
        else
        {
            if ( !ONE_MULTIPLICITY.equals( multiplicity ) )
            {
                if ( getType().equalsIgnoreCase( "Set" ) )
                {
                    setType( ModelDefault.SET );
                }
                if ( getType().equalsIgnoreCase( "List" ) )
                {
                    setType( ModelDefault.LIST );
                }
                if ( getType().equalsIgnoreCase( "Map" ) )
                {
                    setType( ModelDefault.MAP );
                }
                if ( getType().equalsIgnoreCase( "Properties" ) )
                {
                    setType( ModelDefault.PROPERTIES );
                }
                else
                {
                    throw new ModelValidationException(
                        "The type of element '" + getName() + "' must be List, Map, Properties or Set." );
                }

                if ( isEmpty( getDefaultValue() ) )
                {
                    ModelDefault modelDefault = getModelClass().getModel().getDefault( getType() );

//                    setType( modelDefault.getKey() );

                    setDefaultValue( modelDefault.getValue() );
                }
            }
        }
    }
}
