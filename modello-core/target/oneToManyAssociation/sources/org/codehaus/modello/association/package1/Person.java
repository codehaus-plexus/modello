/*
 * $Id$
 */

package org.codehaus.modello.association.package1;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Date;
import org.codehaus.modello.association.package2.Location;

/**
 * null
 * 
 * @version $Revision$ $Date$
 */
public class Person implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field location
     */
    private Location location;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method breakLocationAssociation
     * 
     * @param location
     */
    public void breakLocationAssociation(Location location)
    {
        if ( this.location != location )
        {
            throw new IllegalStateException( "location isn't associated." );
        }
        
        this.location = null;
    } //-- void breakLocationAssociation(Location) 

    /**
     * Method createLocationAssociation
     * 
     * @param location
     */
    public void createLocationAssociation(Location location)
    {
        if ( this.location != null )
        {
            breakLocationAssociation( this.location );
        }
        
        this.location = location;
    } //-- void createLocationAssociation(Location) 

    /**
     * Get null
     */
    public Location getLocation()
    {
        return this.location;
    } //-- Location getLocation() 

    /**
     * Set null
     * 
     * @param location
     */
    public void setLocation(Location location)
    {
        if ( this.location != null )
        {
            this.location.breakPersonAssociation( this );
        }
        
        this.location = location;
        
        if ( location != null )
        {
            this.location.createPersonAssociation( this );
        }
    } //-- void setLocation(Location) 


    private String modelEncoding = "UTF-8";

    public void setModelEncoding( String modelEncoding )
    {
        this.modelEncoding = modelEncoding;
    }

    public String getModelEncoding()
    {
        return modelEncoding;
    }}
