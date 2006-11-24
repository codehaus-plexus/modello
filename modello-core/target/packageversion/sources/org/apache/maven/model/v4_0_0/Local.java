/*
 * $Id$
 */

package org.apache.maven.model.v4_0_0;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Date;

/**
 * 
 *         Local contains the information that is specific to the
 * user's
 *         local environment. This would only be expected in a user
 * or site pom,
 *         not a project POM.
 *       
 * 
 * @version $Revision$ $Date$
 */
public class Local implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field repository
     */
    private String repository;

    /**
     * Field online
     */
    private boolean online = true;

    /**
     * Field date
     */
    private java.util.Date date;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Get null
     */
    public java.util.Date getDate()
    {
        return this.date;
    } //-- java.util.Date getDate() 

    /**
     * Get 
     *             The local repository that contains downloaded
     * artifacts.
     *           
     */
    public String getRepository()
    {
        return this.repository;
    } //-- String getRepository() 

    /**
     * Get 
     *             Whether to run the build online. If not, no
     * remote repositories are consulted for plugins or
     * dependencies
     *             and this configuration may be used by other
     * plugins requiring online access.
     *           
     */
    public boolean isOnline()
    {
        return this.online;
    } //-- boolean isOnline() 

    /**
     * Set null
     * 
     * @param date
     */
    public void setDate(java.util.Date date)
    {
        this.date = date;
    } //-- void setDate(java.util.Date) 

    /**
     * Set 
     *             Whether to run the build online. If not, no
     * remote repositories are consulted for plugins or
     * dependencies
     *             and this configuration may be used by other
     * plugins requiring online access.
     *           
     * 
     * @param online
     */
    public void setOnline(boolean online)
    {
        this.online = online;
    } //-- void setOnline(boolean) 

    /**
     * Set 
     *             The local repository that contains downloaded
     * artifacts.
     *           
     * 
     * @param repository
     */
    public void setRepository(String repository)
    {
        this.repository = repository;
    } //-- void setRepository(String) 


    private String modelEncoding = "UTF-8";

    public void setModelEncoding( String modelEncoding )
    {
        this.modelEncoding = modelEncoding;
    }

    public String getModelEncoding()
    {
        return modelEncoding;
    }}
