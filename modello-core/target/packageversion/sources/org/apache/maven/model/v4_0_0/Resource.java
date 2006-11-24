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
public class Resource extends PatternSet 
implements java.io.Serializable
{


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field directory
     */
    private String directory;

    /**
     * Field targetPath
     */
    private String targetPath;

    /**
     * Field filtering
     */
    private boolean filtering = false;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Get 
     *             Describe the directory where the resource is
     * stored.
     *             The path may be absolute, or relative to the
     * project.xml file.
     *           
     */
    public String getDirectory()
    {
        return this.directory;
    } //-- String getDirectory() 

    /**
     * Get 
     *             Describe the resource target path. For example,
     * if you want that resource
     *             appear into a specific package (
     *             <code>org.apache.maven.messages</code>), you
     * must specify this
     *             element with this value :
     *             <code>org/apache/maven/messages</code>
     *           
     */
    public String getTargetPath()
    {
        return this.targetPath;
    } //-- String getTargetPath() 

    /**
     * Get Describe if resources are filtered or not.
     */
    public boolean isFiltering()
    {
        return this.filtering;
    } //-- boolean isFiltering() 

    /**
     * Set 
     *             Describe the directory where the resource is
     * stored.
     *             The path may be absolute, or relative to the
     * project.xml file.
     *           
     * 
     * @param directory
     */
    public void setDirectory(String directory)
    {
        this.directory = directory;
    } //-- void setDirectory(String) 

    /**
     * Set Describe if resources are filtered or not.
     * 
     * @param filtering
     */
    public void setFiltering(boolean filtering)
    {
        this.filtering = filtering;
    } //-- void setFiltering(boolean) 

    /**
     * Set 
     *             Describe the resource target path. For example,
     * if you want that resource
     *             appear into a specific package (
     *             <code>org.apache.maven.messages</code>), you
     * must specify this
     *             element with this value :
     *             <code>org/apache/maven/messages</code>
     *           
     * 
     * @param targetPath
     */
    public void setTargetPath(String targetPath)
    {
        this.targetPath = targetPath;
    } //-- void setTargetPath(String) 


    private String modelEncoding = "UTF-8";

    public void setModelEncoding( String modelEncoding )
    {
        this.modelEncoding = modelEncoding;
    }

    public String getModelEncoding()
    {
        return modelEncoding;
    }}
