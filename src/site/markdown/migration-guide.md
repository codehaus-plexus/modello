Migration Guide
===============

Migration from 1.11 to 2.0
--------------------------

* `useJava5(boolean)` has been replaced with `javaSource(int)`
* Associate attributes `java.generate-create` and `java.generate-break` have been replaced with attribute `java.bidi`
* Associate attribute `java.use-interface` has been renamed to `java.useInterface`
* Class attribute `xml.namespace` has been moved to model
* Class attribute `xml.schemaLocation` has been moved to model
* Field attribute `java.adder` has been moved to association
* Field attribute `xml.associationTagName` has been moved to `xml.tagName` in association
* Field attribute `xml.listStyle` has been moved to  `xml.itemsStyle` in association
* Field type `Content` has been replaced with field attribute `xml.content="true"` + type `String`
* Model attribute `xsd.target-namespace` has been renamed to `xsd.targetNamespace`

