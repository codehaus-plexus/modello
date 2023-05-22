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

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.modello.ModelloParameterConstants;

/**
 * Creates an XML schema from the model.
 *
 * @author <a href="mailto:brett@codehaus.org">Brett Porter</a>
 */
@Mojo(name = "xsd", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class ModelloXsdMojo extends AbstractModelloGeneratorMojo {
    /**
     * The output directory of the generated XML Schema.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-site/resources/xsd", required = true)
    private File outputDirectory;

    /**
     * Boolean flag to enforce mandatory elements in the XML schema.
     * If set to {@code false} the XSD won't enforce mandatory elements in the XML which can be useful if the XML is post processed (e.g. POM merging with parents).
     * The default value is {@code false} for backwards compatibility reasons, but should be set to {@code true} for most cases.
     * @since 2.1.0
     */
    @Parameter(defaultValue = "false")
    private boolean enforceMandatoryElements;

    /**
     *
     * @since 1.0-alpha-21
     */
    @Parameter
    private String xsdFileName;

    protected String getGeneratorType() {
        return "xsd";
    }

    protected void customizeParameters(Properties parameters) {
        super.customizeParameters(parameters);

        if (xsdFileName != null) {
            parameters.put(ModelloParameterConstants.OUTPUT_XSD_FILE_NAME, xsdFileName);
        }
        parameters.put(ModelloParameterConstants.XSD_ENFORCE_MANDATORY_ELEMENTS, enforceMandatoryElements);
    }

    protected boolean producesCompilableResult() {
        return false;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
}
