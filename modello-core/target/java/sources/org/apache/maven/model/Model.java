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
public class Model implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field extend
     */
    private String extend;

    /**
     * Field parent
     */
    private Parent parent;

    /**
     * Field modelVersion
     */
    private String modelVersion;

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
     * Field name
     */
    private String name;

    /**
     * Field version
     */
    private String version;

    /**
     * Field shortDescription
     */
    private String shortDescription;

    /**
     * Field description
     */
    private String description;

    /**
     * Field url
     */
    private String url;

    /**
     * Field logo
     */
    private String logo;

    /**
     * Field issueManagement
     */
    private IssueManagement issueManagement;

    /**
     * Field ciManagement
     */
    private CiManagement ciManagement;

    /**
     * Field inceptionYear
     */
    private String inceptionYear;

    /**
     * Field repositories
     */
    private java.util.List repositories;

    /**
     * This may be removed or relocated in the near future. It is
     * undecided whether plugins really need a
     *             remote repository set of their own.
     */
    private java.util.List pluginRepositories;

    /**
     * Field mailingLists
     */
    private java.util.List mailingLists;

    /**
     * Field developers
     */
    private java.util.List developers;

    /**
     * Field contributors
     */
    private java.util.List contributors;

    /**
     * These should ultimately only be compile time dependencies
     * when transitive dependencies come into
     *             play.
     */
    private java.util.List dependencies;

    /**
     * Field overrides
     */
    private java.util.List overrides;

    /**
     * Field licenses
     */
    private java.util.List licenses;

    /**
     * Field packageGroups
     */
    private java.util.List packageGroups;

    /**
     * Field reports
     */
    private java.util.List reports;

    /**
     * Field scm
     */
    private Scm scm;

    /**
     * Field build
     */
    private Build build;

    /**
     * Field organization
     */
    private Organization organization;

    /**
     * Field distributionManagement
     */
    private DistributionManagement distributionManagement;

    /**
     * Field local
     */
    private Local local;

    /**
     * Field properties
     */
    private java.util.Properties properties;

    /**
     * Field preGoals
     */
    private java.util.List preGoals;

    /**
     * Field postGoals
     */
    private java.util.List postGoals;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addContributor
     * 
     * @param contributor
     */
    public void addContributor(Contributor contributor)
    {
        if ( !(contributor instanceof Contributor) )
        {
            throw new ClassCastException( "Model.addContributors(contributor) parameter must be instanceof " + Contributor.class.getName() );
        }
        getContributors().add( contributor );
    } //-- void addContributor(Contributor) 

    /**
     * Method addDependency
     * 
     * @param dependency
     */
    public void addDependency(Dependency dependency)
    {
        if ( !(dependency instanceof Dependency) )
        {
            throw new ClassCastException( "Model.addDependencies(dependency) parameter must be instanceof " + Dependency.class.getName() );
        }
        getDependencies().add( dependency );
    } //-- void addDependency(Dependency) 

    /**
     * Method addDeveloper
     * 
     * @param developer
     */
    public void addDeveloper(Developer developer)
    {
        if ( !(developer instanceof Developer) )
        {
            throw new ClassCastException( "Model.addDevelopers(developer) parameter must be instanceof " + Developer.class.getName() );
        }
        getDevelopers().add( developer );
    } //-- void addDeveloper(Developer) 

    /**
     * Method addLicense
     * 
     * @param license
     */
    public void addLicense(License license)
    {
        if ( !(license instanceof License) )
        {
            throw new ClassCastException( "Model.addLicenses(license) parameter must be instanceof " + License.class.getName() );
        }
        getLicenses().add( license );
    } //-- void addLicense(License) 

    /**
     * Method addMailingList
     * 
     * @param mailingList
     */
    public void addMailingList(MailingList mailingList)
    {
        if ( !(mailingList instanceof MailingList) )
        {
            throw new ClassCastException( "Model.addMailingLists(mailingList) parameter must be instanceof " + MailingList.class.getName() );
        }
        getMailingLists().add( mailingList );
    } //-- void addMailingList(MailingList) 

    /**
     * Method addOverride
     * 
     * @param override
     */
    public void addOverride(Override override)
    {
        if ( !(override instanceof Override) )
        {
            throw new ClassCastException( "Model.addOverrides(override) parameter must be instanceof " + Override.class.getName() );
        }
        getOverrides().add( override );
    } //-- void addOverride(Override) 

    /**
     * Method addPackageGroup
     * 
     * @param packageGroup
     */
    public void addPackageGroup(PackageGroup packageGroup)
    {
        if ( !(packageGroup instanceof PackageGroup) )
        {
            throw new ClassCastException( "Model.addPackageGroups(packageGroup) parameter must be instanceof " + PackageGroup.class.getName() );
        }
        getPackageGroups().add( packageGroup );
    } //-- void addPackageGroup(PackageGroup) 

    /**
     * Method addPluginRepository
     * 
     * @param repository
     */
    public void addPluginRepository(Repository repository)
    {
        if ( !(repository instanceof Repository) )
        {
            throw new ClassCastException( "Model.addPluginRepositories(repository) parameter must be instanceof " + Repository.class.getName() );
        }
        getPluginRepositories().add( repository );
    } //-- void addPluginRepository(Repository) 

    /**
     * Method addPostGoal
     * 
     * @param postGoal
     */
    public void addPostGoal(PostGoal postGoal)
    {
        if ( !(postGoal instanceof PostGoal) )
        {
            throw new ClassCastException( "Model.addPostGoals(postGoal) parameter must be instanceof " + PostGoal.class.getName() );
        }
        getPostGoals().add( postGoal );
    } //-- void addPostGoal(PostGoal) 

    /**
     * Method addPreGoal
     * 
     * @param preGoal
     */
    public void addPreGoal(PreGoal preGoal)
    {
        if ( !(preGoal instanceof PreGoal) )
        {
            throw new ClassCastException( "Model.addPreGoals(preGoal) parameter must be instanceof " + PreGoal.class.getName() );
        }
        getPreGoals().add( preGoal );
    } //-- void addPreGoal(PreGoal) 

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
     * Method addReport
     * 
     * @param string
     */
    public void addReport(String string)
    {
        if ( !(string instanceof String) )
        {
            throw new ClassCastException( "Model.addReports(string) parameter must be instanceof " + String.class.getName() );
        }
        getReports().add( string );
    } //-- void addReport(String) 

    /**
     * Method addRepository
     * 
     * @param repository
     */
    public void addRepository(Repository repository)
    {
        if ( !(repository instanceof Repository) )
        {
            throw new ClassCastException( "Model.addRepositories(repository) parameter must be instanceof " + Repository.class.getName() );
        }
        getRepositories().add( repository );
    } //-- void addRepository(Repository) 

    /**
     * Get The identifier used when generating the artifact for
     * your project.
     */
    public String getArtifactId()
    {
        return this.artifactId;
    } //-- String getArtifactId() 

    /**
     * Get Information required to build the project.
     */
    public Build getBuild()
    {
        return this.build;
    } //-- Build getBuild() 

    /**
     * Get The project's continuous integration management
     * information.
     */
    public CiManagement getCiManagement()
    {
        return this.ciManagement;
    } //-- CiManagement getCiManagement() 

    /**
     * Method getContributors
     */
    public java.util.List getContributors()
    {
        if ( this.contributors == null )
        {
            this.contributors = new java.util.ArrayList();
        }
        
        return this.contributors;
    } //-- java.util.List getContributors() 

    /**
     * Method getDependencies
     */
    public java.util.List getDependencies()
    {
        if ( this.dependencies == null )
        {
            this.dependencies = new java.util.ArrayList();
        }
        
        return this.dependencies;
    } //-- java.util.List getDependencies() 

    /**
     * Get 
     *             A detailed description of the project.  This
     * element is
     *             usually specified as CDATA to enable the use of
     * HTML tags
     *             within the description.  This description is
     * used to
     *             generate the
     *             <a href="plugins/site/index.html">front page</a>
     *             of the project's web site.
     *           
     */
    public String getDescription()
    {
        return this.description;
    } //-- String getDescription() 

    /**
     * Method getDevelopers
     */
    public java.util.List getDevelopers()
    {
        if ( this.developers == null )
        {
            this.developers = new java.util.ArrayList();
        }
        
        return this.developers;
    } //-- java.util.List getDevelopers() 

    /**
     * Get Distribution information for a project.
     */
    public DistributionManagement getDistributionManagement()
    {
        return this.distributionManagement;
    } //-- DistributionManagement getDistributionManagement() 

    /**
     * Get 
     *             The location of the parent project, if one
     * exists. Values from the parent project will be
     *             the default for this project if they are left
     * unspecified.
     *             The path may be absolute, or relative to the
     * current project.xml file.
     *           
     */
    public String getExtend()
    {
        return this.extend;
    } //-- String getExtend() 

    /**
     * Get The primary grouping for your project.
     */
    public String getGroupId()
    {
        return this.groupId;
    } //-- String getGroupId() 

    /**
     * Get The year the project started.
     */
    public String getInceptionYear()
    {
        return this.inceptionYear;
    } //-- String getInceptionYear() 

    /**
     * Get The project's issue management information.
     */
    public IssueManagement getIssueManagement()
    {
        return this.issueManagement;
    } //-- IssueManagement getIssueManagement() 

    /**
     * Method getLicenses
     */
    public java.util.List getLicenses()
    {
        if ( this.licenses == null )
        {
            this.licenses = new java.util.ArrayList();
        }
        
        return this.licenses;
    } //-- java.util.List getLicenses() 

    /**
     * Get Local configuration information.
     */
    public Local getLocal()
    {
        return this.local;
    } //-- Local getLocal() 

    /**
     * Get The logo for the project.
     */
    public String getLogo()
    {
        return this.logo;
    } //-- String getLogo() 

    /**
     * Method getMailingLists
     */
    public java.util.List getMailingLists()
    {
        if ( this.mailingLists == null )
        {
            this.mailingLists = new java.util.ArrayList();
        }
        
        return this.mailingLists;
    } //-- java.util.List getMailingLists() 

    /**
     * Get The version of this model you are using.
     */
    public String getModelVersion()
    {
        return this.modelVersion;
    } //-- String getModelVersion() 

    /**
     * Get Human readable name of the project.
     */
    public String getName()
    {
        return this.name;
    } //-- String getName() 

    /**
     * Get 
     *             This element describes various attributes of the
     * organziation to
     *             which the project belongs.  These attributes are
     * utilized when
     *             documentation is created (for copyright notices
     * and links).
     *           
     */
    public Organization getOrganization()
    {
        return this.organization;
    } //-- Organization getOrganization() 

    /**
     * Method getOverrides
     */
    public java.util.List getOverrides()
    {
        if ( this.overrides == null )
        {
            this.overrides = new java.util.ArrayList();
        }
        
        return this.overrides;
    } //-- java.util.List getOverrides() 

    /**
     * Method getPackageGroups
     */
    public java.util.List getPackageGroups()
    {
        if ( this.packageGroups == null )
        {
            this.packageGroups = new java.util.ArrayList();
        }
        
        return this.packageGroups;
    } //-- java.util.List getPackageGroups() 

    /**
     * Get Specified which project to extend.
     */
    public Parent getParent()
    {
        return this.parent;
    } //-- Parent getParent() 

    /**
     * Method getPluginRepositories
     */
    public java.util.List getPluginRepositories()
    {
        if ( this.pluginRepositories == null )
        {
            this.pluginRepositories = new java.util.ArrayList();
        }
        
        return this.pluginRepositories;
    } //-- java.util.List getPluginRepositories() 

    /**
     * Method getPostGoals
     */
    public java.util.List getPostGoals()
    {
        if ( this.postGoals == null )
        {
            this.postGoals = new java.util.ArrayList();
        }
        
        return this.postGoals;
    } //-- java.util.List getPostGoals() 

    /**
     * Method getPreGoals
     */
    public java.util.List getPreGoals()
    {
        if ( this.preGoals == null )
        {
            this.preGoals = new java.util.ArrayList();
        }
        
        return this.preGoals;
    } //-- java.util.List getPreGoals() 

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
     * Method getReports
     */
    public java.util.List getReports()
    {
        if ( this.reports == null )
        {
            this.reports = new java.util.ArrayList();
        }
        
        return this.reports;
    } //-- java.util.List getReports() 

    /**
     * Method getRepositories
     */
    public java.util.List getRepositories()
    {
        if ( this.repositories == null )
        {
            this.repositories = new java.util.ArrayList();
        }
        
        return this.repositories;
    } //-- java.util.List getRepositories() 

    /**
     * Get Specification for the SCM use by the project.
     */
    public Scm getScm()
    {
        return this.scm;
    } //-- Scm getScm() 

    /**
     * Get An abbreviated description of the project.
     */
    public String getShortDescription()
    {
        return this.shortDescription;
    } //-- String getShortDescription() 

    /**
     * Get The type of artifact this project produces.
     */
    public String getType()
    {
        return this.type;
    } //-- String getType() 

    /**
     * Get The URL where the project can be found.
     */
    public String getUrl()
    {
        return this.url;
    } //-- String getUrl() 

    /**
     * Get The current version of the project.
     */
    public String getVersion()
    {
        return this.version;
    } //-- String getVersion() 

    /**
     * Method removeContributor
     * 
     * @param contributor
     */
    public void removeContributor(Contributor contributor)
    {
        if ( !(contributor instanceof Contributor) )
        {
            throw new ClassCastException( "Model.removeContributors(contributor) parameter must be instanceof " + Contributor.class.getName() );
        }
        getContributors().remove( contributor );
    } //-- void removeContributor(Contributor) 

    /**
     * Method removeDependency
     * 
     * @param dependency
     */
    public void removeDependency(Dependency dependency)
    {
        if ( !(dependency instanceof Dependency) )
        {
            throw new ClassCastException( "Model.removeDependencies(dependency) parameter must be instanceof " + Dependency.class.getName() );
        }
        getDependencies().remove( dependency );
    } //-- void removeDependency(Dependency) 

    /**
     * Method removeDeveloper
     * 
     * @param developer
     */
    public void removeDeveloper(Developer developer)
    {
        if ( !(developer instanceof Developer) )
        {
            throw new ClassCastException( "Model.removeDevelopers(developer) parameter must be instanceof " + Developer.class.getName() );
        }
        getDevelopers().remove( developer );
    } //-- void removeDeveloper(Developer) 

    /**
     * Method removeLicense
     * 
     * @param license
     */
    public void removeLicense(License license)
    {
        if ( !(license instanceof License) )
        {
            throw new ClassCastException( "Model.removeLicenses(license) parameter must be instanceof " + License.class.getName() );
        }
        getLicenses().remove( license );
    } //-- void removeLicense(License) 

    /**
     * Method removeMailingList
     * 
     * @param mailingList
     */
    public void removeMailingList(MailingList mailingList)
    {
        if ( !(mailingList instanceof MailingList) )
        {
            throw new ClassCastException( "Model.removeMailingLists(mailingList) parameter must be instanceof " + MailingList.class.getName() );
        }
        getMailingLists().remove( mailingList );
    } //-- void removeMailingList(MailingList) 

    /**
     * Method removeOverride
     * 
     * @param override
     */
    public void removeOverride(Override override)
    {
        if ( !(override instanceof Override) )
        {
            throw new ClassCastException( "Model.removeOverrides(override) parameter must be instanceof " + Override.class.getName() );
        }
        getOverrides().remove( override );
    } //-- void removeOverride(Override) 

    /**
     * Method removePackageGroup
     * 
     * @param packageGroup
     */
    public void removePackageGroup(PackageGroup packageGroup)
    {
        if ( !(packageGroup instanceof PackageGroup) )
        {
            throw new ClassCastException( "Model.removePackageGroups(packageGroup) parameter must be instanceof " + PackageGroup.class.getName() );
        }
        getPackageGroups().remove( packageGroup );
    } //-- void removePackageGroup(PackageGroup) 

    /**
     * Method removePluginRepository
     * 
     * @param repository
     */
    public void removePluginRepository(Repository repository)
    {
        if ( !(repository instanceof Repository) )
        {
            throw new ClassCastException( "Model.removePluginRepositories(repository) parameter must be instanceof " + Repository.class.getName() );
        }
        getPluginRepositories().remove( repository );
    } //-- void removePluginRepository(Repository) 

    /**
     * Method removePostGoal
     * 
     * @param postGoal
     */
    public void removePostGoal(PostGoal postGoal)
    {
        if ( !(postGoal instanceof PostGoal) )
        {
            throw new ClassCastException( "Model.removePostGoals(postGoal) parameter must be instanceof " + PostGoal.class.getName() );
        }
        getPostGoals().remove( postGoal );
    } //-- void removePostGoal(PostGoal) 

    /**
     * Method removePreGoal
     * 
     * @param preGoal
     */
    public void removePreGoal(PreGoal preGoal)
    {
        if ( !(preGoal instanceof PreGoal) )
        {
            throw new ClassCastException( "Model.removePreGoals(preGoal) parameter must be instanceof " + PreGoal.class.getName() );
        }
        getPreGoals().remove( preGoal );
    } //-- void removePreGoal(PreGoal) 

    /**
     * Method removeReport
     * 
     * @param string
     */
    public void removeReport(String string)
    {
        if ( !(string instanceof String) )
        {
            throw new ClassCastException( "Model.removeReports(string) parameter must be instanceof " + String.class.getName() );
        }
        getReports().remove( string );
    } //-- void removeReport(String) 

    /**
     * Method removeRepository
     * 
     * @param repository
     */
    public void removeRepository(Repository repository)
    {
        if ( !(repository instanceof Repository) )
        {
            throw new ClassCastException( "Model.removeRepositories(repository) parameter must be instanceof " + Repository.class.getName() );
        }
        getRepositories().remove( repository );
    } //-- void removeRepository(Repository) 

    /**
     * Set The identifier used when generating the artifact for
     * your project.
     * 
     * @param artifactId
     */
    public void setArtifactId(String artifactId)
    {
        this.artifactId = artifactId;
    } //-- void setArtifactId(String) 

    /**
     * Set Information required to build the project.
     * 
     * @param build
     */
    public void setBuild(Build build)
    {
        this.build = build;
    } //-- void setBuild(Build) 

    /**
     * Set The project's continuous integration management
     * information.
     * 
     * @param ciManagement
     */
    public void setCiManagement(CiManagement ciManagement)
    {
        this.ciManagement = ciManagement;
    } //-- void setCiManagement(CiManagement) 

    /**
     * Set 
     *             This element describes all of the contributors
     * associated with a
     *             project who are not developers.  Each
     * contributor is described by a
     *             <code>contributor</code> element, which is then
     * describe by additional
     *             elements (described below).  The auto-generated
     * site documentation
     *             references this information.
     *           
     * 
     * @param contributors
     */
    public void setContributors(java.util.List contributors)
    {
        this.contributors = contributors;
    } //-- void setContributors(java.util.List) 

    /**
     * Set 
     *             This element describes all of the dependencies
     * associated with a
     *             project.  Each dependency is described by a
     *             <code>dependency</code> element, which is then
     * described by
     *             additional elements (described below).
     *           
     * 
     * @param dependencies
     */
    public void setDependencies(java.util.List dependencies)
    {
        this.dependencies = dependencies;
    } //-- void setDependencies(java.util.List) 

    /**
     * Set 
     *             A detailed description of the project.  This
     * element is
     *             usually specified as CDATA to enable the use of
     * HTML tags
     *             within the description.  This description is
     * used to
     *             generate the
     *             <a href="plugins/site/index.html">front page</a>
     *             of the project's web site.
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
     *             This element describes all of the developers
     * associated with a
     *             project.  Each developer is described by a
     *             <code>developer</code> element, which is then
     * described by
     *             additional elements (described below).  The
     * auto-generated site
     *             documentation references this information.
     *           
     * 
     * @param developers
     */
    public void setDevelopers(java.util.List developers)
    {
        this.developers = developers;
    } //-- void setDevelopers(java.util.List) 

    /**
     * Set Distribution information for a project.
     * 
     * @param distributionManagement
     */
    public void setDistributionManagement(DistributionManagement distributionManagement)
    {
        this.distributionManagement = distributionManagement;
    } //-- void setDistributionManagement(DistributionManagement) 

    /**
     * Set 
     *             The location of the parent project, if one
     * exists. Values from the parent project will be
     *             the default for this project if they are left
     * unspecified.
     *             The path may be absolute, or relative to the
     * current project.xml file.
     *           
     * 
     * @param extend
     */
    public void setExtend(String extend)
    {
        this.extend = extend;
    } //-- void setExtend(String) 

    /**
     * Set The primary grouping for your project.
     * 
     * @param groupId
     */
    public void setGroupId(String groupId)
    {
        this.groupId = groupId;
    } //-- void setGroupId(String) 

    /**
     * Set The year the project started.
     * 
     * @param inceptionYear
     */
    public void setInceptionYear(String inceptionYear)
    {
        this.inceptionYear = inceptionYear;
    } //-- void setInceptionYear(String) 

    /**
     * Set The project's issue management information.
     * 
     * @param issueManagement
     */
    public void setIssueManagement(IssueManagement issueManagement)
    {
        this.issueManagement = issueManagement;
    } //-- void setIssueManagement(IssueManagement) 

    /**
     * Set 
     *             This element describes all of the licenses for
     * this project.  Each license is described by a
     *             <code>license</code> element, which is then
     * describe by additional
     *             elements (described below).  The auto-generated
     * site documentation
     *             references this information.  Projects should
     * only list the license(s) that
     *             applies to the project and not the licenses that
     * apply to dependencies.
     *           
     * 
     * @param licenses
     */
    public void setLicenses(java.util.List licenses)
    {
        this.licenses = licenses;
    } //-- void setLicenses(java.util.List) 

    /**
     * Set Local configuration information.
     * 
     * @param local
     */
    public void setLocal(Local local)
    {
        this.local = local;
    } //-- void setLocal(Local) 

    /**
     * Set The logo for the project.
     * 
     * @param logo
     */
    public void setLogo(String logo)
    {
        this.logo = logo;
    } //-- void setLogo(String) 

    /**
     * Set The mailing lists for the project.
     * 
     * @param mailingLists
     */
    public void setMailingLists(java.util.List mailingLists)
    {
        this.mailingLists = mailingLists;
    } //-- void setMailingLists(java.util.List) 

    /**
     * Set The version of this model you are using.
     * 
     * @param modelVersion
     */
    public void setModelVersion(String modelVersion)
    {
        this.modelVersion = modelVersion;
    } //-- void setModelVersion(String) 

    /**
     * Set Human readable name of the project.
     * 
     * @param name
     */
    public void setName(String name)
    {
        this.name = name;
    } //-- void setName(String) 

    /**
     * Set 
     *             This element describes various attributes of the
     * organziation to
     *             which the project belongs.  These attributes are
     * utilized when
     *             documentation is created (for copyright notices
     * and links).
     *           
     * 
     * @param organization
     */
    public void setOrganization(Organization organization)
    {
        this.organization = organization;
    } //-- void setOrganization(Organization) 

    /**
     * Set 
     *             This element describes all of the dependency
     * overrides for a
     *             project.  Each dependency is described by a
     *             <code>override</code> element, which is then
     * described by
     *             additional elements (described below).
     *           
     * 
     * @param overrides
     */
    public void setOverrides(java.util.List overrides)
    {
        this.overrides = overrides;
    } //-- void setOverrides(java.util.List) 

    /**
     * Set Package groups required for complete javadocs.
     * 
     * @param packageGroups
     */
    public void setPackageGroups(java.util.List packageGroups)
    {
        this.packageGroups = packageGroups;
    } //-- void setPackageGroups(java.util.List) 

    /**
     * Set Specified which project to extend.
     * 
     * @param parent
     */
    public void setParent(Parent parent)
    {
        this.parent = parent;
    } //-- void setParent(Parent) 

    /**
     * Set The lists of the remote repositories for discovering
     * plugins
     * 
     * @param pluginRepositories
     */
    public void setPluginRepositories(java.util.List pluginRepositories)
    {
        this.pluginRepositories = pluginRepositories;
    } //-- void setPluginRepositories(java.util.List) 

    /**
     * Set Set of decorator(s) injected after the target goal(s).
     * 
     * @param postGoals
     */
    public void setPostGoals(java.util.List postGoals)
    {
        this.postGoals = postGoals;
    } //-- void setPostGoals(java.util.List) 

    /**
     * Set Set of decorator(s) injected before the target goal(s).
     * 
     * @param preGoals
     */
    public void setPreGoals(java.util.List preGoals)
    {
        this.preGoals = preGoals;
    } //-- void setPreGoals(java.util.List) 

    /**
     * Set 
     *             Properties about the project. This allows you to
     * configure your project and the
     *             plugins it uses.
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
     *             This element includes the specification of
     * reports to be
     *             included in a Maven-generated site.  These
     * reports will be run
     *             when a user executes
     *             <code>maven site</code>.  All of the
     *             reports will be included in the navigation bar
     * for browsing in
     *             the order they are specified.
     *           
     * 
     * @param reports
     */
    public void setReports(java.util.List reports)
    {
        this.reports = reports;
    } //-- void setReports(java.util.List) 

    /**
     * Set The lists of the remote repositories
     * 
     * @param repositories
     */
    public void setRepositories(java.util.List repositories)
    {
        this.repositories = repositories;
    } //-- void setRepositories(java.util.List) 

    /**
     * Set Specification for the SCM use by the project.
     * 
     * @param scm
     */
    public void setScm(Scm scm)
    {
        this.scm = scm;
    } //-- void setScm(Scm) 

    /**
     * Set An abbreviated description of the project.
     * 
     * @param shortDescription
     */
    public void setShortDescription(String shortDescription)
    {
        this.shortDescription = shortDescription;
    } //-- void setShortDescription(String) 

    /**
     * Set The type of artifact this project produces.
     * 
     * @param type
     */
    public void setType(String type)
    {
        this.type = type;
    } //-- void setType(String) 

    /**
     * Set The URL where the project can be found.
     * 
     * @param url
     */
    public void setUrl(String url)
    {
        this.url = url;
    } //-- void setUrl(String) 

    /**
     * Set The current version of the project.
     * 
     * @param version
     */
    public void setVersion(String version)
    {
        this.version = version;
    } //-- void setVersion(String) 


            private String packageName;

            public void setPackage(String packageName)
            {
            this.packageName = packageName;
            }

            public String getPackage()
            {
            return packageName;
            }
          
            public String getId()
            {
            StringBuffer id = new StringBuffer();

            id.append( getGroupId() );
            id.append( ":" );
            id.append( getArtifactId() );
            id.append( ":" );
            id.append( getType() );
            id.append( ":" );
            id.append( getVersion() );

            return id.toString();
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
