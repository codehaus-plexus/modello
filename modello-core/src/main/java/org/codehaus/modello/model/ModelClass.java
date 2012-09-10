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

import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.metadata.ClassMetadata;
import org.codehaus.plexus.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 */
public class ModelClass
    extends ModelType
{
    private String superClass;

    private boolean isInternalSuperClass;

    private List<String> interfaces;

    private List<ModelField> fields;

    private transient Map<String, List<ModelField>> fieldMap = new HashMap<String, List<ModelField>>();

    public ModelClass()
    {
        super();
    }

    public ModelClass( Model model, String name )
    {
        super( model, name );
    }

    public String getSuperClass()
    {
        return superClass;
    }

    public void setSuperClass( String superClass )
    {
        this.superClass = superClass;
    }

    // ----------------------------------------------------------------------
    // Interfaces
    // ----------------------------------------------------------------------

    /**
     * Returns the list of all interfaces of this class.
     *
     * @return Returns the list of all interfaces of this class.
     */
    public List<String> getInterfaces()
    {
        if ( interfaces == null )
        {
            interfaces = new ArrayList<String>();
        }

        return interfaces;
    }

    public void addInterface( String modelInterface )
    {
        if ( getInterfaces().contains( modelInterface ) )
        {
            throw new ModelloRuntimeException( "Duplicate interface in " + getName() + ": " + modelInterface + "." );
        }

        getInterfaces().add( modelInterface );
    }

    // ----------------------------------------------------------------------
    // Field
    // ----------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public List<ModelField> getAllFields()
    {
        if ( fields == null )
        {
            fields = new ArrayList<ModelField>();
        }

        return fields;
    }

    /**
     * Returns all the fields in this class and all super classes if withInheritedField equals to true.
     *
     * @return Returns all the fields in this class and all super classes.
     */
    public List<ModelField> getAllFields( boolean withInheritedField )
    {
        if ( ! withInheritedField )
        {
            return getAllFields();
        }

        List<ModelField> fields = new ArrayList<ModelField>( getAllFields() );

        ModelClass c = this;

        while ( c.hasSuperClass() && c.isInternalSuperClass() )
        {
            ModelClass parent = getModel().getClass( c.getSuperClass(), getVersionRange() );

            fields.addAll( parent.getAllFields() );

            c = parent;
        }

        return fields;
    }

    public ModelField getField( String type, VersionRange versionRange )
    {
        List<ModelField> fieldList = fieldMap.get( type );

        if ( fieldList != null )
        {
            for ( ModelField modelField : fieldList )
            {
                if ( versionRange.getFromVersion().inside( modelField.getVersionRange() )
                    && versionRange.getToVersion().inside( modelField.getVersionRange() ) )
                {
                    return modelField;
                }
            }
        }

        throw new ModelloRuntimeException( "There are no field '" + type + "' in version range '" + versionRange.toString() + "'." );
    }

    public void addField( ModelField modelField )
    {
        if ( fieldMap.containsKey( modelField.getName() ) )
        {
            List<ModelField> fieldList = fieldMap.get( modelField.getName() );

            for ( ModelField currentField : fieldList )
            {
                if ( VersionUtil.isInConflict( modelField.getVersionRange(), currentField.getVersionRange() ) )
                {
                    throw new ModelloRuntimeException( "Duplicate field in " + getName() + ": " + modelField.getName() + "." );
                }
            }
        }
        else
        {
            List<ModelField> fieldList = new ArrayList<ModelField>();

            fieldMap.put( modelField.getName(), fieldList );
        }

        getAllFields().add( modelField );

        fieldMap.get( modelField.getName() ).add( modelField );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public boolean hasSuperClass()
    {
        return StringUtils.isNotEmpty( superClass );
    }

    public boolean isInternalSuperClass()
    {
        return isInternalSuperClass;
    }

    public ClassMetadata getMetadata( String key )
    {
        return getMetadata( ClassMetadata.class, key );
    }

    public void initialize( Model model )
    {
        super.initialize( model );

        for ( ModelField modelField : getAllFields() )
        {
            modelField.initialize( this );
        }
    }

    public void validateElement()
        throws ModelValidationException
    {
        // Check if superClass exists
        if ( hasSuperClass() )
        {
            try
            {
                getModel().getClass( superClass, getVersionRange() );
                isInternalSuperClass = true;
            }
            catch ( ModelloRuntimeException e )
            {
                isInternalSuperClass = false;
            }
        }

        if ( getModel().getDefault( ModelDefault.CHECK_DEPRECATION ).getBoolean() )
        {
            if ( ! Version.INFINITE.equals( getVersionRange().getToVersion() )
                 && getDeprecatedVersion() == null )
            {
                throw new ModelValidationException( "You must define the deprecated version of '" + getName() + "' class." );
            }
        }
    }

    // ----------------------------------------------------------------------
    // Object Overrides
    // ----------------------------------------------------------------------

    public boolean equals( Object o )
    {
        if ( ! super.equals( o ) )
        {
            return false;
        }

        if ( !( o instanceof ModelClass ) )
        {
            return false;
        }

        ModelClass other = (ModelClass) o;

        return getPackageName().equals( other.getPackageName() );

    }

    public int hashCode()
    {
        int hashCode = getName().hashCode();

        if ( !StringUtils.isEmpty( getPackageName() ) )
        {
            hashCode += 37 * getPackageName().hashCode();
        }

        return hashCode;
    }
}
