package org.codehaus.modello.maven;

/*
 * Copyright (c) 2004, Codehaus.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.io.File;
import java.util.Properties;

import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.modello.ModelloParameterConstants;

/**
 * @author Hervé Boutemy
 */
public abstract class AbstractModelloSourceGeneratorMojo
    extends AbstractModelloGeneratorMojo
{
    /**
     * The output directory of the generated Java beans.
     */
    @Parameter( defaultValue = "${project.build.directory}/generated-sources/modello", required = true )
    private File outputDirectory;

    /**
     * The encoding to use when generating Java source files.
     *
     * @since 1.0-alpha-19
     */
    @Parameter( defaultValue = "${project.build.sourceEncoding}" )
    private String encoding;

    /**
     * Generate Java 5 sources, with generic collections.
     * @since 1.0
     */
    @Parameter( defaultValue = "${maven.compiler.source}" )
    private String javaSource;

    /**
     * Generate DOM content as plexus-utils <code>Xpp3Dom</code> objects instead of <code>org.w3c.dom.Element</code>.
     * @since 1.6
     */
    @Parameter( defaultValue = "true" )
    private boolean domAsXpp3;

    /**
     * Generate DOM with custom interfaces.  If set, this flag takes precedence over the {@link #domAsXpp3} one.
     *
     * @since 2.1
     * @see #domAsCustomInterface
     * @see #domAsCustomBuilder
     * @see #domAsCustomLocationBuilder
     */
    @Parameter
    private String domAsCustom;

    /**
     * If the {@link #domAsCustom} flag is set, this property should hold
     * the name of the Dom node interface to use. The interface (<code>Dom</code>
     * in the following definition) is supposed to define the following methods:
     * <ul>
     *     <li><code></code>String getName()</code></li>
     *     <li><code>String getValue()</code></li>
     *     <li><code>Map<String, String> getAttributes()</code></li>
     *     <li><code>Collection<? extends Dom> getChildren()</code></li>
     *     <li><code>Object getInputLocation()</code> if location tracking is enabled</li>
     * </ul>
     * @since 2.1
     */
    @Parameter
    private String domAsCustomInterface;

    /**
     * If the {@link #domAsCustom} flag is set, this property should hold
     * the name of the Dom builder class to use.  This class is supposed to define
     * a <code>public static Dom build(XmlPullParser, boolean)</code> method, where
     * <code>Dom</code> is the name given by the {@link #domAsCustomInterface}
     * property and, if location tracking is enabled, a
     * <code>public static Dom build(XmlPullParser, boolean, LocationBuilder)</code>
     * where the <code>LocationBuilder</code> is actually the value of the
     * {@link #domAsCustomLocationBuilder} property.
     *
     * @since 2.1
     * @see #domAsCustom
     * @see #domAsCustomInterface
     * @see #domAsCustomLocationBuilder
     */
    @Parameter
    private String domAsCustomBuilder;

    /**
     * The name of the class to use for location tracking.
     * The class is supposed to define a constructor which will receive the input location data class
     * and instances of those classes will be passed to the <code>build</code> method of the builder.
     *
     * @since 2.1
     * @see #domAsCustom
     * @see #domAsCustomBuilder
     */
    @Parameter
    private String domAsCustomLocationBuilder;

    @Override
    protected boolean producesCompilableResult()
    {
        return true;
    }

    public File getOutputDirectory()
    {
        return outputDirectory;
    }

    public void setOutputDirectory( File outputDirectory )
    {
        this.outputDirectory = outputDirectory;
    }

    @Override
    protected void customizeParameters( Properties parameters )
    {
        super.customizeParameters( parameters );

        if ( encoding != null )
        {
            parameters.setProperty( ModelloParameterConstants.ENCODING, encoding );
        }

        if ( javaSource != null )
        {
            if ( javaSource.startsWith( "1." ) )
            {
                javaSource = javaSource.substring( "1.".length() );
            }
            parameters.setProperty( ModelloParameterConstants.OUTPUT_JAVA_SOURCE, javaSource );
        }

        parameters.setProperty( ModelloParameterConstants.DOM_AS_XPP3, Boolean.toString( domAsXpp3 ) );

        if ( domAsCustom != null )
        {
            parameters.setProperty( ModelloParameterConstants.DOM_AS_CUSTOM, domAsCustom );
        }
        if ( domAsCustomInterface != null )
        {
            parameters.setProperty( ModelloParameterConstants.DOM_AS_CUSTOM_INTERFACE, domAsCustomInterface );
        }
        if ( domAsCustomBuilder != null )
        {
            parameters.setProperty( ModelloParameterConstants.DOM_AS_CUSTOM_BUILDER, domAsCustomBuilder );
        }
        if ( domAsCustomLocationBuilder != null )
        {
            parameters.setProperty( ModelloParameterConstants.DOM_AS_CUSTOM_LOCATION_BUILDER, domAsCustomLocationBuilder );
        }
    }
}
