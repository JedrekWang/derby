/*
 
   Derby - Class
   org.apache.derby.impl.services.replication.master.MasterController
 
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to you under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 
*/

package org.apache.derby.impl.services.replication.master;

import org.apache.derby.iapi.error.StandardException;
import org.apache.derby.iapi.reference.SQLState;
import org.apache.derby.iapi.services.monitor.ModuleControl;
import org.apache.derby.iapi.services.monitor.ModuleSupportable;

import org.apache.derby.iapi.store.raw.RawStoreFactory;
import org.apache.derby.iapi.store.raw.log.LogFactory;
import org.apache.derby.iapi.store.raw.data.DataFactory;

import org.apache.derby.iapi.services.replication.master.MasterFactory;
import org.apache.derby.impl.services.replication.buffer.ReplicationLogBuffer;
import org.apache.derby.impl.services.replication.buffer.LogBufferFullException;

import java.util.Properties;

/**
 * <p> 
 * This is an implementation of the replication master controller
 * service. The service is booted when this instance of Derby will
 * have the replication master role for this database.
 * </p> 
 * <p>
 * Note: The current version of the class is far from complete. Code
 * to control the replication master behavior will be added as more
 * parts of the replication functionality is added to Derby. 
 * </p>
 *
 * @see MasterFactory
 */
public class MasterController implements MasterFactory, ModuleControl,
                                         ModuleSupportable {

    private static final int DEFAULT_LOG_BUFFER_SIZE = 32768; //32K

    private RawStoreFactory rawStoreFactory;
    private DataFactory dataFactory;
    private LogFactory logFactory;
    private ReplicationLogBuffer logBuffer;
    // waiting for code to go into trunk:
    //    private LogShipper logShipper; 
    //    private NetworkTransmit connection; 

    private String replicationMode;
    private String slavehost;
    private int slaveport;



    /**
     * Empty constructor required by Monitor.bootServiceModule
     */
    public MasterController() { }

    ////////////////////////////////////////////////////////////
    // Implementation of methods from interface ModuleControl //
    ////////////////////////////////////////////////////////////

    /**
     * Used by Monitor.bootServiceModule to start the service. Will:
     *
     * Set up basic variables
     * Connect to the slave using the network service (DERBY-2921)
     *
     * Not implemented yet
     *
     * @param create Currently ignored
     * @param properties Properties used to start the service in the
     * correct mode
     * @exception StandardException Standard Derby exception policy,
     * thrown on error.
     */
    public void boot(boolean create, Properties properties)
        throws StandardException {

        replicationMode =
            properties.getProperty(MasterFactory.REPLICATION_MODE);

        slavehost = properties.getProperty(MasterFactory.SLAVE_HOST);

        String port = properties.getProperty(MasterFactory.SLAVE_PORT);
        if (port != null) {
            slaveport = new Integer(port).intValue();
        }

        // Added when Network Service has been committed to trunk
        // connection = new NetworkTransmit();

        System.out.println("MasterController booted");
    }

    /**
     * Will stop the replication master service
     *
     * Not implemented yet
     */
    public void stop() { }

    ////////////////////////////////////////////////////////////////
    // Implementation of methods from interface ModuleSupportable //
    ////////////////////////////////////////////////////////////////

    /**
     * Used by Monitor.bootServiceModule to check if this class is
     * usable for replication. To be usable, we require that
     * asynchronous replication is specified in startParams by
     * checking that a property with key
     * MasterFactory.REPLICATION_MODE has the value
     * MasterFactory.ASYNCHRONOUS_MODE. 
     * @param startParams The properties used to boot replication
     * @return true if asynchronous replication is requested, meaning
     * that this MasterController is a suitable implementation for the
     * MasterFactory service. False otherwise
     * @see ModuleSupportable#canSupport 
     */
    public boolean canSupport(Properties startParams) {
        String modeParam =
            startParams.getProperty(MasterFactory.REPLICATION_MODE);

        // currently only one attribute: asynchronous replication mode
        if (modeParam != null && 
            modeParam.equals(MasterFactory.ASYNCHRONOUS_MODE)) {
            return true;
        } else {
            return false;
        }
    }

    ////////////////////////////////////////////////////////////
    // Implementation of methods from interface MasterFactory //
    ////////////////////////////////////////////////////////////

    /**
     * Will perform all the work that is needed to set up replication
     *
     * Not implemented yet
     *
     * @param rawStore The RawStoreFactory for the database
     * @param dataFac The DataFactory for this database
     * @param logFac The LogFactory ensuring recoverability for this database
     * @exception StandardException Standard Derby exception policy,
     * thrown on replication startup error. 
     */
    public void startMaster(RawStoreFactory rawStore,
                            DataFactory dataFac, LogFactory logFac)
        throws StandardException{
        // Added when Network Service has been committed to trunk:
        // connection.connect(); // sets up a network connection to the slave

        rawStoreFactory = rawStore;
        dataFactory = dataFac;
        logFactory = logFac;
        logBuffer = new ReplicationLogBuffer(DEFAULT_LOG_BUFFER_SIZE);
        //  logFactory.setReplicationMaster(this); //added later

        logFactory.startReplicationMasterRole(this);

        if (replicationMode.equals(MasterFactory.ASYNCHRONOUS_MODE)) {
            System.out.println("MasterController would now " +
                               "start asynchronous log shipping");
            // Added when Master Log Shipping code has been committed to trunk:
            // logShipper = new AsynchronousLogShipper(connection);
        }

        // Add code that initializes replication by sending the
        // database to the slave, making logFactory add logrecords to
        // the buffer etc. Repliation should be up and running when
        // this method returns.

        System.out.println("MasterController started");
    }

    /**
     * Will perform all work that is needed to shut down replication
     *
     * Not implemented yet
     */
    public void stopMaster() {
        // logFactory.stopReplicationLogging(); // added later

        // Added when Network Service has been committed to trunk:
        // if (connection.isUp()) {
        //     logShipper.flushAllLog();
        // }

        // logBuffer.stop();
        System.out.println("MasterController stopped");
    }

    /**
     * Append a single log record to the replication log buffer.
     *
     * @param dataLength            number of bytes in data[]
     * @param instant               the log address of this log record.
     * @param data                  "from" array to copy "data" portion of rec
     * @param dataOffset            offset in data[] to start copying from.
     * @param optionalData          "from" array to copy "optional data" from
     * @param optionalDataOffset    offset in optionalData[] to start copy from
     * @param optionalDataLength    number of bytes in optionalData[]
     *
     **/
    public void appendLogRecord(int dataLength,
                                long instant,
                                byte[] data,
                                int dataOffset,
                                byte[] optionalData, 
                                int optionalDataOffset,
                                int optionalDataLength) {
        try {
            logBuffer.appendLogRecord(instant, dataLength, dataOffset,
                                      optionalDataLength, optionalDataOffset,
                                      data, optionalData);
        } catch (LogBufferFullException lbfe) {
            // Waiting for log shipper to implement this
            // We have multiple alternatives: 
            //  1) Try to force-send some log to the slave:
            //     logShipper.forceFlush()
            //  2) Increase the size of the buffer
            // Stop replication if both these are unsuccessful or not
            // an alternative. 
        }
    }

    /**
     * Used by the LogFactory to notify the replication master
     * controller that the log records up to this instant have been
     * flushed to disk. The master controller takes action according
     * to the current replication strategy when this method is called.
     *
     * When the asynchronous replication strategy is used, the method
     * does not force log shipping to the slave; the log records may
     * be shipped now or later at the MasterController's discretion.
     *
     * However, if another strategy like 2-safe replication is
     * implemented in the future, a call to this method may force log
     * shipment before returning control to the caller.
     *
     * Currently, only asynchronous replication is supported.
     *
     * Not implemented yet
     *
     * @param instant The highest log instant that has been flushed to
     * disk
     *
     * @see MasterFactory#flushedTo
     * @see LogFactory#flush
     */
    public void flushedTo(long instant) {
        // logShipper.flushedTo(instant); 
    }
    
    /**
     * Used by the log shipper to inform the master controller about the 
     * exception condition that caused it to terminate unexpectedly.
     *
     * @param exception the exception which caused the log shipper to terminate
     *                  in an unexcepted manner.
     */
    void handleExceptions(Exception exception) {
    }
}
