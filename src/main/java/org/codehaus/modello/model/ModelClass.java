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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.metadata.ClassMetadata;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 *
 * @version $Id$
 */
public class ModelClass
    extends BaseElement
{
    private String superClass;

    private List interfaces;

    private String packageName;

    private List fields;

    private List codeSegments;

    private transient Model model;

    private transient Map fieldMap = new HashMap();

    private transient Map codeSegmentMap = new HashMap();

    public ModelClass()
    {
        super( true );
    }

    public ModelClass( Model model, String name )
    {
        super( true, name );

        this.model = model;
    }

    public String getSuperClass()
    {
        return superClass;
    }

    public void setSuperClass( String superClass )
    {
        this.superClass = superClass;
    }

    public Model getModel()
    {
        return model;
    }

    /**
     * Returns the list of all interfaces of this class. 
     * 
     * @return Returns the list of all interfaces of this class.
     */
    public List getInterfaces()
    {
        if ( interfaces == null )
        {
            interfaces = new ArrayList();
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

    public String getPackageName( boolean withVersion, Version version )
    {
        String p;

        if ( packageName != null )
        {
            p = packageName;
        }
        else
        {
            try
            {
                p = model.getDefault( ModelDefault.PACKAGE ).getValue();
            }
            catch( Exception e )
            {
                p = ModelDefault.PACKAGE_VALUE;
            }
        }

        if ( withVersion )
        {
            p += "." + version.toString();
        }

        return p;
    }

    public void setPackageName( String packageName )
    {
        this.packageName = packageName;
    }

    /**
     * Returns the list of all fields in this class. 
     * 
     * It does not include the fields of super classes.
     * 
     * @return Returns the list of all fields in this class. It does not include the 
     *         fields of super classes.
     */
    public List getAllFields()
    {
        if ( fields == null )
        {
            fields = new ArrayList();
        }

        return fields;
    }

    public List getAllFields( Version version, boolean withInheritedField )
    {
        ArrayList allFieldsList = new ArrayList();

        ArrayList fieldList = new ArrayList();

        for (Iterator i = getAllFields( withInheritedField ).iterator(); i.hasNext(); )
        {
            ModelField currentField = (ModelField) i.next();

            if ( version.inside( currentField.getVersionRange() ) )
            {
                allFieldsList.add( currentField );
            }
        }

        for (Iterator i = allFieldsList.iterator(); i.hasNext(); )
        {
            ModelField currentField = (ModelField) i.next();

            if ( version.inside( currentField.getVersionRange() ) )
            {
                fieldList.add( currentField );
            }
        }

        return fieldList;
    }

    /**
     * Returns the list of all fields in this class for a specific version. 
     * 
     * It does not include the fields of super classes.
     * 
     * @return Returns the list of all fields in this class. It does not include the 
     *         fields of super classes.
     */
    public List getFields( Version version )
    {
        ArrayList fieldList = new ArrayList();

        for (Iterator i = getAllFields().iterator(); i.hasNext(); )
        {
            ModelField currentField = (ModelField) i.next();

            if ( version.inside( currentField.getVersionRange() ) )
            {
                fieldList.add( currentField );
            }
        }

        return fieldList;
    }

    /**
     * Returns all the fields in this class and all super classes if withInheritedField equals to true.
     * 
     * @return Returns all the fields in this class and all super classes.
     */
    public List getAllFields( boolean withInheritedField )
    {
        if ( ! withInheritedField )
        {
            return getAllFields();
        }
        
        List fields = new ArrayList( getAllFields() );

        ModelClass c = this;

        while ( c.getSuperClass() != null )
        {
            ModelClass parent = model.getClass( c.getSuperClass(), getVersionRange() );

            fields.addAll( parent.getAllFields() );

            c = parent;
        }

        return fields;
    }

    public ModelField getField( String type, Version version )
    {
        return getField( type, new VersionRange( version.getMajor() + "." + version.getMinor() + "." + version.getMicro() ) );
    }

    public ModelField getField( String type, VersionRange versionRange )
    {
        ArrayList fieldList = (ArrayList) fieldMap.get( type );
        
        if ( fieldList != null )
        {
            for (Iterator i = fieldList.iterator(); i.hasNext(); )
            {
                ModelField modelField = (ModelField) i.next();

                if (  versionRange.getFromVersion().inside( modelField.getVersionRange() )
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
            ArrayList fieldList = (ArrayList) fieldMap.get( modelField.getName() );

            for (Iterator i = fieldList.iterator(); i.hasNext(); )
            {
                ModelField currentField = (ModelField) i.next();

                if ( VersionUtil.isInConflict( modelField.getVersionRange(), currentField.getVersionRange() ) )
                {
                    throw new ModelloRuntimeException( "Duplicate field in " + getName() + ": " + modelField.getName() + "." );
                }
            }
        }
        else
        {
            ArrayList fieldList = new ArrayList();
        
            fieldMap.put( modelField.getName(), fieldList );
        }

        getAllFields().add( modelField );

        ( (ArrayList) fieldMap.get( modelField.getName() ) ).add( modelField );
    }

    public List getAllCodeSegments()
    {
        if ( codeSegments == null )
        {
            codeSegments = new ArrayList();
        }

        return codeSegments;
    }

    public List getCodeSegments( Version version )
    {
        return getCodeSegments( new VersionRange( version.getMajor() + "." + version.getMinor() + "." + version.getMicro() ) );
    }

    public List getCodeSegments( VersionRange versionRange )
    {
        ArrayList codeSegments = (ArrayList) getAllCodeSegments();

        ArrayList codeSegmentsList = new ArrayList();

        if ( codeSegments != null )
        {
            for (Iterator i = codeSegments.iterator(); i.hasNext(); )
            {
                CodeSegment codeSegment = (CodeSegment) i.next();

                if (  versionRange.getFromVersion().inside( codeSegment.getVersionRange() )
                    && versionRange.getToVersion().inside( codeSegment.getVersionRange() ) )
                {
                    codeSegmentsList.add( codeSegment );
                }
            }
        }

        return codeSegmentsList;
    }

    public void addCodeSegment( CodeSegment codeSegment )
    {
        getAllCodeSegments().add( codeSegment );

        codeSegmentMap.put( codeSegment.getName(), codeSegment );
    }

    public boolean hasSuperClass()
    {
        return ( superClass != null );
    }

    public ClassMetadata getMetadata( String key )
    {
        return (ClassMetadata) getMetadata( ClassMetadata.class, key );
    }

    public void initialize( Model model )
    {
        this.model = model;

        if ( packageName == null )
        {
            packageName = model.getPackageName( false, null );
        }

        for ( Iterator it = getAllFields().iterator(); it.hasNext(); )
        {
            Object field = it.next();

            if ( field instanceof ModelAssociation )
            {
                ModelAssociation modelAssociation = (ModelAssociation) field;

                modelAssociation.initialize( this );
            }
            else
            {
                ModelField modelField = (ModelField) field;

                modelField.initialize( this );
            }
        }
    }

    public void validateElement()
        throws ModelValidationException
    {
        // Check if superClass exists
        if ( ! isEmpty( superClass ) )
        {
            model.getClass( superClass, getVersionRange() );
        }

        if ( model.getDefault( ModelDefault.CHECK_DEPRECATION ).getBoolean() )
        {
            if ( ! Version.INFINITE.equals( getVersionRange().getToVersion() ) &&
                getDeprecatedVersion() == null)
            {
                throw new ModelValidationException( "You must define the deprecated version of '" + getName() + "' class." );
            }
        }
    }
}
