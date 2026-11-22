// javadoc:javadoc must document the generated sources, including the private members (show = private)
// and link to the source (linksource); the output directory depends on the Maven version
String model = "org/plexus/modello/demo/model/Root.html"
List<String> pages = []
new File(basedir, "target").eachFileRecurse { f ->
    pages << f.path.replace(File.separator, "/")
}

List<String> apiPages = pages.findAll { it.endsWith("/apidocs/" + model) }
assert apiPages.size() == 1 : "Expected one javadoc page for Root, found " + apiPages
assert new File(apiPages[0]).getText("UTF-8").contains("simpleField") : "javadoc of Root does not describe simpleField"
assert pages.any { it.endsWith("/apidocs/src-html/" + model) } : "javadoc did not link the source of Root"
