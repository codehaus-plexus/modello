/*
 * $Id$
 */

package org.apache.maven.model;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Date;

/**
 * null
 * 
 * @version $Revision$ $Date$
 */
public class GoalDecorator implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field name
     */
    private String name;

    /**
     * Field attain
     */
    private String attain;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Get 
     *             The goal which should be injected into the
     * execution chain.
     *           
     */
    public String getAttain()
    {
        return this.attain;
    } //-- String getAttain() 

    /**
     * Get The target goal which should be decorated.
     */
    public String getName()
    {
        return this.name;
    } //-- String getName() 

    /**
     * Set 
     *             The goal which should be injected into the
     * execution chain.
     *           
     * 
     * @param attain
     */
    public void setAttain(String attain)
    {
        this.attain = attain;
    } //-- void setAttain(String) 

    /**
     * Set The target goal which should be decorated.
     * 
     * @param name
     */
    public void setName(String name)
    {
        this.name = name;
    } //-- void setName(String) 


    private String modelEncoding = "UTF-8";

    public void setModelEncoding( String modelEncoding )
    {
        this.modelEncoding = modelEncoding;
    }

    public String getModelEncoding()
    {
        return modelEncoding;
    }}
