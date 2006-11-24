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
public class UnitTest extends PatternSet 
implements java.io.Serializable
{


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field resources
     */
    private java.util.List resources;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addResource
     * 
     * @param resource
     */
    public void addResource(Resource resource)
    {
        if ( !(resource instanceof Resource) )
        {
            throw new ClassCastException( "UnitTest.addResources(resource) parameter must be instanceof " + Resource.class.getName() );
        }
        getResources().add( resource );
    } //-- void addResource(Resource) 

    /**
     * Method getResources
     */
    public java.util.List getResources()
    {
        if ( this.resources == null )
        {
            this.resources = new java.util.ArrayList();
        }
        
        return this.resources;
    } //-- java.util.List getResources() 

    /**
     * Method removeResource
     * 
     * @param resource
     */
    public void removeResource(Resource resource)
    {
        if ( !(resource instanceof Resource) )
        {
            throw new ClassCastException( "UnitTest.removeResources(resource) parameter must be instanceof " + Resource.class.getName() );
        }
        getResources().remove( resource );
    } //-- void removeResource(Resource) 

    /**
     * Set the description
     * 
     * @param resources
     */
    public void setResources(java.util.List resources)
    {
        this.resources = resources;
    } //-- void setResources(java.util.List) 


    private String modelEncoding = "UTF-8";

    public void setModelEncoding( String modelEncoding )
    {
        this.modelEncoding = modelEncoding;
    }

    public String getModelEncoding()
    {
        return modelEncoding;
    }}
