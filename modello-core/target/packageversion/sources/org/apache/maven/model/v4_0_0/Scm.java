/*
 * $Id$
 */

package org.apache.maven.model.v4_0_0;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Date;

/**
 * null
 * 
 * @version $Revision$ $Date$
 */
public class Scm implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field connection
     */
    private String connection;

    /**
     * Field developerConnection
     */
    private String developerConnection;

    /**
     * Field url
     */
    private String url;

    /**
     * Field branches
     */
    private java.util.List branches;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addBranch
     * 
     * @param string
     */
    public void addBranch(String string)
    {
        if ( !(string instanceof String) )
        {
            throw new ClassCastException( "Scm.addBranches(string) parameter must be instanceof " + String.class.getName() );
        }
        getBranches().add( string );
    } //-- void addBranch(String) 

    /**
     * Method getBranches
     */
    public java.util.List getBranches()
    {
        if ( this.branches == null )
        {
            this.branches = new java.util.ArrayList();
        }
        
        return this.branches;
    } //-- java.util.List getBranches() 

    /**
     * Get 
     *             The source configuration management system URL
     *             that describes the repository and how to connect
     * to the
     *             repository.  This is used by Maven when
     *             <a
     *               href="plugins/dist/index.html">building
     * versions</a>
     *             from specific ID.
     *           
     */
    public String getConnection()
    {
        return this.connection;
    } //-- String getConnection() 

    /**
     * Get 
     *             Just like connection, but for developers, i.e.
     * this scm connection
     *             will not be read only.
     *           
     */
    public String getDeveloperConnection()
    {
        return this.developerConnection;
    } //-- String getDeveloperConnection() 

    /**
     * Get The URL to the project's browsable CVS repository.
     */
    public String getUrl()
    {
        return this.url;
    } //-- String getUrl() 

    /**
     * Method removeBranch
     * 
     * @param string
     */
    public void removeBranch(String string)
    {
        if ( !(string instanceof String) )
        {
            throw new ClassCastException( "Scm.removeBranches(string) parameter must be instanceof " + String.class.getName() );
        }
        getBranches().remove( string );
    } //-- void removeBranch(String) 

    /**
     * Set 
     *             The SCM branches that are currently active for
     * the project. These should only be those forked from the
     * current branch or trunk that are intended to be used.
     * 
     * @param branches
     */
    public void setBranches(java.util.List branches)
    {
        this.branches = branches;
    } //-- void setBranches(java.util.List) 

    /**
     * Set 
     *             The source configuration management system URL
     *             that describes the repository and how to connect
     * to the
     *             repository.  This is used by Maven when
     *             <a
     *               href="plugins/dist/index.html">building
     * versions</a>
     *             from specific ID.
     *           
     * 
     * @param connection
     */
    public void setConnection(String connection)
    {
        this.connection = connection;
    } //-- void setConnection(String) 

    /**
     * Set 
     *             Just like connection, but for developers, i.e.
     * this scm connection
     *             will not be read only.
     *           
     * 
     * @param developerConnection
     */
    public void setDeveloperConnection(String developerConnection)
    {
        this.developerConnection = developerConnection;
    } //-- void setDeveloperConnection(String) 

    /**
     * Set The URL to the project's browsable CVS repository.
     * 
     * @param url
     */
    public void setUrl(String url)
    {
        this.url = url;
    } //-- void setUrl(String) 


    private String modelEncoding = "UTF-8";

    public void setModelEncoding( String modelEncoding )
    {
        this.modelEncoding = modelEncoding;
    }

    public String getModelEncoding()
    {
        return modelEncoding;
    }}
