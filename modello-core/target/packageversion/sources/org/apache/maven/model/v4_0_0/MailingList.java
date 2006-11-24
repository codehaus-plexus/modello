/*
 * $Id$
 */

package org.apache.maven.model.v4_0_0;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Date;

/**
 * 
 *         This element describes all of the mailing lists
 * associated with
 *         a project.  Each mailing list is described by a
 *         <code>mailingList</code> element, which is then
 * described by
 *         additional elements (described below).  The
 * auto-generated site
 *         documentation references this information.
 *       
 * 
 * @version $Revision$ $Date$
 */
public class MailingList implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field name
     */
    private String name;

    /**
     * Field subscribe
     */
    private String subscribe;

    /**
     * Field unsubscribe
     */
    private String unsubscribe;

    /**
     * Field post
     */
    private String post;

    /**
     * Field archive
     */
    private String archive;

    /**
     * Field archives
     */
    private java.util.List archives;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addArchive
     * 
     * @param string
     */
    public void addArchive(String string)
    {
        if ( !(string instanceof String) )
        {
            throw new ClassCastException( "MailingList.addArchives(string) parameter must be instanceof " + String.class.getName() );
        }
        getArchives().add( string );
    } //-- void addArchive(String) 

    /**
     * Get The link to a URL where you can browse the archive.
     */
    public String getArchive()
    {
        return this.archive;
    } //-- String getArchive() 

    /**
     * Method getArchives
     */
    public java.util.List getArchives()
    {
        if ( this.archives == null )
        {
            this.archives = new java.util.ArrayList();
        }
        
        return this.archives;
    } //-- java.util.List getArchives() 

    /**
     * Get The name of the mailing list.
     */
    public String getName()
    {
        return this.name;
    } //-- String getName() 

    /**
     * Get 
     *             The email address or link that can be used to
     * post to
     *             the mailing list.  If this is an email address,
     * a
     *             <code>mailto:</code> link will automatically be
     * created
     *             when the documentation is created.
     *           
     */
    public String getPost()
    {
        return this.post;
    } //-- String getPost() 

    /**
     * Get 
     *             The email address or link that can be used to
     * subscribe to the mailing list.
     *             If this is an email address, a
     *             <code>mailto:</code> link will automatically be
     * created when
     *             the documentation is created.
     *           
     */
    public String getSubscribe()
    {
        return this.subscribe;
    } //-- String getSubscribe() 

    /**
     * Get 
     *             The email address or link that can be used to
     * unsubscribe to
     *             the mailing list.  If this is an email address,
     * a
     *             <code>mailto:</code> link will automatically be
     * created
     *             when the documentation is created.
     *           
     */
    public String getUnsubscribe()
    {
        return this.unsubscribe;
    } //-- String getUnsubscribe() 

    /**
     * Method removeArchive
     * 
     * @param string
     */
    public void removeArchive(String string)
    {
        if ( !(string instanceof String) )
        {
            throw new ClassCastException( "MailingList.removeArchives(string) parameter must be instanceof " + String.class.getName() );
        }
        getArchives().remove( string );
    } //-- void removeArchive(String) 

    /**
     * Set The link to a URL where you can browse the archive.
     * 
     * @param archive
     */
    public void setArchive(String archive)
    {
        this.archive = archive;
    } //-- void setArchive(String) 

    /**
     * Set The link to a URL where you can browse the archive.
     * 
     * @param archives
     */
    public void setArchives(java.util.List archives)
    {
        this.archives = archives;
    } //-- void setArchives(java.util.List) 

    /**
     * Set The name of the mailing list.
     * 
     * @param name
     */
    public void setName(String name)
    {
        this.name = name;
    } //-- void setName(String) 

    /**
     * Set 
     *             The email address or link that can be used to
     * post to
     *             the mailing list.  If this is an email address,
     * a
     *             <code>mailto:</code> link will automatically be
     * created
     *             when the documentation is created.
     *           
     * 
     * @param post
     */
    public void setPost(String post)
    {
        this.post = post;
    } //-- void setPost(String) 

    /**
     * Set 
     *             The email address or link that can be used to
     * subscribe to the mailing list.
     *             If this is an email address, a
     *             <code>mailto:</code> link will automatically be
     * created when
     *             the documentation is created.
     *           
     * 
     * @param subscribe
     */
    public void setSubscribe(String subscribe)
    {
        this.subscribe = subscribe;
    } //-- void setSubscribe(String) 

    /**
     * Set 
     *             The email address or link that can be used to
     * unsubscribe to
     *             the mailing list.  If this is an email address,
     * a
     *             <code>mailto:</code> link will automatically be
     * created
     *             when the documentation is created.
     *           
     * 
     * @param unsubscribe
     */
    public void setUnsubscribe(String unsubscribe)
    {
        this.unsubscribe = unsubscribe;
    } //-- void setUnsubscribe(String) 


    private String modelEncoding = "UTF-8";

    public void setModelEncoding( String modelEncoding )
    {
        this.modelEncoding = modelEncoding;
    }

    public String getModelEncoding()
    {
        return modelEncoding;
    }}
