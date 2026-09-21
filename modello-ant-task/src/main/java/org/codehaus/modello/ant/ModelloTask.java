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
    public void setVersion(String version) {
        this.version = version;
    }

    public void setVelocityBasedir(File velocityBasedir) {
        this.velocityBasedir = velocityBasedir;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void setPackageWithVersion(boolean packageWithVersion) {
        this.packageWithVersion = packageWithVersion;
    }

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

    public void setJavaSource(String javaSource) {
        this.javaSource = javaSource;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setDomAsXpp3(boolean domAsXpp3) {
        this.domAsXpp3 = domAsXpp3;
    }

    public void setLicenseFile(File licenseFile) {
        this.licenseFile = licenseFile;
    }

    public void setLicenseText(String licenseText) {
        this.licenseText = licenseText;
    }

    public void setExtendedClassnameSuffix(String extendedClassnameSuffix) {
        this.extendedClassnameSuffix = extendedClassnameSuffix;
    }

    public void setXsdFileName(String xsdFileName) {
        this.xsdFileName = xsdFileName;
    }

    public void setEnforceMandatoryElements(boolean enforceMandatoryElements) {
        this.enforceMandatoryElements = enforceMandatoryElements;
    }

    public void setJsonSchemaFileName(String jsonSchemaFileName) {
        this.jsonSchemaFileName = jsonSchemaFileName;
    }

    public void setFirstVersion(String firstVersion) {
        this.firstVersion = firstVersion;
    }

    public void setXdocFileName(String xdocFileName) {
        this.xdocFileName = xdocFileName;
    }

    // Nested Elements Handlers
    public void addConfiguredModel(ModelElement m) {
        if (m.getFile() == null) {
            throw new BuildException("The 'file' attribute is required for <model>.");
        }
        this.models.add(m.getFile());
    }

    public void addConfiguredTemplate(NameElement t) {
        this.templates.add(requireName(t.getName(), "template"));
    }

    public void addConfiguredGoal(NameElement g) {
        this.goals.add(requireName(g.getName(), "goal"));
    }

    public void addConfiguredPackagedVersion(NameElement v) {
        this.packagedVersions.add(requireName(v.getName(), "packagedVersion"));
    }

    public void addConfiguredParam(ParamElement p) {
        this.velocityParams.put(requireName(p.getName(), "param"), p.getValue());
    }

    public void addConfiguredPluralException(ParamElement p) {
        this.pluralExceptions.put(requireName(p.getName(), "pluralException"), p.getValue());
    }

    public void addConfiguredProperty(ParamElement p) {
        this.properties.put(requireName(p.getName(), "property"), p.getValue());
    }

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

            // Attach Velocity configs if provided
            if (velocityBasedir != null) {
                parameters.put("modello.velocity.basedir", velocityBasedir.getAbsolutePath());
                parameters.put("modello.velocity.templates", String.join(",", templates));
                parameters.put("modello.velocity.parameters", velocityParams);
            }

            for (File modelFile : models) {
                log("Generating sources for " + modelFile.getName());
                for (String goal : goals) {
                    try (XmlStreamReader reader = new XmlStreamReader(modelFile)) {
                        modello.generate(reader, goal, parameters);
                    }
                }
            }
        } catch (BuildException e) {
            throw e;
        } catch (Exception e) {
            throw new BuildException("Modello generation failed: " + e.getMessage(), e);
        }
    }

    // Helper classes for Ant's nested XML elements
    public static class ModelElement {
        private File file;

        public void setFile(File file) {
            this.file = file;
        }

        public File getFile() {
            return file;
        }
    }

    public static class NameElement {
        private String name;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

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

    public static class LicenseElement {
        private File file;
        private String text;

        public void setFile(File file) {
            this.file = file;
        }

        public File getFile() {
            return file;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void addText(String text) {
            this.text = text;
        }
    }
}
