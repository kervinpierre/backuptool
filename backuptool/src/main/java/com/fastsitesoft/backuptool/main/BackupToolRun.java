/*
 *   SLU Dev Inc. CONFIDENTIAL
 *   DO NOT COPY
 *  
 *  Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 *  All Rights Reserved.
 *  
 *  NOTICE:  All information contained herein is, and remains
 *   the property of SLU Dev Inc. and its suppliers,
 *   if any.  The intellectual and technical concepts contained
 *   herein are proprietary to SLU Dev Inc. and its suppliers and
 *   may be covered by U.S. and Foreign Patents, patents in process,
 *   and are protected by trade secret or copyright law.
 *   Dissemination of this information or reproduction of this material
 *   is strictly forbidden unless prior written permission is obtained
 *   from SLU Dev Inc.
 */
package com.fastsitesoft.backuptool.main;

import com.fastsitesoft.backuptool.jobs.BackupJob;
import com.fastsitesoft.backuptool.jobs.IBackupJob;
import com.fastsitesoft.backuptool.jobs.RestoreJob;
import com.fastsitesoft.backuptool.config.entities.BackupConfig;
import com.fastsitesoft.backuptool.enums.BackupToolResultStatus;
import com.fastsitesoft.backuptool.utils.BackupToolException;
import com.fastsitesoft.backuptool.utils.BackupToolLockFile;
import com.fastsitesoft.backuptool.utils.BackupToolResult;
import com.fastsitesoft.backuptool.utils.BackupToolUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Kervin
 */
public final class BackupToolRun implements Callable<BackupToolResult>
{
    private static final Logger log 
                             = LogManager.getLogger(BackupToolRun.class);
    
    private final BackupConfig config;
    private final Path lockFile;
    
    public Path getLockFile()
    {
        return lockFile;
    }

    public BackupConfig getConfig()
    {
        return config;
    }

    public BackupToolRun(final BackupConfig c)
    {
        this(c, c.getLockFilePath());
    }

    public BackupToolRun(final BackupConfig c, final Path lk)
    {
        config = c;
        lockFile = lk;
    }

    @Override
    public BackupToolResult call() throws Exception
    {
        log.debug("New BackupToolRun call() started");

        BackupToolResult res = null;
        
        if( config.isDisplayVersion() )
        {
            BackupToolUtil.displayVersion();
            res = new BackupToolResult(BackupToolResultStatus.SUCCESS);
            
            return res;
        }
        
        if( config.isDisplayUsage() )
        {
            BackupToolUtil.displayUsage(config.getUsageConfig());
            res = new BackupToolResult(BackupToolResultStatus.SUCCESS);
            
            return res;
        }

        if( BooleanUtils.isNotTrue(config.isIgnoreLock())
                && config.getLockFilePath() != null )
        {
            // Setup the acquiring and release of the lock file
            acquireLockFile(getLockFile());
            setupLockFileShutdownHook(getLockFile());
        }

        IBackupJob mainJob = null;
        
        if( config.getBackup() )
        {
            // We're doing a backup.
            BackupJob bj = new BackupJob(config);
            
            mainJob = bj;
        }
        else if( config.getRestore() )
        {
            // We're doing a restore.
            RestoreJob rj = new RestoreJob(config);
            
            mainJob = rj;
        }
        
        if( mainJob == null )
        {
            // We should have been given something to do
            throw new BackupToolException("Missing program options. Neither backup or restore options were specified.");
        }
        
        // Start the relevant threads
        FutureTask<BackupToolResult> mainTask = new FutureTask<>(mainJob);
        
        BasicThreadFactory mainThreadFactory = new BasicThreadFactory.Builder()
            .namingPattern("job-monitor-thread-%d")
            .build();
        
        ExecutorService mainExe = Executors.newSingleThreadExecutor(mainThreadFactory);
        
        Future mainExeRes = mainExe.submit(mainTask);
        
        mainExe.shutdown();
        
        BackupToolResult mainRes = null;
        
        try
        {
            while( mainRes == null )
            {
                if( mainExeRes.isDone() )
                {
                    mainRes = mainTask.get();
                }
                
                // At this point we can block/wait on all threads but I'll
                // sleep for now until there's some processing to be done on
                // the main thread.
                Thread.sleep(2000);
            }
        }
        catch (InterruptedException ex)
        {
            log.error("Application 'run' thread was interrupted", ex);
            
            // We don't have to do much here because the interrupt got us out
            // of the while loop.
            
        }
        catch (ExecutionException ex)
        {
            log.error("Application 'run' execution error", ex);
        }
        finally
        {
            mainExe.shutdownNow();
        }

        if( res == null )
        {
            res = new BackupToolResult(BackupToolResultStatus.SUCCESS);
        }

        return res;
    }

    private static void acquireLockFile(final Path lk) throws BackupToolException
    {
        if( lk != null )
        {
            if( Files.exists(lk) )
            {
                int runningPID = 0;
                
                try
                {
                    runningPID = BackupToolLockFile.getLockPID(lk);
                }
                catch (Exception ex)
                {
                    throw new BackupToolException(String.format(
                            "Exception reading lock file '%s'.  "
                          + "It may require further access permissions, or maybe corrupt.", lk), ex);
                }
                
                throw new BackupToolException(String.format(
                            "Process '%d' already has the lock file '%s'",
                                            runningPID, lk));
            }
            else
            {
                if( !BackupToolLockFile.acquireLockFile(lk) )
                {
                    String errMsg = String.format(
                            "Error acquiring the lock file '%s'", lk);
                    throw new BackupToolException(errMsg);
                }
            }
        }
    }
    
    private static void setupLockFileShutdownHook(final Path lk)
    {
        Runtime.getRuntime().addShutdownHook(new Thread() 
        {
            @Override
            public void run() 
            {
                // Release the locks if necessary
                if( lk != null )
                {
                    if( Files.exists(lk) )
                    {
                        if( !BackupToolLockFile.releaseLockFile(lk) )
                        {
                            String errMsg = String.format(
                                    "Error releasing the lock file '%s'", lk);
                            
                            log.error(errMsg);
                        }
                    }
                    else
                    {
                        log.error(String.format(
                                    "Expected lock file '%s' to exist.", lk));
                    }
                }
            }
        }); 
    }
}
