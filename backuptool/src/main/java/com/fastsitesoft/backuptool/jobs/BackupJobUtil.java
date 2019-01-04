/*
 * SLU Dev Inc. CONFIDENTIAL
 * DO NOT COPY
 *
 * Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of SLU Dev Inc. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to SLU Dev Inc. and its suppliers and
 * may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from SLU Dev Inc.
 *
 */

package com.fastsitesoft.backuptool.jobs;

import com.fastsitesoft.backuptool.backend.IBackendFileSystem;
import com.fastsitesoft.backuptool.config.entities.JobSetState;
import com.fastsitesoft.backuptool.config.entities.JobState;
import com.fastsitesoft.backuptool.config.entities.writers.JobSetStateWriter;
import com.fastsitesoft.backuptool.config.entities.writers.JobStateWriter;
import com.fastsitesoft.backuptool.config.parsers.JobSetStateParser;
import com.fastsitesoft.backuptool.config.parsers.JobStateParser;
import com.fastsitesoft.backuptool.config.parsers.ParserUtil;
import com.fastsitesoft.backuptool.enums.BackupToolFileFormats;
import com.fastsitesoft.backuptool.enums.BackupToolJobDisposition;
import com.fastsitesoft.backuptool.enums.BackupToolJobMemberType;
import com.fastsitesoft.backuptool.enums.BackupToolJobStatus;
import com.fastsitesoft.backuptool.enums.BackupToolNameComponentType;
import com.fastsitesoft.backuptool.enums.FSSBackupHashType;
import com.fastsitesoft.backuptool.enums.FSSBackupType;
import com.fastsitesoft.backuptool.file.IBackupFilePart;
import com.fastsitesoft.backuptool.utils.BackupToolException;
import com.fastsitesoft.backuptool.utils.BackupToolResult;
import com.fastsitesoft.backuptool.utils.BackupToolSystemID;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Created by kervin on 2015-09-21.
 */
public final class BackupJobUtil
{
    private static final Logger log = LogManager.getLogger(BackupJobUtil.class);


    public static Map<BackupToolJobMemberType, Object> retrieveJobDirs(IBackendFileSystem bfs,
                                                                       URI bURL,
                                                                       Path stateFileName,
                                                                       Path jobFileNameTemplate,
                                                                       Path errorFileName,
                                                                       Pattern jobFileNamePattern,
                                                                       FSSBackupType setType,
                                                                       List<BackupToolNameComponentType> jobFileNameComponent,
                                                                       boolean createJobSetDir,
                                                                       boolean createJobDir) throws BackupToolException
    {
        Map<BackupToolJobMemberType, Object> res = new HashMap<>();
        JobState currJob;
        JobSetState currJobSetState;

        // Test or create the JOBSET directory
        IBackupFilePart jobSetDir
                = bfs.resolveRemoteFile(Paths.get(bURL.getPath()));
        if( bfs.exists(jobSetDir) )
        {
            Path jobSetStateFile = jobSetDir.getPath().resolve(stateFileName.toString());

            try
            {
                currJobSetState = retrieveJobSetRemote(bfs, jobSetStateFile);
            }
            catch( FileNotFoundException ex )
            {
                // We have a Job Set directory with no State File
                String errMsg = String.format("Job Set State File '%s' not found.",
                        jobSetStateFile);

                log.debug( errMsg, ex );

                throw new BackupToolException(errMsg, ex);
            }
        }
        else if( createJobSetDir )
        {
            // Create remote directory structure.
            bfs.createFolder(jobSetDir);

            // Create a Job Set State File
            currJobSetState = JobSetState.from( UUID.randomUUID(), // Job Set Id
                    null, // Last Job ID
                    null, // Last Successful Job ID
                    jobSetDir.getPath().getFileName() // Path
            );

            saveJobSetRemote(bfs,
                    currJobSetState,
                    jobSetDir.getPath().resolve(stateFileName.toString()));
        }
        else
        {
            throw new BackupToolException(
                    "JOB SET Directory does not exist and we were not asked to create it");
        }

        // Create the JOBS directory...
        IBackupFilePart jobsDir
                = bfs.resolveRemoteFile(jobSetDir.getPath().resolve("jobs"));
        if( bfs.exists(jobsDir) )
        {
            ;
        }
        else if( createJobDir )
        {
            // Create remote directory structure.
            bfs.createFolder(jobsDir);
        }
        else
        {
            throw new BackupToolException(
                    "JOB Directory does not exist and we were not asked to create it");
        }

        // Get the next job directory name
        Path currJobTemplate = bfs.nextTemplate(jobsDir, jobFileNameTemplate, null, false);

        Path currJobName = IBackendFileSystem.nextName(currJobTemplate,
                jobFileNamePattern,
                jobFileNameComponent,
                false,
                null);

        // Test or create the JOB directory
        IBackupFilePart currJobDir
                = bfs.resolveRemoteFile(jobsDir.getPath().resolve(currJobName));
        if( bfs.exists(currJobDir) )
        {
            // The job directory already exists
            // READ JOBSET STATE

            // Test and set the JOB state if necessary
            try
            {
                // The Job State File exists.  read it.
                currJob = retrieveJobRemote(bfs,
                        currJobDir.getPath().resolve(stateFileName.toString()));
            }
            catch( FileNotFoundException ex)
            {
                // We have a Job directory with no State File
                String errMsg = String.format("Job '%s' has no state file.",
                        currJobDir.getUri());

                log.debug( errMsg, ex );

                throw new BackupToolException(errMsg, ex);
            }
        }
        else
        {
            // Create remote directory structure for the Job
            bfs.createFolder(currJobDir);

            currJob = JobState.from(null, UUID.randomUUID(),
                    currJobDir.getPath(),
                    Instant.now(),
                    null,
                    setType,
                    BackupToolSystemID.getType1(),
                    BackupToolJobStatus.NONE,
                    BackupToolJobDisposition.NONE,
                    currJobSetState.getJobSetId(),
                    errorFileName,
                    null,
                    FSSBackupHashType.NONE );

            // Create the first job state and error file
            saveJobRemote(bfs,
                    currJob,
                    currJobDir.getPath().resolve(stateFileName.toString()),
                    currJobDir.getPath().resolve(errorFileName.toString())
                    );
        }

        IBackupFilePart currJobArchiveDir
                = bfs.resolveRemoteFile(currJobDir.getPath().resolve("archive"));
        if( bfs.exists(currJobArchiveDir) )
        {
            ;
        }
        else
        {
            // Create remote directory structure for the archive files.
            bfs.createFolder(currJobArchiveDir);
        }

        // Record the last Job in the Job Set State
        currJobSetState.getJobs().put(currJob.getJobId(), currJob);

        // Update JOBSET state object
        currJobSetState = JobSetState.from( currJobSetState.getJobSetId(), // Job Set Id
                currJob.getJobId(), // Last Job ID
                null, // Last Successful Job ID
                jobSetDir.getPath(), // Path
                currJobSetState.getJobs()
        );

        // Save the JOBSET state object
        saveJobSetRemote(bfs, currJobSetState,
                jobSetDir.getPath().resolve(stateFileName.toString()));

        res.put(BackupToolJobMemberType.JOBSETOBJECT, currJobSetState);
        res.put(BackupToolJobMemberType.JOBOBJECT, currJob);
        res.put(BackupToolJobMemberType.JOBDIR, currJobDir.getPath());
        res.put(BackupToolJobMemberType.JOBARCHIVEDIR, currJobDir.getPath().resolve("archive"));
        res.put(BackupToolJobMemberType.JOBSETDIR, jobSetDir.getPath());

        return res;
    }

    public static BackupToolResult saveJobRemote(final IBackendFileSystem bfs,
                                                 final JobState js,
                                                 final Path stateDest,
                                                 final Path errorDest) throws BackupToolException
    {
        BackupToolResult res = null;

        IBackupFilePart currJobStateFile = bfs.resolveRemoteFile(stateDest);
        IBackupFilePart currJobErrorFile = bfs.resolveRemoteFile(errorDest);

        Pair<Path,Path> temp = JobStateWriter.write(js);

        IBackupFilePart tempStateFile = bfs.resolveLocalFile(temp.getLeft());
        bfs.copy(tempStateFile, currJobStateFile);
        tempStateFile = bfs.resolveLocalFile(temp.getRight());
        bfs.copy(tempStateFile, currJobErrorFile);

        return res;
    }

    public static BackupToolResult saveJobSetRemote(final IBackendFileSystem bfs,
                                                    final JobSetState js,
                                                    final Path dest)
            throws BackupToolException
    {
        BackupToolResult res = null;

        IBackupFilePart currJobSetStateFile = bfs.resolveRemoteFile(dest);

        Path tempPath = JobSetStateWriter.write(js);
        IBackupFilePart tempStateFile = bfs.resolveLocalFile(tempPath);
        bfs.copy(tempStateFile, currJobSetStateFile);

        return res;
    }

    public static JobSetState retrieveJobSetRemote( final IBackendFileSystem bfs,
                                                    final Path src )
            throws BackupToolException, FileNotFoundException
    {
        JobSetState res = null;
        IBackupFilePart tempFile;

        IBackupFilePart currJobSetStateFile = bfs.resolveRemoteFile(src);

        if( bfs.exists(currJobSetStateFile) == false )
        {
            throw new FileNotFoundException(
                    String.format("JOB SET State Path '%s' was not found.", src)
            );
        }

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
        res = JobSetStateParser.readConfig(
                ParserUtil.readConfig(tempFile.getPath(), BackupToolFileFormats.JOBSETSTATE));

        return res;
    }


    public static JobState retrieveJobRemote( final IBackendFileSystem bfs,
                                              final Path src )
            throws BackupToolException, FileNotFoundException
    {
        JobState res = null;
        IBackupFilePart tempFile;

        IBackupFilePart currJobStateFile = bfs.resolveRemoteFile(src);

        if( bfs.exists(currJobStateFile) )
        {
            throw new FileNotFoundException(
                    String.format("JOB State Path '%s' was not found.", src)
            );
        }

        try
        {
            tempFile = bfs.resolveLocalFile( Files.createTempFile("jobState", ".tmp") );
        }
        catch( IOException ex )
        {
            String errMsg = "Error creating temp file for Job State.";
            log.debug(errMsg, ex);

            throw new BackupToolException(errMsg, ex);
        }

        bfs.copy(currJobStateFile, tempFile);
        res = JobStateParser.readConfig(
                ParserUtil.readConfig(tempFile.getPath(), BackupToolFileFormats.JOBSTATE));

        return res;
    }
}
