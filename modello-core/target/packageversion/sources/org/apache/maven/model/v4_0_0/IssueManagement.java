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
public class IssueManagement implements java.io.Serializable {


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


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Get The name of the issue management system i.e. Bugzilla
     */
    public String getSystem()
    {
        return this.system;
    } //-- String getSystem() 

    /**
     * Get Url for the issue management system use by the project.
     */
    public String getUrl()
    {
        return this.url;
    } //-- String getUrl() 

    /**
     * Set The name of the issue management system i.e. Bugzilla
     * 
     * @param system
     */
    public void setSystem(String system)
    {
        this.system = system;
    } //-- void setSystem(String) 

    /**
     * Set Url for the issue management system use by the project.
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
