/*
 *  SLU Dev Inc. CONFIDENTIAL
 *  DO NOT COPY
 * 
 *  Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 *  All Rights Reserved.
 * 
 *  NOTICE:  All information contained herein is, and remains
 *  the property of SLU Dev Inc. and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to SLU Dev Inc. and its suppliers and
 *  may be covered by U.S. and Foreign Patents, patents in process,
 *  and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from SLU Dev Inc.
 */
package com.fastsitesoft.backuptool.jobs;

import com.fastsitesoft.backuptool.config.entities.BackupConfig;
import com.fastsitesoft.backuptool.enums.BackupToolResultStatus;
import com.fastsitesoft.backuptool.utils.BackupToolResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Restore class for storing the job description and all other options.
 * 
 * Restore jobs are responsible for restoring backups to the file-system.
 * 
 * @author Kervin Pierre <info@sludev.com>
 */
public class RestoreJob implements IBackupJob
{
    private static final Logger log 
                                = LogManager.getLogger(RestoreJob.class);

    private final BackupConfig backupConfig;

    @Override
    public BackupConfig getBackupConfig()
    {
        return backupConfig;
    }

    public RestoreJob(BackupConfig bc)
    {
        backupConfig = bc;
    }
    
    @Override
    public BackupToolResult call() throws Exception
    {
        BackupToolResult pr = new BackupToolResult(BackupToolResultStatus.NONE);
        
        return pr;
    }
}
