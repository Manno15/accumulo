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

import java.util.Properties;

import org.apache.accumulo.core.client.admin.InstanceOperations;
import org.apache.accumulo.core.client.admin.NamespaceOperations;
import org.apache.accumulo.core.client.admin.ReplicationOperations;
import org.apache.accumulo.core.client.admin.SecurityOperations;
import org.apache.accumulo.core.client.admin.TableOperations;
import org.apache.accumulo.core.client.security.tokens.AuthenticationToken;
import org.apache.accumulo.core.security.Authorizations;

/**
 * Client connection to an Accumulo instance. Allows the user to request a scanner, deleter or
 * writer for the instance as well as various objects that permit administrative operations.
 * Enforces security on the client side with by requiring user credentials.
 *
 * Supports fluent API for creation. Various options can be provided to {@link Accumulo#newClient()}
 * and when finished a call to build() will return the AccumuloClient object. For example:
 *
 * {@code Accumulo.newClient().forInstance(instanceName, zookeepers)
 *         .usingPassword(user, password).withZkTimeout(1234).build();}
 *
 * @since 2.0.0
 */
public interface AccumuloClient {

  /**
   * Factory method to create a BatchScanner connected to Accumulo.
   *
   * @param tableName
   *          the name of the table to query
   * @param authorizations
   *          A set of authorization labels that will be checked against the column visibility of
   *          each key in order to filter data. The authorizations passed in must be a subset of the
   *          accumulo user's set of authorizations. If the accumulo user has authorizations (A1,
   *          A2) and authorizations (A2, A3) are passed, then an exception will be thrown.
   * @param numQueryThreads
   *          the number of concurrent threads to spawn for querying
   *
   * @return BatchScanner object for configuring and querying
   * @throws TableNotFoundException
   *           when the specified table doesn't exist
   */
  public BatchScanner createBatchScanner(String tableName, Authorizations authorizations,
      int numQueryThreads) throws TableNotFoundException;

  /**
   * Factory method to create a BatchScanner connected to Accumulo. This method uses the number of
   * query threads configured when AccumuloClient was created. If none were configured, defaults
   * will be used.
   *
   * @param tableName
   *          the name of the table to query
   * @param authorizations
   *          A set of authorization labels that will be checked against the column visibility of
   *          each key in order to filter data. The authorizations passed in must be a subset of the
   *          accumulo user's set of authorizations. If the accumulo user has authorizations (A1,
   *          A2) and authorizations (A2, A3) are passed, then an exception will be thrown.
   *
   * @return BatchScanner object for configuring and querying
   * @throws TableNotFoundException
   *           when the specified table doesn't exist
   */
  public BatchScanner createBatchScanner(String tableName, Authorizations authorizations)
      throws TableNotFoundException;

  /**
   * Factory method to create BatchDeleter
   *
   * @param tableName
   *          the name of the table to query and delete from
   * @param authorizations
   *          A set of authorization labels that will be checked against the column visibility of
   *          each key in order to filter data. The authorizations passed in must be a subset of the
   *          accumulo user's set of authorizations. If the accumulo user has authorizations (A1,
   *          A2) and authorizations (A2, A3) are passed, then an exception will be thrown.
   * @param numQueryThreads
   *          the number of concurrent threads to spawn for querying
   * @param config
   *          configuration used to create batch writer. This config takes precedence. Any unset
   *          values will be merged with config set when the AccumuloClient was created. If no
   *          config was set during AccumuloClient creation, BatchWriterConfig defaults will be
   *          used.
   * @return BatchDeleter object for configuring and deleting
   */

  public BatchDeleter createBatchDeleter(String tableName, Authorizations authorizations,
      int numQueryThreads, BatchWriterConfig config) throws TableNotFoundException;

  /**
   * Factory method to create BatchDeleter. This method uses BatchWriterConfig set when
   * AccumuloClient was created. If none was set, BatchWriterConfig defaults will be used.
   *
   * @param tableName
   *          the name of the table to query and delete from
   * @param authorizations
   *          A set of authorization labels that will be checked against the column visibility of
   *          each key in order to filter data. The authorizations passed in must be a subset of the
   *          accumulo user's set of authorizations. If the accumulo user has authorizations (A1,
   *          A2) and authorizations (A2, A3) are passed, then an exception will be thrown.
   * @param numQueryThreads
   *          the number of concurrent threads to spawn for querying
   * @return BatchDeleter object
   * @throws TableNotFoundException
   *           if table not found
   */
  public BatchDeleter createBatchDeleter(String tableName, Authorizations authorizations,
      int numQueryThreads) throws TableNotFoundException;

  /**
   * Factory method to create a BatchWriter connected to Accumulo.
   *
   * @param tableName
   *          the name of the table to insert data into
   * @param config
   *          configuration used to create batch writer. This config will take precedence. Any unset
   *          values will merged with config set when the AccumuloClient was created. If no config
   *          was set during AccumuloClient creation, BatchWriterConfig defaults will be used.
   * @return BatchWriter object for configuring and writing data to
   */
  public BatchWriter createBatchWriter(String tableName, BatchWriterConfig config)
      throws TableNotFoundException;

  /**
   * Factory method to create a BatchWriter. This method uses BatchWriterConfig set when
   * AccumuloClient was created. If none was set, BatchWriterConfig defaults will be used.
   *
   * @param tableName
   *          the name of the table to insert data into
   * @return BatchWriter object
   * @throws TableNotFoundException
   *           if table not found
   */
  public BatchWriter createBatchWriter(String tableName) throws TableNotFoundException;

  /**
   * Factory method to create a Multi-Table BatchWriter connected to Accumulo. Multi-table batch
   * writers can queue data for multiple tables. Also data for multiple tables can be sent to a
   * server in a single batch. Its an efficient way to ingest data into multiple tables from a
   * single process.
   *
   * @param config
   *          configuration used to create multi-table batch writer. This config will take
   *          precedence. Any unset values will merged with config set when the AccumuloClient was
   *          created. If no config was set during AccumuloClient creation, BatchWriterConfig
   *          defaults will be used.
   * @return MultiTableBatchWriter object for configuring and writing data to
   */
  public MultiTableBatchWriter createMultiTableBatchWriter(BatchWriterConfig config);

  /**
   * Factory method to create a Multi-Table BatchWriter. This method uses BatchWriterConfig set when
   * AccumuloClient was created. If none was set, BatchWriterConfig defaults will be used.
   *
   * @return MultiTableBatchWriter object
   */
  public MultiTableBatchWriter createMultiTableBatchWriter();

  /**
   * Factory method to create a Scanner connected to Accumulo.
   *
   * @param tableName
   *          the name of the table to query data from
   * @param authorizations
   *          A set of authorization labels that will be checked against the column visibility of
   *          each key in order to filter data. The authorizations passed in must be a subset of the
   *          accumulo user's set of authorizations. If the accumulo user has authorizations (A1,
   *          A2) and authorizations (A2, A3) are passed, then an exception will be thrown.
   *
   * @return Scanner object for configuring and querying data with
   * @throws TableNotFoundException
   *           when the specified table doesn't exist
   *
   * @see IsolatedScanner
   */
  public Scanner createScanner(String tableName, Authorizations authorizations)
      throws TableNotFoundException;

  /**
   * Factory method to create a ConditionalWriter connected to Accumulo.
   *
   * @param tableName
   *          the name of the table to query data from
   * @param config
   *          configuration used to create conditional writer
   *
   * @return ConditionalWriter object for writing ConditionalMutations
   * @throws TableNotFoundException
   *           when the specified table doesn't exist
   */
  public ConditionalWriter createConditionalWriter(String tableName, ConditionalWriterConfig config)
      throws TableNotFoundException;

  /**
   * Get the current user for this AccumuloClient
   *
   * @return the user name
   */
  public String whoami();

  /**
   * Returns a unique string that identifies this instance of accumulo.
   *
   * @return a UUID
   */
  public String getInstanceID();

  /**
   * Retrieves a TableOperations object to perform table functions, such as create and delete.
   *
   * @return an object to manipulate tables
   */
  public abstract TableOperations tableOperations();

  /**
   * Retrieves a NamespaceOperations object to perform namespace functions, such as create and
   * delete.
   *
   * @return an object to manipulate namespaces
   */
  public NamespaceOperations namespaceOperations();

  /**
   * Retrieves a SecurityOperations object to perform user security operations, such as creating
   * users.
   *
   * @return an object to modify users and permissions
   */
  public SecurityOperations securityOperations();

  /**
   * Retrieves an InstanceOperations object to modify instance configuration.
   *
   * @return an object to modify instance configuration
   */
  public InstanceOperations instanceOperations();

  /**
   * Retrieves a ReplicationOperations object to manage replication configuration.
   *
   * @return an object to modify replication configuration
   */
  public ReplicationOperations replicationOperations();

  /**
   * @return {@link ClientInfo} which contains information about client connection to Accumulo
   */
  public abstract ClientInfo info();

  /**
   * Change user
   *
   * @param principal
   *          Principal/username
   * @param token
   *          Authentication token
   * @return {@link AccumuloClient} for new user
   */
  public abstract AccumuloClient changeUser(String principal, AuthenticationToken token)
      throws AccumuloSecurityException, AccumuloException;

  /**
   * Builds ClientInfo after all options have been specified
   */
  public interface ClientInfoFactory {

    /**
     * Builds ClientInfo after all options have been specified
     *
     * @return ClientInfo
     */
    ClientInfo info();
  }

  /**
   * Builds AccumuloClient
   */
  public interface AccumuloClientFactory extends ClientInfoFactory {

    /**
     * Builds AccumuloClient after all options have been specified
     *
     * @return AccumuloClient
     */
    AccumuloClient build() throws AccumuloException, AccumuloSecurityException;

  }

  /**
   * Builder method for setting Accumulo instance and zookeepers
   */
  public interface InstanceArgs {
    AuthenticationArgs forInstance(String instanceName, String zookeepers);
  }

  /**
   * Builder methods for creating AccumuloClient using properties
   */
  public interface PropertyOptions extends InstanceArgs {

    /**
     * Build using properties file. An example properties file can be found at
     * conf/accumulo-client.properties in the Accumulo tarball distribution.
     *
     * @param propertiesFile
     *          Path to properties file
     * @return this builder
     */
    AccumuloClientFactory usingProperties(String propertiesFile);

    /**
     * Build using Java properties object. A list of available properties can be found in the
     * documentation on the project website (http://accumulo.apache.org) under 'Development' -&gt;
     * 'Client Properties'
     *
     * @param properties
     *          Properties object
     * @return this builder
     */
    AccumuloClientFactory usingProperties(Properties properties);
  }

  public interface ClientInfoOptions extends PropertyOptions {

    /**
     * Build using Accumulo client information
     *
     * @param clientInfo
     *          ClientInfo object
     * @return this builder
     */
    FromOptions usingClientInfo(ClientInfo clientInfo);
  }

  /**
   * Build methods for authentication
   */
  public interface AuthenticationArgs {

    /**
     * Build using password-based credentials
     *
     * @param username
     *          User name
     * @param password
     *          Password
     * @return this builder
     */
    ConnectionOptions usingPassword(String username, CharSequence password);

    /**
     * Build using Kerberos credentials
     *
     * @param principal
     *          Principal
     * @param keyTabFile
     *          Path to keytab file
     * @return this builder
     */
    ConnectionOptions usingKerberos(String principal, String keyTabFile);

    /**
     * Build using specified credentials
     *
     * @param principal
     *          Principal/username
     * @param token
     *          Authentication token
     * @return this builder
     */
    ConnectionOptions usingToken(String principal, AuthenticationToken token);
  }

  /**
   * Build methods for SSL/TLS
   */
  public interface SslOptions extends AccumuloClientFactory {

    /**
     * Build with SSL trust store
     *
     * @param path
     *          Path to trust store
     * @return this builder
     */
    SslOptions withTruststore(String path);

    /**
     * Build with SSL trust store
     *
     * @param path
     *          Path to trust store
     * @param password
     *          Password used to encrypt trust store
     * @param type
     *          Trust store type
     * @return this builder
     */
    SslOptions withTruststore(String path, String password, String type);

    /**
     * Build with SSL key store
     *
     * @param path
     *          Path to SSL key store
     * @return this builder
     */
    SslOptions withKeystore(String path);

    /**
     * Build with SSL key store
     *
     * @param path
     *          Path to keystore
     * @param password
     *          Password used to encrypt key store
     * @param type
     *          Key store type
     * @return this builder
     */
    SslOptions withKeystore(String path, String password, String type);

    /**
     * Use JSSE system properties to configure SSL
     *
     * @return this builder
     */
    SslOptions useJsse();
  }

  /**
   * Build methods for SASL
   */
  public interface SaslOptions extends AccumuloClientFactory {

    /**
     * Build with Kerberos Server Primary
     *
     * @param kerberosServerPrimary
     *          Kerberos server primary
     * @return this builder
     */
    SaslOptions withPrimary(String kerberosServerPrimary);

    /**
     * Build with SASL quality of protection
     *
     * @param qualityOfProtection
     *          Quality of protection
     * @return this builder
     */
    SaslOptions withQop(String qualityOfProtection);
  }

  /**
   * Build methods for connection options
   */
  public interface ConnectionOptions extends AccumuloClientFactory {

    /**
     * Build using Zookeeper timeout
     *
     * @param timeout
     *          Zookeeper timeout (in milliseconds)
     * @return this builder
     */
    ConnectionOptions withZkTimeout(int timeout);

    /**
     * Build with SSL/TLS options
     *
     * @return this builder
     */
    SslOptions withSsl();

    /**
     * Build with SASL options
     *
     * @return this builder
     */
    SaslOptions withSasl();

    /**
     * Build with BatchWriterConfig defaults for BatchWriter, MultiTableBatchWriter &amp;
     * BatchDeleter
     *
     * @param batchWriterConfig
     *          BatchWriterConfig
     * @return this builder
     */
    ConnectionOptions withBatchWriterConfig(BatchWriterConfig batchWriterConfig);

    /**
     * Build with default number of query threads for BatchScanner
     */
    ConnectionOptions withBatchScannerQueryThreads(int numQueryThreads);

    /**
     * Build with default batch size for Scanner
     */
    ConnectionOptions withScannerBatchSize(int batchSize);
  }

  public interface FromOptions extends ConnectionOptions, PropertyOptions, AuthenticationArgs {

  }
}
