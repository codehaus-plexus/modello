package org.codehaus.modello.plugin.xdoc;

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

import javax.inject.Named;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import com.github.chhorz.javadoc.JavaDoc;
import com.github.chhorz.javadoc.JavaDocParserBuilder;
import com.github.chhorz.javadoc.OutputType;
import com.github.chhorz.javadoc.tags.BlockTag;
import com.github.chhorz.javadoc.tags.SinceTag;
import org.apache.maven.doxia.module.xdoc.XdocSinkFactory;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkFactory;
import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.model.BaseElement;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelDefault;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.Version;
import org.codehaus.modello.model.VersionRange;
import org.codehaus.modello.plugin.xdoc.metadata.XdocClassMetadata;
import org.codehaus.modello.plugin.xdoc.metadata.XdocFieldMetadata;
import org.codehaus.modello.plugin.xsd.XsdModelHelper;
import org.codehaus.modello.plugins.xml.AbstractXmlGenerator;
import org.codehaus.modello.plugins.xml.metadata.XmlAssociationMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlClassMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlFieldMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlModelMetadata;
import org.codehaus.plexus.util.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 * @author <a href="mailto:emmanuel@venisse.net">Emmanuel Venisse</a>
 */
@Named("xdoc")
public class XdocGenerator extends AbstractXmlGenerator {
    private static final VersionRange DEFAULT_VERSION_RANGE = new VersionRange("0.0.0+");

    private Version firstVersion = DEFAULT_VERSION_RANGE.getFromVersion();

    private Version version = DEFAULT_VERSION_RANGE.getFromVersion();

    @Override
    public void generate(Model model, Map<String, Object> parameters) throws ModelloException {
        initialize(model, parameters);

        if (parameters.get(ModelloParameterConstants.FIRST_VERSION) != null) {
            firstVersion = new Version((String) parameters.get(ModelloParameterConstants.FIRST_VERSION));
        }

        if (parameters.get(ModelloParameterConstants.VERSION) != null) {
            version = new Version((String) parameters.get(ModelloParameterConstants.VERSION));
        }

        try {
            generateXdoc(parameters);
        } catch (IOException ex) {
            throw new ModelloException("Exception while generating XDoc.", ex);
        }
    }

    private void generateXdoc(Map<String, Object> parameters) throws IOException {
        Model objectModel = getModel();

        File directory = getOutputDirectory();

        if (isPackageWithVersion()) {
            directory = new File(directory, getGeneratedVersion().toString());
        }

        if (!directory.exists()) {
            directory.mkdirs();
        }

        // we assume parameters not null
        String xdocFileName = (String) parameters.get(ModelloParameterConstants.OUTPUT_XDOC_FILE_NAME);

        File f = new File(directory, objectModel.getId() + ".xml");

        if (xdocFileName != null) {
            f = new File(directory, xdocFileName);
        }

        OutputStream outputStream = new FileOutputStream(f);
        SinkFactory sinkFactory = new XdocSinkFactory();
        Sink sink = sinkFactory.createSink(outputStream, StandardCharsets.UTF_8.name());

        // Start document
        sink.head();
        sink.title();
        sink.text(objectModel.getName());
        sink.title_();
        sink.head_();

        // Body
        sink.body();

        sink.section1();
        sink.sectionTitle1();
        sink.text(objectModel.getName());
        sink.sectionTitle1_();

        sink.paragraph();
        writeMarkupViaSink(sink, getDescription(objectModel));
        sink.paragraph_();

        // XML representation of the model with links
        ModelClass root = objectModel.getClass(objectModel.getRoot(getGeneratedVersion()), getGeneratedVersion());

        sink.verbatim(null);
        sink.rawText("\n" + getModelXmlDescriptor(root));
        sink.verbatim_();

        // Element descriptors
        // Traverse from root so "abstract" models aren't included
        writeModelDescriptor(sink, root);

        sink.section1_();

        sink.body_();

        sink.flush();
        sink.close();

        outputStream.flush();
        outputStream.close();
    }

    /**
     * Get the anchor name by which model classes can be accessed in the generated xdoc/html file.
     *
     * @param tagName the name of the XML tag of the model class
     * @param modelClass the model class, that eventually can have customized anchor name
     * @return the corresponding anchor name
     */
    private String getAnchorName(String tagName, ModelClass modelClass) {
        XdocClassMetadata xdocClassMetadata = (XdocClassMetadata) modelClass.getMetadata(XdocClassMetadata.ID);

        String anchorName = xdocClassMetadata.getAnchorName();

        return "class_" + (anchorName == null ? tagName : anchorName);
    }

    /**
     * Write description of the whole model.
     *
     * @param sink the Doxia sink
     * @param rootModelClass the root class of the model
     */
    private void writeModelDescriptor(Sink sink, ModelClass rootModelClass) {
        writeElementDescriptor(sink, rootModelClass, null, new HashSet<>(), new HashMap<>());
    }

    /**
     * Write description of an element of the XML representation of the model. This method is recursive.
     *
     * @param sink the Doxia sink
     * @param modelClass the mode class to describe
     * @param association the association we are coming from (can be <code>null</code>)
     * @param writtenIds set of data already written ids
     * @param writtenAnchors map of already written anchors with corresponding ids
     */
    private void writeElementDescriptor(
            Sink sink,
            ModelClass modelClass,
            ModelAssociation association,
            Set<String> writtenIds,
            Map<String, String> writtenAnchors) {
        String tagName = resolveTagName(modelClass, association);

        String id = getId(tagName, modelClass);
        if (writtenIds.contains(id)) {
            // tag already written for this model class accessed as this tag name
            return;
        }
        writtenIds.add(id);

        String anchorName = getAnchorName(tagName, modelClass);
        if (writtenAnchors.containsKey(anchorName)) {
            // TODO use logging API?
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

        sink.paragraph();
        writeMarkupViaSink(sink, getDescription(modelClass));
        sink.paragraph_();

        List<ModelField> elementFields = getFieldsForXml(modelClass, getGeneratedVersion());

        ModelField contentField = getContentField(elementFields);

        if (contentField != null) {
            // this model class has a Content field
            sink.paragraph();
            sink.bold();
            sink.text("Element Content: ");
            sink.bold_();
            writeMarkupViaSink(sink, getDescription(contentField));
            sink.paragraph_();
        }

        List<ModelField> attributeFields = getXmlAttributeFields(elementFields);

        elementFields.removeAll(attributeFields);

        writeFieldsTable(sink, attributeFields, false); // write attributes
        writeFieldsTable(sink, elementFields, true); // write elements

        sink.section2_();

        // check every fields that are inner associations to write their element descriptor
        for (ModelField f : elementFields) {
            if (isInnerAssociation(f)) {
                ModelAssociation assoc = (ModelAssociation) f;
                ModelClass fieldModelClass = getModel().getClass(assoc.getTo(), getGeneratedVersion());

                if (!writtenIds.contains(getId(resolveTagName(fieldModelClass, assoc), fieldModelClass))) {
                    writeElementDescriptor(sink, fieldModelClass, assoc, writtenIds, writtenAnchors);
                }
            }
        }
    }

    private String getId(String tagName, ModelClass modelClass) {
        return tagName + '/' + modelClass.getPackageName() + '.' + modelClass.getName();
    }

    /**
     * Write a table containing model fields description.
     *
     * @param sink the Doxia sink
     * @param fields the fields to add in the table
     * @param elementFields <code>true</code> if fields are elements, <code>false</code> if fields are attributes
     */
    private void writeFieldsTable(Sink sink, List<ModelField> fields, boolean elementFields) {
        if (fields == null || fields.isEmpty()) {
            // skip empty table
            return;
        }

        // skip if only one element field with xml.content == true
        if (elementFields && (fields.size() == 1) && hasContentField(fields)) {
            return;
        }

        sink.table();

        sink.tableRow();

        sink.tableHeaderCell();
        sink.text(elementFields ? "Element" : "Attribute");
        sink.tableHeaderCell_();

        sink.tableHeaderCell();
        sink.text("Type");
        sink.tableHeaderCell_();

        boolean showSinceColumn = version.greaterThan(firstVersion);

        if (showSinceColumn) {
            sink.tableHeaderCell();
            sink.text("Since");
            sink.tableHeaderCell_();
        }

        sink.tableHeaderCell();
        sink.text("Description");
        sink.tableHeaderCell_();

        sink.tableRow_();

        for (ModelField f : fields) {
            XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) f.getMetadata(XmlFieldMetadata.ID);

            if (xmlFieldMetadata.isContent()) {
                continue;
            }

            sink.tableRow();

            // Element/Attribute column

            String tagName = resolveTagName(f, xmlFieldMetadata);

            sink.tableCell();

            sink.monospaced();

            boolean manyAssociation = false;

            if (f instanceof ModelAssociation) {
                ModelAssociation assoc = (ModelAssociation) f;

                XmlAssociationMetadata xmlAssociationMetadata =
                        (XmlAssociationMetadata) assoc.getAssociationMetadata(XmlAssociationMetadata.ID);

                manyAssociation = assoc.isManyMultiplicity();

                String itemTagName = manyAssociation ? resolveTagName(tagName, xmlAssociationMetadata) : tagName;

                if (manyAssociation && xmlAssociationMetadata.isWrappedItems()) {
                    sink.text(tagName);
                    sink.rawText("/");
                }
                if (isInnerAssociation(f)) {
                    sink.link("#" + getAnchorName(itemTagName, assoc.getToClass()));
                    sink.text(itemTagName);
                    sink.link_();
                } else if (ModelDefault.PROPERTIES.equals(f.getType())) {
                    if (xmlAssociationMetadata.isMapExplode()) {
                        sink.text("(key,value)");
                    } else {
                        sink.rawText("<i>key</i>=<i>value</i>");
                    }
                } else {
                    sink.text(itemTagName);
                }
                if (manyAssociation) {
                    sink.text("*");
                }
            } else {
                sink.text(tagName);
            }

            sink.monospaced_();

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
                        sink.rawText(fromVersion.toString());
                    }
                }

                sink.tableCell_();
            }

            // Description column

            sink.tableCell();

            if (manyAssociation) {
                sink.rawText("<b>(Many)</b> ");
            }

            writeMarkupViaSink(sink, getDescription(f));

            // Write the default value, if it exists.
            // But only for fields that are not a ModelAssociation
            if (f.getDefaultValue() != null && !(f instanceof ModelAssociation)) {
                sink.rawText("<p><strong>Default value</strong>: ");

                sink.monospaced();
                sink.text(f.getDefaultValue());
                sink.monospaced_();

                sink.rawText("</p>");
            }

            sink.tableCell_();

            sink.tableRow_();
        }

        sink.table_();
    }

    /**
     * Build the pretty tree describing the XML representation of the model.
     *
     * @param rootModelClass the model root class
     * @return the String representing the tree model
     */
    private String getModelXmlDescriptor(ModelClass rootModelClass) {
        return getElementXmlDescriptor(rootModelClass, null, new Stack<>());
    }

    /**
     * Build the pretty tree describing the XML representation of an element of the model. This method is recursive.
     *
     * @param modelClass the class we are printing the model
     * @param association the association we are coming from (can be <code>null</code>)
     * @param stack the stack of elements that have been traversed to come to the current one
     * @return the String representing the tree model
     * @throws ModelloRuntimeException
     */
    private String getElementXmlDescriptor(ModelClass modelClass, ModelAssociation association, Stack<String> stack)
            throws ModelloRuntimeException {
        StringBuilder sb = new StringBuilder();

        appendSpacer(sb, stack.size());

        String tagName = resolveTagName(modelClass, association);

        // <tagName
        sb.append("&lt;<a href=\"#").append(getAnchorName(tagName, modelClass)).append("\">");
        sb.append(tagName).append("</a>");

        boolean addNewline = false;
        if (stack.isEmpty()) {
            // try to add XML Schema reference
            try {
                String targetNamespace =
                        XsdModelHelper.getTargetNamespace(modelClass.getModel(), getGeneratedVersion());

                XmlModelMetadata xmlModelMetadata =
                        (XmlModelMetadata) modelClass.getModel().getMetadata(XmlModelMetadata.ID);

                if (StringUtils.isNotBlank(targetNamespace) && (xmlModelMetadata.getSchemaLocation() != null)) {
                    String schemaLocation = xmlModelMetadata.getSchemaLocation(getGeneratedVersion());

                    sb.append(" xmlns=\"" + targetNamespace + "\"");
                    sb.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
                    sb.append("  xsi:schemaLocation=\"" + targetNamespace);
                    sb.append(" <a href=\"" + schemaLocation + "\">" + schemaLocation + "</a>\"");

                    addNewline = true;
                }
            } catch (ModelloException me) {
                // ignore unavailable XML Schema configuration
            }
        }

        String id = tagName + '/' + modelClass.getPackageName() + '.' + modelClass.getName();
        if (stack.contains(id)) {
            // recursion detected
            sb.append("&gt;...recursion...&lt;").append(tagName).append("&gt;\n");
            return sb.toString();
        }

        List<ModelField> fields = getFieldsForXml(modelClass, getGeneratedVersion());

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

                sb.append(resolveTagName(f, xmlFieldMetadata)).append("=..");
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

                String fieldTagName = resolveTagName(f, xmlFieldMetadata);

                if (isInnerAssociation(f)) {
                    ModelAssociation assoc = (ModelAssociation) f;

                    boolean wrappedItems = false;
                    if (assoc.isManyMultiplicity()) {
                        XmlAssociationMetadata xmlAssociationMetadata =
                                (XmlAssociationMetadata) assoc.getAssociationMetadata(XmlAssociationMetadata.ID);
                        wrappedItems = xmlAssociationMetadata.isWrappedItems();
                    }

                    if (wrappedItems) {
                        appendSpacer(sb, stack.size());

                        sb.append("&lt;").append(fieldTagName).append("&gt;\n");

                        stack.push(fieldTagName);
                    }

                    ModelClass fieldModelClass = getModel().getClass(assoc.getTo(), getGeneratedVersion());

                    sb.append(getElementXmlDescriptor(fieldModelClass, assoc, stack));

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

    /**
     * Compute the tagName of a given class, living inside an association.
     * @param modelClass the class we are looking for the tag name
     * @param association the association where this class is used
     * @return the tag name to use
     * @todo refactor to use XmlModelHelpers.resolveTagName helpers instead
     */
    private String resolveTagName(ModelClass modelClass, ModelAssociation association) {
        XmlClassMetadata xmlClassMetadata = (XmlClassMetadata) modelClass.getMetadata(XmlClassMetadata.ID);

        String tagName;
        if (xmlClassMetadata == null || xmlClassMetadata.getTagName() == null) {
            if (association == null) {
                tagName = uncapitalise(modelClass.getName());
            } else {
                tagName = association.getName();

                if (association.isManyMultiplicity()) {
                    tagName = singular(tagName);
                }
            }
        } else {
            tagName = xmlClassMetadata.getTagName();
        }

        if (association != null) {
            XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) association.getMetadata(XmlFieldMetadata.ID);

            XmlAssociationMetadata xmlAssociationMetadata =
                    (XmlAssociationMetadata) association.getAssociationMetadata(XmlAssociationMetadata.ID);

            if (xmlFieldMetadata != null) {
                if (xmlAssociationMetadata.getTagName() != null) {
                    tagName = xmlAssociationMetadata.getTagName();
                } else if (xmlFieldMetadata.getTagName() != null) {
                    tagName = xmlFieldMetadata.getTagName();

                    if (association.isManyMultiplicity()) {
                        tagName = singular(tagName);
                    }
                }
            }
        }

        return tagName;
    }

    /**
     * Appends the required spacers to the given StringBuilder.
     * @param sb where to append the spacers
     * @param depth the depth of spacers to generate
     */
    private static void appendSpacer(StringBuilder sb, int depth) {
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }
    }

    private static String getDescription(BaseElement element) {
        return (element.getDescription() == null) ? "No description." : rewrite(element.getDescription());
    }

    private static void writeMarkupViaSink(Sink sink, String markup) {
        sink.rawText(markup);
    }

    /**
     * Ensures that text will have balanced tags
     *
     * @param text xml or html based content
     * @return valid XML string
     */
    private static String rewrite(String text) {
        JavaDoc javaDoc = JavaDocParserBuilder.withStandardJavadocTags()
                .withOutputType(OutputType.HTML)
                .build()
                .parse(text);
        String html = javaDoc.getDescription()
                + javaDoc.getTags().stream()
                        .map(XdocGenerator::renderJavaDocTag)
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining("\n", "\n", ""));
        Document document = Jsoup.parseBodyFragment(html);
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        return document.body().html();
    }

    private static String renderJavaDocTag(BlockTag tag) {
        if (tag instanceof SinceTag) {
            return "<p><b>Since</b>: " + ((SinceTag) tag).getSinceText() + "</p>";
        }
        return null;
    }
}
