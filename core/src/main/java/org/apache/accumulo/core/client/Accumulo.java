/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.accumulo.core.client;

import org.apache.accumulo.core.client.impl.AccumuloClientImpl;
import org.apache.accumulo.core.client.lexicoder.Lexicoder;
import org.apache.accumulo.core.client.rfile.RFile;

//CHECKSTYLE:OFF
/**
 * Entry point for majority of Accumulo's public API. Other Accumulo API entry points are linked
 * below.
 *
 * <H2>API Definition</H2>
 *
 * Accumulo's public API is composed of all public types in the following packages and their
 * sub-packages excluding those named <i>impl</i>, <i>thrift</i>, or <i>crypto</i>.
 *
 * <UL>
 * <LI>org.apache.accumulo.core.client
 * <LI>org.apache.accumulo.core.data
 * <LI>org.apache.accumulo.core.security
 * <LI>org.apache.accumulo.minicluster
 * </UL>
 *
 * <p>
 * A type is a class, interface, or enum. Anything with public or protected access in an API type is
 * in the API. This includes, but is not limited to: methods, members classes, interfaces, and
 * enums. Package-private types in the above packages are <i>not</i> considered public API.
 *
 * <p>
 * API changes are made according to semver 2.0. Accumulo did not always follow semver, it was
 * adopted for Accumulo 1.6.2 and 1.7.0. The Accumulo project maintains binary compatibility across
 * this API within a major release, as defined in the Java Language Specification 3rd ed. Accumulo
 * code outside of the API does not follow semver and may change in incompatible ways at any
 * release.
 *
 * <p>
 * The following regex matches imports that are <i>not</i> Accumulo public API. This regex can be
 * used with <a href="http://checkstyle.sourceforge.net/config_regexp.html">RegexpSingleline</a> to
 * automatically find suspicious imports in a project using Accumulo.
 *
 * <p>
 * {@code import\s+org\.apache\.accumulo\.(.*\.(impl|thrift|crypto)\..*|(?!core|minicluster).*|core\.(?!client|data|security).*)}
 *
 * <H2>API Entry Points</H2>
 *
 * <p>
 * This class contains all API entry points created in 2.0.0 or later. The majority of the API is
 * accessible indirectly via methods in this class. Below are a list of APIs entry points that were
 * created before Accumulo 2.0 and not accessible from here.
 *
 * <UL>
 * <LI>Hadoop input, output formats and partitioners in
 * {@code org.apache.accumulo.core.client.mapred} and
 * {@code org.apache.accumulo.core.client.mapreduce} packages (excluding {@code impl} sub-packages).
 * <LI>{@code org.apache.accumulo.minicluster.MiniAccumuloCluster} Not linkable by javadoc, because
 * in a separate module.
 * <LI>{@link Lexicoder} and all of its implementations in the same package (excluding the
 * {@code impl} sub-package).
 * <LI>{@link RFile}
 * </UL>
 *
 * @see <a href="http://accumulo.apache.org/">Accumulo Website</a>
 * @see <a href="http://semver.org/spec/v2.0.0">Semver 2.0</a>
 * @since 2.0.0
 */
// CHECKSTYLE:ON
public final class Accumulo {

  private Accumulo() {}

  /**
   * Fluent entry point for creating an {@link AccumuloClient}. For example:
   *
   * <pre>
   * <code>
   *    AccumuloClient client = Accumulo.newClient()
   *      .forInstance(instanceName, zookeepers)
   *      .usingPassword(user, password)
   *      .withZkTimeout(1234)
   *      .build();
   * </code>
   * </pre>
   *
   * @return a builder object for Accumulo clients
   */
  public static AccumuloClient.ClientInfoOptions newClient() {
    return new AccumuloClientImpl.AccumuloClientBuilderImpl();
  }
}
