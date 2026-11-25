import java.io.*

// every generator must have produced its classes, and all of them compile without plexus-utils;
// the classes of one XML library must not refer to the others, as when a project uses only that library
def others = [
    "dom4j" : ["org/jdom/", "org/codehaus/stax2/", "javax/xml/stream/"],
    "jdom" : ["org/dom4j/", "org/codehaus/stax2/", "javax/xml/stream/"],
    "stax" : ["org/dom4j/", "org/jdom/"]]
for (String name : [
            "test/io/dom4j/ThingDom4jReader.class",
            "test/io/dom4j/ThingDom4jWriter.class",
            "test/io/jdom/ThingJDOMWriter.class",
            "test/io/stax/ThingStaxReader.class",
            "test/io/stax/ThingStaxWriter.class"
        ]) {
    File clazz = new File(basedir, "target/classes/" + name)
    if (!clazz.isFile()) {
        throw new FileNotFoundException("Missing " + clazz)
    }
    String constants = new String(clazz.bytes, "ISO-8859-1")
    for (String other : others[name.split("/")[2]]) {
        assert !constants.contains(other) : name + " refers to " + other
    }
}
