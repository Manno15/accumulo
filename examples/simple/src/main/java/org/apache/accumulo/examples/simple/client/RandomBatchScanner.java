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
package org.apache.accumulo.examples.simple.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.accumulo.core.cli.ClientOnRequiredTable;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchScanner;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.hadoop.io.Text;
import org.apache.log4j.Logger;

import com.beust.jcommander.Parameter;

/**
 * Internal class used to verify validity of data read.
 */
class CountingVerifyingReceiver {
  private static final Logger log = Logger.getLogger(CountingVerifyingReceiver.class);
  
  long count = 0;
  int expectedValueSize = 0;
  HashMap<Text,Boolean> expectedRows;
  
  CountingVerifyingReceiver(HashMap<Text,Boolean> expectedRows, int expectedValueSize) {
    this.expectedRows = expectedRows;
    this.expectedValueSize = expectedValueSize;
  }
  
  public void receive(Key key, Value value) {
    
    String row = key.getRow().toString();
    long rowid = Integer.parseInt(row.split("_")[1]);
    
    byte expectedValue[] = RandomBatchWriter.createValue(rowid, expectedValueSize);
    
    if (!Arrays.equals(expectedValue, value.get())) {
      log.error("Got unexpected value for " + key + " expected : " + new String(expectedValue) + " got : " + new String(value.get()));
    }
    
    if (!expectedRows.containsKey(key.getRow())) {
      log.error("Got unexpected key " + key);
    } else {
      expectedRows.put(key.getRow(), true);
    }
    
    count++;
  }
}

/**
 * Simple example for reading random batches of data from Accumulo. See docs/examples/README.batch for instructions.
 */
public class RandomBatchScanner {
  private static final Logger log = Logger.getLogger(CountingVerifyingReceiver.class);
  
  /**
   * Generate a number of ranges, each covering a single random row.
   * 
   * @param num
   *          the number of ranges to generate
   * @param min
   *          the minimum row that will be generated
   * @param max
   *          the maximum row that will be generated
   * @param r
   *          a random number generator
   * @param ranges
   *          a set in which to store the generated ranges
   * @param expectedRows
   *          a map in which to store the rows covered by the ranges (initially mapped to false)
   */
  static void generateRandomQueries(int num, long min, long max, Random r, HashSet<Range> ranges, HashMap<Text,Boolean> expectedRows) {
    log.info(String.format("Generating %,d random queries...", num));
    while (ranges.size() < num) {
      long rowid = (Math.abs(r.nextLong()) % (max - min)) + min;
      
      Text row1 = new Text(String.format("row_%010d", rowid));
      
      Range range = new Range(new Text(row1));
      ranges.add(range);
      expectedRows.put(row1, false);
    }
    
    log.info("finished");
  }
  
  /**
   * Prints a count of the number of rows mapped to false.
   * 
   * @param expectedRows
   */
  private static void printRowsNotFound(HashMap<Text,Boolean> expectedRows) {
    int count = 0;
    for (Entry<Text,Boolean> entry : expectedRows.entrySet())
      if (!entry.getValue())
        count++;
    
    if (count > 0)
      log.warn("Did not find " + count + " rows");
  }
  
  /**
   * Generates a number of random queries, verifies that the key/value pairs returned were in the queried ranges and that the values were generated by
   * {@link RandomBatchWriter#createValue(long, int)}. Prints information about the results.
   * 
   * @param num
   *          the number of queries to generate
   * @param min
   *          the min row to query
   * @param max
   *          the max row to query
   * @param evs
   *          the expected size of the values
   * @param r
   *          a random number generator
   * @param tsbr
   *          a batch scanner
   */
  static void doRandomQueries(int num, long min, long max, int evs, Random r, BatchScanner tsbr) {
    
    HashSet<Range> ranges = new HashSet<Range>(num);
    HashMap<Text,Boolean> expectedRows = new java.util.HashMap<Text,Boolean>();
    
    generateRandomQueries(num, min, max, r, ranges, expectedRows);
    
    tsbr.setRanges(ranges);
    
    CountingVerifyingReceiver receiver = new CountingVerifyingReceiver(expectedRows, evs);
    
    long t1 = System.currentTimeMillis();
    
    for (Entry<Key,Value> entry : tsbr) {
      receiver.receive(entry.getKey(), entry.getValue());
    }
    
    long t2 = System.currentTimeMillis();
    
    log.info(String.format("%6.2f lookups/sec %6.2f secs%n", num / ((t2 - t1) / 1000.0), ((t2 - t1) / 1000.0)));
    log.info(String.format("num results : %,d%n", receiver.count));
    
    printRowsNotFound(expectedRows);
  }
  
  public static class Opts  extends ClientOnRequiredTable {
    @Parameter(names="--min", description="miniumum row that will be generated")
    long min = 0;
    @Parameter(names="--max", description="maximum ow that will be generated")
    long max = 0;
    @Parameter(names="--num", required=true, description="number of ranges to generate")
    int num = 0;
    @Parameter(names="--size", required=true, description="size of the value to write")
    int size = 0;
    @Parameter(names="--seed", description="seed for pseudo-random number generator")
    Long seed = null;
  }
  
  /**
   * Scans over a specified number of entries to Accumulo using a {@link BatchScanner}. Completes scans twice to compare times for a fresh query with those for
   * a repeated query which has cached metadata and connections already established.
   * 
   * @param args
   * @throws AccumuloException
   * @throws AccumuloSecurityException
   * @throws TableNotFoundException
   */
  public static void main(String[] args) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
    Opts opts = new Opts();
    opts.parseArgs(RandomBatchScanner.class.getName(), args);
    
    Connector connector = opts.getConnector();
    BatchScanner tsbr = connector.createBatchScanner(opts.tableName, opts.auths, opts.scanThreads);
    
    Random r;
    if (opts.seed == null)
      r = new Random();
    else
      r = new Random(opts.seed);
    
    // do one cold
    doRandomQueries(opts.num, opts.min, opts.max, opts.size, r, tsbr);
    
    System.gc();
    System.gc();
    System.gc();
    
    if (opts.seed == null)
      r = new Random();
    else
      r = new Random(opts.seed);
    
    // do one hot (connections already established, metadata table cached)
    doRandomQueries(opts.num, opts.min, opts.max, opts.size, r, tsbr);
    
    tsbr.close();
  }
}
