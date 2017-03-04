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
import java.util.List;
import java.util.Map;

/**
 * Either a model class or interface.
 *
 * @author <a href="mailto:hboutemy@codehaus.org">Hervé Boutemy</a>
 */
public abstract class ModelType
    extends BaseElement
{
    private String packageName;

    private List<CodeSegment> codeSegments;

    private transient Model model;

    private transient Map<String, CodeSegment> codeSegmentMap = new HashMap<String, CodeSegment>();

    public ModelType()
    {
        super( true );
    }

    public ModelType( Model model, String name )
    {
        super( true, name );

        this.model = model;
    }

    public String getPackageName()
    {
        return getPackageName( false, null );
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
            catch ( Exception e )
            {
                p = ModelDefault.PACKAGE_VALUE;
            }
        }

        if ( withVersion )
        {
            p += "." + version.toString( "v", "_" );
        }

        return p;
    }

    public void setPackageName( String packageName )
    {
        this.packageName = packageName;
    }

    public Model getModel()
    {
        return model;
    }

    // ----------------------------------------------------------------------
    // CodeSegment
    // ----------------------------------------------------------------------

    public List<CodeSegment> getAllCodeSegments()
    {
        if ( codeSegments == null )
        {
            codeSegments = new ArrayList<CodeSegment>();
        }

        return codeSegments;
    }

    public List<CodeSegment> getCodeSegments( Version version )
    {
        return getCodeSegments( new VersionRange( version ) );
    }

    public List<CodeSegment> getCodeSegments( VersionRange versionRange )
    {
        List<CodeSegment> codeSegments = getAllCodeSegments();

        List<CodeSegment> codeSegmentsList = new ArrayList<CodeSegment>();

        if ( codeSegments != null )
        {
            for ( CodeSegment codeSegment : codeSegments )
            {
                if ( versionRange.getFromVersion().inside( codeSegment.getVersionRange() )
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

    // ----------------------------------------------------------------------
    // Field
    // ----------------------------------------------------------------------

    /**
     * Returns the list of all fields in this class.
     *
     * It does not include the fields of super classes.
     *
     * @return Returns the list of all fields in this class. It does not include the
     *         fields of super classes.
     */
    public abstract List<ModelField> getAllFields();

    /**
     * Returns all the fields in this class and all super classes if withInheritedField equals to true.
     *
     * @param withInheritedField whether inherited fields should be included.
     * @return Returns all the fields in this class and all super classes.
     */
    public abstract List<ModelField> getAllFields( boolean withInheritedField );

    public abstract ModelField getField( String type, VersionRange versionRange );

    /**
     * Returns the list of all fields in this class for a specific version.
     *
     * It does not include the fields of super classes.
     *
     * @param version the specific version
     * @return Returns the list of all fields in this class. It does not include the
     *         fields of super classes.
     */
    public List<ModelField> getFields( Version version )
    {
        List<ModelField> fieldList = new ArrayList<ModelField>();

        for ( ModelField currentField : getAllFields() )
        {
            if ( version.inside( currentField.getVersionRange() ) )
            {
                fieldList.add( currentField );
            }
        }

        return fieldList;
    }

    public List<ModelField> getAllFields( Version version, boolean withInheritedField )
    {
        List<ModelField> allFieldsList = new ArrayList<ModelField>();

        List<ModelField> fieldList = new ArrayList<ModelField>();

        for ( ModelField currentField : getAllFields( withInheritedField ) )
        {
            if ( version.inside( currentField.getVersionRange() ) )
            {
                allFieldsList.add( currentField );
            }
        }

        for ( ModelField currentField : allFieldsList )
        {
            if ( version.inside( currentField.getVersionRange() ) )
            {
                fieldList.add( currentField );
            }
        }

        return fieldList;
    }

    public boolean hasField( String type, Version version )
    {
        try
        {
            getField( type, new VersionRange( version ) );

            return true;
        }
        catch ( Exception e )
        {
            return false;
        }
    }

    public ModelField getField( String type, Version version )
    {
        return getField( type, new VersionRange( version ) );
    }

    public List<ModelField> getIdentifierFields( Version version )
    {
        List<ModelField> identifierFields = new ArrayList<ModelField>();

        for ( ModelField field : getFields( version ) )
        {
            if ( field.isIdentifier() )
            {
                identifierFields.add( field );
            }
        }

        return identifierFields;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void initialize( Model model )
    {
        this.model = model;

        if ( packageName == null )
        {
            packageName = model.getDefaultPackageName( false, null );
        }
    }
}
