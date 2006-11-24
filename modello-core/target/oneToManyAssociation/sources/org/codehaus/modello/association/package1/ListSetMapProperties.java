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
public class ListSetMapProperties implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field list
     */
    private java.util.List list;

    /**
     * Field set
     */
    private java.util.Set set;

    /**
     * Field map
     */
    private java.util.Map map;

    /**
     * Field properties
     */
    private java.util.Properties properties;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addList
     * 
     * @param person
     */
    public void addList(Person person)
    {
        if ( !(person instanceof Person) )
        {
            throw new ClassCastException( "ListSetMapProperties.addList(person) parameter must be instanceof " + Person.class.getName() );
        }
        getList().add( person );
    } //-- void addList(Person) 

    /**
     * Method addMap
     * 
     * @param key
     * @param value
     */
    public void addMap(Object key, Person value)
    {
        getMap().put( key, value );
    } //-- void addMap(Object, Person) 

    /**
     * Method addProperty
     * 
     * @param key
     * @param value
     */
    public void addProperty(String key, Person value)
    {
        getProperties().put( key, value );
    } //-- void addProperty(String, Person) 

    /**
     * Method addSet
     * 
     * @param person
     */
    public void addSet(Person person)
    {
        if ( !(person instanceof Person) )
        {
            throw new ClassCastException( "ListSetMapProperties.addSet(person) parameter must be instanceof " + Person.class.getName() );
        }
        getSet().add( person );
    } //-- void addSet(Person) 

    /**
     * Method getList
     */
    public java.util.List getList()
    {
        if ( this.list == null )
        {
            this.list = new java.util.ArrayList();
        }
        
        return this.list;
    } //-- java.util.List getList() 

    /**
     * Method getMap
     */
    public java.util.Map getMap()
    {
        if ( this.map == null )
        {
            this.map = new java.util.HashMap();
        }
        
        return this.map;
    } //-- java.util.Map getMap() 

    /**
     * Method getProperties
     */
    public java.util.Properties getProperties()
    {
        if ( this.properties == null )
        {
            this.properties = new java.util.Properties();
        }
        
        return this.properties;
    } //-- java.util.Properties getProperties() 

    /**
     * Method getSet
     */
    public java.util.Set getSet()
    {
        if ( this.set == null )
        {
            this.set = new java.util.HashSet();
        }
        
        return this.set;
    } //-- java.util.Set getSet() 

    /**
     * Method removeList
     * 
     * @param person
     */
    public void removeList(Person person)
    {
        if ( !(person instanceof Person) )
        {
            throw new ClassCastException( "ListSetMapProperties.removeList(person) parameter must be instanceof " + Person.class.getName() );
        }
        getList().remove( person );
    } //-- void removeList(Person) 

    /**
     * Method removeSet
     * 
     * @param person
     */
    public void removeSet(Person person)
    {
        if ( !(person instanceof Person) )
        {
            throw new ClassCastException( "ListSetMapProperties.removeSet(person) parameter must be instanceof " + Person.class.getName() );
        }
        getSet().remove( person );
    } //-- void removeSet(Person) 

    /**
     * Set null
     * 
     * @param list
     */
    public void setList(java.util.List list)
    {
        this.list = list;
    } //-- void setList(java.util.List) 

    /**
     * Set null
     * 
     * @param map
     */
    public void setMap(java.util.Map map)
    {
        this.map = map;
    } //-- void setMap(java.util.Map) 

    /**
     * Set null
     * 
     * @param properties
     */
    public void setProperties(java.util.Properties properties)
    {
        this.properties = properties;
    } //-- void setProperties(java.util.Properties) 

    /**
     * Set null
     * 
     * @param set
     */
    public void setSet(java.util.Set set)
    {
        this.set = set;
    } //-- void setSet(java.util.Set) 


    private String modelEncoding = "UTF-8";

    public void setModelEncoding( String modelEncoding )
    {
        this.modelEncoding = modelEncoding;
    }

    public String getModelEncoding()
    {
        return modelEncoding;
    }}
