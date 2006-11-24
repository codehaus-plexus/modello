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
public class SourceModification extends Resource 
implements java.io.Serializable
{


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field className
     */
    private String className;

    /**
     * Field property
     */
    private String property;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Get 
     *             If the class with this name can
     *             <strong>not</strong> be
     *             loaded, then the includes and excludes specified
     * below
     *             will be applied to the contents of the
     *             <a href="#sourceDirectory">sourceDirectory</a>
     *           
     */
    public String getClassName()
    {
        return this.className;
    } //-- String getClassName() 

    /**
     * Get the description
     */
    public String getProperty()
    {
        return this.property;
    } //-- String getProperty() 

    /**
     * Set 
     *             If the class with this name can
     *             <strong>not</strong> be
     *             loaded, then the includes and excludes specified
     * below
     *             will be applied to the contents of the
     *             <a href="#sourceDirectory">sourceDirectory</a>
     *           
     * 
     * @param className
     */
    public void setClassName(String className)
    {
        this.className = className;
    } //-- void setClassName(String) 

    /**
     * Set the description
     * 
     * @param property
     */
    public void setProperty(String property)
    {
        this.property = property;
    } //-- void setProperty(String) 


    private String modelEncoding = "UTF-8";

    public void setModelEncoding( String modelEncoding )
    {
        this.modelEncoding = modelEncoding;
    }

    public String getModelEncoding()
    {
        return modelEncoding;
    }}
