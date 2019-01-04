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

package com.fastsitesoft.backuptool.config.entities;

import com.fastsitesoft.backuptool.utils.BackupToolException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Maintains the Job Map's state in memory.  Also manages the Job Map's state on disk.
 *
 * Created by kervin on 8/11/15.
 */
public class JobSetState
{
    private static final Logger log = LogManager.getLogger( JobSetState.class );

    private final UUID jobSetId;
    private final UUID lastJobId;
    private final UUID lastSuccessfulJobId;
    private final Path path;
    private final Map<UUID, JobState> jobs;

    public UUID getJobSetId()
    {
        return jobSetId;
    }

    public Path getPath()
    {
        return path;
    }

    public Map<UUID, JobState> getJobs()
    {
        return jobs;
    }

    public UUID getLastJobId()
    {
        return lastJobId;
    }

    public UUID getLastSuccessfulJobId()
    {
        return lastSuccessfulJobId;
    }

    private JobSetState( final UUID jobSetId,
                         final UUID lastJobId,
                         final UUID lastSuccessfulJobId,
                         final Path path,
                         final Map<UUID, JobState> jobs )
    {
        this.jobSetId = jobSetId;
        this.lastJobId = lastJobId;
        this.lastSuccessfulJobId = lastSuccessfulJobId;
        this.path = path;
        this.jobs = jobs;
    }

    public static JobSetState from( final UUID jobSetId,
                                    final UUID lastJobId,
                                    final UUID lastSuccessfulJobId,
                                    final Path path )
    {
        JobSetState res;
        Map<UUID, JobState> jobs = new HashMap<>();

        res = from(jobSetId, lastJobId, lastSuccessfulJobId, path, jobs);

        return res;
    }

    public static JobSetState from(final UUID jobSetId,
                                   final UUID lastJobId,
                                   final UUID lastSuccessfulJobId,
                                   final Path path,
                                   final Map<UUID, JobState> jobs)
    {
        JobSetState res;

        res = new JobSetState(jobSetId, lastJobId, lastSuccessfulJobId, path, jobs);
        return res;
    }

    public static JobSetState from(final String jobSetId,
                                   final String lastJobId,
                                   final String lastSuccessfulJobId,
                                   final String path,
                                   final Map<UUID, JobState> jobs) throws BackupToolException
    {
        JobSetState res;

        UUID currJobSetId = null;
        UUID currLastJobId = null;
        UUID currSuccessfulJobID = null;
        Path currPath = Paths.get(path);

        if( StringUtils.isNoneBlank(jobSetId) )
        {
            try
            {
                currJobSetId = UUID.fromString(jobSetId);
            }
            catch( IllegalArgumentException ex )
            {
                String errMsg = String.format("Invalid Job Set Id '%s'", jobSetId);

                log.debug(errMsg);
                throw new BackupToolException(errMsg, ex);
            }
        }

        if( StringUtils.isNoneBlank(lastJobId) )
        {
            try
            {
                currLastJobId = UUID.fromString(lastJobId);
            }
            catch( IllegalArgumentException ex )
            {
                String errMsg = String.format("Invalid Last Job Id '%s'", lastJobId);

                log.debug(errMsg);
                throw new BackupToolException(errMsg, ex);
            }
        }

        if( StringUtils.isNoneBlank(lastSuccessfulJobId) )
        {
            try
            {
                currSuccessfulJobID = UUID.fromString(lastSuccessfulJobId);
            }
            catch( IllegalArgumentException ex )
            {
                String errMsg = String.format("Invalid Last Successful Id '%s'", lastSuccessfulJobId);

                log.debug(errMsg);
                throw new BackupToolException(errMsg, ex);
            }
        }

        res = from(currJobSetId, currLastJobId, currSuccessfulJobID, currPath, jobs);

        return res;
    }
}
