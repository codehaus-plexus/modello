/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Intalio, Inc.  For written permission,
 *    please contact info@codehaus.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Intalio, Inc. Exolab is a registered
 *    trademark of Intalio, Inc.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.codehaus.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY INTALIO, INC. AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * INTALIO, INC. OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001-2002 (C) Intalio, Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.codehaus.modello.plugin.java.javasource;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.codehaus.plexus.util.WriterFactory;

/**
 * This class represents the basic Java "structure" for a Java
 * source file. This is the base class for JClass and JInterface.
 *
 * This is a useful utility when creating in memory source code.
 * The code in this package was modelled after the Java Reflection API
 * as much as possible to reduce the learning curve.
 *
 * @author <a href="mailto:skopp@riege.de">Martin Skopp</a>
 * @author <a href="mailto:kvisco@intalio.com">Keith Visco</a>
 * @version $Revision$ $Date$
 */
public abstract class JStructure extends JType {

    /**
     * The Id for Source control systems
     * I needed to separate this line to prevent CVS from
     * expanding it here! ;-)
     */
    static final String DEFAULT_HEADER = "$" + "Id$";

    /**
     * The source control version for listed in the JavaDoc
     * I needed to separate this line to prevent CVS from
     * expanding it here! ;-)
     */
    static final String version = "$" + "Revision$ $" + "Date$";

    /**
     * The source header
     */
    private JComment header = null;

    /**
     * List of imported classes and packages
     */
    private List<String> imports = null;

    /**
     * The set of interfaces implemented/extended by this JStructure
     */
    private List<String> interfaces = null;

    /**
     * The Javadoc for this JStructure
     */
    private JDocComment jdc = null;

    /**
     * The JModifiers for this JStructure, which allows us to
     * change the resulting qualifiers
     */
    private JModifiers modifiers = null;

    /**
     * The package to which this JStructure belongs
     */
    private final String packageName;

    private JAnnotations annotations = null;

    /**
     * Creates a new JStructure with the given name.
     *
     * @param name the name of the JStructure.
     * @throws java.lang.IllegalArgumentException when the given name
     * is not a valid Class name.
     */
    protected JStructure(String name) throws IllegalArgumentException {
        super(name);

        // -- verify name is a valid java class name
        if (!isValidClassName(name)) {
            String lname = getLocalName();
            String err = "'" + lname + "' is ";
            if (JNaming.isKeyword(lname)) err += "a reserved word and may not be used as " + " a class name.";
            else err += "not a valid Java identifier.";

            throw new IllegalArgumentException(err);
        }
        this.packageName = getPackageFromClassName(name);
        imports = new ArrayList<String>();
        interfaces = new ArrayList<String>();
        jdc = new JDocComment();
        modifiers = new JModifiers();
        // -- initialize default Java doc
        jdc.addDescriptor(JDocDescriptor.createVersionDesc(version));
    } // -- JStructure

    /**
     * Adds the given JField to this JStructure.
     * <p>
     * This method is implemented by subclasses and
     * should only accept the proper fields for the
     * subclass otherwise an IllegalArgumentException
     * will be thrown. For example a JInterface will
     * only accept static fields.
     * <p>
     * @param jField, the JField to add
     * @exception java.lang.IllegalArgumentException when the given
     * JField has a name of an existing JField
     */
    public abstract void addField(JField jField) throws IllegalArgumentException;

    /**
     * Adds the given JMember to this JStructure.
     * <p>
     * This method is implemented by subclasses and
     * should only accept the proper types for the
     * subclass otherwise an IllegalArgumentException
     * will be thrown.
     * <p>
     * @param jMember the JMember to add to this JStructure.
     * @throws java.lang.IllegalArgumentException when the given
     * JMember has the same name of an existing JField
     * or JMethod respectively.
     */
    public abstract void addMember(JMember jMember) throws IllegalArgumentException;

    /**
     * Adds the given import to this JStructure
     *
     * @param className the className of the class to import.
     */
    public void addImport(String className) {
        if (className == null) return;
        if (className.length() == 0) return;

        // -- getPackageName
        String pkgName = getPackageFromClassName(className);

        if (pkgName != null) {
            if (pkgName.equals(this.packageName)) return;

            // -- XXX: Fix needed for this...
            // -- This may cause issues if the current package
            // -- defines any classes that have the same name
            // -- name as the java.lang package.
            if ("java.lang".equals(pkgName)) return;

            // -- for readabilty keep import list sorted, and make sure
            // -- we do not include more than one of the same import
            for (int i = 0; i < imports.size(); i++) {
                String imp = imports.get(i);
                if (imp.equals(className)) return;
                if (imp.compareTo(className) > 0) {
                    imports.add(i, className);
                    return;
                }
            }
            imports.add(className);
        }
    } // -- addImport

    /**
     * Adds the given interface to the list of interfaces this
     * JStructure inherits method declarations from, and either
     * implements (JClass) or extends (JInterface).
     *
     * @param interfaceName the name of the interface to "inherit"
     * method declarations from.
     */
    public void addInterface(String interfaceName) {
        if (!interfaces.contains(interfaceName)) interfaces.add(interfaceName);
    } // -- addInterface

    /**
     * Adds the given interface to the list of interfaces this
     * JStructure inherits method declarations from, and either
     * implements (JClass) or extends (JInterface).
     *
     * @param jInterface the JInterface to inherit from.
     */
    public void addInterface(JInterface jInterface) {
        if (jInterface == null) return;
        String interfaceName = jInterface.getName();
        if (!interfaces.contains(interfaceName)) {
            interfaces.add(interfaceName);
        }
    } // -- addInterface

    /**
     * Adds the given JMethodSignature to this JClass
     *
     * @param jMethodSig the JMethodSignature to add.
     * @throws java.lang.IllegalArgumentException when the given
     * JMethodSignature conflicts with an existing
     * method signature.
     */
    /*
        public void addMethod(JMethodSignature jMethodSig)
            throws IllegalArgumentException
        {
            if (jMethodSig == null) {
                String err = "The JMethodSignature cannot be null.";
                throw new IllegalArgumentException(err);
            }

            //-- XXXX: check method name and signatures *add later*

            //-- keep method list sorted for esthetics when printing
            //-- START SORT :-)
            boolean added = false;
            short modifierVal = 0;
            JModifiers modifiers = jMethodSig.getModifiers();
            for (int i = 0; i < methods.size(); i++) {
                JMethodSignature tmp = (JMethodSignature) methods.elementAt(i);
                //-- first compare modifiers
                if (tmp.getModifiers().isProtected()) {
                    if (!modifiers.isProtected()) {
                        methods.insertElementAt(jMethodSig, i);
                        added = true;
                        break;
                    }
                }
                //-- compare names
                if (jMethodSig.getName().compareTo(tmp.getName()) < 0) {
                        methods.insertElementAt(jMethodSig, i);
                        added = true;
                        break;
                }
            }
            //-- END SORT
            if (!added) methods.addElement(jMethodSig);

            //-- check parameter packages to make sure we have them
            //-- in our import list

            String[] pkgNames = jMethodSig.getParameterClassNames();
            for (int i = 0; i < pkgNames.length; i++) {
                addImport(pkgNames[i]);
            }
            //-- check return type to make sure it's included in the
            //-- import list
            JType jType = jMethodSig.getReturnType();
            if (jType != null) {
                while (jType.isArray())
                    jType = jType.getComponentType();

                if   (!jType.isPrimitive())
                     addImport(jType.getName());
            }
            //-- check exceptions
            JClass[] exceptions = jMethodSig.getExceptions();
            for (int i = 0; i < exceptions.length; i++) {
                addImport(exceptions[i].getName());
            }
        } //-- addMethod
    */
    /**
     * Returns the field with the given name, or null if no field
     * was found with the given name.
     *
     * @param name the name of the field to return.
     * @return the field with the given name, or null if no field
     * was found with the given name.
     */
    public abstract JField getField(String name);

    /**
     * Returns an array of all the JFields of this JStructure
     *
     * @return an array of all the JFields of this JStructure
     */
    public abstract JField[] getFields();

    /**
     * Returns the name of the file that this JStructure would be
     * printed to, given a call to #print.
     *
     * @param destDir the destination directory. This may be null.
     * @return the name of the file that this JInterface would be
     * printed as, given a call to #print.
     */
    public String getFilename(String destDir) {

        String filename = getLocalName() + ".java";

        // -- Convert Java package to path string
        String javaPackagePath = "";
        if ((packageName != null) && (packageName.length() > 0)) {
            javaPackagePath = packageName.replace('.', File.separatorChar);
        }

        // -- Create fully qualified path (including 'destDir') to file
        File pathFile;
        if (destDir == null) pathFile = new File(javaPackagePath);
        else pathFile = new File(destDir, javaPackagePath);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }

        // -- Prefix filename with path
        if (pathFile.toString().length() > 0) filename = pathFile.toString() + File.separator + filename;

        return filename;
    } // -- getFilename

    /**
     * Returns the JComment header to display at the top of the source file
     * for this JStructure, or null if no header was set.
     *
     * @return the JComment header or null if none exists.
     */
    public JComment getHeader() {
        return this.header;
    } // -- getHeader

    /**
     * Returns an Enumeration of imported package and
     * class names for this JStructure.
     *
     * @return the Enumeration of imports. May be empty.
     */
    public Enumeration<String> getImports() {
        return Collections.enumeration(imports);
    } // -- getImports

    /**
     * Returns an Enumeration of interface names that this
     * JStructure inherits from.
     *
     * @return the Enumeration of interface names for this
     * JStructure. May be empty.
     */
    public Enumeration<String> getInterfaces() {
        return Collections.enumeration(interfaces);
    } // -- getInterfaces

    /**
     * Returns the Java Doc comment for this JStructure
     *
     * @return the JDocComment for this JStructure
     */
    public JDocComment getJDocComment() {
        return jdc;
    } // -- getJDocComment

    /**
     * Returns an array of all the JMethodSignatures of this JInterface.
     *
     * @return an array of all the JMethodSignatures of this JInterface.
     */
    /*
        public JMethodSignature[] getMethods() {
            JMethodSignature[] marray = new JMethodSignature[methods.size()];
            methods.copyInto(marray);
            return marray;
        } //-- getMethods
    */

    /**
     * Returns the JMethodSignature with the given name,
     * and occuring at or after the given starting index.
     *
     * @param name the name of the JMethodSignature to return.
     * @param startIndex the starting index to begin searching
     * from.
     * @return the JMethodSignature, or null if not found.
     */
    /*
        public JMethodSignature getMethod(String name, int startIndex) {
            for (int i = startIndex; i < methods.size(); i++) {
                JMethodSignature jMethod = (JMethodSignature)methods.elementAt(i);
                if (jMethod.getName().equals(name)) return jMethod;
            }
            return null;
        } //-- getMethod
    */

    /**
     * Returns the JMethodSignature at the given index.
     *
     * @param index the index of the JMethodSignature to return.
     * @return the JMethodSignature at the given index.
     */
    /*
       public JMethodSignature getMethod(int index) {
           return (JMethodSignature)methods.elementAt(index);
       } //-- getMethod
    */

    /**
     * Returns the JModifiers which allows the qualifiers to be changed.
     *
     * @return the JModifiers for this JStructure.
     */
    public JModifiers getModifiers() {
        return modifiers;
    } // -- getModifiers

    /**
     * Returns the name of the package that this JStructure is a member
     * of.
     *
     * @return the name of the package that this JStructure is a member
     * of, or null if there is no current package name defined.
     */
    public String getPackageName() {
        return this.packageName;
    } // -- getPackageName

    /**
     * Returns the name of the interface.
     *
     * @param stripPackage a boolean that when true indicates that only
     * the local name (no package) should be returned.
     * @return the name of the class.
     */
    public String getName(boolean stripPackage) {
        String name = super.getName();
        if (stripPackage) {
            int period = name.lastIndexOf(".");
            if (period > 0) name = name.substring(period + 1);
        }
        return name;
    } // -- getName

    /**
     * Returns true if the given classname exists in the imports
     * of this JStructure
     *
     * @param classname the class name to check for
     * @return true if the given classname exists in the imports list
     */
    public boolean hasImport(String classname) {
        return imports.contains(classname);
    } // -- hasImport

    public boolean removeImport(String className) {
        boolean result = false;
        if (className == null) return result;
        if (className.length() == 0) return result;

        return imports.remove(className);
    } // -- removeImport

    public boolean isAbstract() {
        return modifiers.isAbstract();
    }

    public static boolean isValidClassName(String name) {

        if (name == null) return false;

        // -- ignore package information, for now
        int period = name.lastIndexOf(".");
        if (period > 0) name = name.substring(period + 1);

        return JNaming.isValidJavaIdentifier(name);
    } // -- isValidClassName

    /**
     * Prints the source code for this JStructure in the current
     * working directory. Sub-directories will be created if necessary
     * for the package.
     */
    public void print() {
        print((String) null, (String) null);
    } // -- printSrouce

    /**
     * Prints the source code for this JStructure to the destination
     * directory. Sub-directories will be created if necessary for the
     * package.
     *
     * @param destDir the destination directory
     * @param lineSeparator the line separator to use at the end of each line.
     * If null, then the default line separator for the runtime platform will
     * be used.
     */
    public void print(String destDir, String lineSeparator) {

        //        String name = getLocalName();

        // -- open output file
        String filename = getFilename(destDir);

        File file = new File(filename);
        JSourceWriter jsw = null;
        try {
            jsw = new JSourceWriter(WriterFactory.newPlatformWriter(file));
        } catch (java.io.IOException ioe) {
            System.out.println("unable to create class file: " + filename);
            return;
        }
        if (lineSeparator == null) {
            lineSeparator = System.getProperty("line.separator");
        }
        jsw.setLineSeparator(lineSeparator);
        print(jsw);
        jsw.close();
    } // -- print

    /**
     * Prints the source code for this JStructure to the given
     * JSourceWriter.
     *
     * @param jsw the JSourceWriter to print to.
     */
    public abstract void print(JSourceWriter jsw);

    /**
     * A utility method that prints the header to the given
     * JSourceWriter
     *
     * @param jsw the JSourceWriter to print to.
     */
    public void printHeader(JSourceWriter jsw) {

        if (jsw == null) {
            throw new IllegalArgumentException("argument 'jsw' should not be null.");
        }

        // -- write class header
        JComment header = getHeader();
        if (header != null) header.print(jsw);
        else {
            jsw.writeln("/*");
            jsw.writeln(" * " + DEFAULT_HEADER);
            jsw.writeln(" */");
        }
        jsw.writeln();
        jsw.flush();
    } // -- printHeader

    /**
     * A utility method that prints the imports to the given
     * JSourceWriter
     *
     * @param jsw the JSourceWriter to print to.
     */
    public void printImportDeclarations(JSourceWriter jsw) {

        if (jsw == null) {
            throw new IllegalArgumentException("argument 'jsw' should not be null.");
        }

        // -- print imports
        if (imports.size() > 0) {
            jsw.writeln("  //---------------------------------/");
            jsw.writeln(" //- Imported classes and packages -/");
            jsw.writeln("//---------------------------------/");
            jsw.writeln();
            for (String imp : imports) {
                jsw.write("import ");
                jsw.write(imp);
                jsw.writeln(';');
            }
            jsw.writeln();
            jsw.flush();
        }
    } // -- printImportDeclarations

    /**
     * A utility method that prints the packageDeclaration to
     * the given JSourceWriter
     *
     * @param jsw the JSourceWriter to print to.
     */
    public void printPackageDeclaration(JSourceWriter jsw) {

        if (jsw == null) {
            throw new IllegalArgumentException("argument 'jsw' should not be null.");
        }

        // -- print package name
        if ((packageName != null) && (packageName.length() > 0)) {
            jsw.write("package ");
            jsw.write(packageName);
            jsw.writeln(';');
            jsw.writeln();
        }
        jsw.flush();
    } // -- printPackageDeclaration

    /**
     * Prints the source code for this JStructure to the given
     * JSourceWriter.
     *
     * @param jsw the JSourceWriter to print to.
     *
     * public abstract void print(JSourceWriter jsw);
     *
     *
     * StringBuilder buffer = new StringBuilder();
     *
     *
     * printHeader();
     * printPackageDeclaration();
     * printImportDeclarations();
     *
     * //------------/
     * //- Java Doc -/
     * //------------/
     *
     * jdc.print(jsw);
     *
     * //-- print class information
     * //-- we need to add some JavaDoc API adding comments
     *
     * buffer.setLength(0);
     *
     * if (modifiers.isPrivate()) {
     * buffer.append("private ");
     * }
     * else if (modifiers.isPublic()) {
     * buffer.append("public ");
     * }
     *
     * if (modifiers.isAbstract()) {
     * buffer.append("abstract ");
     * }
     *
     * buffer.append("interface ");
     * buffer.append(getLocalName());
     * buffer.append(' ');
     * if (interfaces.size() > 0) {
     * boolean endl = false;
     * if (interfaces.size() > 1) {
     * jsw.writeln(buffer.toString());
     * buffer.setLength(0);
     * endl = true;
     * }
     * buffer.append("extends ");
     * for (int i = 0; i < interfaces.size(); i++) {
     * if (i > 0) buffer.append(", ");
     * buffer.append(interfaces.elementAt(i));
     * }
     * if (endl) {
     * jsw.writeln(buffer.toString());
     * buffer.setLength(0);
     * }
     * else buffer.append(' ');
     * }
     *
     * buffer.append('{');
     * jsw.writeln(buffer.toString());
     * buffer.setLength(0);
     * jsw.writeln();
     *
     * jsw.indent();
     *
     * //-- print method signatures
     *
     * if (methods.size() > 0) {
     * jsw.writeln();
     * jsw.writeln("  //-----------/");
     * jsw.writeln(" //- Methods -/");
     * jsw.writeln("//-----------/");
     * jsw.writeln();
     * }
     *
     * for (int i = 0; i < methods.size(); i++) {
     * JMethodSignature signature = (JMethodSignature) methods.elementAt(i);
     * signature.print(jsw);
     * jsw.writeln(';');
     * }
     *
     * jsw.unindent();
     * jsw.writeln('}');
     * jsw.flush();
     * jsw.close();
     * } //-- printSource
     */

    /**
     * Sets the header comment for this JStructure
     *
     * @param comment the comment to display at the top of the source file
     * when printed
     */
    public void setHeader(JComment comment) {
        this.header = comment;
    } // -- setHeader

    // ---------------------/
    // - Protected Methods -/
    // ---------------------/

    protected int getInterfaceCount() {
        return interfaces.size();
    }

    /**
     * Prints the given source string to the JSourceWriter using the given prefix at
     * the beginning of each new line.
     *
     * @param prefix the prefix for each new line.
     * @param source the source code to print
     * @param jsw the JSourceWriter to print to.
     */
    protected static void printlnWithPrefix(String prefix, String source, JSourceWriter jsw) {
        jsw.write(prefix);
        if (source == null) return;

        char[] chars = source.toCharArray();
        int lastIdx = 0;
        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            if (ch == '\n') {
                // -- free buffer
                jsw.write(chars, lastIdx, (i - lastIdx) + 1);
                lastIdx = i + 1;
                if (i < chars.length) {
                    jsw.write(prefix);
                }
            }
        }
        // -- free buffer
        if (lastIdx < chars.length) {
            jsw.write(chars, lastIdx, chars.length - lastIdx);
        }
        jsw.writeln();
    } // -- printlnWithPrefix

    /**
     * Returns the package name from the given class name
     *
     * @param className the className
     * @return the package of the class, otherwise {@code null}
     */
    protected static String getPackageFromClassName(String className) {
        int idx = -1;
        if ((idx = className.lastIndexOf('.')) > 0) {
            return className.substring(0, idx);
        }
        return null;
    } // -- getPackageFromClassName

    /**
     * @return the annotations
     */
    public JAnnotations getAnnotations() {
        return annotations;
    }

    /**
     * @param annotation the annotation to append
     */
    public void appendAnnotation(String annotation) {
        if (annotations == null) {
            annotations = new JAnnotations();
        }
        annotations.appendAnnotation(annotation);
    }

    /**
     * @param annotations the annotations to set
     */
    public void setAnnotations(JAnnotations annotations) {
        this.annotations = annotations;
    }
} // -- JStructure
