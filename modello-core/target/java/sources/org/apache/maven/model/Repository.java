/*
 * $Id$
 */

package org.apache.maven.model;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Date;

/**
 * 
 *          Repository contains the information needed
 *          for establishing connections with remote repoistory
 *       
 * 
 * @version $Revision$ $Date$
 */
public class Repository implements java.io.Serializable {


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
     *             A unique identifier for a repository.
     *           
     */
    public String getId()
    {
        return this.id;
    } //-- String getId() 

    /**
     * Get 
     *             Human readable name of the repository
     *           
     */
    public String getName()
    {
        return this.name;
    } //-- String getName() 

    /**
     * Get 
     *              The url of of the repository
     *           
     */
    public String getUrl()
    {
        return this.url;
    } //-- String getUrl() 

    /**
     * Set 
     *             A unique identifier for a repository.
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
     *             Human readable name of the repository
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
     *              The url of of the repository
     *           
     * 
     * @param url
     */
    public void setUrl(String url)
    {
        this.url = url;
    } //-- void setUrl(String) 


            public boolean equals( Object obj )
            {
            Repository other = ( Repository ) obj;

            boolean retValue = false;

            if ( id != null )
            {
            retValue = id.equals( other.id );
            }

            return retValue;
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
