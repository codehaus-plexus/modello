package org.codehaus.modello;

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

    public ModelField()
    {
        super( true );
    }

    public ModelField( ModelClass modelClass, String name )
    {
        super( true, name );

        this.modelClass = modelClass;
    }

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

    public ModelClass getModelClass()
    {
        return modelClass;
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

        String[] validTypes = new String[]{
            "boolean",
            "char",
            "short",
            "int",
            "long",
            "float",
            "double",
            "String"
        };

        for ( int i = 0; i < validTypes.length; i++ )
        {
            String validType = validTypes[i];

            if ( type.equals( validType ) )
            {
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
