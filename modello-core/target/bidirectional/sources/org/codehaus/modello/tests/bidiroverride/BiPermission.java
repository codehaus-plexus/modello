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
public class BiPermission 
implements org.codehaus.modello.plugin.java.Permission, java.io.Serializable
{


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field name
     */
    private String name;

    /**
     * Field operations
     */
    private java.util.List operations;

    /**
     * Field tasks
     */
    private java.util.List tasks;

    /**
     * Field resources
     */
    private java.util.List resources = new java.util.ArrayList();

    /**
     * Field statusIndicators
     */
    private java.util.Map statusIndicators;


      //----------------/
     //- Constructors -/
    //----------------/

    public BiPermission() {
        this.operations = new java.util.ArrayList();
        this.statusIndicators = new java.util.HashMap();
    } //-- org.codehaus.modello.tests.bidiroverride.BiPermission()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addOperation
     * 
     * @param string
     */
    public void addOperation(String string)
    {
        if ( !(string instanceof String) )
        {
            throw new ClassCastException( "BiPermission.addOperations(string) parameter must be instanceof " + String.class.getName() );
        }
        getOperations().add( string );
    } //-- void addOperation(String) 

    /**
     * Method addResource
     * 
     * @param string
     */
    public void addResource(String string)
    {
        if ( !(string instanceof String) )
        {
            throw new ClassCastException( "BiPermission.addResources(string) parameter must be instanceof " + String.class.getName() );
        }
        getResources().add( string );
    } //-- void addResource(String) 

    /**
     * Method addStatusIndicator
     * 
     * @param key
     * @param value
     */
    public void addStatusIndicator(Object key, String value)
    {
        getStatusIndicators().put( key, value );
    } //-- void addStatusIndicator(Object, String) 

    /**
     * Method addTask
     * 
     * @param string
     */
    public void addTask(String string)
    {
        if ( !(string instanceof String) )
        {
            throw new ClassCastException( "BiPermission.addTasks(string) parameter must be instanceof " + String.class.getName() );
        }
        getTasks().add( string );
    } //-- void addTask(String) 

    /**
     * Get null
     */
    public String getName()
    {
        return this.name;
    } //-- String getName() 

    /**
     * Method getOperations
     */
    public java.util.List getOperations()
    {
        return this.operations;
    } //-- java.util.List getOperations() 

    /**
     * Method getResources
     */
    public java.util.List getResources()
    {
        return this.resources;
    } //-- java.util.List getResources() 

    /**
     * Method getStatusIndicators
     */
    public java.util.Map getStatusIndicators()
    {
        return this.statusIndicators;
    } //-- java.util.Map getStatusIndicators() 

    /**
     * Method getTasks
     */
    public java.util.List getTasks()
    {
        if ( this.tasks == null )
        {
            this.tasks = new java.util.ArrayList();
        }
        
        return this.tasks;
    } //-- java.util.List getTasks() 

    /**
     * Method removeOperation
     * 
     * @param string
     */
    public void removeOperation(String string)
    {
        if ( !(string instanceof String) )
        {
            throw new ClassCastException( "BiPermission.removeOperations(string) parameter must be instanceof " + String.class.getName() );
        }
        getOperations().remove( string );
    } //-- void removeOperation(String) 

    /**
     * Method removeResource
     * 
     * @param string
     */
    public void removeResource(String string)
    {
        if ( !(string instanceof String) )
        {
            throw new ClassCastException( "BiPermission.removeResources(string) parameter must be instanceof " + String.class.getName() );
        }
        getResources().remove( string );
    } //-- void removeResource(String) 

    /**
     * Method removeTask
     * 
     * @param string
     */
    public void removeTask(String string)
    {
        if ( !(string instanceof String) )
        {
            throw new ClassCastException( "BiPermission.removeTasks(string) parameter must be instanceof " + String.class.getName() );
        }
        getTasks().remove( string );
    } //-- void removeTask(String) 

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
     * @param operations
     */
    public void setOperations(java.util.List operations)
    {
        this.operations = operations;
    } //-- void setOperations(java.util.List) 

    /**
     * Set null
     * 
     * @param resources
     */
    public void setResources(java.util.List resources)
    {
        this.resources = resources;
    } //-- void setResources(java.util.List) 

    /**
     * Set null
     * 
     * @param statusIndicators
     */
    public void setStatusIndicators(java.util.Map statusIndicators)
    {
        this.statusIndicators = statusIndicators;
    } //-- void setStatusIndicators(java.util.Map) 

    /**
     * Set null
     * 
     * @param tasks
     */
    public void setTasks(java.util.List tasks)
    {
        this.tasks = tasks;
    } //-- void setTasks(java.util.List) 


    private String modelEncoding = "UTF-8";

    public void setModelEncoding( String modelEncoding )
    {
        this.modelEncoding = modelEncoding;
    }

    public String getModelEncoding()
    {
        return modelEncoding;
    }}
