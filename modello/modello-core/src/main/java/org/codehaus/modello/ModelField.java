package org.codehaus.modello;

import org.codehaus.modello.metadata.FieldMetadata;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 *
 * @version $Id$
 */
public class ModelField
    extends BaseElement
{
    private String type;

    private String specification;

    private String defaultValue;

    private String typeValidator;

    private boolean required;

    transient private ModelClass modelClass;

    transient private boolean primitive;

    public ModelField()
    {
        super( true );
    }

    public ModelField( ModelClass modelClass, String name )
    {
        super( true, name );

        this.modelClass = modelClass;
    }

    // ----------------------------------------------------------------------
    // Property accessors
    // ----------------------------------------------------------------------

    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue( String defaultValue )
    {
        this.defaultValue = defaultValue;
    }

    public String getSpecification()
    {
        return specification;
    }

    public void setSpecifiaction( String specification )
    {
        this.specification = specification;
    }

    public String getTypeValidator()
    {
        return typeValidator;
    }

    public void setTypeValidator( String typeValidator )
    {
        this.typeValidator = typeValidator;
    }

    public boolean isRequired()
    {
        return required;
    }

    public void setRequired( boolean required )
    {
        this.required = required;
    }

    // ----------------------------------------------------------------------
    // Misc
    // ----------------------------------------------------------------------

    public ModelClass getModelClass()
    {
        return modelClass;
    }

    /**
     * @return Returns true if this field is a java primitive.
     */
    public boolean isPrimitive()
    {
        return primitive;
    }

    public FieldMetadata getMetadata( String key )
    {
        return (FieldMetadata) getMetadata( FieldMetadata.class, key );
    }

    public void initialize( ModelClass modelClass )
    {
        this.modelClass = modelClass;
    }

    public void validateElement()
        throws ModelValidationException
    {
        validateFieldNotEmpty( "Field", "name", getName() );

        validateFieldNotEmpty( "Field " + getName(), "type", type );

        String[] primitiveTypes = new String[]{
            "boolean",
            "char",
            "short",
            "int",
            "long",
            "float",
            "double",
            "String"
        };

        for ( int i = 0; i < primitiveTypes.length; i++ )
        {
            String validType = primitiveTypes[i];

            if ( type.equals( validType ) )
            {
                primitive = true;

                return;
            }
        }

        ModelClass modelClass = getModelClass().getModel().getClass( type );

        if ( modelClass != null )
        {
            return;
        }

        throw new ModelValidationException( "Field '" + getName() + "': Illegal type: '" + type + "'." );
    }
}
