/*
 *  BackupLogic LLC CONFIDENTIAL
 *  DO NOT COPY
 *
 * Copyright (c) [2012] - [2019] BackupLogic LLC <info@citymsp.nyc>
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 *  the property of BackupLogic LLC and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to BackupLogic LLC and its suppliers and
 *  may be covered by U.S. and Foreign Patents, patents in process,
 *  and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from BackupLogic LLC
 */
package com.fastsitesoft.backuptool.jobs;

import com.fastsitesoft.backuptool.config.entities.BackupConfig;
import com.fastsitesoft.backuptool.constants.BackupConstants;
import com.fastsitesoft.backuptool.enums.BackupToolResultStatus;
import com.fastsitesoft.backuptool.enums.FSSBackupType;
import com.fastsitesoft.backuptool.utils.BackupToolException;
import com.fastsitesoft.backuptool.utils.BackupToolResult;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * Backup class for storing the job description and all other options.
 * 
 * Backup jobs are responsible for creating backups.
 * 
 * @author Kervin Pierre <info@sludev.com>
 */
public class BackupJob implements IBackupJob
{
    private static final Logger log 
                                = LogManager.getLogger(BackupJob.class);

    private final BackupConfig backupConfig;

    @Override
    public BackupConfig getBackupConfig()
    {
        return backupConfig;
    }
    
    public BackupJob(BackupConfig bc)
    {
        backupConfig = bc;
    }
    
    /**
     * Ran at least once before the first backup.  Sets up secondary objects from
     * the configuration objects.
     * 
     * @param bc
     * @return 
     */
    public BackupToolResult preBackup(BackupConfig bc)
    {
        BackupToolResult res = null;
        
        switch( bc.getChecksumType() )
        {
            case MD5:
                break;
                
            case SHA1:
               // fileDigest = new FSSDigestSHA1();
                break;
            
            case SHA2:
                break;
                
            default:
                break;
        }

        if( res == null )
        {
            res = new BackupToolResult(BackupToolResultStatus.SUCCESS);
        }

        return res;
    }

    /**
     * Ran after successful backup jobs.
     * 
     * @param bc
     * @return 
     */
    public BackupToolResult postBackup(BackupConfig bc)
    {
        BackupToolResult res = new BackupToolResult(BackupToolResultStatus.NONE);
        
        return res;
    }
        
    @Override
    public BackupToolResult call() throws Exception
    {
        log.debug("New BackupJob call() started");

        BackupToolResult pr;
        IBackupJob backupJob;
        
        pr = validate(getBackupConfig());

        FSSBackupType currBackupType = getBackupConfig().getSetType();
        if( currBackupType == null )
        {
            currBackupType = BackupConstants.DEFAULT_BACKUP_TYPE;
        }

        switch( currBackupType )
        {
            case FULL:
                backupJob = new BackupFull(getBackupConfig());
                break;

            default:
                throw new BackupToolException(
                        String.format("Invalid Backup Type '%s'",
                                getBackupConfig().getSetType()));
        }

        // Start the main backup thread.
        // This current thread will monitor and manage the backup thread in time.
        FutureTask<BackupToolResult> mainTask = new FutureTask<>(backupJob);

        BasicThreadFactory mainThreadFactory = new BasicThreadFactory.Builder()
                .namingPattern("backup-job-thread-%d")
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

        return pr;
    }
    
    /**
     * Check that a configuration file can be run.  I.e. the backup job as all it needs.
     * 
     * @param bc
     * @return 
     * @throws com.fastsitesoft.backuptool.utils.BackupToolException 
     */
    public static BackupToolResult validate(BackupConfig bc) throws BackupToolException
    {
        BackupToolResult pr = BackupConfig.validate(bc);
        
        return pr;
    }

}
