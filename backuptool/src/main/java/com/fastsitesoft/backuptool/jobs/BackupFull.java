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
package com.fastsitesoft.backuptool.jobs;

import com.fastsitesoft.backuptool.BackupFileOutput;
import com.fastsitesoft.backuptool.backend.IBackendFileSystem;
import com.fastsitesoft.backuptool.backend.StorageBackendFileSystem;
import com.fastsitesoft.backuptool.config.entities.BackupConfig;
import com.fastsitesoft.backuptool.config.entities.BackupConfigCompression;
import com.fastsitesoft.backuptool.config.entities.BackupConfigDirectory;
import com.fastsitesoft.backuptool.config.entities.BackupConfigFile;
import com.fastsitesoft.backuptool.config.entities.BackupConfigStorageBackend;
import com.fastsitesoft.backuptool.config.entities.JobArchive;
import com.fastsitesoft.backuptool.config.entities.JobArchiveListingState;
import com.fastsitesoft.backuptool.config.entities.JobArchiveSummary;
import com.fastsitesoft.backuptool.config.entities.JobSetState;
import com.fastsitesoft.backuptool.config.entities.JobState;
import com.fastsitesoft.backuptool.constants.BackupConstants;
import com.fastsitesoft.backuptool.enums.BackupToolCompressionType;
import com.fastsitesoft.backuptool.enums.BackupToolJobDisposition;
import com.fastsitesoft.backuptool.enums.BackupToolJobMemberType;
import com.fastsitesoft.backuptool.enums.BackupToolJobStatus;
import com.fastsitesoft.backuptool.enums.BackupToolNameComponentType;
import com.fastsitesoft.backuptool.enums.BackupToolResultStatus;
import com.fastsitesoft.backuptool.enums.FSSBackupType;
import com.fastsitesoft.backuptool.file.IBackupFilePart;
import com.fastsitesoft.backuptool.file.local.BackupConfigItemResolver;
import com.fastsitesoft.backuptool.file.local.BackupItem;
import com.fastsitesoft.backuptool.utils.BackupToolException;
import com.fastsitesoft.backuptool.utils.BackupToolResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * A full backup job.  All files will be backed up.
 * 
 * @author Kervin Pierre <info@sludev.com>
 */
public final class BackupFull  implements IBackupJob
{
    private static final Logger log 
                                = LogManager.getLogger(BackupFull.class);

    private final BackupConfig backupConfig;

    @Override
    public BackupConfig getBackupConfig()
    {
        return backupConfig;
    }

    public BackupFull(final BackupConfig bc)
    {
        backupConfig = bc;
    }
    
//    @Override
//    public BackupToolResult preBackup(BackupConfig bc)
//    {
//        BackupToolResult res = super.preBackup(bc);
//
//        return res;
//    }
//
//    @Override
//    public BackupToolResult postBackup(BackupConfig bc)
//    {
//        BackupToolResult res = super.postBackup(bc);
//
//        return res;
//    }

    /**
     * Run the actual full backup job tasks.
     * 
     * @param bc The full backup configuration read mergeFoundItems disk and program arguments.
     * @return The result mergeFoundItems running the backup
     * @throws BackupToolException
     */
    public static BackupToolResult doBackup(BackupConfig bc) throws BackupToolException
    {
        BackupToolResult res = null;
        Map<Path, Deque<BackupItem>>  fullFullList;

        List<BackupConfigDirectory> currDirList = bc.getDirList();
        List<BackupConfigFile> currFileList = bc.getFileList();

        BackupToolResult resolverRes = null;

        // FIXME : Create the job directory using 'job name' regex
        BackupConfigStorageBackend bcsb = bc.getStorageBackend();

        // Get a backend file system object
        IBackendFileSystem bfs = StorageBackendFileSystem.from(bcsb.getUser(), bcsb.getPass(), bcsb.getUrl());
        bfs.init();

        Path currJobFileNameTemplate = bc.getJobFileNameTemplate();
        if( currJobFileNameTemplate == null )
        {
            currJobFileNameTemplate = Paths.get(BackupConstants.DEFAULT_JOB_NAME_TEMPLATE);
        }

        Pattern currJobFileNamePattern = bc.getJobFileNamePattern();
        if( currJobFileNamePattern == null )
        {
            currJobFileNamePattern = Pattern.compile(BackupConstants.DEFAULT_JOB_NAME_RULE_PATTERN);
        }

        List<BackupToolNameComponentType> currJobFileNameComp = bc.getJobFileNameComponent();
        if( currJobFileNameComp == null )
        {
            currJobFileNameComp = new ArrayList<>();
        }

        if( currJobFileNameComp.size() < 1 )
        {
            currJobFileNameComp.add(
                            BackupConstants.DEFAULT_JOB_NAME_RULE_COMPONENT);
        }

        Path currStateFileName = bc.getStateFileName();
        if( currStateFileName == null )
        {
            currStateFileName = Paths.get(BackupConstants.DEFAULT_STATE_FILENAME);
        }

        Path currErrorFileName = bc.getErrorFileName();
        if( currErrorFileName == null )
        {
            currErrorFileName = Paths.get(BackupConstants.DEFAULT_ERROR_FILENAME);
        }

        if( bc.getSetType() != FSSBackupType.FULL )
        {
            String errMsg
                    = String.format("Started a full backup but type set to '%s'", bc.getSetType());

            log.debug(errMsg);
        }

        // Setup the job directories if needed
        Map<BackupToolJobMemberType, Object> jobDirs = BackupJobUtil.retrieveJobDirs(bfs,
                bcsb.getUrl(),
                currStateFileName,
                currJobFileNameTemplate,
                currErrorFileName,
                currJobFileNamePattern,
                FSSBackupType.FULL,
                currJobFileNameComp,
                true,
                true);

        JobState currJob = (JobState)jobDirs.get(BackupToolJobMemberType.JOBOBJECT);
        JobSetState currJobSetState = (JobSetState) jobDirs.get(BackupToolJobMemberType.JOBSETOBJECT);
        Path currJobArchiveDir = (Path)jobDirs.get(BackupToolJobMemberType.JOBARCHIVEDIR);
        Path currJobSetDir = (Path)jobDirs.get(BackupToolJobMemberType.JOBSETDIR);
        Path currJobDir = (Path)jobDirs.get(BackupToolJobMemberType.JOBDIR);

        // Get the full list of files mergeFoundItems the file-system
        BackupConfigItemResolver bcir = BackupConfigItemResolver.from(
                currJob,
                currFileList,
                currDirList,
                null,
                false);
        
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

        if( resolverRes.getStatus() == BackupToolResultStatus.SUCCESS )
        {
            // Archive only if the file-system resolver returns a success response.

            // Local temp folder for this job run
            Path tempLocalDir;
            Path tempLocalArchiveDir;

            try
            {
                tempLocalDir = Files.createTempDirectory(bc.getHoldingDirectory(),
                        String.format("job-%s-%s-",
                                bc.getSetName(),
                                Instant.now().toString().replace(":", "_")));

                tempLocalArchiveDir = Files.createDirectory(tempLocalDir.resolve("archive"));
            }
            catch( IOException ex )
            {
                log.debug("Error creating the new local temp dir.", ex);

                throw new BackupToolException("Error creating the new local temp dir.", ex);
            }

            Path currArchiveFileNameTemplate = bc.getArchiveFileNameTemplate();
            if( currArchiveFileNameTemplate == null )
            {
                currArchiveFileNameTemplate
                        = Paths.get(BackupConstants.DEFAULT_ARCHIVE_NAME_TEMPLATE);
            }

            // One backup 'base path' per loop iteration
            // These paths generally map to a base directory where
            // backups should start
            for( Path currPath : fullFullList.keySet() )
            {

                Pattern currArchiveFileNamePattern = bc.getArchiveFileNamePattern();
                if( currArchiveFileNamePattern == null )
                {
                    currArchiveFileNamePattern
                            = Pattern.compile(BackupConstants.DEFAULT_ARCHIVE_NAME_RULE_PATTERN);
                }

                List<BackupToolNameComponentType> currArchiveFileNameComp
                        = bc.getArchiveFileNameComponent();
                if( currArchiveFileNameComp == null )
                {
                    currArchiveFileNameComp = new ArrayList<>();
                }

                if( currArchiveFileNameComp.size() < 1 )
                {
                    currArchiveFileNameComp.add(
                            BackupConstants.DEFAULT_ARCHIVE_NAME_RULE_COMPONENT);
                }

                // Get the next template file name remotely
                Path currJobArchiveTemplate = bfs.nextTemplate(
                                                    bfs.resolveRemoteFile(currJobArchiveDir),
                                                    currArchiveFileNameTemplate,
                                                    currArchiveFileNamePattern,
                                                    false);

                if( currJobArchiveTemplate == null )
                {
                    String errMsg = String.format("Template '%s' on '%s' returned null.",
                            currArchiveFileNameTemplate, currJobArchiveDir);

                    log.debug(errMsg);

                    throw new BackupToolException(errMsg);
                }

                // Use the template file name to get the next archive name
                Path currJobArchiveName = IBackendFileSystem.nextName(currJobArchiveTemplate,
                        currArchiveFileNamePattern,
                        currArchiveFileNameComp,
                        false,
                        null);

                // TODO : Create the listing file, one per archive
                Path currJobArchiveListing;

                // Build the next archive file locally in the holding directory.
                // We're adding a "*.tmp" suffix, which may have to be ignored later.
                Path currTempArchive;
                try
                {
                    currTempArchive = Files.createFile(tempLocalArchiveDir.resolve(currJobArchiveName));

                    log.debug(String.format("Created temp file for archive '%s'.", currTempArchive));
                }
                catch( Exception ex )
                {
                    String errMsg = "Error creating temporary file for archiving.";

                    log.debug(errMsg, ex);

                    throw new BackupToolException(errMsg, ex);
                }

                Deque<JobArchive> archives = new ArrayDeque<>();
                Path currArchiveListingPath
                        = JobArchiveListingState.getListingName(currTempArchive, false);
                JobArchiveListingState currListingState
                        = JobArchiveListingState.from(UUID.randomUUID(), currArchiveListingPath);

                archives.add(JobArchive.from(null, currListingState, currTempArchive, null, true, null, null,
                        null, null));

                BackupToolCompressionType currCompType = BackupToolCompressionType.NONE;
                BackupConfigCompression currComp = bc.getCompression();
                if( currComp != null && currComp.getCompressionType() != null )
                {
                    currCompType = currComp.getCompressionType();
                }

                // Check that we actually have files to backup.
                Deque<BackupItem> currInPaths = fullFullList.get(currPath);
                if( currInPaths == null || currInPaths.size() < 1 )
                {
                    String errMsg = String.format("Path returned no files for archive '%s'.", currPath);

                    log.debug(errMsg);
                    throw new BackupToolException(errMsg);
                }

                // Create the actual archive(s)
                Instant archStart = Instant.now();
                BackupToolResult archRes = BackupFileOutput.addBackupItems(currJob,
                        currInPaths,
                        LinkOption.NOFOLLOW_LINKS,
                        archives,
                        bc.getArchiveFileNamePattern(),
                        bc.getChunk(),
                        bc.getArchiveFileNameComponent(),
                        null,
                        currCompType,
                        true,
                        ".tmp");
                Instant archEnd = Instant.now();

                // Add the returned archives to the current JOB
                for( JobArchive ar : archives )
                {
                    currJob.getArchives().add(
                            JobArchiveSummary.from(ar,
                                    ar.getListing().getPath(),
                                    archStart,
                                    archEnd));

                    // Copy the local archive file to the remote backend
                    IBackupFilePart remoteArchive
                            = bfs.resolveRemoteFile(
                            currJobArchiveDir.resolve(
                                    ar.getPath().getFileName()));

                    if( bfs.exists(remoteArchive) )
                    {
                        // This file should never exist here
                        throw new BackupToolException(String.format("Archive file already exists '%s'",
                                remoteArchive.getPath()));
                    }

                    currTempArchive = tempLocalArchiveDir.resolve(ar.getPath().getFileName());

                    // Make the final copy to the remote server
                    IBackupFilePart localArchive = bfs.resolveLocalFile(currTempArchive);

                    // Do some quick sanity checks on the local archive
                    if( Files.notExists(currTempArchive) )
                    {
                        // File should exist at this point
                        String errMsg = String.format("Local Archive File '%s' does not exist.",
                                currTempArchive);

                        log.debug(errMsg);

                        throw new BackupToolException(errMsg);
                    }

                    try
                    {
                        if( Files.size(currTempArchive) < 1 )
                        {
                            // The archive should not be an empty file
                            String errMsg = String.format("Local Archive File '%s' is empty.",
                                    currTempArchive);

                            log.debug(errMsg);

                            throw new BackupToolException(errMsg);
                        }
                    }
                    catch( IOException ex )
                    {
                        String errMsg = String.format("I/O exception with '%s'",
                                currTempArchive);

                        log.debug(errMsg);

                        throw new BackupToolException(errMsg);
                    }

                    // Copy the local archive to the remote server
                    bfs.copy(localArchive, remoteArchive);

                    try
                    {
                        Files.delete(currTempArchive);
                    }
                    catch( IOException ex )
                    {
                        String errMsg = String.format("Error deleting archive '%s'", currTempArchive);

                        log.debug(errMsg, ex);
                        throw new BackupToolException(errMsg);
                    }

                    Path currLocalArchiveListing = tempLocalArchiveDir.resolve(ar.getListing().getPath());
                    IBackupFilePart localArchiveListing
                            = bfs.resolveLocalFile(currLocalArchiveListing);

                    IBackupFilePart remoteArchiveListing
                            = bfs.resolveRemoteFile(
                                    currJobArchiveDir.resolve(
                                                        ar.getListing()
                                                                .getPath()
                                                                    .getFileName()));

                    if( bfs.exists(remoteArchiveListing) )
                    {
                        // This file should never exist here
                        throw new BackupToolException(String.format("Remote Archive Listing File already exists '%s'",
                                remoteArchiveListing.getPath()));
                    }

                    // Make sure the local listing file exists before copying it
                    if( Files.notExists(ar.getListing().getPath()) )
                    {
                        throw new BackupToolException(
                                String.format("Local Archive Listing file does not exist '%s'",
                                    ar.getListing().getPath()));
                    }

                    bfs.copy(localArchiveListing, remoteArchiveListing);

                    try
                    {
                        Files.delete(currLocalArchiveListing);
                    }
                    catch( IOException ex )
                    {
                        String errMsg = String.format("Error deleting listing '%s'", currLocalArchiveListing);

                        log.debug(errMsg, ex);
                        throw new BackupToolException(errMsg);
                    }
                }
            }
        }

        // Update JOB state object
        currJob = JobState.from(currJob, null,
                null,
                null,
                Instant.now(),
                null,
                null,
                BackupToolJobStatus.COMPLETED,
                BackupToolJobDisposition.NONE,
                null,
                null,
                null,
                null);

        // Update JOBSET state object
        currJobSetState.getJobs().put(currJob.getJobId(), currJob);
        currJobSetState = JobSetState.from(currJobSetState.getJobSetId(), // Job Set Id
                currJob.getJobId(), // Last Job ID
                currJob.getJobId(), // Last Successful Job ID
                currJobSetDir, // Path
                currJobSetState.getJobs()
        );

        // Save the JOB state object
        BackupJobUtil.saveJobRemote(bfs,
                currJob,
                currJobDir.resolve(currStateFileName.toString()),
                currJobDir.resolve(currErrorFileName.toString()));

        // Save the JOBSET state object
        BackupJobUtil.saveJobSetRemote(bfs,
                currJobSetState,
                currJobSetDir.resolve(currStateFileName.toString()));

        // TODO : Delete files in the holding directory

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

//      pr = preBackup(getConfig());
        pr = BackupFull.doBackup(getBackupConfig());
//      pr = postBackup(getConfig());

        return pr;
    }
}
