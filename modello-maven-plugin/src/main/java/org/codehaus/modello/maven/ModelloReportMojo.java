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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.impl.SinkEventAttributeSet;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.BaseElement;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelDefault;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.ModelValidationException;
import org.codehaus.modello.model.Version;
import org.codehaus.modello.model.VersionRange;
import org.codehaus.modello.plugin.xdoc.metadata.XdocClassMetadata;
import org.codehaus.modello.plugin.xdoc.metadata.XdocFieldMetadata;
import org.codehaus.modello.plugin.xsd.XsdModelHelper;
import org.codehaus.modello.plugins.xml.XmlModelHelpers;
import org.codehaus.modello.plugins.xml.metadata.XmlAssociationMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlFieldMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlModelMetadata;
import org.codehaus.plexus.util.StringUtils;

/**
 * Creates documentation for the model as a Maven report, writing directly to the Doxia {@link Sink}.
 *
 */
@Mojo(name = "report", threadSafe = true)
public class ModelloReportMojo extends AbstractMavenReport {

    private static final VersionRange DEFAULT_VERSION_RANGE = new VersionRange("0.0.0+");

    /**
     * Base directory of the project, from where the Modello models are loaded.
     */
    @Parameter(defaultValue = "${basedir}", required = true)
    private String basedir;

    /**
     * List of relative paths to mdo files containing the models.
     */
    @Parameter(required = true)
    private String[] models;

    /**
     * The version of the model we will be working on.
     */
    @Parameter(property = "version", required = true)
    private String version;

    /**
     * The first version of the model. This is used to decide whether or not
     * to show the since column. If this is not specified, it defaults to the
     * version of the model, which in turn means that the since column will not
     * be shown.
     *
     * @since 1.0-alpha-14
     */
    @Parameter
    private String firstVersion;

    /**
     * <p>Note: This is passed by Maven and must not be configured by the user.</p>
     */
    @Component
    private ModelloCore modelloCore;

    @Override
    public String getOutputName() {
        return "modello-xdoc";
    }

    @Override
    public String getName(Locale locale) {
        return "Modello Model Documentation";
    }

    @Override
    public String getDescription(Locale locale) {
        return "Creates documentation for the Modello model.";
    }

    @Override
    protected void executeReport(Locale locale) throws MavenReportException {

        for (String modelStr : models) {
            File modelFile = new File(basedir, modelStr);
            try {
                Model model = modelloCore.loadModel(modelFile);

                Version ver = new Version(version);
                Version firstVer;
                if (firstVersion != null) {
                    firstVer = new Version(firstVersion);
                } else {
                    firstVer = ver;
                }
                Sink sink = getSinkFactory()
                        .createSink(new File(getOutputDirectory()), modelFile.getName(), getOutputEncoding());
                renderModel(sink, model, ver, firstVer);
            } catch (IOException e) {
                throw new MavenReportException("Couldn't read model file: " + modelFile, e);
            } catch (ModelloException e) {
                throw new MavenReportException("Error loading model: " + e.getMessage(), e);
            } catch (ModelValidationException e) {
                throw new MavenReportException("Error validating model: " + e.getMessage(), e);
            }
        }
    }

    // ----------------------------------------------------------------------
    // Rendering
    // ----------------------------------------------------------------------

    private void renderModel(Sink sink, Model model, Version version, Version firstVersion) {
        sink.head();
        sink.title();
        sink.text(model.getName());
        sink.title_();
        sink.head_();

        sink.body();

        sink.section1();
        sink.sectionTitle1();
        sink.text(model.getName());
        sink.sectionTitle1_();

        // Model description
        sink.paragraph();
        sink.rawText(getDescription(model));
        sink.paragraph_();

        // XML representation of the model with links
        ModelClass root = model.getClass(model.getRoot(version), version);

        sink.verbatim(SinkEventAttributeSet.BOXED);
        sink.rawText("\n" + getModelXmlDescriptor(model, root, version));
        sink.verbatim_();

        // Element descriptors - traverse from root so "abstract" models aren't included
        writeModelDescriptor(sink, model, root, version, firstVersion);

        sink.section1_();

        sink.body_();
    }

    /**
     * Write description of the whole model.
     */
    private void writeModelDescriptor(
            Sink sink, Model model, ModelClass rootModelClass, Version version, Version firstVersion) {
        writeElementDescriptor(
                sink, model, rootModelClass, null, version, firstVersion, new HashSet<>(), new HashMap<>());
    }

    /**
     * Write description of an element of the XML representation of the model. This method is recursive.
     */
    private void writeElementDescriptor(
            Sink sink,
            Model model,
            ModelClass modelClass,
            ModelAssociation association,
            Version version,
            Version firstVersion,
            Set<String> writtenIds,
            Map<String, String> writtenAnchors) {
        String tagName = XmlModelHelpers.resolveTagName(modelClass);

        String id = getId(tagName, modelClass);
        if (writtenIds.contains(id)) {
            return;
        }
        writtenIds.add(id);

        String anchorName = getAnchorName(tagName, modelClass);
        if (writtenAnchors.containsKey(anchorName)) {
            System.out.println("[warn] model class " + id + " with tagName " + tagName + " gets duplicate anchorName "
                    + anchorName + ", conflicting with model class " + writtenAnchors.get(anchorName));
        } else {
            writtenAnchors.put(anchorName, id);
        }

        sink.anchor(anchorName);
        sink.anchor_();

        sink.section2();
        sink.sectionTitle2();
        sink.text(tagName);
        sink.sectionTitle2_();

        // Description
        sink.paragraph();
        sink.rawText(getDescription(modelClass));
        sink.paragraph_();

        List<ModelField> elementFields = XmlModelHelpers.getFieldsForXml(modelClass, version);

        ModelField contentField = getContentField(elementFields);

        if (contentField != null) {
            sink.paragraph();
            sink.bold();
            sink.text("Element Content: ");
            sink.bold_();
            sink.rawText(getDescription(contentField));
            sink.paragraph_();
        }

        List<ModelField> attributeFields = getXmlAttributeFields(elementFields);

        elementFields.removeAll(attributeFields);

        writeFieldsTable(sink, model, attributeFields, false, version, firstVersion); // attributes
        writeFieldsTable(sink, model, elementFields, true, version, firstVersion); // elements

        sink.section2_();

        // Recurse into inner associations
        for (ModelField f : elementFields) {
            if (isInnerAssociation(f, model, version)) {
                ModelAssociation assoc = (ModelAssociation) f;
                ModelClass fieldModelClass = model.getClass(assoc.getTo(), version);

                /** TODO: recurse correctly
                if (!writtenIds.contains(
                        getId(XmlModelHelpers.resolveTagName(fieldModelClass, assoc), fieldModelClass))) {
                    writeElementDescriptor(
                            sink, model, fieldModelClass, assoc, version, firstVersion, writtenIds, writtenAnchors);
                }*/
            }
        }
    }

    /**
     * Write a table containing model fields description.
     */
    private void writeFieldsTable(
            Sink sink,
            Model model,
            List<ModelField> fields,
            boolean elementFields,
            Version version,
            Version firstVersion) {
        if (fields == null || fields.isEmpty()) {
            return;
        }

        // skip if only one element field with xml.content == true
        if (elementFields && (fields.size() == 1) && hasContentField(fields)) {
            return;
        }

        boolean showSinceColumn = version.greaterThan(firstVersion);

        // Table header
        sink.table();
        sink.tableRow();

        writeTableHeaderCell(sink, elementFields ? "Element" : "Attribute");
        writeTableHeaderCell(sink, "Type");

        if (showSinceColumn) {
            writeTableHeaderCell(sink, "Since");
        }

        writeTableHeaderCell(sink, "Description");

        sink.tableRow_();

        // Table body
        for (ModelField f : fields) {
            XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) f.getMetadata(XmlFieldMetadata.ID);

            if (xmlFieldMetadata.isContent()) {
                continue;
            }

            sink.tableRow();

            // Element/Attribute column
            String fieldTagName = XmlModelHelpers.resolveTagName(f, xmlFieldMetadata);

            sink.tableCell();

            boolean manyAssociation = false;

            if (f instanceof ModelAssociation) {
                ModelAssociation assoc = (ModelAssociation) f;

                XmlAssociationMetadata xmlAssociationMetadata =
                        (XmlAssociationMetadata) assoc.getAssociationMetadata(XmlAssociationMetadata.ID);

                manyAssociation = assoc.isManyMultiplicity();

                String itemTagName = manyAssociation
                        ? XmlModelHelpers.resolveTagName(fieldTagName, xmlAssociationMetadata)
                        : fieldTagName;

                if (manyAssociation && xmlAssociationMetadata.isWrappedItems()) {
                    sink.monospaced();
                    sink.text(fieldTagName);
                    sink.monospaced_();
                    sink.text("/");
                }
                if (isInnerAssociation(f, model, version)) {
                    sink.monospaced();
                    sink.link("#" + getAnchorName(itemTagName, assoc.getToClass()));
                    sink.text(itemTagName);
                    sink.link_();
                    sink.monospaced_();
                } else if (ModelDefault.PROPERTIES.equals(f.getType())) {
                    if (xmlAssociationMetadata.isMapExplode()) {
                        sink.monospaced();
                        sink.text("(key,value)");
                        sink.monospaced_();
                    } else {
                        sink.rawText("<code><i>key</i>=<i>value</i></code>");
                    }
                } else {
                    sink.monospaced();
                    sink.text(itemTagName);
                    sink.monospaced_();
                }
                if (manyAssociation) {
                    sink.monospaced();
                    sink.text("*");
                    sink.monospaced_();
                }
            } else {
                sink.monospaced();
                sink.text(fieldTagName);
                sink.monospaced_();
            }

            sink.tableCell_();

            // Type column
            sink.tableCell();
            sink.monospaced();

            if (f instanceof ModelAssociation) {
                ModelAssociation assoc = (ModelAssociation) f;

                if (assoc.isOneMultiplicity()) {
                    sink.text(assoc.getTo());
                } else {
                    sink.text(assoc.getType().substring("java.util.".length()));

                    if (assoc.isGenericType()) {
                        sink.text("<" + assoc.getTo() + ">");
                    }
                }
            } else {
                sink.text(f.getType());
            }

            sink.monospaced_();
            sink.tableCell_();

            // Since column
            if (showSinceColumn) {
                sink.tableCell();

                if (f.getVersionRange() != null) {
                    Version fromVersion = f.getVersionRange().getFromVersion();
                    if (fromVersion != null && fromVersion.greaterThan(firstVersion)) {
                        sink.text(fromVersion.toString());
                    }
                }

                sink.tableCell_();
            }

            // Description column
            sink.tableCell();

            if (manyAssociation) {
                sink.bold();
                sink.text("(Many)");
                sink.bold_();
                sink.text(" ");
            }

            sink.rawText(getDescription(f));

            // Write the default value, if it exists (only for non-association fields)
            if (f.getDefaultValue() != null && !(f instanceof ModelAssociation)) {
                sink.paragraph();
                sink.bold();
                sink.text("Default value");
                sink.bold_();
                sink.text(": ");
                sink.monospaced();
                sink.text(f.getDefaultValue());
                sink.monospaced_();
                sink.paragraph_();
            }

            sink.tableCell_();

            sink.tableRow_();
        }

        sink.table_();
    }

    // ----------------------------------------------------------------------
    // XML tree model descriptor
    // ----------------------------------------------------------------------

    /**
     * Build the pretty tree describing the XML representation of the model.
     */
    private String getModelXmlDescriptor(Model model, ModelClass rootModelClass, Version version) {
        return getElementXmlDescriptor(model, rootModelClass, null, version, new Stack<>());
    }

    /**
     * Build the pretty tree describing the XML representation of an element of the model. This method is recursive.
     */
    private String getElementXmlDescriptor(
            Model model, ModelClass modelClass, ModelAssociation association, Version version, Stack<String> stack)
            throws ModelloRuntimeException {
        StringBuilder sb = new StringBuilder();

        appendSpacer(sb, stack.size());

        final String tagName;
        if (association != null) {
            // TODO: support multiplicity in tag name resolution
            XmlFieldMetadata xmlFieldMetadata =
                    (XmlFieldMetadata) association.getAssociationMetadata(XmlFieldMetadata.ID);

            tagName = XmlModelHelpers.resolveTagName(association, xmlFieldMetadata);
        } else {
            tagName = XmlModelHelpers.resolveTagName(modelClass);
        }

        // <tagName
        sb.append("&lt;<a href=\"#").append(getAnchorName(tagName, modelClass)).append("\">");
        sb.append(tagName).append("</a>");

        boolean addNewline = false;
        if (stack.isEmpty()) {
            try {
                String targetNamespace = XsdModelHelper.getTargetNamespace(modelClass.getModel(), version);

                XmlModelMetadata xmlModelMetadata =
                        (XmlModelMetadata) modelClass.getModel().getMetadata(XmlModelMetadata.ID);

                if (StringUtils.isNotBlank(targetNamespace) && (xmlModelMetadata.getSchemaLocation() != null)) {
                    String schemaLocation = xmlModelMetadata.getSchemaLocation(version);

                    sb.append(" xmlns=\"").append(targetNamespace).append("\"");
                    sb.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
                    sb.append("  xsi:schemaLocation=\"").append(targetNamespace);
                    sb.append(" <a href=\"")
                            .append(schemaLocation)
                            .append("\">")
                            .append(schemaLocation)
                            .append("</a>\"");

                    addNewline = true;
                }
            } catch (ModelloException me) {
                // ignore unavailable XML Schema configuration
            }
        }

        String id = tagName + '/' + modelClass.getPackageName() + '.' + modelClass.getName();
        if (stack.contains(id)) {
            sb.append("&gt;...recursion...&lt;").append(tagName).append("&gt;\n");
            return sb.toString();
        }

        List<ModelField> fields = XmlModelHelpers.getFieldsForXml(modelClass, version);

        List<ModelField> attributeFields = getXmlAttributeFields(fields);

        if (!attributeFields.isEmpty()) {
            for (ModelField f : attributeFields) {
                XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) f.getMetadata(XmlFieldMetadata.ID);

                if (addNewline) {
                    addNewline = false;
                    sb.append("\n  ");
                } else {
                    sb.append(' ');
                }

                sb.append(XmlModelHelpers.resolveTagName(f, xmlFieldMetadata)).append("=..");
            }

            sb.append(' ');
        }

        fields.removeAll(attributeFields);

        if ((fields.isEmpty()) || ((fields.size() == 1) && hasContentField(fields))) {
            sb.append("/&gt;\n");
        } else {
            sb.append("&gt;\n");

            stack.push(id);

            for (ModelField f : fields) {
                XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) f.getMetadata(XmlFieldMetadata.ID);
                XdocFieldMetadata xdocFieldMetadata = (XdocFieldMetadata) f.getMetadata(XdocFieldMetadata.ID);

                if (XdocFieldMetadata.BLANK.equals(xdocFieldMetadata.getSeparator())) {
                    sb.append('\n');
                }

                String fieldTagName = XmlModelHelpers.resolveTagName(f, xmlFieldMetadata);

                if (isInnerAssociation(f, model, version)) {
                    ModelAssociation assoc = (ModelAssociation) f;
                    XmlAssociationMetadata xmlAssociationMetadata =
                            (XmlAssociationMetadata) assoc.getAssociationMetadata(XmlAssociationMetadata.ID);

                    boolean wrappedItems = false;
                    if (assoc.isManyMultiplicity()) {

                        wrappedItems = xmlAssociationMetadata.isWrappedItems();
                    }

                    if (wrappedItems) {
                        appendSpacer(sb, stack.size());
                        sb.append("&lt;").append(fieldTagName).append("&gt;\n");
                        stack.push(fieldTagName);
                    }

                    ModelClass fieldModelClass = model.getClass(assoc.getTo(), version);
                    sb.append(getElementXmlDescriptor(model, fieldModelClass, assoc, version, stack));

                    if (wrappedItems) {
                        stack.pop();
                        appendSpacer(sb, stack.size());
                        sb.append("&lt;/").append(fieldTagName).append("&gt;\n");
                    }
                } else if (ModelDefault.PROPERTIES.equals(f.getType())) {
                    ModelAssociation assoc = (ModelAssociation) f;
                    XmlAssociationMetadata xmlAssociationMetadata =
                            (XmlAssociationMetadata) assoc.getAssociationMetadata(XmlAssociationMetadata.ID);

                    appendSpacer(sb, stack.size());
                    sb.append("&lt;").append(fieldTagName).append("&gt;\n");

                    if (xmlAssociationMetadata.isMapExplode()) {
                        appendSpacer(sb, stack.size() + 1);
                        sb.append("&lt;key/&gt;\n");
                        appendSpacer(sb, stack.size() + 1);
                        sb.append("&lt;value/&gt;\n");
                    } else {
                        appendSpacer(sb, stack.size() + 1);
                        sb.append("&lt;<i>key</i>&gt;<i>value</i>&lt;/<i>key</i>&gt;\n");
                    }

                    appendSpacer(sb, stack.size());
                    sb.append("&lt;/").append(fieldTagName).append("&gt;\n");
                } else {
                    appendSpacer(sb, stack.size());
                    sb.append("&lt;").append(fieldTagName).append("/&gt;\n");
                }
            }

            stack.pop();

            appendSpacer(sb, stack.size());
            sb.append("&lt;/").append(tagName).append("&gt;\n");
        }

        return sb.toString();
    }

    private static String getAnchorName(String tagName, ModelClass modelClass) {
        XdocClassMetadata xdocClassMetadata = (XdocClassMetadata) modelClass.getMetadata(XdocClassMetadata.ID);
        String anchorName = xdocClassMetadata.getAnchorName();
        return "class_" + (anchorName == null ? tagName : anchorName);
    }

    private static String getId(String tagName, ModelClass modelClass) {
        return tagName + '/' + modelClass.getPackageName() + '.' + modelClass.getName();
    }

    private static boolean isInnerAssociation(ModelField field, Model model, Version version) {
        if (!(field instanceof ModelAssociation)) {
            return false;
        }
        String to = ((ModelAssociation) field).getTo();
        try {
            return model.getClass(to, version) != null;
        } catch (Exception e) {
            return false;
        }
    }

    private static ModelField getContentField(List<ModelField> modelFields) {
        if (modelFields == null) {
            return null;
        }
        for (ModelField field : modelFields) {
            XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) field.getMetadata(XmlFieldMetadata.ID);
            if (xmlFieldMetadata.isContent()) {
                return field;
            }
        }
        return null;
    }

    private static boolean hasContentField(List<ModelField> modelFields) {
        return getContentField(modelFields) != null;
    }

    private static List<ModelField> getXmlAttributeFields(List<ModelField> modelFields) {
        List<ModelField> xmlAttributeFields = new ArrayList<>();
        for (ModelField field : modelFields) {
            XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) field.getMetadata(XmlFieldMetadata.ID);
            if (xmlFieldMetadata.isAttribute()) {
                xmlAttributeFields.add(field);
            }
        }
        return xmlAttributeFields;
    }

    // ----------------------------------------------------------------------
    // Sink helpers
    // ----------------------------------------------------------------------

    private static void writeTableHeaderCell(Sink sink, String text) {
        sink.tableHeaderCell();
        sink.text(text);
        sink.tableHeaderCell_();
    }

    private static void appendSpacer(StringBuilder sb, int depth) {
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }
    }

    private static String getDescription(BaseElement element) {
        return (element.getDescription() == null) ? "No description." : element.getDescription();
    }

    // ----------------------------------------------------------------------
    // Accessors
    // ----------------------------------------------------------------------

    public String getFirstVersion() {
        return firstVersion;
    }

    public void setFirstVersion(String firstVersion) {
        this.firstVersion = firstVersion;
    }
}
