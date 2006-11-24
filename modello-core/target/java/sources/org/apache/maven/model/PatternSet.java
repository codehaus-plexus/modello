/*
 * $Id$
 */

package org.apache.maven.model;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Date;

/**
 * null
 * 
 * @version $Revision$ $Date$
 */
public class PatternSet implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field includes
     */
    private java.util.List includes;

    /**
     * Field excludes
     */
    private java.util.List excludes;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addExclude
     * 
     * @param string
     */
    public void addExclude(String string)
    {
        if ( !(string instanceof String) )
        {
            throw new ClassCastException( "PatternSet.addExcludes(string) parameter must be instanceof " + String.class.getName() );
        }
        getExcludes().add( string );
    } //-- void addExclude(String) 

    /**
     * Method addInclude
     * 
     * @param string
     */
    public void addInclude(String string)
    {
        if ( !(string instanceof String) )
        {
            throw new ClassCastException( "PatternSet.addIncludes(string) parameter must be instanceof " + String.class.getName() );
        }
        getIncludes().add( string );
    } //-- void addInclude(String) 

    /**
     * Method getExcludes
     */
    public java.util.List getExcludes()
    {
        if ( this.excludes == null )
        {
            this.excludes = new java.util.ArrayList();
        }
        
        return this.excludes;
    } //-- java.util.List getExcludes() 

    /**
     * Method getIncludes
     */
    public java.util.List getIncludes()
    {
        if ( this.includes == null )
        {
            this.includes = new java.util.ArrayList();
        }
        
        return this.includes;
    } //-- java.util.List getIncludes() 

    /**
     * Method removeExclude
     * 
     * @param string
     */
    public void removeExclude(String string)
    {
        if ( !(string instanceof String) )
        {
            throw new ClassCastException( "PatternSet.removeExcludes(string) parameter must be instanceof " + String.class.getName() );
        }
        getExcludes().remove( string );
    } //-- void removeExclude(String) 

    /**
     * Method removeInclude
     * 
     * @param string
     */
    public void removeInclude(String string)
    {
        if ( !(string instanceof String) )
        {
            throw new ClassCastException( "PatternSet.removeIncludes(string) parameter must be instanceof " + String.class.getName() );
        }
        getIncludes().remove( string );
    } //-- void removeInclude(String) 

    /**
     * Set the description
     * 
     * @param excludes
     */
    public void setExcludes(java.util.List excludes)
    {
        this.excludes = excludes;
    } //-- void setExcludes(java.util.List) 

    /**
     * Set the description
     * 
     * @param includes
     */
    public void setIncludes(java.util.List includes)
    {
        this.includes = includes;
    } //-- void setIncludes(java.util.List) 


            public java.util.List getDefaultExcludes()
            {
            java.util.List defaultExcludes = new java.util.ArrayList();
            defaultExcludes.add( "**/*~" );
            defaultExcludes.add( "**/#*#" );
            defaultExcludes.add( "**/.#*" );
            defaultExcludes.add( "**/%*%" );
            defaultExcludes.add( "**/._*" );

            // CVS
            defaultExcludes.add( "**/CVS" );
            defaultExcludes.add( "**/CVS/**" );
            defaultExcludes.add( "**/.cvsignore" );

            // SCCS
            defaultExcludes.add( "**/SCCS" );
            defaultExcludes.add( "**/SCCS/**" );

            // Visual SourceSafe
            defaultExcludes.add( "**/vssver.scc" );

            // Subversion
            defaultExcludes.add( "**/.svn" );
            defaultExcludes.add( "**/.svn/**" );

            // Mac
            defaultExcludes.add( "**/.DS_Store" );
            return defaultExcludes;
            }
          
    private String modelEncoding = "UTF-8";

    public void setModelEncoding( String modelEncoding )
    {
        this.modelEncoding = modelEncoding;
    }

    public String getModelEncoding()
    {
        return modelEncoding;
    }}
