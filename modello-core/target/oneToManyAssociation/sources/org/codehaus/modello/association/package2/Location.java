/*
 * $Id$
 */

package org.codehaus.modello.association.package2;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Collection;
import java.util.Date;
import org.codehaus.modello.association.package1.ListSetMapProperties;
import org.codehaus.modello.association.package1.Person;

/**
 * null
 * 
 * @version $Revision$ $Date$
 */
public class Location implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field persons
     */
    private java.util.List persons;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addPerson
     * 
     * @param person
     */
    public void addPerson(Person person)
    {
        if ( !(person instanceof Person) )
        {
            throw new ClassCastException( "Location.addPersons(person) parameter must be instanceof " + Person.class.getName() );
        }
        getPersons().add( person );
        person.createLocationAssociation( this );
    } //-- void addPerson(Person) 

    /**
     * Method breakPersonAssociation
     * 
     * @param person
     */
    public void breakPersonAssociation(Person person)
    {
        if ( ! getPersons().contains( person ) )
        {
            throw new IllegalStateException( "person isn't associated." );
        }
        
        getPersons().remove( person );
    } //-- void breakPersonAssociation(Person) 

    /**
     * Method createPersonAssociation
     * 
     * @param person
     */
    public void createPersonAssociation(Person person)
    {
        Collection persons = getPersons();
        
        if ( getPersons().contains(person) )
        {
            throw new IllegalStateException( "person is already assigned." );
        }
        
        persons.add( person );
    } //-- void createPersonAssociation(Person) 

    /**
     * Method getPersons
     */
    public java.util.List getPersons()
    {
        if ( this.persons == null )
        {
            this.persons = new java.util.ArrayList();
        }
        
        return this.persons;
    } //-- java.util.List getPersons() 

    /**
     * Method removePerson
     * 
     * @param person
     */
    public void removePerson(Person person)
    {
        if ( !(person instanceof Person) )
        {
            throw new ClassCastException( "Location.removePersons(person) parameter must be instanceof " + Person.class.getName() );
        }
        person.breakLocationAssociation( this );
        getPersons().remove( person );
    } //-- void removePerson(Person) 

    /**
     * Set null
     * 
     * @param persons
     */
    public void setPersons(java.util.List persons)
    {
        this.persons = persons;
    } //-- void setPersons(java.util.List) 


    private String modelEncoding = "UTF-8";

    public void setModelEncoding( String modelEncoding )
    {
        this.modelEncoding = modelEncoding;
    }

    public String getModelEncoding()
    {
        return modelEncoding;
    }}
