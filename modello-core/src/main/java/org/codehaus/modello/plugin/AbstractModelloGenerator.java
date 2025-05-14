package org.codehaus.modello.plugin;

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

import javax.inject.Inject;

import java.io.File;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelDefault;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.Version;
import org.codehaus.plexus.build.BuildContext;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.io.CachingWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 */
public abstract class AbstractModelloGenerator implements ModelloGenerator {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final Map<String, String> PLURAL_EXCEPTIONS = new HashMap<>();

    static {
        // Irregular names
        PLURAL_EXCEPTIONS.put("children", "child");
        PLURAL_EXCEPTIONS.put("feet", "foot");
        PLURAL_EXCEPTIONS.put("geese", "goose");
        PLURAL_EXCEPTIONS.put("indices", "index");
        PLURAL_EXCEPTIONS.put("men", "man");
        PLURAL_EXCEPTIONS.put("mice", "mouse");
        PLURAL_EXCEPTIONS.put("people", "person");
        PLURAL_EXCEPTIONS.put("teeth", "tooth");
        PLURAL_EXCEPTIONS.put("women", "woman");

        // Invariant names
        PLURAL_EXCEPTIONS.put("aircraft", "aircraft");
        PLURAL_EXCEPTIONS.put("bison", "bison");
        PLURAL_EXCEPTIONS.put("deer", "deer");
        PLURAL_EXCEPTIONS.put("elk", "elk");
        PLURAL_EXCEPTIONS.put("fish", "fish");
        PLURAL_EXCEPTIONS.put("series", "series");
        PLURAL_EXCEPTIONS.put("sheep", "sheep");
        PLURAL_EXCEPTIONS.put("species", "species");

        // Special "oes" exceptions
        PLURAL_EXCEPTIONS.put("buffaloes", "buffalo");
        PLURAL_EXCEPTIONS.put("cargoes", "cargo");
        PLURAL_EXCEPTIONS.put("echoes", "echo");
        PLURAL_EXCEPTIONS.put("goes", "go");
        PLURAL_EXCEPTIONS.put("haloes", "halo");
        PLURAL_EXCEPTIONS.put("heroes", "hero");
        PLURAL_EXCEPTIONS.put("mosquitoes", "mosquito");
        PLURAL_EXCEPTIONS.put("noes", "no");
        PLURAL_EXCEPTIONS.put("potatoes", "potato");
        PLURAL_EXCEPTIONS.put("tomatoes", "tomato");
        PLURAL_EXCEPTIONS.put("torpedoes", "torpedo");
        PLURAL_EXCEPTIONS.put("vetoes", "veto");
        PLURAL_EXCEPTIONS.put("volcanoes", "volcano");

        // Special "ses" exceptions
        PLURAL_EXCEPTIONS.put("horses", "horse");
        PLURAL_EXCEPTIONS.put("licenses", "license");
        PLURAL_EXCEPTIONS.put("phases", "phase");

        // Special "zzes" exceptions
        PLURAL_EXCEPTIONS.put("fezzes", "fez");
        PLURAL_EXCEPTIONS.put("whizzes", "whiz");

        // Special "ies" exceptions
        PLURAL_EXCEPTIONS.put("movies", "movie");

        // Special "ves" exceptions
        PLURAL_EXCEPTIONS.put("relatives", "relative");
    }

    private Model model;

    private File outputDirectory;

    private Version generatedVersion;

    private boolean packageWithVersion;

    private String encoding;

    private List<String> licenseText;

    @Inject
    private BuildContext buildContext;

    protected Logger getLogger() {
        return logger;
    }

    @SuppressWarnings("uncheked")
    protected void initialize(Model model, Map<String, Object> parameters) throws ModelloException {
        this.model = model;

        outputDirectory = new File(requireParameter(parameters, ModelloParameterConstants.OUTPUT_DIRECTORY));

        String version = requireParameter(parameters, ModelloParameterConstants.VERSION);

        generatedVersion = new Version(version);

        packageWithVersion =
                Boolean.parseBoolean(requireParameter(parameters, ModelloParameterConstants.PACKAGE_WITH_VERSION));

        encoding = (String) parameters.get(ModelloParameterConstants.ENCODING);

        licenseText = (List<String>) parameters.get(ModelloParameterConstants.LICENSE_TEXT);

        Optional.ofNullable(parameters.get(ModelloParameterConstants.PLURAL_EXCEPTIONS))
                .ifPresent(o -> PLURAL_EXCEPTIONS.putAll((Map<String, String>) o));
    }

    protected Model getModel() {
        return model;
    }

    protected Version getGeneratedVersion() {
        return generatedVersion;
    }

    protected boolean isPackageWithVersion() {
        return packageWithVersion;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    protected String getEncoding() {
        return encoding;
    }

    protected List<String> getHeader() {
        List<String> header = new ArrayList<>();
        List<String> license = getLicenseHeader();
        if (license != null) {
            header.addAll(license);
        }
        List<String> generated = getGeneratedHeader();
        if (generated != null) {
            header.addAll(generated);
        }
        return header;
    }

    protected List<String> getGeneratedHeader() {
        String version = getClass().getPackage().getImplementationVersion();
        return Arrays.asList(
                "=================== DO NOT EDIT THIS FILE ====================",
                "Generated by Modello" + ((version == null) ? "" : (' ' + version)) + ",",
                "any modifications will be overwritten.",
                "==============================================================");
    }

    protected List<String> getLicenseHeader() {
        return licenseText;
    }

    protected boolean isClassInModel(String fieldType, Model model) {
        try {
            return model.getClass(fieldType, generatedVersion) != null;
        } catch (Exception e) {
        }

        return false;
    }

    /**
     * Return the child fields of this class.
     *
     * @param modelClass current class
     * @return the list of fields of this class
     */
    protected List<ModelField> getFieldsForClass(ModelClass modelClass) {
        List<ModelField> fields = new ArrayList<ModelField>();

        while (modelClass != null) {
            fields.addAll(modelClass.getFields(getGeneratedVersion()));

            String superClass = modelClass.getSuperClass();
            if (superClass != null) {
                modelClass = getModel().getClass(superClass, getGeneratedVersion());
            } else {
                modelClass = null;
            }
        }

        return fields;
    }

    protected boolean isInnerAssociation(ModelField field) {
        return field instanceof ModelAssociation && isClassInModel(((ModelAssociation) field).getTo(), getModel());
    }

    protected boolean isMap(String fieldType) {
        return ModelDefault.MAP.equals(fieldType) || ModelDefault.PROPERTIES.equals(fieldType);
    }

    protected boolean isCollection(String fieldType) {
        return ModelDefault.LIST.equals(fieldType) || ModelDefault.SET.equals(fieldType);
    }

    protected String capitalise(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }

        return new StringBuilder(str.length())
                .append(Character.toTitleCase(str.charAt(0)))
                .append(str.substring(1))
                .toString();
    }

    public static String singular(String name) {
        if (name == null || name.isEmpty()) return name;

        String lower = name.toLowerCase();

        if (PLURAL_EXCEPTIONS.containsKey(lower)) {
            return PLURAL_EXCEPTIONS.get(lower);
        }

        // Suffix-based rules
        if (lower.endsWith("ies") && name.length() > 3) {
            return name.substring(0, name.length() - 3) + "y";
        }
        if (lower.endsWith("aves") || lower.endsWith("lves") || lower.endsWith("rves")) {
            return name.substring(0, name.length() - 3) + "f";
        }
        if (lower.endsWith("ves") && !lower.endsWith("fves")) {
            return name.substring(0, name.length() - 3) + "fe";
        }
        if (lower.endsWith("zzes")) {
            return name.substring(0, name.length() - 2);
        }
        if (lower.endsWith("sses")) {
            return name.substring(0, name.length() - 2);
        }
        if (lower.endsWith("ses")) {
            return name.substring(0, name.length() - 2);
        }
        if (lower.endsWith("ches") || lower.endsWith("shes")) {
            return name.substring(0, name.length() - 2);
        }
        if (lower.endsWith("xes")) {
            return name.substring(0, name.length() - 2);
        }
        if (lower.endsWith("oes")) {
            return name.substring(0, name.length() - 1);
        }
        if (lower.endsWith("s") && name.length() > 1) {
            return name.substring(0, name.length() - 1);
        }

        return name;
    }

    public static String uncapitalise(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }

        return new StringBuilder(str.length())
                .append(Character.toLowerCase(str.charAt(0)))
                .append(str.substring(1))
                .toString();
    }

    // ----------------------------------------------------------------------
    // Text utils
    // ----------------------------------------------------------------------

    protected boolean isEmpty(String string) {
        return string == null || string.trim().length() == 0;
    }

    // ----------------------------------------------------------------------
    // Parameter utils
    // ----------------------------------------------------------------------

    protected String requireParameter(Map<String, Object> parameters, String name) {
        String value = (String) parameters.get(name);

        if (value == null) {
            throw new ModelloRuntimeException("Missing parameter '" + name + "'.");
        }

        return value;
    }

    protected String getParameter(Map<String, Object> parameters, String name, String defaultValue) {
        return (String) parameters.getOrDefault(name, defaultValue);
    }

    protected BuildContext getBuildContext() {
        return buildContext;
    }

    protected Writer newWriter(Path path) throws IOException {
        Charset charset = getEncoding() != null ? Charset.forName(getEncoding()) : Charset.defaultCharset();
        return newWriter(path, charset);
    }

    protected Writer newWriter(Path path, Charset charset) throws IOException {
        CachingWriter cachingWriter = new CachingWriter(path, charset);
        return new FilterWriter(cachingWriter) {
            @Override
            public void close() throws IOException {
                super.close();
                if (cachingWriter.isModified()) {
                    getBuildContext().refresh(path.toFile());
                } else {
                    getLogger().debug("The contents of the file " + path + " matches, skipping writing file.");
                }
            }
        };
    }
}
