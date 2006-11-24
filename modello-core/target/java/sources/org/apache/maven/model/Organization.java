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
public class Organization implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field name
     */
    private String name;

    /**
     * Field url
     */
    private String url;

    /**
     * Field logo
     */
    private String logo;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Get 
     *             The URL to the organization's logo image.  This
     * can be an URL relative
     *             to the base directory of the generated web site,
     *             (e.g.,
     *             <code>/images/org-logo.png</code>) or an
     * absolute URL
     *             (e.g.,
     *             <code>http://my.corp/logo.png</code>).  This
     * value is used
     *             when generating the project documentation.
     *           
     */
    public String getLogo()
    {
        return this.logo;
    } //-- String getLogo() 

    /**
     * Get The full name of the organization.
     */
    public String getName()
    {
        return this.name;
    } //-- String getName() 

    /**
     * Get The URL to the organization's home page.
     */
    public String getUrl()
    {
        return this.url;
    } //-- String getUrl() 

    /**
     * Set 
     *             The URL to the organization's logo image.  This
     * can be an URL relative
     *             to the base directory of the generated web site,
     *             (e.g.,
     *             <code>/images/org-logo.png</code>) or an
     * absolute URL
     *             (e.g.,
     *             <code>http://my.corp/logo.png</code>).  This
     * value is used
     *             when generating the project documentation.
     *           
     * 
     * @param logo
     */
    public void setLogo(String logo)
    {
        this.logo = logo;
    } //-- void setLogo(String) 

    /**
     * Set The full name of the organization.
     * 
     * @param name
     */
    public void setName(String name)
    {
        this.name = name;
    } //-- void setName(String) 

    /**
     * Set The URL to the organization's home page.
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
