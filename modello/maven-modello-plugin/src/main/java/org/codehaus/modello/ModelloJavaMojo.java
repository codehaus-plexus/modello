package org.codehaus.mojo.modello;

/*
 * LICENSE
 */

/**
 * @goal java
 *
 * @description Creates java beans from the Modello model.
 *
 * @parameter
 *  name="outputDirectory"
 *  type="java.lang.String"
 *  required="true"
 *  validator=""
 *  expression="#project.build.sourceDirectory"
 *  description=""
 * @parameter
 *  name="model"
 *  type="java.lang.String"
 *  required="required"
 *  validator=""
 *  expression="#modello.model"
 *  description="The modello model file."
 * @parameter
 *  name="modelVersion"
 *  type="java.lang.String"
 *  required="required"
 *  validator=""
 *  expression="#modello.modelVersion"
 *  description="The modello model version to use."
 * @parameter
 *  name="packageWithVersion"
 *  type="java.lang.Boolean"
 *  required="required"
 *  validator=""
 *  expression="#modello.packageWithVersion"
 *  description="True if the generated package names should include the version."
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
