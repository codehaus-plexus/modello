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

    private List/*<ModelClass>*/ classes = new ArrayList/*<ModelClass>*/();

    private List/*<ModelDefault>*/ defaults = new ArrayList/*<ModelDefault>*/();

    private List/*<ModelInterface>*/ interfaces = new ArrayList/*<ModelInterface>*/();

    private transient Map/*<String,List<ModelClass>>*/ classMap = new HashMap/*<String,List<ModelClass>>*/();

    private transient Map/*<String,ModelDefault>*/ defaultMap = new HashMap/*<String,ModelDefault>*/();

    private transient Map/*<String,List<ModelInterface>>*/ interfaceMap = new HashMap/*<String,list<ModelInterface>>*/();

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

    public List/*<ModelClass>*/ getClasses( Version version )
    {
        List classList = new ArrayList();

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
            "There is no class '" + type + "' in the version range '" + versionRange.toString() + "'." );
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
            List classList = (List) classMap.get( modelClass.getName() );

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
            List classList = new ArrayList();

            classMap.put( modelClass.getName(), classList );
        }

        getAllClasses().add( modelClass );

        ( (List) classMap.get( modelClass.getName() ) ).add( modelClass );
    }

    // ----------------------------------------------------------------------
    // Defaults
    // ----------------------------------------------------------------------

    public List/*<ModelDefault>*/ getDefaults()
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
                throw new ModelloRuntimeException( mve.getMessage(), mve);
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

    public List getAllInterfaces()
    {
        return interfaces;
    }

    public List getInterfaces( Version version )
    {
        List interfaceList = new ArrayList();

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
        List interfaceList = (List) interfaceMap.get( type );

        if ( interfaceList != null )
        {
            for ( Iterator i = interfaceList.iterator(); i.hasNext(); )
            {
                ModelInterface modelInterface = (ModelInterface) i.next();

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
            List interfaceList = (List) interfaceMap.get( modelInterface.getName() );

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
            List interfaceList = new ArrayList();

            interfaceMap.put( modelInterface.getName(), interfaceList );
        }

        getAllInterfaces().add( modelInterface );

        ( (List) interfaceMap.get( modelInterface.getName() ) ).add( modelInterface );
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
