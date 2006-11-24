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
public class License implements java.io.Serializable {


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
     * Field comments
     */
    private String comments;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Get the description
     */
    public String getComments()
    {
        return this.comments;
    } //-- String getComments() 

    /**
     * Get The full legal name of the license.
     */
    public String getName()
    {
        return this.name;
    } //-- String getName() 

    /**
     * Get The official url for the license text.
     */
    public String getUrl()
    {
        return this.url;
    } //-- String getUrl() 

    /**
     * Set the description
     * 
     * @param comments
     */
    public void setComments(String comments)
    {
        this.comments = comments;
    } //-- void setComments(String) 

    /**
     * Set The full legal name of the license.
     * 
     * @param name
     */
    public void setName(String name)
    {
        this.name = name;
    } //-- void setName(String) 

    /**
     * Set The official url for the license text.
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
