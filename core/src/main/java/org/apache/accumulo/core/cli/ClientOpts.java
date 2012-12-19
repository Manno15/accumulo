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
package org.apache.accumulo.core.cli;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import org.apache.accumulo.cloudtrace.instrument.Trace;
import org.apache.accumulo.core.Constants;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriterConfig;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.client.mapreduce.AccumuloInputFormat;
import org.apache.accumulo.core.client.mapreduce.AccumuloOutputFormat;
import org.apache.accumulo.core.client.mock.MockInstance;
import org.apache.accumulo.core.conf.AccumuloConfiguration;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.core.security.ColumnVisibility;
import org.apache.accumulo.core.security.thrift.AuthInfo;
import org.apache.hadoop.mapreduce.Job;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;

public class ClientOpts extends Help {

  public static class TimeConverter implements IStringConverter<Long> {
    @Override
    public Long convert(String value) {
      return AccumuloConfiguration.getTimeInMillis(value);
    }
  }
  
  public static class MemoryConverter implements IStringConverter<Long> {
    @Override
    public Long convert(String value) {
      return AccumuloConfiguration.getMemoryInBytes(value);
    }
  }
  
  public static class AuthConverter implements IStringConverter<Authorizations> {
    @Override
    public Authorizations convert(String value) {
      return new Authorizations(value.split(","));
    }
  }
  
  public static class Password {
    public byte[] value;
    public Password(String dfault) { value = dfault.getBytes(); }
    public String toString() {
      return new String(value);
    }
  }
  
  public static class PasswordConverter implements IStringConverter<Password> {
    @Override
    public Password convert(String value) {
      return new Password(value);
    }
  }
  
  public static class VisibilityConverter implements IStringConverter<ColumnVisibility> {
    @Override
    public ColumnVisibility convert(String value) {
      return new ColumnVisibility(value);
    }
  }
  
  private static final BatchWriterConfig BWDEFAULTS = new BatchWriterConfig();
  
  @Parameter(names={"-u", "--user"}, description = "Connection user")
  public String user = System.getProperty("user.name");
  
  @Parameter(names="-p", converter=PasswordConverter.class, description = "Connection password")
  public Password password = new Password("secret");
  
  @Parameter(names="--password", converter=PasswordConverter.class, description = "Enter the connection password", password=true)
  public Password securePassword = null;
  
  public byte[] getPassword() {
    if (securePassword == null) {
      return password.value;
    }
    return securePassword.value;
  }
  
  @Parameter(names={"-z", "--keepers"}, description="Comma separated list of zookeeper hosts (host:port,host:port)")
  public String zookeepers = "localhost:2181";
  
  @Parameter(names={"-i", "--instance"}, description="The name of the accumulo instance")
  public String instance = null;
  
  @Parameter(names={"-auths", "--auths"}, converter=AuthConverter.class, description="the authorizations to use when reading or writing")
  public Authorizations auths = Constants.NO_AUTHS;
  
  @Parameter(names="--batchThreads", description="Number of threads to use when writing large batches")
  public Integer batchThreads = BWDEFAULTS.getMaxWriteThreads();

  @Parameter(names="--batchLatency", converter=TimeConverter.class, description="The maximum time to wait before flushing data to servers when writing")
  public Long batchLatency = BWDEFAULTS.getMaxLatency(TimeUnit.MILLISECONDS);
  
  @Parameter(names="--batchMemory", converter=MemoryConverter.class, description="memory used to batch data when writing")
  public Long batchMemory = BWDEFAULTS.getMaxMemory();
  
  @Parameter(names="--batchTimeout", converter=TimeConverter.class, description="timeout used to fail a batch write")
  public Long batchTimeout = BWDEFAULTS.getTimeout(TimeUnit.MILLISECONDS);
  
  @Parameter(names="--scanBatchSize", description="the number of key-values to pull during a scan or batch scan")
  public int scanBatchSize = 1000; 

  @Parameter(names="--scanThreads", description="number of threads to use when batch scanning")
  public Integer scanThreads = 10;
  
  @Parameter(names="--debug", description="turn on TRACE-level log messages")
  public boolean debug = false;
  
  @Parameter(names={"-fake", "--mock"}, description="Use a mock Instance")
  public boolean mock=false;
  
  public void startDebugLogging() {
    if (debug)
      Logger.getLogger(Constants.CORE_PACKAGE_NAME).setLevel(Level.TRACE);
  }
  
  @Parameter(names="--trace", description="turn on distributed tracing")
  public boolean trace = false;
  
  public void startTracing(String applicationName) {
    if (trace) {
      Trace.on(applicationName);
    }
  }
  
  public void stopTracing() {
    Trace.off();
  }
  
  public void parseArgs(String programName, String[] args) {
    super.parseArgs(programName, args);
    startDebugLogging();
    startTracing(programName);
  }
  
  public BatchWriterConfig getBatchWriterConfig() {
    BatchWriterConfig config = new BatchWriterConfig();
    config.setMaxLatency(this.batchLatency, TimeUnit.MILLISECONDS);
    config.setMaxMemory(this.batchMemory);
    config.setTimeout(this.batchTimeout, TimeUnit.MILLISECONDS);
    return config;
  }
  
  protected Instance cachedInstance = null;
  
  synchronized public Instance getInstance() {
    if (cachedInstance != null)
      return cachedInstance;
    if (mock)
      return cachedInstance = new MockInstance(instance);
    return cachedInstance = new ZooKeeperInstance(this.instance, this.zookeepers);
  }
  
  public Connector getConnector() throws AccumuloException, AccumuloSecurityException {
    return getInstance().getConnector(this.user, this.getPassword());
  }
  
  public AuthInfo getAuthInfo() {
    return new AuthInfo(user, ByteBuffer.wrap(getPassword()), getInstance().getInstanceID());
  }
  
  public void setAccumuloConfigs(Job job) {
    AccumuloInputFormat.setZooKeeperInstance(job.getConfiguration(), instance, zookeepers);
    AccumuloOutputFormat.setZooKeeperInstance(job.getConfiguration(), instance, zookeepers);
  }
  
}
