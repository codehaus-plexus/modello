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
public class Contributor implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field name
     */
    private String name;

    /**
     * Field email
     */
    private String email;

    /**
     * Field url
     */
    private String url;

    /**
     * Field organization
     */
    private String organization;

    /**
     * Field roles
     */
    private java.util.List roles;

    /**
     * Field timezone
     */
    private String timezone;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addRole
     * 
     * @param string
     */
    public void addRole(String string)
    {
        if ( !(string instanceof String) )
        {
            throw new ClassCastException( "Contributor.addRoles(string) parameter must be instanceof " + String.class.getName() );
        }
        getRoles().add( string );
    } //-- void addRole(String) 

    /**
     * Get The email address of the contributor.
     */
    public String getEmail()
    {
        return this.email;
    } //-- String getEmail() 

    /**
     * Get The full name of the contributor.
     */
    public String getName()
    {
        return this.name;
    } //-- String getName() 

    /**
     * Get The organization to which the contributor belongs.
     */
    public String getOrganization()
    {
        return this.organization;
    } //-- String getOrganization() 

    /**
     * Method getRoles
     */
    public java.util.List getRoles()
    {
        if ( this.roles == null )
        {
            this.roles = new java.util.ArrayList();
        }
        
        return this.roles;
    } //-- java.util.List getRoles() 

    /**
     * Get 
     *             The timezone the contributor is in. This is a
     * number in the range -14 to 14.
     *           
     */
    public String getTimezone()
    {
        return this.timezone;
    } //-- String getTimezone() 

    /**
     * Get The URL for the homepage of the contributor.
     */
    public String getUrl()
    {
        return this.url;
    } //-- String getUrl() 

    /**
     * Method removeRole
     * 
     * @param string
     */
    public void removeRole(String string)
    {
        if ( !(string instanceof String) )
        {
            throw new ClassCastException( "Contributor.removeRoles(string) parameter must be instanceof " + String.class.getName() );
        }
        getRoles().remove( string );
    } //-- void removeRole(String) 

    /**
     * Set The email address of the contributor.
     * 
     * @param email
     */
    public void setEmail(String email)
    {
        this.email = email;
    } //-- void setEmail(String) 

    /**
     * Set The full name of the contributor.
     * 
     * @param name
     */
    public void setName(String name)
    {
        this.name = name;
    } //-- void setName(String) 

    /**
     * Set The organization to which the contributor belongs.
     * 
     * @param organization
     */
    public void setOrganization(String organization)
    {
        this.organization = organization;
    } //-- void setOrganization(String) 

    /**
     * Set 
     *             The roles the contributor plays in the project. 
     * Each role is
     *             describe by a
     *             <code>role</code> element, the body of which is
     * a
     *             role name.
     *           
     * 
     * @param roles
     */
    public void setRoles(java.util.List roles)
    {
        this.roles = roles;
    } //-- void setRoles(java.util.List) 

    /**
     * Set 
     *             The timezone the contributor is in. This is a
     * number in the range -14 to 14.
     *           
     * 
     * @param timezone
     */
    public void setTimezone(String timezone)
    {
        this.timezone = timezone;
    } //-- void setTimezone(String) 

    /**
     * Set The URL for the homepage of the contributor.
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
