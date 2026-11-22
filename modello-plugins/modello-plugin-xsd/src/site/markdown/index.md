---
title: Modello XSD Plugin
---

# Modello XSD Plugin

Modello XSD Plugin generates an XML Schema from the model, with the `xsd` goal of the Modello Maven plugin.
Fields that are `required` in the model are required in the schema, and the others are optional.

## Constraining a String field

Three field attributes add a constraint to the schema type of a `String` field. They are mutually exclusive:
a field that sets more than one of them fails the build.

| Field attribute    | Generated schema                                                                                                           |
|:-------------------|:---------------------------------------------------------------------------------------------------------------------------|
| `xsd.enumerations` | A restriction of `xs:string` with one `xs:enumeration` per value: other values are rejected.                               |
| `xsd.suggestions`  | A union of a fixed enumeration and plain `xs:string`: editors can complete the listed values, but any value is still valid. |
| `xsd.pattern`      | A restriction of `xs:string` with an `xs:pattern` facet.                                                                   |

The values of `xsd.enumerations` and `xsd.suggestions` are separated by commas.

```xml
<field xsd.enumerations="compile,runtime,provided,test,system">
  <name>scope</name>
  <version>1.0.0+</version>
  <type>String</type>
  <description>The dependency scope.</description>
</field>

<field xsd.pattern="[a-zA-Z0-9._\-]+">
  <name>artifactId</name>
  <version>1.0.0+</version>
  <type>String</type>
  <description>The artifact identifier.</description>
</field>
```

## Mixed content

When a class has an element field with `xml.format="mixed"` and no `xml.tagName`, the schema type of
the class is generated with `mixed="true"`, so that it accepts both text and child elements.
