package org.codehaus.modello;

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

/**
 * @goal java
 *
 * @phase generate-sources
 *
 * @description Creates java beans from the Modello model.
 *
 * @parameter
 * name="outputDirectory"
 * type="java.lang.String"
 * required="true"
 * validator=""
 * expression="#basedir/target/generated-sources"
 * description=""
 *
 * @parameter name="project"
 * type=""
 * required="true"
 * validator=""
 * expression="#project"
 * description="The current project"
 *
 * @parameter name="model"
 * type="java.io.File"
 * required="true"
 * validator=""
 * expression="#model"
 * description="The modello model file."
 *
 * @parameter
 * name="version"
 * type="java.lang.String"
 * required="true"
 * validator=""
 * expression="#version"
 * description="The modello model version to use."
 *
 * @parameter
 * name="packageWithVersion"
 * type="java.lang.Boolean"
 * required="true"
 * validator=""
 * expression="#packageWithVersion"
 * default="false"
 * description="True if the generated package names should include the version."
 *
 * @parameter
 * name="modelloCore"
 * type=""
 * required="true"
 * validator=""
 * expression="#component.org.codehaus.modello.core.ModelloCore"
 * description="Modello component"
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ModelloJavaMojo
    extends AbstractModelloGeneratorMojo
{
    protected String getGeneratorType()
    {
        return "java";
    }
}
