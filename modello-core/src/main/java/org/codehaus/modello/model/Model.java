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
import org.codehaus.modello.metadata.ModelMetadata;
import org.codehaus.modello.plugin.model.ModelClassMetadata;
import org.codehaus.plexus.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 */
public class Model
    extends BaseElement
{
    private String id;

    private List<ModelClass> classes = new ArrayList<ModelClass>();

    private List<ModelDefault> defaults = new ArrayList<ModelDefault>();

    private List<ModelInterface> interfaces = new ArrayList<ModelInterface>();

    private transient Map<String, List<ModelClass>> classMap = new HashMap<String, List<ModelClass>>();

    private transient Map<String, ModelDefault> defaultMap = new HashMap<String, ModelDefault>();

    private transient Map<String, List<ModelInterface>> interfaceMap = new HashMap<String, List<ModelInterface>>();

    private VersionDefinition versionDefinition;

    public Model()
    {
        super( true );
    }

    public String getId()
    {
        return id;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    public VersionDefinition getVersionDefinition()
    {
        return versionDefinition;
    }

    public void setVersionDefinition( VersionDefinition versionDefinition )
    {
        this.versionDefinition = versionDefinition;
    }

    public ModelMetadata getMetadata( String key )
    {
        return getMetadata( ModelMetadata.class, key );
    }

    public String getRoot( Version version )
    {
        List<ModelClass> classes = getClasses( version );

        String className = null;

        for ( ModelClass currentClass : classes )
        {
            ModelClassMetadata metadata = null;

            try
            {
                metadata = (ModelClassMetadata) currentClass.getMetadata( ModelClassMetadata.ID );
            }
            catch ( Exception e )
            {
            }

            if ( metadata != null && metadata.isRootElement() )
            {
                if ( className == null )
                {
                    className = currentClass.getName();
                }
                else
                {
                    throw new ModelloRuntimeException(
                        "There are more than one class as root element for this version " + version + "." );
                }
            }
        }

        if ( className == null )
        {
            throw new ModelloRuntimeException( "There aren't root element for version " + version + "." );
        }

        return className;
    }

    /**
     * @deprecated This shouldn't be used, anything querying the model should read the
     *             package of the class. Use getDefaultPackageName(..).
     */
    public String getPackageName( boolean withVersion, Version version )
    {
        return getDefaultPackageName( withVersion, version );
    }

    public List<ModelClass> getAllClasses()
    {
        return classes;
    }

    public List<ModelClass> getClasses( Version version )
    {
        List<ModelClass> classList = new ArrayList<ModelClass>();

        for ( ModelClass currentClass : classes )
        {
            if ( version.inside( currentClass.getVersionRange() ) )
            {
                classList.add( currentClass );
            }
        }

        return classList;
    }

    public ModelClass getClass( String type, Version version, boolean optionnal )
    {
        return getClass( type, new VersionRange( version ), optionnal );
    }

    public ModelClass getClass( String type, Version version )
    {
        return getClass( type, new VersionRange( version ), false );
    }

    public ModelClass getClass( String type, VersionRange versionRange, boolean optionnal )
    {
        ModelClass value = getModelClass( type, versionRange );

        if ( value != null )
        {
            return value;
        }
        if ( optionnal )
        {
            return null;
        }
        throw new ModelloRuntimeException(
            "There is no class '" + type + "' in the version range '" + versionRange.toString() + "'." );
    }

    public ModelClass getClass( String type, VersionRange versionRange )
    {
        ModelClass value = getModelClass( type, versionRange );

        if ( value != null )
        {
            return value;
        }

        throw new ModelloRuntimeException(
            "There is no class '" + type + "' in the version range '" + versionRange.toString() + "'." );
    }

    public boolean hasClass( String type, Version version )
    {
        ModelClass value = getModelClass( type, new VersionRange( version ) );

        return value != null;
    }

    private ModelClass getModelClass( String type, VersionRange versionRange )
    {
        List<ModelClass> classList = classMap.get( type );

        ModelClass value = null;
        if ( classList != null )
        {
            for ( ModelClass modelClass : classList )
            {
                if ( versionRange.getFromVersion().inside( modelClass.getVersionRange() )
                    && versionRange.getToVersion().inside( modelClass.getVersionRange() ) )
                {
                    value = modelClass;
                }
            }
        }
        return value;
    }

    public void addClass( ModelClass modelClass )
    {
        if ( classMap.containsKey( modelClass.getName() ) )
        {
            List<ModelClass> classList = classMap.get( modelClass.getName() );

            for ( ModelClass currentClass : classList )
            {
                if ( VersionUtil.isInConflict( modelClass.getVersionRange(), currentClass.getVersionRange() ) )
                {
                    throw new ModelloRuntimeException( "Duplicate class: " + modelClass.getName() + "." );
                }
            }
        }
        else
        {
            List<ModelClass> classList = new ArrayList<ModelClass>();

            classMap.put( modelClass.getName(), classList );
        }

        getAllClasses().add( modelClass );

        classMap.get( modelClass.getName() ).add( modelClass );
    }

    // ----------------------------------------------------------------------
    // Defaults
    // ----------------------------------------------------------------------

    public List<ModelDefault> getDefaults()
    {
        return defaults;
    }

    public ModelDefault getDefault( String key )
    {
        ModelDefault modelDefault = (ModelDefault) defaultMap.get( key );

        if ( modelDefault == null )
        {
            try
            {
                modelDefault = ModelDefault.getDefault( key );
            }
            catch ( ModelValidationException mve )
            {
                throw new ModelloRuntimeException( mve.getMessage(), mve );
            }
        }

        return modelDefault;
    }

    public void addDefault( ModelDefault modelDefault )
    {
        if ( defaultMap.containsKey( modelDefault.getKey() ) )
        {
            throw new ModelloRuntimeException( "Duplicate default: " + modelDefault.getKey() + "." );
        }

        getDefaults().add( modelDefault );

        defaultMap.put( modelDefault.getKey(), modelDefault );
    }

    public String getDefaultPackageName( boolean withVersion, Version version )
    {
        String packageName = getDefault( ModelDefault.PACKAGE ).getValue();

        if ( withVersion )
        {
            packageName += "." + version.toString( "v", "_" );
        }

        return packageName;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public List<ModelInterface> getAllInterfaces()
    {
        return interfaces;
    }

    public List<ModelInterface> getInterfaces( Version version )
    {
        List<ModelInterface> interfaceList = new ArrayList<ModelInterface>();

        for ( ModelInterface currentInterface : interfaces )
        {
            if ( version.inside( currentInterface.getVersionRange() ) )
            {
                interfaceList.add( currentInterface );
            }
        }

        return interfaceList;
    }

    public ModelInterface getInterface( String type, Version version )
    {
        return getInterface( type, new VersionRange( version ) );
    }

    public ModelInterface getInterface( String type, VersionRange versionRange )
    {
        ModelInterface value = getModelInterface( type, versionRange );

        if ( value != null )
        {
            return value;
        }

        throw new ModelloRuntimeException(
            "There is no interface '" + type + "' in the version range '" + versionRange.toString() + "'." );
    }

    private ModelInterface getModelInterface( String type, VersionRange versionRange )
    {
        List<ModelInterface> interfaceList = interfaceMap.get( type );

        if ( interfaceList != null )
        {
            for ( ModelInterface modelInterface : interfaceList )
            {
                if ( versionRange.getFromVersion().inside( modelInterface.getVersionRange() )
                    && versionRange.getToVersion().inside( modelInterface.getVersionRange() ) )
                {
                    return modelInterface;
                }
            }
        }

        return null;
    }

    public void addInterface( ModelInterface modelInterface )
    {
        if ( interfaceMap.containsKey( modelInterface.getName() ) )
        {
            List<ModelInterface> interfaceList = interfaceMap.get( modelInterface.getName() );

            for ( ModelInterface currentInterface : interfaceList )
            {
                if ( VersionUtil.isInConflict( modelInterface.getVersionRange(), currentInterface.getVersionRange() ) )
                {
                    throw new ModelloRuntimeException( "Duplicate interface: " + modelInterface.getName() + "." );
                }
            }
        }
        else
        {
            List<ModelInterface> interfaceList = new ArrayList<ModelInterface>();

            interfaceMap.put( modelInterface.getName(), interfaceList );
        }

        getAllInterfaces().add( modelInterface );

        interfaceMap.get( modelInterface.getName() ).add( modelInterface );
    }

    public ModelType getType( String type, Version version )
    {
        return getType( type, new VersionRange( version ) );
    }

    public ModelType getType( String type, VersionRange versionRange )
    {
        ModelType value = getModelClass( type, versionRange );

        if ( value != null )
        {
            return value;
        }

        value = getModelInterface( type, versionRange );

        if ( value != null )
        {
            return value;
        }

        throw new ModelloRuntimeException(
            "There is no class or interface '" + type + "' in the version range '" + versionRange.toString() + "'." );
    }

    public void initialize()
    {
        for ( ModelClass modelClass : classes )
        {
            modelClass.initialize( this );
        }

        for ( ModelInterface modelInterface : interfaces )
        {
            modelInterface.initialize( this );
        }
    }

    public void validateElement()
    {
    }

    public ModelClass getLocationTracker( Version version )
    {
        List<ModelClass> modelClasses = getClasses( version );

        ModelClass locationTracker = null;

        for ( ModelClass modelClass : modelClasses )
        {
            ModelClassMetadata metadata = (ModelClassMetadata) modelClass.getMetadata( ModelClassMetadata.ID );

            if ( metadata != null && StringUtils.isNotEmpty( metadata.getLocationTracker() ) )
            {
                if ( locationTracker == null )
                {
                    locationTracker = modelClass;
                }
                else
                {
                    throw new ModelloRuntimeException(
                        "There are multiple location tracker classes (" + locationTracker.getName() + " vs. "
                            + modelClass.getName() + ") for this version " + version + "." );
                }
            }
        }

        return locationTracker;
    }

    public ModelClass getSourceTracker( Version version )
    {
        List<ModelClass> modelClasses = getClasses( version );

        ModelClass sourceTracker = null;

        for ( ModelClass modelClass : modelClasses )
        {
            ModelClassMetadata metadata = (ModelClassMetadata) modelClass.getMetadata( ModelClassMetadata.ID );

            if ( metadata != null && StringUtils.isNotEmpty( metadata.getSourceTracker() ) )
            {
                if ( sourceTracker == null )
                {
                    sourceTracker = modelClass;
                }
                else
                {
                    throw new ModelloRuntimeException(
                        "There are multiple source tracker classes (" + sourceTracker.getName() + " vs. "
                            + modelClass.getName() + ") for this version " + version + "." );
                }
            }
        }

        return sourceTracker;
    }

}
