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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class Model
    extends BaseElement
{
    private String id;

    private List classes = new ArrayList();

    private List defaults = new ArrayList();

    private List interfaces = new ArrayList();

    private transient Map classMap = new HashMap();

    private transient Map defaultMap = new HashMap();

    private transient Map interfaceMap = new HashMap();

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
        return (ModelMetadata) getMetadata( ModelMetadata.class, key );
    }

    public String getRoot( Version version )
    {
        List classes = getClasses( version );

        String className = null;

        for ( Iterator i = classes.iterator(); i.hasNext(); )
        {
            ModelClass currentClass = (ModelClass) i.next();

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

    public List getAllClasses()
    {
        return classes;
    }

    public List getClasses( Version version )
    {
        ArrayList classList = new ArrayList();

        for ( Iterator i = classes.iterator(); i.hasNext(); )
        {
            ModelClass currentClass = (ModelClass) i.next();

            if ( version.inside( currentClass.getVersionRange() ) )
            {
                classList.add( currentClass );
            }
        }

        return classList;
    }

    public ModelClass getClass( String type, Version version )
    {
        return getClass( type, new VersionRange( version ) );
    }

    public ModelClass getClass( String type, VersionRange versionRange )
    {
        ModelClass value = getModelClass( type, versionRange );

        if ( value != null )
        {
            return value;
        }

        throw new ModelloRuntimeException(
            "There are no class '" + type + "' in version range '" + versionRange.toString() + "'." );
    }

    public boolean hasClass( String type, Version version )
    {
        ModelClass value = getModelClass( type, new VersionRange( version ) );

        return value != null;
    }

    private ModelClass getModelClass( String type, VersionRange versionRange )
    {
        ArrayList classList = (ArrayList) classMap.get( type );

        ModelClass value = null;
        if ( classList != null )
        {
            for ( Iterator i = classList.iterator(); i.hasNext() && value == null; )
            {
                ModelClass modelClass = (ModelClass) i.next();

                if ( versionRange.getFromVersion().inside( modelClass.getVersionRange() ) &&
                    versionRange.getToVersion().inside( modelClass.getVersionRange() ) )
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
            ArrayList classList = (ArrayList) classMap.get( modelClass.getName() );

            for ( Iterator i = classList.iterator(); i.hasNext(); )
            {
                ModelClass currentClass = (ModelClass) i.next();

                if ( VersionUtil.isInConflict( modelClass.getVersionRange(), currentClass.getVersionRange() ) )
                {
                    throw new ModelloRuntimeException( "Duplicate class: " + modelClass.getName() + "." );
                }
            }
        }
        else
        {
            ArrayList classList = new ArrayList();

            classMap.put( modelClass.getName(), classList );
        }

        getAllClasses().add( modelClass );

        ( (ArrayList) classMap.get( modelClass.getName() ) ).add( modelClass );
    }

    // ----------------------------------------------------------------------
    // Defaults
    // ----------------------------------------------------------------------

    public List getDefaults()
    {
        return defaults;
    }

    public ModelDefault getDefault( String key )
        throws ModelValidationException
    {
        ModelDefault modelDefault = (ModelDefault) defaultMap.get( key );

        if ( modelDefault == null )
        {
            modelDefault = ModelDefault.getDefault( key );
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
        String packageName;

        try
        {
            packageName = getDefault( ModelDefault.PACKAGE ).getValue();
        }
        catch ( ModelValidationException mve )
        {
            packageName = ModelDefault.PACKAGE_VALUE;
        }

        if ( withVersion )
        {
            packageName += "." + version.toString( "v", "_" );
        }

        return packageName;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public List getAllInterfaces()
    {
        return interfaces;
    }

    public List getInterfaces( Version version )
    {
        ArrayList interfaceList = new ArrayList();

        for ( Iterator i = interfaces.iterator(); i.hasNext(); )
        {
            ModelInterface currentInterface = (ModelInterface) i.next();

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
        ArrayList interfaceList = (ArrayList) interfaceMap.get( type );

        if ( interfaceList != null )
        {
            for ( Iterator i = interfaceList.iterator(); i.hasNext(); )
            {
                ModelInterface modelInterface = (ModelInterface) i.next();

                if ( versionRange.getFromVersion().inside( modelInterface.getVersionRange() ) &&
                    versionRange.getToVersion().inside( modelInterface.getVersionRange() ) )
                {
                    return modelInterface;
                }
            }
        }

        throw new ModelloRuntimeException(
            "There are no interface '" + type + "' in version range '" + versionRange.toString() + "'." );
    }

    public void addInterface( ModelInterface modelInterface )
    {
        if ( interfaceMap.containsKey( modelInterface.getName() ) )
        {
            ArrayList interfaceList = (ArrayList) interfaceMap.get( modelInterface.getName() );

            for ( Iterator i = interfaceList.iterator(); i.hasNext(); )
            {
                ModelInterface currentInterface = (ModelInterface) i.next();

                if ( VersionUtil.isInConflict( modelInterface.getVersionRange(), currentInterface.getVersionRange() ) )
                {
                    throw new ModelloRuntimeException( "Duplicate interface: " + modelInterface.getName() + "." );
                }
            }
        }
        else
        {
            ArrayList interfaceList = new ArrayList();

            interfaceMap.put( modelInterface.getName(), interfaceList );
        }

        getAllInterfaces().add( modelInterface );

        ( (ArrayList) interfaceMap.get( modelInterface.getName() ) ).add( modelInterface );
    }

    public void initialize()
    {
        for ( Iterator i = classes.iterator(); i.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) i.next();

            modelClass.initialize( this );
        }

        for ( Iterator i = interfaces.iterator(); i.hasNext(); )
        {
            ModelInterface modelInterface = (ModelInterface) i.next();

            modelInterface.initialize( this );
        }
    }

    public void validateElement()
    {
    }
}
