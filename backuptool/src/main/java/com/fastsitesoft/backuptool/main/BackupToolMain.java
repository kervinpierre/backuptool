/*
 *  CityMSP LLC CONFIDENTIAL
 *  DO NOT COPY
 *
 * Copyright (c) [2012] - [2019] CityMSP LLC <info@citymsp.nyc>
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 *  the property of CityMSP LLC and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to CityMSP LLC and its suppliers and
 *  may be covered by U.S. and Foreign Patents, patents in process,
 *  and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from CityMSP LLC
 */
package com.fastsitesoft.backuptool.main;

import com.fastsitesoft.backuptool.config.entities.BackupConfig;
import com.fastsitesoft.backuptool.enums.BackupToolResultStatus;
import com.fastsitesoft.backuptool.utils.BackupToolException;
import com.fastsitesoft.backuptool.utils.BackupToolResult;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Fastsite BackupToolMain - Backup to and from cloud networks.
 *
 */
public class BackupToolMain 
{
    private static final Logger log 
                             = LogManager.getLogger(BackupToolMain.class);
    
    private static String[] staticArgs = null;
    private static ExecutorService mainThreadExe = null;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {  
        log.debug("Starting Backup Tool via its Command Line Interface.");

        commonStart(args);
    }
    
    
    /**
     * A common static method for all interfaces to us.  This interface is called
     * currently from the...
     * 1. Command Line Interface - main()
     * 2. Unix Service Interface - JServ
     * 3. Windows Service Interface - ProcRun
     * 
     * @param args 
     */
    public static void commonStart(String[] args)
    {
        log.debug( String.format("BackupToolMain::commonStart() called [%s]",
                Arrays.toString(args)) );
        
        // Initialize only.  Don't actually run or do anything else
        BackupConfig res = BackupToolInitialize.initialize(args);
        BackupToolResult procRes;
        
        try
        {
            procRes = processStart(res); 
        }
        catch (BackupToolException ex)
        {
            log.error("Error running application.", ex);
        }   
    }
    
    public static void commonStop(String[] args)
    {
        log.debug( String.format("BackupToolMain::commonStop() called [%s]",
                Arrays.toString(args)) );
        
        // Initialize only.  Don't actually run or do anything else
        BackupConfig res = BackupToolInitialize.initialize(args);
        BackupToolResult procRes;

        procRes = processStop(res); 
  
    }
    
    public void destroy()
    {
        log.info("Service destroy called.");
        
        commonStop(BackupToolMain.staticArgs);
    }

    public void init(String[] args) 
    {
        log.info( String.format("Service init called.\n[[%s]]\n", 
                Arrays.toString(args)) );
        
        BackupToolMain.staticArgs = args;
    }

    /**
     * Unix/Linux JSvc start method
     * 
     */
    public void start()
    {
        log.info("Starting BackupTool via its Unix Service Interface.");
        
        commonStart(BackupToolMain.staticArgs);
    }

    /**
     * Service stop method.
     * 
     */
    public void stop()
    {
        log.info("Service stop called...");
        
        ;
    }
    
    /**
     * Stop the ProcRun Windows Service.
     * 
     * @param args 
     */
    public static void windowsStop(String args[])
    {
        log.info("Windows service stop called...");

        commonStop(args);
    }

    /**
     * Start the ProcRun Windows Service.  This method must be kept running.
     * The stop method may also be run from a separate thread.
     * 
     * An example of the ProcRun install command which must be run prior...
     * 
     * C:\commons-daemon\amd64\prunsrv.exe  //IS/LOGCHECK 
     *   --Classpath=c:\src\fastsitesoft.com\svn\src\target\logcheck-0.9.jar 
     *   --StartMode=jvm --StartClass=com.sludev.logs.logcheck.main.BackupToolMain 
     *   --StartMethod=windowsStart --StopMethod=windowsStop
     *   --JavaHome="C:\Program Files\Java\jdk1.7.0_51" 
     *   --Jvm="C:\Program Files\Java\jdk1.7.0_51\jre\bin\server\jvm.dll" 
     *   --StdOutput=auto --StdError=auto
     * 
     * Then the service can be run using...
     * C:\commons-daemon\amd64\prunsrv.exe  //RS/LOGCHECK
     * or...
     * C:\commons-daemon\amd64\prunsrv.exe  //TS/LOGCHECK
     * 
     * or deleted...
     * C:\commons-daemon\amd64\prunsrv.exe  //DS/LOGCHECK
     * 
     * @param args 
     */
    public static void windowsStart(String args[])
    {
        log.info("Starting BackupTool via its Windows Service Interface.");
        
        commonStart(args);
    }
    
    /**
     * Start a process thread for doing the actual work.
     * 
     * @param config 
     * @return  
     * @throws BackupToolException  
     */
    public static BackupToolResult processStart(BackupConfig config) throws BackupToolException
    {
        BackupToolResult resp = null;
        BackupToolRun currRun = new BackupToolRun(config);
        FutureTask<BackupToolResult> currRunTask = new FutureTask<>(currRun);

        BasicThreadFactory thFactory = new BasicThreadFactory.Builder()
            .namingPattern("main-run-thread-%d")
            .build();

        mainThreadExe = Executors.newSingleThreadExecutor(thFactory);
        Future exeRes = mainThreadExe.submit(currRunTask);

        mainThreadExe.shutdown();

        try
        {
            resp = currRunTask.get();
        }
        catch (InterruptedException ex)
        {
            String errMsg = "Application 'main' thread was interrupted";
            log.debug(errMsg, ex);
            throw new BackupToolException(errMsg, ex);
        }
        catch (ExecutionException ex)
        {
            String errMsg = "Application 'main' thread execution error";
            log.debug(errMsg, ex);
            throw new BackupToolException(errMsg, ex);
        }
        finally
        {
            // If main leaves for any reason, shutdown all threads
            mainThreadExe.shutdownNow();
        }
        
        return resp;
    }
    
    /**
     * Stop the currently running main thread process of the service and related
     * threads.
     * 
     * @param config
     * @return
     */
    public static BackupToolResult processStop(BackupConfig config)
    {
        BackupToolResult resp = new BackupToolResult(BackupToolResultStatus.NONE);
        
        if( mainThreadExe != null )
        {
            // Shutdown the main thread if we have one.
            // This will only work in ProcRun/JSvc JVM hosted mode.
            // IPC would be needed otherwise
            mainThreadExe.shutdownNow();
        }
        
        return resp;
    }
}
