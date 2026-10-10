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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModelloCliTest {

    @Test
    void testLegacyPositionalArguments() throws Exception {
        String[] args = {"model.mdo", "java", "target/generated-sources", "1.0.0", "false", "11", "UTF-8"};

        ModelloCli.parseArguments(args);

        assertEquals(new File("model.mdo"), ModelloCli.getModelFile());
        assertEquals("java", ModelloCli.getOutputType());
        Map<String, Object> params = ModelloCli.getParameters();
        assertEquals("target/generated-sources", params.get(ModelloParameterConstants.OUTPUT_DIRECTORY));
        assertEquals("1.0.0", params.get(ModelloParameterConstants.VERSION));
        assertEquals("false", params.get(ModelloParameterConstants.PACKAGE_WITH_VERSION));
        assertEquals("11", params.get(ModelloParameterConstants.OUTPUT_JAVA_SOURCE));
        assertEquals("UTF-8", params.get(ModelloParameterConstants.ENCODING));
    }

    @Test
    void testLegacyMissingArgsThrows() {
        String[] args = {"model.mdo", "java"};
        assertThrows(IllegalArgumentException.class, () -> ModelloCli.parseArguments(args));
    }

    @Test
    void testNamedWordOptions() throws Exception {
        String[] args = {
            "--model", "src/model.mdo",
            "--type", "java",
            "--dir", "target/out",
            "--version", "1.0.0",
            "--packageWithVersion", "true",
            "--source", "17",
            "--encoding", "ISO-8859-1"
        };

        ModelloCli.parseArguments(args);

        assertEquals(new File("src/model.mdo"), ModelloCli.getModelFile());
        assertEquals("java", ModelloCli.getOutputType());
        Map<String, Object> params = ModelloCli.getParameters();
        assertEquals("target/out", params.get(ModelloParameterConstants.OUTPUT_DIRECTORY));
        assertEquals("1.0.0", params.get(ModelloParameterConstants.VERSION));
        assertEquals("true", params.get(ModelloParameterConstants.PACKAGE_WITH_VERSION));
        assertEquals("17", params.get(ModelloParameterConstants.OUTPUT_JAVA_SOURCE));
        assertEquals("ISO-8859-1", params.get(ModelloParameterConstants.ENCODING));
    }

    @Test
    void testSingleLetterOptionsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ModelloCli.parseArguments(
                        new String[] {"-m", "model.mdo", "--type", "java", "--dir", "out", "--version", "1.0.0"}));
        assertThrows(
                IllegalArgumentException.class,
                () -> ModelloCli.parseArguments(
                        new String[] {"--model", "model.mdo", "-t", "java", "--dir", "out", "--version", "1.0.0"}));
        assertThrows(
                IllegalArgumentException.class,
                () -> ModelloCli.parseArguments(
                        new String[] {"--model", "model.mdo", "--type", "java", "-d", "out", "--version", "1.0.0"}));
        assertThrows(
                IllegalArgumentException.class,
                () -> ModelloCli.parseArguments(
                        new String[] {"--model", "model.mdo", "--type", "java", "--dir", "out", "-v", "1.0.0"}));
    }

    @Test
    void testNamedCamelCaseOptions() throws Exception {
        String[] args = {
            "--modelFile",
            "model.mdo",
            "--outputType",
            "xpp3-reader",
            "--outputDirectory",
            "target/out",
            "--modelVersion",
            "1.1.0",
            "--packageWithVersion",
            "--javaSource",
            "11",
            "--domAsXpp3",
            "true",
            "--velocityBasedir",
            "templates",
            "--templates",
            "t1.vm,t2.vm",
            "--params",
            "k1=v1,k2=v2",
            "--xsdFileName",
            "model.xsd",
            "--jsonSchemaFileName",
            "model.json",
            "--enforceMandatoryElements",
            "--extendedClassnameSuffix",
            "Ex",
            "--firstVersion",
            "1.0.0",
            "--xdocFileName",
            "model.xml",
            "--pluralExceptions",
            "kisses=kiss,parties=party"
        };

        ModelloCli.parseArguments(args);

        assertEquals(new File("model.mdo"), ModelloCli.getModelFile());
        assertEquals("xpp3-reader", ModelloCli.getOutputType());
        Map<String, Object> params = ModelloCli.getParameters();
        assertEquals("target/out", params.get(ModelloParameterConstants.OUTPUT_DIRECTORY));
        assertEquals("1.1.0", params.get(ModelloParameterConstants.VERSION));
        assertEquals("true", params.get(ModelloParameterConstants.PACKAGE_WITH_VERSION));
        assertEquals("11", params.get(ModelloParameterConstants.OUTPUT_JAVA_SOURCE));
        assertEquals("true", params.get(ModelloParameterConstants.DOM_AS_XPP3));
        assertEquals("templates", params.get(ModelloParameterConstants.VELOCITY_BASEDIR));
        assertEquals("t1.vm,t2.vm", params.get(ModelloParameterConstants.VELOCITY_TEMPLATES));
        assertEquals("model.xsd", params.get(ModelloParameterConstants.OUTPUT_XSD_FILE_NAME));
        assertEquals("model.json", params.get(ModelloParameterConstants.OUTPUT_JSONSCHEMA_FILE_NAME));
        assertEquals("true", params.get(ModelloParameterConstants.XSD_ENFORCE_MANDATORY_ELEMENTS));
        assertEquals("Ex", params.get(ModelloParameterConstants.EXTENDED_CLASSNAME_SUFFIX));
        assertEquals("1.0.0", params.get(ModelloParameterConstants.FIRST_VERSION));
        assertEquals("model.xml", params.get(ModelloParameterConstants.OUTPUT_XDOC_FILE_NAME));

        @SuppressWarnings("unchecked")
        Map<String, String> vParams = (Map<String, String>) params.get(ModelloParameterConstants.VELOCITY_PARAMETERS);
        assertEquals("v1", vParams.get("k1"));

        @SuppressWarnings("unchecked")
        Map<String, String> plurals = (Map<String, String>) params.get(ModelloParameterConstants.PLURAL_EXCEPTIONS);
        assertEquals("kiss", plurals.get("kisses"));
        assertEquals("party", plurals.get("parties"));
    }

    @Test
    void testNamedKebabCaseOptions() throws Exception {
        String[] args = {
            "--model-file",
            "model.mdo",
            "--output-type",
            "xpp3-writer",
            "--output-directory",
            "target/out",
            "--model-version",
            "1.2.0",
            "--package-with-version",
            "--java-source",
            "17",
            "--dom-as-xpp3",
            "false",
            "--velocity-basedir",
            "vtemplates",
            "--velocity-templates",
            "t1.vm",
            "--velocity-parameters",
            "k1=v1",
            "--xsd-file-name",
            "model.xsd",
            "--json-schema-file-name",
            "model.json",
            "--enforce-mandatory-elements",
            "--extended-classname-suffix",
            "Ex",
            "--first-version",
            "1.0.0",
            "--xdoc-file-name",
            "model.xml",
            "--plural-exceptions",
            "kisses=kiss"
        };

        ModelloCli.parseArguments(args);

        assertEquals(new File("model.mdo"), ModelloCli.getModelFile());
        assertEquals("xpp3-writer", ModelloCli.getOutputType());
        Map<String, Object> params = ModelloCli.getParameters();
        assertEquals("target/out", params.get(ModelloParameterConstants.OUTPUT_DIRECTORY));
        assertEquals("1.2.0", params.get(ModelloParameterConstants.VERSION));
        assertEquals("true", params.get(ModelloParameterConstants.PACKAGE_WITH_VERSION));
        assertEquals("17", params.get(ModelloParameterConstants.OUTPUT_JAVA_SOURCE));
        assertEquals("false", params.get(ModelloParameterConstants.DOM_AS_XPP3));
        assertEquals("vtemplates", params.get(ModelloParameterConstants.VELOCITY_BASEDIR));
        assertEquals("t1.vm", params.get(ModelloParameterConstants.VELOCITY_TEMPLATES));
        assertEquals("model.xsd", params.get(ModelloParameterConstants.OUTPUT_XSD_FILE_NAME));
        assertEquals("model.json", params.get(ModelloParameterConstants.OUTPUT_JSONSCHEMA_FILE_NAME));
        assertEquals("true", params.get(ModelloParameterConstants.XSD_ENFORCE_MANDATORY_ELEMENTS));
        assertEquals("Ex", params.get(ModelloParameterConstants.EXTENDED_CLASSNAME_SUFFIX));
        assertEquals("1.0.0", params.get(ModelloParameterConstants.FIRST_VERSION));
        assertEquals("model.xml", params.get(ModelloParameterConstants.OUTPUT_XDOC_FILE_NAME));
    }

    @Test
    void testNamedEqualsSyntax() throws Exception {
        String[] args = {
            "--model=src/model.xml",
            "--type=java,xpp3-reader",
            "--dir=target/gen",
            "--version=2.0.0",
            "--source=21",
            "--domAsXpp3=false"
        };

        ModelloCli.parseArguments(args);

        assertEquals(new File("src/model.xml"), ModelloCli.getModelFile());
        assertEquals("java,xpp3-reader", ModelloCli.getOutputType());
        Map<String, Object> params = ModelloCli.getParameters();
        assertEquals("target/gen", params.get(ModelloParameterConstants.OUTPUT_DIRECTORY));
        assertEquals("2.0.0", params.get(ModelloParameterConstants.VERSION));
        assertEquals("21", params.get(ModelloParameterConstants.OUTPUT_JAVA_SOURCE));
        assertEquals("false", params.get(ModelloParameterConstants.DOM_AS_XPP3));
        assertEquals("false", params.get(ModelloParameterConstants.PACKAGE_WITH_VERSION));
    }

    @Test
    void testTrailingModelFile() throws Exception {
        String[] args = {
            "--type", "java",
            "--dir", "target/gen",
            "--version", "1.0.0",
            "src/model.mdo"
        };

        ModelloCli.parseArguments(args);

        assertEquals(new File("src/model.mdo"), ModelloCli.getModelFile());
        assertEquals("java", ModelloCli.getOutputType());
    }

    @Test
    void testGenericSystemPropertyOption() throws Exception {
        String[] args = {
            "--model",
            "model.mdo",
            "--type",
            "java",
            "--dir",
            "out",
            "--version",
            "1.0.0",
            "-Dcustom.plugin.param=customValue",
            "-DdomAsXpp3=false"
        };

        ModelloCli.parseArguments(args);

        Map<String, Object> params = ModelloCli.getParameters();
        assertEquals("customValue", params.get("custom.plugin.param"));
        assertEquals("false", params.get(ModelloParameterConstants.DOM_AS_XPP3));
    }

    @Test
    void testLicenseFileAndText(@TempDir Path tempDir) throws Exception {
        Path licPath = tempDir.resolve("LICENSE.txt");
        Files.write(licPath, Arrays.asList("Line 1", "Line 2"));

        String[] args = {
            "--model", "model.mdo",
            "--type", "java",
            "--dir", "out",
            "--version", "1.0.0",
            "--licenseFile", licPath.toString()
        };

        ModelloCli.parseArguments(args);

        @SuppressWarnings("unchecked")
        List<String> license = (List<String>) ModelloCli.getParameters().get(ModelloParameterConstants.LICENSE_TEXT);
        assertNotNull(license);
        assertEquals(2, license.size());
        assertEquals("Line 1", license.get(0));
        assertEquals("Line 2", license.get(1));
    }

    @Test
    void testLicenseTextDirect() throws Exception {
        String[] args = {
            "--model", "model.mdo",
            "--type", "java",
            "--dir", "out",
            "--version", "1.0.0",
            "--licenseText", "Line A\nLine B"
        };

        ModelloCli.parseArguments(args);

        @SuppressWarnings("unchecked")
        List<String> license = (List<String>) ModelloCli.getParameters().get(ModelloParameterConstants.LICENSE_TEXT);
        assertNotNull(license);
        assertEquals(2, license.size());
        assertEquals("Line A", license.get(0));
        assertEquals("Line B", license.get(1));
    }

    @Test
    void testMissingRequiredNamedParamsThrows() {
        // Missing model
        assertThrows(
                IllegalArgumentException.class,
                () -> ModelloCli.parseArguments(new String[] {"--type", "java", "--dir", "out", "--version", "1.0.0"}));

        // Missing type
        assertThrows(
                IllegalArgumentException.class,
                () -> ModelloCli.parseArguments(
                        new String[] {"--model", "model.mdo", "--dir", "out", "--version", "1.0.0"}));

        // Missing dir
        assertThrows(
                IllegalArgumentException.class,
                () -> ModelloCli.parseArguments(
                        new String[] {"--model", "model.mdo", "--type", "java", "--version", "1.0.0"}));

        // Missing version
        assertThrows(
                IllegalArgumentException.class,
                () -> ModelloCli.parseArguments(
                        new String[] {"--model", "model.mdo", "--type", "java", "--dir", "out"}));
    }
}
