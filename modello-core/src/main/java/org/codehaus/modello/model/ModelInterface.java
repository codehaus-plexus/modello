package org.codehaus.modello.model;

/*
 * Copyright (c) 2004, Jason van Zyl
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

/**
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 *
 * @version $Id$
 */
public class ModelInterface
    extends BaseElement
{
    private String superInterface;

    private String packageName;

    private List codeSegments;

    private Model model;

    private transient Map codeSegmentMap = new HashMap();

    public ModelInterface()
    {
        super( true );
    }

    public ModelInterface( Model model, String name )
    {
        super( true, name );

        this.model = model;
    }

    public void setSuperInterface( String superInterface )
    {
        this.superInterface = superInterface;
    }

    public String getSuperInterface()
    {
        return superInterface;
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

    public void initialize( Model model )
    {
        this.model = model;

        if ( packageName == null )
        {
            packageName = model.getPackageName( false, null );
        }
    }

    public void validateElement()
        throws ModelValidationException
    {
    }
}
