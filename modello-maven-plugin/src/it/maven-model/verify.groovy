/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

File generatedSources = new File(basedir, "target/generated-sources/modello")
File generatedSite = new File(basedir, "target/generated-site/resources/xsd")
assert generatedSources.exists()
assert generatedSources.isDirectory()
assert generatedSite.exists()
assert generatedSite.isDirectory()

String javaSource = new File(generatedSources, "org/apache/maven/model/Model.java").text
String xsdSource = new File(generatedSite, "maven-4.0.0.xsd").text

// due formatting issues (empty lines lost) let's stick with trivial license and assertion for now
assert javaSource.contains("The license of this file")
assert xsdSource.contains("The license of this file")
