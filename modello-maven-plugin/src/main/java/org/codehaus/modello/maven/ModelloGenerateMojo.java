package org.codehaus.modello.maven;

/*
 * Copyright (c) 2007, Codehaus.org
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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.modello.plugin.ModelloGenerator;

import java.util.Map;

/**
 * <p>
 * A dynamic way to use generators and Modello plugins.
 * </p>
 *
 * <p>
 * Example Usage:
 * </p>
 * <pre>
 *   &lt;plugin&gt;
 *     &lt;groupId&gt;org.codehaus.modello&lt;/groupId&gt;
 *     &lt;artifactId&gt;modello-maven-plugin&lt;/artifactId&gt;
 *     &lt;version&gt;1.3&lt;/version&gt;
 *     &lt;dependencies&gt;
 *       &lt;dependency&gt;
 *         &lt;groupId&gt;org.codehaus.modello&lt;/groupId&gt;
 *         &lt;artifactId&gt;modello-plugin-jpa&lt;/artifactId&gt;
 *         &lt;version&gt;1.0.0-SNAPSHOT&lt;/version&gt;
 *       &lt;/dependency&gt;
 *     &lt;/dependencies&gt;
 *     &lt;configuration&gt;
 *       &lt;version&gt;1.0.0&lt;/version&gt;
 *       &lt;packageWithVersion&gt;false&lt;/packageWithVersion&gt;
 *       &lt;models&gt;
 *         &lt;model&gt;src/main/mdo/project-model.xml&lt;/model&gt;
 *       &lt;/models&gt;
 *     &lt;/configuration&gt;
 *     &lt;executions&gt;
 *       &lt;execution&gt;
 *         &lt;id&gt;java&lt;/id&gt;
 *         &lt;goals&gt;
 *           &lt;goal&gt;generate&lt;/goal&gt;
 *         &lt;/goals&gt;
 *         &lt;configuration&gt;
 *           &lt;generatorId&gt;java&lt;/generatorId&gt;
 *         &lt;/configuration&gt;
 *       &lt;/execution&gt;
 *       &lt;execution&gt;
 *         &lt;id&gt;jpa&lt;/id&gt;
 *         &lt;goals&gt;
 *           &lt;goal&gt;generate&lt;/goal&gt;
 *         &lt;/goals&gt;
 *         &lt;configuration&gt;
 *           &lt;generatorId&gt;jpa-mapping&lt;/generatorId&gt;
 *         &lt;/configuration&gt;
 *       &lt;/execution&gt;
 *     &lt;/executions&gt;
 *   &lt;/plugin&gt;
 * </pre>
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 */
@Mojo( name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true )
public class ModelloGenerateMojo
    extends AbstractModelloSourceGeneratorMojo
{
    @Component( role = ModelloGenerator.class )
    private Map<String, ModelloGenerator> generatorMap;

    @Parameter( property = "modello.generator.id", defaultValue = "java" )
    private String generatorId;

    protected String getGeneratorType()
    {
        return generatorId;
    }

    public void execute()
        throws MojoExecutionException
    {
        if ( !generatorMap.containsKey( generatorId ) )
        {
            throw new MojoExecutionException( "Unable to execute modello, generator id [" + generatorId
                            + "] not found.  (Available generator ids : " + generatorMap.keySet() + ")" );
        }

        getLog().info( "[modello:generate {generator: " + generatorId + "}]" );

        super.execute();
    }
}
