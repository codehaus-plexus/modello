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

import org.codehaus.modello.metadata.FieldMetadata;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl </a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse </a>
 */
public class ModelField
    extends BaseElement
{
    private String type;

    private String defaultValue;

    private String typeValidator;

    private boolean required;

    private boolean identifier;

    private String alias;

    private transient ModelClass modelClass;

    private static final String[] PRIMITIVE_TYPES =
        { "boolean", "Boolean", "char", "Character", "byte", "Byte", "short", "Short", "int", "Integer", "long",
            "Long", "float", "Float", "double", "Double", "String", "Date", "DOM" };

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

    public boolean isIdentifier()
    {
        return identifier;
    }

    public void setIdentifier( boolean identifier )
    {
        this.identifier = identifier;
    }

    public String getAlias()
    {
        return alias;
    }

    public void setAlias( String alias )
    {
        this.alias = alias;
    }
    // ----------------------------------------------------------------------
    // Misc
    // ----------------------------------------------------------------------

    public ModelClass getModelClass()
    {
        return modelClass;
    }

    public FieldMetadata getMetadata( String key )
    {
        return getMetadata( FieldMetadata.class, key );
    }

    public boolean isPrimitive()
    {
        String type = getType();

        // TODO: This should not happen
        if ( type == null )
        {
            return false;
        }

        for ( int i = 0; i < PRIMITIVE_TYPES.length; i++ )
        {
            String validType = PRIMITIVE_TYPES[i];

            if ( type.equals( validType ) )
            {
                return true;
            }
        }

        return false;
    }

    public boolean isArray()
    {
        return getType().endsWith( "[]" );
    }

    public boolean isPrimitiveArray()
    {
        String type = getType();

        for ( int i = 0; i < PRIMITIVE_TYPES.length; i++ )
        {
            String validType = PRIMITIVE_TYPES[i] + "[]";

            if ( validType.equals( type ) )
            {
                return true;
            }
        }

        return false;
    }

    // ----------------------------------------------------------------------
    // BaseElement Overrides
    // ----------------------------------------------------------------------

    public void initialize( ModelClass modelClass )
    {
        this.modelClass = modelClass;

        if ( defaultValue == null )
        {
            if ( "boolean".equals( type ) )
            {
                defaultValue = "false";
            }
            else if ( "float".equals( type ) || "double".equals( type ) )
            {
                defaultValue = "0.0";
            }
            else if ( "int".equals( type ) || "long".equals( type ) || "short".equals( type ) || "byte".equals( type ) )
            {
                defaultValue = "0";
            }
            else if ( "char".equals( type ) )
            {
                defaultValue = "\0";
            }
        }
    }

    public void validateElement()
        throws ModelValidationException
    {
        validateFieldNotEmpty( "field", "name", getName() );

        validateFieldNotEmpty( "field '" + getName() + "'", "type", type );

        // TODO: these definitions are duplicated throughout. Defined centrally, and loop through in the various uses

        if ( !isPrimitive() && !isPrimitiveArray() )
        {
            throw new ModelValidationException( "Field '" + getName() + "': Illegal type: '" + type + "'." );
        }
    }

    // ----------------------------------------------------------------------
    // Object Overrides
    // ----------------------------------------------------------------------

    public String toString()
    {
        return "[Field: name=" + getName() + ", alias: " + alias + ", type: " + type + ", " + "version: "
            + getVersionRange() + "]";
    }

    public boolean isModelVersionField()
    {
        Model model = modelClass.getModel();
        VersionDefinition versionDefinition = model.getVersionDefinition();

        return ( versionDefinition != null ) && versionDefinition.isFieldType()
            && ( versionDefinition.getValue().equals( getName() ) || versionDefinition.getValue().equals( alias ) );
    }
}
