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
public class PackageGroup implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field title
     */
    private String title;

    /**
     * Field packages
     */
    private String packages;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Get the description
     */
    public String getPackages()
    {
        return this.packages;
    } //-- String getPackages() 

    /**
     * Get the description
     */
    public String getTitle()
    {
        return this.title;
    } //-- String getTitle() 

    /**
     * Set the description
     * 
     * @param packages
     */
    public void setPackages(String packages)
    {
        this.packages = packages;
    } //-- void setPackages(String) 

    /**
     * Set the description
     * 
     * @param title
     */
    public void setTitle(String title)
    {
        this.title = title;
    } //-- void setTitle(String) 


    private String modelEncoding = "UTF-8";

    public void setModelEncoding( String modelEncoding )
    {
        this.modelEncoding = modelEncoding;
    }

    public String getModelEncoding()
    {
        return modelEncoding;
    }}
