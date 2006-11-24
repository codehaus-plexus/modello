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
public class Dependency implements java.io.Serializable {


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
     * Field version
     */
    private String version;

    /**
     * Field url
     */
    private String url;

    /**
     * Field artifact
     */
    private String artifact;

    /**
     * Field type
     */
    private String type = "jar";

    /**
     * Field properties
     */
    private java.util.Properties properties;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addProperty
     * 
     * @param key
     * @param value
     */
    public void addProperty(String key, String value)
    {
        getProperties().put( key, value );
    } //-- void addProperty(String, String) 

    /**
     * Get Literal name of the artifact
     */
    public String getArtifact()
    {
        return this.artifact;
    } //-- String getArtifact() 

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
     *             This url will be provided to the user if the jar
     * file cannot be downloaded
     *             from the central repository.
     *           
     */
    public String getUrl()
    {
        return this.url;
    } //-- String getUrl() 

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
     * Set Literal name of the artifact
     * 
     * @param artifact
     */
    public void setArtifact(String artifact)
    {
        this.artifact = artifact;
    } //-- void setArtifact(String) 

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
     *             Properties about the dependency. Various plugins
     * allow you to
     *             <code>mark</code> dependencies with properties.
     * For example the
     *             <a href="plugins/war/index.html">war</a> plugin
     * looks for a
     *             <code>war.bundle</code> property, and if found
     * will include the dependency
     *             in
     *             <code>WEB-INF/lib</code>. For example syntax,
     * check the war plugin docs.
     *           
     * 
     * @param properties
     */
    public void setProperties(java.util.Properties properties)
    {
        this.properties = properties;
    } //-- void setProperties(java.util.Properties) 

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
     *             This url will be provided to the user if the jar
     * file cannot be downloaded
     *             from the central repository.
     *           
     * 
     * @param url
     */
    public void setUrl(String url)
    {
        this.url = url;
    } //-- void setUrl(String) 

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


            public String toString()
            {
            return groupId + "/" + type + "s:" + artifactId + "-" + version;
            }
          
            public String getId()
            {
            return groupId + ":" + artifactId + ":" + type + ":" + version;
            }
          
    private String modelEncoding = "UTF-8";

    public void setModelEncoding( String modelEncoding )
    {
        this.modelEncoding = modelEncoding;
    }

    public String getModelEncoding()
    {
        return modelEncoding;
    }}
