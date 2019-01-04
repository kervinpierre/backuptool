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

import com.fastsitesoft.backuptool.backend.IBackendFileSystem;
import com.fastsitesoft.backuptool.backend.StorageBackendFileSystem;
import com.fastsitesoft.backuptool.config.entities.BackupConfig;
import com.fastsitesoft.backuptool.config.entities.BackupConfigChunk;
import com.fastsitesoft.backuptool.config.entities.BackupConfigDirectory;
import com.fastsitesoft.backuptool.config.entities.BackupConfigFile;
import com.fastsitesoft.backuptool.config.entities.BackupConfigStorageBackend;
import com.fastsitesoft.backuptool.config.entities.JobArchive;
import com.fastsitesoft.backuptool.config.entities.JobSetState;
import com.fastsitesoft.backuptool.config.entities.JobState;
import com.fastsitesoft.backuptool.config.entities.writers.JobSetStateWriter;
import com.fastsitesoft.backuptool.config.parsers.JobSetStateParser;
import com.fastsitesoft.backuptool.config.parsers.JobStateParser;
import com.fastsitesoft.backuptool.config.parsers.ParserUtil;
import com.fastsitesoft.backuptool.enums.BackupToolFileFormats;
import com.fastsitesoft.backuptool.enums.BackupToolJobDisposition;
import com.fastsitesoft.backuptool.enums.BackupToolJobStatus;
import com.fastsitesoft.backuptool.enums.BackupToolResultStatus;
import com.fastsitesoft.backuptool.enums.FSSBackupHashType;
import com.fastsitesoft.backuptool.enums.FSSBackupType;
import com.fastsitesoft.backuptool.file.IBackupFilePart;
import com.fastsitesoft.backuptool.file.local.BackupConfigItemResolver;
import com.fastsitesoft.backuptool.file.local.BackupItem;
import com.fastsitesoft.backuptool.utils.BackupToolException;
import com.fastsitesoft.backuptool.utils.BackupToolResult;
import com.fastsitesoft.backuptool.utils.BackupToolSystemID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * An incremental or differential backup job.  Only files changed since the last
 * backup are updated.
 * 
 * @author Kervin Pierre <info@sludev.com>
 */
public final class BackupDiff implements IBackupJob
{
    private static final Logger log 
                                = LogManager.getLogger(BackupDiff.class);

    private final BackupConfig backupConfig;

    @Override
    public BackupConfig getBackupConfig()
    {
        return backupConfig;
    }

    public BackupDiff(BackupConfig bc)
    {
        backupConfig = bc;
    }

    /**
     * Run the actual differential backup job tasks.
     * 
     * @param bc The full backup configuration read from disk and program arguments.
     * @return The result from running the backup
     * @throws BackupToolException
     */
    public BackupToolResult doBackup(BackupConfig bc) throws BackupToolException
    {
        BackupToolResult res = null;
        Map<Path, Deque<BackupItem>>  fullFullList = null;
        BackupConfigChunk currChunk = bc.getChunk();

        List<BackupConfigDirectory> currDirList = bc.getDirList();
        List<BackupConfigFile> currFileList = bc.getFileList();

        // Create the initial JOB state
        JobState currJob = JobState.from(null,
                UUID.randomUUID(),
                null,
                Instant.now(),
                null,
                FSSBackupType.INCREMENTAL,
                BackupToolSystemID.getType1(),
                BackupToolJobStatus.STARTED,
                BackupToolJobDisposition.NONE,
                null,
                null, // error path
                null,
                FSSBackupHashType.NONE);

        BackupToolResult resolverRes = null;

        // FIXME : Create the job folder using 'job name' regex
        BackupConfigStorageBackend bcsb = getBackupConfig().getStorageBackend();

        // Get a backend file system object
        IBackendFileSystem bfs = StorageBackendFileSystem.from(bcsb.getUser(), bcsb.getPass(), bcsb.getUrl());
        bfs.init();

        // Test or create the JOBSET folder
        JobSetState currJobSetState;
        IBackupFilePart jobSetDir
                = bfs.resolveRemoteFile(Paths.get(bcsb.getUrl().getPath()));
        if( bfs.exists(jobSetDir) )
        {
            IBackupFilePart currJobSetStateFile = bfs.resolveRemoteFile(
                    jobSetDir.getPath().resolve(getBackupConfig().getStateFileName().toString()));

            if( bfs.exists(currJobSetStateFile) )
            {
                IBackupFilePart tempFile;

                try
                {
                    tempFile = bfs.resolveLocalFile( Files.createTempFile("JobSetState", ".tmp") );
                }
                catch( IOException ex )
                {
                    String errMsg = "Error creating temp file for Job Set State.";
                    log.debug(errMsg, ex);

                    throw new BackupToolException(errMsg, ex);
                }

                bfs.copy(currJobSetStateFile, tempFile);
                currJobSetState = JobSetStateParser.readConfig(
                        ParserUtil.readConfig(tempFile.getPath(), BackupToolFileFormats.JOBSETSTATE));
            }
            else
            {
                // We have a Job Set folder with no State File
                String errMsg = String.format("Job Set '%s' has no state file. Defined as '%s'", jobSetDir.getUri(),
                        currJobSetStateFile.getUri());

                log.debug( errMsg );

                throw new BackupToolException(errMsg);
            }
        }
        else
        {
            // Create remote folder structure.
            bfs.createFolder(jobSetDir);

            // Create a Job Set State File
            currJobSetState = JobSetState.from( UUID.randomUUID(), // Job Set Id
                    null, // Last Job ID
                    null, // Last Successful Job ID
                    jobSetDir.getPath() // Path
                    );

            IBackupFilePart currJobSetStateFile = bfs.resolveRemoteFile(
                    jobSetDir.getPath().resolve(getBackupConfig().getStateFileName().toString()));

            Path temp = JobSetStateWriter.write(currJobSetState);
            IBackupFilePart tempStateFile = bfs.resolveLocalFile(temp);
            bfs.copy(tempStateFile, currJobSetStateFile);
        }

        // Get the next job folder name
        Path currJobTemplate = bfs.nextTemplate(jobSetDir,
                                        getBackupConfig().getJobFileNameTemplate(),
                                        null,
                                        false);

        Path currJobName = IBackendFileSystem.nextName(currJobTemplate,
                getBackupConfig().getJobFileNamePattern(),
                getBackupConfig().getJobFileNameComponent(),
                false,
                null);

        // Test or create the JOB folder
        JobState currJobState;
        IBackupFilePart currJobDir
                = bfs.resolveRemoteFile(jobSetDir.getPath().resolve(currJobName));
        if( bfs.exists(currJobDir) )
        {
            // The job folder already exists
            // READ JOBSET STATE
            IBackupFilePart currJobStateFile = bfs.resolveRemoteFile(
                    currJobDir.getPath().resolve(getBackupConfig().getStateFileName().toString()));

            // Test and set the JOB state if necessary
            if( bfs.exists(currJobStateFile) )
            {
                // The Job State File exists.  read it.
                IBackupFilePart tempFile;

                try
                {
                    tempFile = bfs.resolveLocalFile(Files.createTempFile("jobState", ".tmp"));
                }
                catch( IOException ex )
                {
                    String errMsg = "Error creating temp file for Job State.";
                    log.debug(errMsg, ex);

                    throw new BackupToolException(errMsg, ex);
                }

                bfs.copy(currJobStateFile, tempFile);
                Document currDoc = ParserUtil.readConfig(tempFile.getPath(), BackupToolFileFormats.JOBSTATE);
                currJobState = JobStateParser.readConfig(currDoc);

            }
            else
            {
                // We have a Job folder with no State File
                String errMsg = String.format("Job '%s' has no state file. Defined as '%s'", jobSetDir.getUri(),
                        currJobStateFile.getUri());

                log.debug( errMsg );

                throw new BackupToolException(errMsg);
            }
        }
        else
        {
            // Create remote folder structure for the Job
            bfs.createFolder(currJobDir);

            currJobState = JobState.from( null, UUID.randomUUID(),
                                            currJobDir.getPath(),
                                            Instant.now(),
                                            null,
                                            getBackupConfig().getSetType(),
                                            BackupToolSystemID.getType1(),
                                            BackupToolJobStatus.NONE,
                                            BackupToolJobDisposition.NONE,
                                            currJobSetState.getJobSetId(),
                                            null,
                                            null,
                                            FSSBackupHashType.NONE );
        }

        IBackupFilePart currJobArchiveDir
                = bfs.resolveRemoteFile(currJobDir.getPath().resolve("archive"));
        if( bfs.exists(currJobArchiveDir) )
        {
            ;
        }
        else
        {
            // Create remote folder structure for the archive files.
            bfs.createFolder(currJobArchiveDir);
        }

        // Get the full list of files from the file-system
        BackupConfigItemResolver bcir = BackupConfigItemResolver.from(
                                            currJob,
                                            currFileList,
                                            currDirList,
                                            null,
                                            false );

        try
        {
            // TODO : Spawn the resolver thread, monitor and keep the user updated
            resolverRes = bcir.call();
            fullFullList = BackupItem.mergeFoundItems(bcir.getFoundFolders(), bcir.getFoundFiles());
        }
        catch( Exception ex )
        {
            throw new BackupToolException("Error resolving files.", ex);
        }

        // TODO : Filter the full list using the previous job's state
        JobState prevJob = null;
        for( JobState j : currJobSetState.getJobs().values() )
        {
            if( currJobSetState.getLastSuccessfulJobId() == j.getJobId() )
            {
                prevJob = j;
            }
        }

        // TODO : Get prevJob file list for comparison

        // One archive per loop iteration
        for( Path currPath : fullFullList.keySet() )
        {
            // Get the next template file name remotely
            Path currJobArchiveTemplate = bfs.nextTemplate( currJobArchiveDir,
                    getBackupConfig().getArchiveFileNameTemplate(),
                    getBackupConfig().getArchiveFileNamePattern(),
                    false);

            // Use the template file name to get the next archive name
            Path currJobArchiveName = IBackendFileSystem.nextName(currJobArchiveTemplate,
                    getBackupConfig().getArchiveFileNamePattern(),
                    getBackupConfig().getArchiveFileNameComponent(),
                    false,
                    null);

            // TODO : Create the listing file, one per archive
            Path currJobArchiveListing;

            // Build the next archive file locally in the holding folder
            Path currTempArchive;
            try
            {
                currTempArchive = Files.createTempFile(getBackupConfig()
                        .getHoldingDirectory(), "archive-", ".btaf.tmp");

                log.debug(String.format("Created temp file for archive '%s'.", currTempArchive));
            }
            catch( Exception ex )
            {
                String errMsg = "Error creating temporary file for archiving.";

                log.debug(errMsg, ex);

                throw new BackupToolException(errMsg, ex);
            }

            Deque<JobArchive> archives = new ArrayDeque<>();
            archives.add(JobArchive.from(null, null, currTempArchive, null, true, null, null,
                    null, null));

/*            BackupToolResult archRes = BackupFileOutput.addBackupItems(fullFullList.get(currPath),
                    LinkOption.NOFOLLOW_LINKS,
                    archives,
                    getBackupConfig().getArchiveFileNamePattern(),
                    null,
                    getBackupConfig().getArchiveFileNameComponent(),
                    null,
                    BackupToolCompressionType.NONE,
                    true);*/

            // Copy the local archive file to the remote backend
            IBackupFilePart remoteArchive
                    = bfs.resolveRemoteFile(currJobArchiveDir.getPath().resolve(currJobArchiveName));

            if( bfs.exists(remoteArchive) )
            {
                // This file should never exist here
                throw new BackupToolException(String.format("Archive file already exists '%s'",
                        remoteArchive.getPath()));
            }

            // Make the final copy to the remote server
            IBackupFilePart localArchive = bfs.resolveLocalFile(currTempArchive);
            bfs.copy(localArchive, remoteArchive);

            try
            {
                Files.delete(currTempArchive);
            }
            catch( IOException ex )
            {
                String errMsg = String.format("Error deleting '%s'", currTempArchive);

                log.debug(errMsg, ex);
                throw new BackupToolException(errMsg);
            }
        }

        // Update and save JOBSET state.
        currJobSetState = JobSetState.from( currJobSetState.getJobSetId(), // Job Set Id
                currJob.getJobId(), // Last Job ID
                currJob.getJobId(), // Last Successful Job ID
                jobSetDir.getPath() // Path
        );

        IBackupFilePart currJobSetStateFile = bfs.resolveRemoteFile(
                jobSetDir.getPath().resolve(getBackupConfig().getStateFileName().toString()));

        Path temp = JobSetStateWriter.write(currJobSetState);
        IBackupFilePart tempStateFile = bfs.resolveLocalFile(temp);
        bfs.copy(tempStateFile, currJobSetStateFile);

        if( res == null )
        {
            res = new BackupToolResult(BackupToolResultStatus.SUCCESS);
        }

        return res;
    }

    @Override
    public BackupToolResult call() throws Exception
    {
        BackupToolResult pr;

        pr = BackupConfig.validate(getBackupConfig());

//      pr = preBackup(getBackupConfig());
        pr = doBackup(getBackupConfig());
//      pr = postBackup(getBackupConfig());

        return pr;
    }
}
