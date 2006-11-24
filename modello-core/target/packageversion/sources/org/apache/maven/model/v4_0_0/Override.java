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
public class Override implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field groupId
     */
    private String groupId;

    /**
     * Field artifactId
     */
    private String artifactId;

    /**
     * Field type
     */
    private String type = "jar";

    /**
     * Field version
     */
    private String version;

    /**
     * Field file
     */
    private String file;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Get 
     *             The unique id for an artifact produced by the
     * project group, e.g.
     *             <code>germonimo-jms</code>
     *           
     */
    public String getArtifactId()
    {
        return this.artifactId;
    } //-- String getArtifactId() 

    /**
     * Get 
     *             The filename of the dependency that will be used
     * to override the one from the repository, e.g.
     *             <code>lib/non-distributable-code-1.3.jar</code>
     *           
     */
    public String getFile()
    {
        return this.file;
    } //-- String getFile() 

    /**
     * Get 
     *             The project group that produced the dependency,
     * e.g.
     *             <code>geronimo</code>.
     *           
     */
    public String getGroupId()
    {
        return this.groupId;
    } //-- String getGroupId() 

    /**
     * Get 
     *             Other known recognised dependency types are:
     *             <code>ejb</code> and
     *             <code>plugin</code>.
     *           
     */
    public String getType()
    {
        return this.type;
    } //-- String getType() 

    /**
     * Get 
     *             The version of the dependency., e.g.
     *             <code>3.2.1</code>
     *           
     */
    public String getVersion()
    {
        return this.version;
    } //-- String getVersion() 

    /**
     * Set 
     *             The unique id for an artifact produced by the
     * project group, e.g.
     *             <code>germonimo-jms</code>
     *           
     * 
     * @param artifactId
     */
    public void setArtifactId(String artifactId)
    {
        this.artifactId = artifactId;
    } //-- void setArtifactId(String) 

    /**
     * Set 
     *             The filename of the dependency that will be used
     * to override the one from the repository, e.g.
     *             <code>lib/non-distributable-code-1.3.jar</code>
     *           
     * 
     * @param file
     */
    public void setFile(String file)
    {
        this.file = file;
    } //-- void setFile(String) 

    /**
     * Set 
     *             The project group that produced the dependency,
     * e.g.
     *             <code>geronimo</code>.
     *           
     * 
     * @param groupId
     */
    public void setGroupId(String groupId)
    {
        this.groupId = groupId;
    } //-- void setGroupId(String) 

    /**
     * Set 
     *             Other known recognised dependency types are:
     *             <code>ejb</code> and
     *             <code>plugin</code>.
     *           
     * 
     * @param type
     */
    public void setType(String type)
    {
        this.type = type;
    } //-- void setType(String) 

    /**
     * Set 
     *             The version of the dependency., e.g.
     *             <code>3.2.1</code>
     *           
     * 
     * @param version
     */
    public void setVersion(String version)
    {
        this.version = version;
    } //-- void setVersion(String) 


    private String modelEncoding = "UTF-8";

    public void setModelEncoding( String modelEncoding )
    {
        this.modelEncoding = modelEncoding;
    }

    public String getModelEncoding()
    {
        return modelEncoding;
    }}
