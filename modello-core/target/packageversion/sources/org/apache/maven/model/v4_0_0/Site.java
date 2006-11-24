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
 *          Site contains the information needed
 *          for deploying websites.
 *       
 * 
 * @version $Revision$ $Date$
 */
public class Site implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field id
     */
    private String id;

    /**
     * Field name
     */
    private String name;

    /**
     * Field url
     */
    private String url;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Get 
     *             A unique identifier for a deployment locataion.
     *           
     */
    public String getId()
    {
        return this.id;
    } //-- String getId() 

    /**
     * Get 
     *             Human readable name of the deployment location
     *           
     */
    public String getName()
    {
        return this.name;
    } //-- String getName() 

    /**
     * Get 
     *              The url of of the location where website is
     * deployed
     *           
     */
    public String getUrl()
    {
        return this.url;
    } //-- String getUrl() 

    /**
     * Set 
     *             A unique identifier for a deployment locataion.
     *           
     * 
     * @param id
     */
    public void setId(String id)
    {
        this.id = id;
    } //-- void setId(String) 

    /**
     * Set 
     *             Human readable name of the deployment location
     *           
     * 
     * @param name
     */
    public void setName(String name)
    {
        this.name = name;
    } //-- void setName(String) 

    /**
     * Set 
     *              The url of of the location where website is
     * deployed
     *           
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
