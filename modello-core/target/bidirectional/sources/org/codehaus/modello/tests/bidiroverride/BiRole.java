/*
 * $Id$
 */

package org.codehaus.modello.tests.bidiroverride;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Date;

/**
 * null
 * 
 * @version $Revision$ $Date$
 */
public class BiRole extends org.codehaus.modello.plugin.java.AbstractPrincipal 
implements org.codehaus.modello.plugin.java.Role, java.io.Serializable
{


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field name
     */
    private String name;

    /**
     * Field roles
     */
    private java.util.List roles;

    /**
     * Field permission
     */
    private BiPermission permission;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addRole
     * 
     * @param biRole
     */
    public void addRole(org.codehaus.modello.plugin.java.Role biRole)
    {
        if ( !(biRole instanceof BiRole) )
        {
            throw new ClassCastException( "BiRole.addRoles(biRole) parameter must be instanceof " + BiRole.class.getName() );
        }
        getRoles().add( ( (BiRole) biRole ) );
    } //-- void addRole(org.codehaus.modello.plugin.java.Role) 

    /**
     * Get null
     */
    public String getName()
    {
        return this.name;
    } //-- String getName() 

    /**
     * Get null
     */
    public org.codehaus.modello.plugin.java.Permission getPermission()
    {
        return (org.codehaus.modello.plugin.java.Permission) this.permission;
    } //-- org.codehaus.modello.plugin.java.Permission getPermission() 

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
     * Method removeRole
     * 
     * @param biRole
     */
    public void removeRole(org.codehaus.modello.plugin.java.Role biRole)
    {
        if ( !(biRole instanceof BiRole) )
        {
            throw new ClassCastException( "BiRole.removeRoles(biRole) parameter must be instanceof " + BiRole.class.getName() );
        }
        getRoles().remove( ( (BiRole) biRole ) );
    } //-- void removeRole(org.codehaus.modello.plugin.java.Role) 

    /**
     * Set null
     * 
     * @param name
     */
    public void setName(String name)
    {
        this.name = name;
    } //-- void setName(String) 

    /**
     * Set null
     * 
     * @param permission
     */
    public void setPermission(org.codehaus.modello.plugin.java.Permission permission)
    {
        if ( !(permission instanceof org.codehaus.modello.plugin.java.Permission) )
        {
            throw new ClassCastException( "BiRole.setPermission(permission) parameter must be instanceof " + org.codehaus.modello.plugin.java.Permission.class.getName() );
        }
        this.permission = (BiPermission) permission;
    } //-- void setPermission(org.codehaus.modello.plugin.java.Permission) 

    /**
     * Set 
     *             Child Roles
     *           
     * 
     * @param roles
     */
    public void setRoles(java.util.List roles)
    {
        this.roles = roles;
    } //-- void setRoles(java.util.List) 


    private String modelEncoding = "UTF-8";

    public void setModelEncoding( String modelEncoding )
    {
        this.modelEncoding = modelEncoding;
    }

    public String getModelEncoding()
    {
        return modelEncoding;
    }}
