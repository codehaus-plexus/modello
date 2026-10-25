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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.XmlStreamReader;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class ModelloCli {
    private static File modelFile;

    private static String outputType;

    private static Map<String, Object> parameters;

    private static final Map<String, String> PARAM_ALIASES = new HashMap<>();

    private static final Set<String> BOOLEAN_OPTIONS = new HashSet<>();

    static {
        // Output directory
        alias(ModelloParameterConstants.OUTPUT_DIRECTORY, "--dir", "--outputDirectory", "--output-directory");

        // Version
        alias(ModelloParameterConstants.VERSION, "--version", "--modelVersion", "--model-version");

        // Package with version
        alias(ModelloParameterConstants.PACKAGE_WITH_VERSION, "--packageWithVersion", "--package-with-version");

        // Packaged versions
        alias(
                ModelloParameterConstants.ALL_VERSIONS,
                "--packagedVersions",
                "--packaged-versions",
                "--allVersions",
                "--all-versions");

        // Java Source
        alias(ModelloParameterConstants.OUTPUT_JAVA_SOURCE, "--javaSource", "--java-source", "--source");

        // Encoding
        alias(ModelloParameterConstants.ENCODING, "--encoding");

        // DOM as XPP3
        alias(ModelloParameterConstants.DOM_AS_XPP3, "--domAsXpp3", "--dom-as-xpp3");

        // Velocity Mojo
        alias(
                ModelloParameterConstants.VELOCITY_BASEDIR,
                "--velocityBasedir",
                "--velocity-basedir",
                "--velocityBaseDir");
        alias(
                ModelloParameterConstants.VELOCITY_TEMPLATES,
                "--templates",
                "--velocityTemplates",
                "--velocity-templates");
        alias(
                ModelloParameterConstants.VELOCITY_PARAMETERS,
                "--params",
                "--velocityParameters",
                "--velocity-parameters");

        // XML / Docs / Schema Mojos
        alias(
                ModelloParameterConstants.XSD_ENFORCE_MANDATORY_ELEMENTS,
                "--enforceMandatoryElements",
                "--enforce-mandatory-elements");
        alias(ModelloParameterConstants.OUTPUT_XSD_FILE_NAME, "--xsdFileName", "--xsd-file-name");
        alias(
                ModelloParameterConstants.OUTPUT_JSONSCHEMA_FILE_NAME,
                "--jsonSchemaFileName",
                "--jsonschema-file-name",
                "--json-schema-file-name");
        alias(
                ModelloParameterConstants.EXTENDED_CLASSNAME_SUFFIX,
                "--extendedClassnameSuffix",
                "--extended-classname-suffix");
        alias(ModelloParameterConstants.FIRST_VERSION, "--firstVersion", "--first-version");
        alias(ModelloParameterConstants.OUTPUT_XDOC_FILE_NAME, "--xdocFileName", "--xdoc-file-name");

        // License & Grammar
        alias(ModelloParameterConstants.PLURAL_EXCEPTIONS, "--pluralExceptions", "--plural-exceptions");

        // Boolean flags
        BOOLEAN_OPTIONS.add("--packageWithVersion");
        BOOLEAN_OPTIONS.add("--package-with-version");
        BOOLEAN_OPTIONS.add("--domAsXpp3");
        BOOLEAN_OPTIONS.add("--dom-as-xpp3");
        BOOLEAN_OPTIONS.add("--enforceMandatoryElements");
        BOOLEAN_OPTIONS.add("--enforce-mandatory-elements");
    }

    private static void alias(String paramKey, String... flags) {
        for (String flag : flags) {
            PARAM_ALIASES.put(flag, paramKey);
        }
    }

    public static void main(String[] args) throws Exception {
        Modello modello = new Modello();

        parseArgumentsFromCommandLine(args);

        for (String type : outputType.split(",")) {
            type = type.trim();
            if (!type.isEmpty()) {
                generate(modello, type);
            }
        }
    }

    // Mirrors AbstractModelloGeneratorMojo#doExecute: current version, then each packaged one in its own package
    private static void generate(Modello modello, String type) throws Exception {
        modello.generate(new XmlStreamReader(modelFile), type, parameters);

        String allVersions = (String) parameters.get(ModelloParameterConstants.ALL_VERSIONS);
        if (StringUtils.isEmpty(allVersions)) {
            return;
        }
        Map<String, Object> versioned = new HashMap<>(parameters);
        versioned.put(ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.toString(true));
        for (String version : allVersions.split(",")) {
            version = version.trim();
            if (!version.isEmpty()) {
                versioned.put(ModelloParameterConstants.VERSION, version);
                modello.generate(new XmlStreamReader(modelFile), type, versioned);
            }
        }
    }

    public static void parseArgumentsFromCommandLine(String[] args) throws Exception {
        try {
            parseArguments(args);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            usage();
            System.exit(1);
        }
    }

    static void parseArguments(String[] args) throws Exception {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("No arguments specified");
        }

        if (args[0].startsWith("-")) {
            parseNamedArguments(args);
        } else {
            parseLegacyArguments(args);
        }
    }

    private static void parseNamedArguments(String[] args) throws Exception {
        parameters = new HashMap<>();
        modelFile = null;
        outputType = null;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if ("-h".equals(arg) || "--help".equals(arg)) {
                usage();
                System.exit(0);
            }

            if (arg.startsWith("-D")) {
                parseProperty(arg.substring(2));
                continue;
            }

            if (!arg.startsWith("-")) {
                if (modelFile == null) {
                    modelFile = new File(arg);
                    continue;
                } else {
                    throw new IllegalArgumentException("Unexpected argument: " + arg);
                }
            }

            String key = arg;
            String val = null;
            int eq = arg.indexOf('=');
            if (eq > 0) {
                key = arg.substring(0, eq);
                val = arg.substring(eq + 1);
            }

            if (val == null) {
                if (BOOLEAN_OPTIONS.contains(key)) {
                    if (i + 1 < args.length
                            && ("true".equalsIgnoreCase(args[i + 1]) || "false".equalsIgnoreCase(args[i + 1]))) {
                        val = args[++i];
                    } else {
                        val = "true";
                    }
                } else {
                    if (i + 1 >= args.length || args[i + 1].startsWith("-")) {
                        throw new IllegalArgumentException("Missing value for option: " + key);
                    }
                    val = args[++i];
                }
            }

            if ("--model".equals(key)
                    || "--models".equals(key)
                    || "--modelFile".equals(key)
                    || "--model-file".equals(key)) {
                modelFile = new File(val);
            } else if ("--type".equals(key)
                    || "--outputType".equals(key)
                    || "--output-type".equals(key)
                    || "--generatorId".equals(key)
                    || "--generator-id".equals(key)) {
                outputType = val;
            } else if ("--licenseFile".equals(key) || "--license-file".equals(key)) {
                File lf = new File(val);
                if (!lf.exists()) {
                    throw new IllegalArgumentException("License file not found: " + val);
                }
                parameters.put(ModelloParameterConstants.LICENSE_TEXT, Files.readAllLines(lf.toPath()));
            } else if ("--licenseText".equals(key) || "--license-text".equals(key)) {
                parameters.put(ModelloParameterConstants.LICENSE_TEXT, Arrays.asList(val.split("\\r?\\n")));
            } else if ("--params".equals(key)
                    || "--velocityParameters".equals(key)
                    || "--velocity-parameters".equals(key)) {
                parameters.put(ModelloParameterConstants.VELOCITY_PARAMETERS, parseMap(key, val));
            } else if ("--pluralExceptions".equals(key) || "--plural-exceptions".equals(key)) {
                parameters.put(ModelloParameterConstants.PLURAL_EXCEPTIONS, parseMap(key, val));
            } else if (PARAM_ALIASES.containsKey(key)) {
                parameters.put(PARAM_ALIASES.get(key), val);
            } else {
                throw new IllegalArgumentException("Unknown option: " + key);
            }
        }

        validateNamedArguments();
    }

    private static Map<String, String> parseMap(String option, String val) {
        Map<String, String> map = new HashMap<>();
        for (String s : val.split(",")) {
            String[] e = s.split("=", 2);
            if (e.length == 2 && map.putIfAbsent(e[0].trim(), e[1].trim()) != null) {
                throw new IllegalArgumentException("Duplicate key '" + e[0].trim() + "' in option: " + option);
            }
        }
        return map;
    }

    private static void parseProperty(String prop) {
        int eq = prop.indexOf('=');
        if (eq > 0) {
            String k = prop.substring(0, eq).trim();
            String v = prop.substring(eq + 1).trim();
            if (PARAM_ALIASES.containsKey("--" + k)) {
                parameters.put(PARAM_ALIASES.get("--" + k), v);
            } else {
                parameters.put(k, v);
                if (!k.startsWith("modello.")) {
                    parameters.put("modello." + k, v);
                }
            }
        }
    }

    private static void validateNamedArguments() {
        if (modelFile == null) {
            throw new IllegalArgumentException(
                    "Missing required parameter: model file (--model, --modelFile, --model-file)");
        }
        if (StringUtils.isEmpty(outputType)) {
            throw new IllegalArgumentException(
                    "Missing required parameter: output type (--type, --outputType, --output-type, --generatorId, --generator-id)");
        }
        if (!parameters.containsKey(ModelloParameterConstants.OUTPUT_DIRECTORY)
                || StringUtils.isEmpty((String) parameters.get(ModelloParameterConstants.OUTPUT_DIRECTORY))) {
            throw new IllegalArgumentException(
                    "Missing required parameter: output directory (--dir, --outputDirectory, --output-directory)");
        }
        if (!parameters.containsKey(ModelloParameterConstants.VERSION)
                || StringUtils.isEmpty((String) parameters.get(ModelloParameterConstants.VERSION))) {
            throw new IllegalArgumentException(
                    "Missing required parameter: model version (--version, --modelVersion, --model-version)");
        }
        if (!parameters.containsKey(ModelloParameterConstants.PACKAGE_WITH_VERSION)) {
            parameters.put(ModelloParameterConstants.PACKAGE_WITH_VERSION, "false");
        }
        if (!parameters.containsKey(ModelloParameterConstants.OUTPUT_JAVA_SOURCE)) {
            parameters.put(
                    ModelloParameterConstants.OUTPUT_JAVA_SOURCE, ModelloParameterConstants.OUTPUT_JAVA_SOURCE_DEFAULT);
        }
    }

    private static void parseLegacyArguments(String[] args) {
        if (args.length < 6) {
            throw new IllegalArgumentException("Insufficient arguments for legacy positional invocation");
        }

        modelFile = new File(args[0]);

        outputType = args[1];

        parameters = new HashMap<>();

        String outputDirectory = args[2];

        if (StringUtils.isEmpty(outputDirectory)) {
            throw new IllegalArgumentException("Missing required parameter: output directory");
        }

        parameters.put(ModelloParameterConstants.OUTPUT_DIRECTORY, outputDirectory);

        String modelVersion = args[3];

        if (StringUtils.isEmpty(modelVersion)) {
            throw new IllegalArgumentException("Missing required parameter: model version");
        }

        parameters.put(ModelloParameterConstants.VERSION, modelVersion);

        String packageWithVersion = args[4];

        if (StringUtils.isEmpty(packageWithVersion)) {
            throw new IllegalArgumentException("Missing required parameter: package with version");
        }

        parameters.put(ModelloParameterConstants.PACKAGE_WITH_VERSION, packageWithVersion);

        String javaSource = args[5];

        if (StringUtils.isEmpty(javaSource)) {
            throw new IllegalArgumentException("Missing required parameter: Java Source");
        }

        parameters.put(ModelloParameterConstants.OUTPUT_JAVA_SOURCE, javaSource);

        if (args.length > 6) {
            parameters.put(ModelloParameterConstants.ENCODING, args[6]);
        }
    }

    private static void usage() {
        System.err.println("Usage (named options):");
        System.err.println(
                "  modello --model <model> --type <outputType> --dir <outputDirectory> --version <modelVersion> [options]");
        System.err.println("Options:");
        System.err.println("  --model, --modelFile, --model-file <file>            Model file (required)");
        System.err.println("  --type, --outputType, --output-type,");
        System.err.println(
                "  --generatorId, --generator-id <id>                   Output type(s), comma-separated (required)");
        System.err.println("  --dir, --outputDirectory, --output-directory <dir>   Output directory (required)");
        System.err.println(
                "  --version, --modelVersion, --model-version <ver>      Model version to generate (required)");
        System.err.println("  --packageWithVersion, --package-with-version [<bool>]");
        System.err.println(
                "                                                       Append version to package names, e.g. .v1_0_0 (default: false)");
        System.err.println("  --javaSource, --java-source, --source <level>        Java source level (default: 8)");
        System.err.println(
                "  --encoding <enc>                                     Output encoding (default: platform charset)");
        System.err.println(
                "  --domAsXpp3, --dom-as-xpp3 [<bool>]                  Map DOM content to Xpp3Dom, not org.w3c.dom.Element (default: true)");
        System.err.println("  --packagedVersions, --packaged-versions,");
        System.err.println(
                "  --allVersions, --all-versions <vers>                 Additional historical versions (comma-separated), each");
        System.err.println(
                "                                                       generated into a versioned package regardless of --packageWithVersion");
        System.err.println("  --velocityBasedir, --velocity-basedir <dir>          Velocity templates base directory");
        System.err.println("  --templates, --velocityTemplates,");
        System.err.println(
                "  --velocity-templates <files>                         Velocity templates relative to base directory (comma-separated)");
        System.err.println("  --params, --velocityParameters,");
        System.err.println("  --velocity-parameters <p=v,...>                      Velocity parameters (key=val,...)");
        System.err.println("  --licenseText, --license-text <text>                 License header text");
        System.err.println("  --licenseFile, --license-file <file>                 File with license header text");
        System.err.println("  --pluralExceptions, --plural-exceptions <p=s,...>     Plural to singular exceptions");
        System.err.println("  --enforceMandatoryElements,");
        System.err.println(
                "  --enforce-mandatory-elements [<bool>]                Enforce mandatory XML elements in XSD (default: false)");
        System.err.println("  --xsdFileName, --xsd-file-name <file>                XSD output file name");
        System.err.println("  --jsonSchemaFileName, --jsonschema-file-name,");
        System.err.println("  --json-schema-file-name <file>                       JSON schema output file name");
        System.err.println(
                "  --firstVersion, --first-version <ver>                First model version; xdoc shows 'since' column if older than --version");
        System.err.println("  --xdocFileName, --xdoc-file-name <file>              XDoc output file name");
        System.err.println("  --extendedClassnameSuffix,");
        System.err.println(
                "  --extended-classname-suffix <suffix>                 Writer class name suffix for xpp3-extended-writer (default: Ex)");
        System.err.println(
                "  -D<key>=<value>                                      Option name without '--', or raw parameter (also set as modello.<key>)");
        System.err.println("  -h, --help                                           Display this help message");
        System.err.println();
        System.err.println("Usage (legacy positional):");
        System.err.println(
                "  modello <model> <outputType> <outputDirectory> <modelVersion> <packageWithVersion> <javaSource> [<encoding>]");
        System.err.println("  All arguments are required except <encoding>; they mean the same as the named options:");
        System.err.println("  <model>               --model");
        System.err.println("  <outputType>          --type");
        System.err.println("  <outputDirectory>     --dir");
        System.err.println("  <modelVersion>        --version");
        System.err.println("  <packageWithVersion>  --packageWithVersion (true or false)");
        System.err.println("  <javaSource>          --javaSource");
        System.err.println("  <encoding>            --encoding");
        System.err.println("  No other option is available in this form.");
    }

    static File getModelFile() {
        return modelFile;
    }

    static String getOutputType() {
        return outputType;
    }

    static Map<String, Object> getParameters() {
        return parameters;
    }
}
