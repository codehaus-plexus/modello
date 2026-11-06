/*
 * Copyright 2026 Fridrich Strba
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.modello.ant;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.codehaus.modello.Modello;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.plexus.util.xml.XmlStreamReader;

/**
 * Ant task running Modello generators, the counterpart of the modello-maven-plugin goals.
 *
 * <p>Required: {@code version}, {@code outputDirectory}, at least one nested {@code <model>} and one {@code <goal>}.
 * Each model is generated once per goal; see README.md for all attributes and nested elements.
 */
public class ModelloTask extends Task {
    private String version;
    private File velocityBasedir;
    private File outputDirectory;
    private boolean packageWithVersion = false;
    private String javaSource = "8";
    private String encoding = "utf-8";
    private boolean domAsXpp3 = true;
    private File licenseFile;
    private String licenseText;
    private String extendedClassnameSuffix;
    private String xsdFileName;
    private Boolean enforceMandatoryElements;
    private String jsonSchemaFileName;
    private String firstVersion;
    private String xdocFileName;

    private List<File> models = new ArrayList<>();
    private List<String> templates = new ArrayList<>();
    private List<String> goals = new ArrayList<>();
    private List<String> packagedVersions = new ArrayList<>();
    private Map<String, String> velocityParams = new HashMap<>();
    private Map<String, String> pluralExceptions = new HashMap<>();
    private Map<String, String> properties = new HashMap<>();

    // Attribute Setters
    /** Model version to generate. Required. */
    public void setVersion(String version) {
        this.version = version;
    }

    /** Base directory of Velocity templates; defaults to the project base directory. */
    public void setVelocityBasedir(File velocityBasedir) {
        this.velocityBasedir = velocityBasedir;
    }

    /** Directory for generated files; created if missing. Required. */
    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /** Append the version to generated package names. Default {@code false}. */
    public void setPackageWithVersion(boolean packageWithVersion) {
        this.packageWithVersion = packageWithVersion;
    }

    /** Comma-separated historical versions, each generated into a versioned package. */
    public void setPackagedVersions(String packagedVersions) {
        if (packagedVersions != null) {
            for (String v : packagedVersions.split(",")) {
                String trimmed = v.trim();
                if (!trimmed.isEmpty()) {
                    this.packagedVersions.add(trimmed);
                }
            }
        }
    }

    /** Java source level of generated code. Default {@code 8}. */
    public void setJavaSource(String javaSource) {
        this.javaSource = javaSource;
    }

    /** Encoding of generated files. Default {@code utf-8}. */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /** Map DOM content to {@code Xpp3Dom} instead of {@code org.w3c.dom.Element}. Default {@code true}. */
    public void setDomAsXpp3(boolean domAsXpp3) {
        this.domAsXpp3 = domAsXpp3;
    }

    /** File with license header text. Exclusive with {@link #setLicenseText(String)}. */
    public void setLicenseFile(File licenseFile) {
        this.licenseFile = licenseFile;
    }

    /** License header text. Exclusive with {@link #setLicenseFile(File)}. */
    public void setLicenseText(String licenseText) {
        this.licenseText = licenseText;
    }

    /** Writer class name suffix for {@code xpp3-extended-writer}. Default {@code Ex}. */
    public void setExtendedClassnameSuffix(String extendedClassnameSuffix) {
        this.extendedClassnameSuffix = extendedClassnameSuffix;
    }

    /** Output file name for the {@code xsd} goal. */
    public void setXsdFileName(String xsdFileName) {
        this.xsdFileName = xsdFileName;
    }

    /** Enforce mandatory elements in the generated XSD. Default {@code false}. */
    public void setEnforceMandatoryElements(boolean enforceMandatoryElements) {
        this.enforceMandatoryElements = enforceMandatoryElements;
    }

    /** Output file name for the {@code jsonschema} goal. */
    public void setJsonSchemaFileName(String jsonSchemaFileName) {
        this.jsonSchemaFileName = jsonSchemaFileName;
    }

    /** First model version; xdoc shows the 'since' column for newer versions. */
    public void setFirstVersion(String firstVersion) {
        this.firstVersion = firstVersion;
    }

    /** Output file name for the {@code xdoc} goal. */
    public void setXdocFileName(String xdocFileName) {
        this.xdocFileName = xdocFileName;
    }

    // Nested Elements Handlers
    /** Nested {@code <model file="..."/>}: model file to read. At least one required. */
    public void addConfiguredModel(ModelElement m) {
        if (m.getFile() == null) {
            throw new BuildException("The 'file' attribute is required for <model>.");
        }
        this.models.add(m.getFile());
    }

    /** Nested {@code <template name="..."/>}: Velocity template relative to {@code velocityBasedir}. */
    public void addConfiguredTemplate(NameElement t) {
        this.templates.add(requireName(t.getName(), "template"));
    }

    /** Nested {@code <goal name="..."/>}: generator id, e.g. {@code java}. At least one required. */
    public void addConfiguredGoal(NameElement g) {
        this.goals.add(requireName(g.getName(), "goal"));
    }

    /** Nested {@code <packagedVersion name="..."/>}: same as {@link #setPackagedVersions(String)}. */
    public void addConfiguredPackagedVersion(NameElement v) {
        this.packagedVersions.add(requireName(v.getName(), "packagedVersion"));
    }

    /** Nested {@code <param name="..." value="..."/>}: parameter passed to Velocity templates. */
    public void addConfiguredParam(ParamElement p) {
        this.velocityParams.put(requireName(p.getName(), "param"), p.getValue());
    }

    /** Nested {@code <pluralException name="plural" value="singular"/>}. */
    public void addConfiguredPluralException(ParamElement p) {
        this.pluralExceptions.put(requireName(p.getName(), "pluralException"), p.getValue());
    }

    /** Nested {@code <property name="..." value="..."/>}: raw Modello parameter. */
    public void addConfiguredProperty(ParamElement p) {
        this.properties.put(requireName(p.getName(), "property"), p.getValue());
    }

    /** Nested {@code <license file="..."/>} or {@code <license>text</license>}; only one source allowed. */
    public void addConfiguredLicense(LicenseElement l) {
        if (l.getFile() != null) {
            this.licenseFile = l.getFile();
        }
        if (l.getText() != null) {
            this.licenseText = l.getText();
        }
        if (l.getFile() == null && l.getText() == null) {
            throw new BuildException("Either 'file' or 'text' attribute is required for <license>.");
        }
    }

    private static String requireName(String name, String elementTag) {
        if (name == null || name.trim().isEmpty()) {
            throw new BuildException("The 'name' attribute is required for <" + elementTag + ">.");
        }
        return name.trim();
    }

    @Override
    public void execute() throws BuildException {
        if (version == null || outputDirectory == null || models.isEmpty() || goals.isEmpty()) {
            throw new BuildException("version, outputDirectory, <model>, and <goal> are required.");
        }

        if (!outputDirectory.mkdirs() && !outputDirectory.isDirectory()) {
            throw new BuildException("Failed to create output directory: " + outputDirectory.getAbsolutePath());
        }

        try {
            Modello modello = new Modello();
            Map<String, Object> parameters = new HashMap<>();

            parameters.put(ModelloParameterConstants.OUTPUT_DIRECTORY, outputDirectory.getAbsolutePath());
            parameters.put(ModelloParameterConstants.VERSION, version);
            parameters.put(ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.toString(packageWithVersion));
            parameters.put(ModelloParameterConstants.OUTPUT_JAVA_SOURCE, javaSource);
            parameters.put(ModelloParameterConstants.ENCODING, encoding);
            parameters.put(ModelloParameterConstants.DOM_AS_XPP3, Boolean.toString(domAsXpp3));
            if (!pluralExceptions.isEmpty()) {
                parameters.put(ModelloParameterConstants.PLURAL_EXCEPTIONS, pluralExceptions);
            }
            if (!packagedVersions.isEmpty()) {
                parameters.put(ModelloParameterConstants.ALL_VERSIONS, String.join(",", packagedVersions));
            }
            if (licenseText != null && licenseFile != null) {
                throw new BuildException("Specify either a license file or license text, not both.");
            }
            if (licenseText != null) {
                parameters.put(ModelloParameterConstants.LICENSE_TEXT, Arrays.asList(licenseText.split("\\r?\\n")));
            } else if (licenseFile != null) {
                if (!licenseFile.exists()) {
                    throw new BuildException("License file not found: " + licenseFile.getAbsolutePath());
                }
                try {
                    parameters.put(ModelloParameterConstants.LICENSE_TEXT, Files.readAllLines(licenseFile.toPath()));
                } catch (IOException e) {
                    throw new BuildException("Failed to read license file: " + licenseFile.getAbsolutePath(), e);
                }
            }
            if (extendedClassnameSuffix != null) {
                parameters.put(ModelloParameterConstants.EXTENDED_CLASSNAME_SUFFIX, extendedClassnameSuffix);
            }
            if (xsdFileName != null) {
                parameters.put(ModelloParameterConstants.OUTPUT_XSD_FILE_NAME, xsdFileName);
            }
            if (enforceMandatoryElements != null) {
                parameters.put(
                        ModelloParameterConstants.XSD_ENFORCE_MANDATORY_ELEMENTS,
                        Boolean.toString(enforceMandatoryElements));
            }
            if (jsonSchemaFileName != null) {
                parameters.put(ModelloParameterConstants.OUTPUT_JSONSCHEMA_FILE_NAME, jsonSchemaFileName);
            }
            if (firstVersion != null) {
                parameters.put(ModelloParameterConstants.FIRST_VERSION, firstVersion);
            }
            if (xdocFileName != null) {
                parameters.put(ModelloParameterConstants.OUTPUT_XDOC_FILE_NAME, xdocFileName);
            }

            // Arbitrary properties
            parameters.putAll(properties);

            // Like ModelloVelocityMojo: basedir defaults to project basedir
            File basedir = velocityBasedir != null
                    ? velocityBasedir
                    : getProject() != null ? getProject().getBaseDir() : null;
            if (basedir != null) {
                parameters.put("modello.velocity.basedir", basedir.getAbsolutePath());
            }
            parameters.put("modello.velocity.templates", String.join(",", templates));
            parameters.put("modello.velocity.parameters", velocityParams);

            for (File modelFile : models) {
                if (!modelFile.isFile()) {
                    throw new BuildException("Model file not found: " + modelFile.getAbsolutePath());
                }
            }

            for (File modelFile : models) {
                log("Generating sources for " + modelFile.getName());
                for (String goal : goals) {
                    generate(modello, modelFile, goal, parameters);
                }
            }
        } catch (BuildException e) {
            throw e;
        } catch (Exception e) {
            throw new BuildException("Modello generation failed: " + e.getMessage(), e);
        }
    }

    // Mirrors AbstractModelloGeneratorMojo#doExecute: current version, then each packaged one in its own package
    private void generate(Modello modello, File modelFile, String goal, Map<String, Object> parameters)
            throws Exception {
        try (XmlStreamReader reader = new XmlStreamReader(modelFile)) {
            modello.generate(reader, goal, parameters);
        }
        Map<String, Object> versioned = new HashMap<>(parameters);
        versioned.put(ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.toString(true));
        for (String packagedVersion : packagedVersions) {
            versioned.put(ModelloParameterConstants.VERSION, packagedVersion);
            try (XmlStreamReader reader = new XmlStreamReader(modelFile)) {
                modello.generate(reader, goal, versioned);
            }
        }
    }

    // Helper classes for Ant's nested XML elements
    /** Nested element with a {@code file} attribute. */
    public static class ModelElement {
        private File file;

        public void setFile(File file) {
            this.file = file;
        }

        public File getFile() {
            return file;
        }
    }

    /** Nested element with a {@code name} attribute. */
    public static class NameElement {
        private String name;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /** Nested element with {@code name} and {@code value} attributes. */
    public static class ParamElement {
        private String name;
        private String value;

        public void setName(String name) {
            this.name = name;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }

    /** Nested {@code <license>}: {@code file}, {@code text} attribute or nested text. */
    public static class LicenseElement {
        private File file;
        private String text;
        private boolean textAttribute;

        public void setFile(File file) {
            this.file = file;
        }

        public File getFile() {
            return file;
        }

        public void setText(String text) {
            this.text = text;
            this.textAttribute = true;
        }

        public String getText() {
            return text;
        }

        // Ant may deliver PCDATA in chunks
        public void addText(String text) {
            if (textAttribute) {
                throw new BuildException("Use either the 'text' attribute or nested text for <license>, not both.");
            }
            this.text = this.text == null ? text : this.text + text;
        }
    }
}
