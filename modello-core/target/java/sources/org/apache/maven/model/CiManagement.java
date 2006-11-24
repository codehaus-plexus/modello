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
public class CiManagement implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field system
     */
    private String system;

    /**
     * Field url
     */
    private String url;

    /**
     * Field nagEmailAddress
     */
    private String nagEmailAddress;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Get Email address for the party to be notified on
     * unsuccessful builds.
     */
    public String getNagEmailAddress()
    {
        return this.nagEmailAddress;
    } //-- String getNagEmailAddress() 

    /**
     * Get The name of the continuous integration system i.e.
     * Bugzilla
     */
    public String getSystem()
    {
        return this.system;
    } //-- String getSystem() 

    /**
     * Get Url for the continuous integration system use by the
     * project.
     */
    public String getUrl()
    {
        return this.url;
    } //-- String getUrl() 

    /**
     * Set Email address for the party to be notified on
     * unsuccessful builds.
     * 
     * @param nagEmailAddress
     */
    public void setNagEmailAddress(String nagEmailAddress)
    {
        this.nagEmailAddress = nagEmailAddress;
    } //-- void setNagEmailAddress(String) 

    /**
     * Set The name of the continuous integration system i.e.
     * Bugzilla
     * 
     * @param system
     */
    public void setSystem(String system)
    {
        this.system = system;
    } //-- void setSystem(String) 

    /**
     * Set Url for the continuous integration system use by the
     * project.
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
