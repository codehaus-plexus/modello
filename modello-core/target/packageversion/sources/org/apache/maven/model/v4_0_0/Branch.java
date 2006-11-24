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
 *         This element describes each of the branches of the
 *         project. Each branch is described by a
 *         <code>tag</code>
 *         element
 *       
 * 
 * @version $Revision$ $Date$
 */
public class Branch implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field tag
     */
    private String tag;

    /**
     * Field description
     */
    private String description;

    /**
     * Field lastMergeTag
     */
    private String lastMergeTag;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Get 
     *             A description of the branch and its strategy.
     *           
     */
    public String getDescription()
    {
        return this.description;
    } //-- String getDescription() 

    /**
     * Get 
     *             This is the tag in the version control system
     * that was last used
     *             to merge from the branch to the current
     * codebase. Future merges
     *             should merge only the changes from this tag to
     * the next.
     *           
     */
    public String getLastMergeTag()
    {
        return this.lastMergeTag;
    } //-- String getLastMergeTag() 

    /**
     * Get 
     *             The branch tag in the version control system
     *             (e.g. cvs) used by the project for the source
     *             code associated with this branch of the
     *             project.
     *           
     */
    public String getTag()
    {
        return this.tag;
    } //-- String getTag() 

    /**
     * Set 
     *             A description of the branch and its strategy.
     *           
     * 
     * @param description
     */
    public void setDescription(String description)
    {
        this.description = description;
    } //-- void setDescription(String) 

    /**
     * Set 
     *             This is the tag in the version control system
     * that was last used
     *             to merge from the branch to the current
     * codebase. Future merges
     *             should merge only the changes from this tag to
     * the next.
     *           
     * 
     * @param lastMergeTag
     */
    public void setLastMergeTag(String lastMergeTag)
    {
        this.lastMergeTag = lastMergeTag;
    } //-- void setLastMergeTag(String) 

    /**
     * Set 
     *             The branch tag in the version control system
     *             (e.g. cvs) used by the project for the source
     *             code associated with this branch of the
     *             project.
     *           
     * 
     * @param tag
     */
    public void setTag(String tag)
    {
        this.tag = tag;
    } //-- void setTag(String) 


    private String modelEncoding = "UTF-8";

    public void setModelEncoding( String modelEncoding )
    {
        this.modelEncoding = modelEncoding;
    }

    public String getModelEncoding()
    {
        return modelEncoding;
    }}
