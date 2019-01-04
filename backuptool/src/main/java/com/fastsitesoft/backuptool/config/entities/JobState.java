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

import com.fastsitesoft.backuptool.enums.BackupToolJobDisposition;
import com.fastsitesoft.backuptool.enums.BackupToolJobStatus;
import com.fastsitesoft.backuptool.enums.FSSBackupHashType;
import com.fastsitesoft.backuptool.enums.FSSBackupType;
import com.fastsitesoft.backuptool.utils.BackupToolException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Maintains the state for a single job, in memory or on disk.
 *
 * Created by kervin on 2015-08-11.
 */
public final class JobState
{
    private static final Logger log = LogManager.getLogger(JobState.class);

    private final UUID jobId;
    private final Path path;
    private final Instant start;
    private final Instant end;
    private final FSSBackupType type;
    private final String systemId;
    private final BackupToolJobStatus status;
    private final BackupToolJobDisposition disposition;
    private final UUID jobSetId;
    private final Path jobErrorsFile;
    private final UUID previousJobId;
    private final FSSBackupHashType hashType;
    private final List<JobArchiveSummary> archives;
    private final List<JobError> errors;

    public UUID getJobId()
    {
        return jobId;
    }

    public Path getPath()
    {
        return path;
    }

    public Instant getStart()
    {
        return start;
    }

    public Instant getEnd()
    {
        return end;
    }

    public FSSBackupType getType()
    {
        return type;
    }

    public String getSystemId()
    {
        return systemId;
    }

    public BackupToolJobStatus getStatus()
    {
        return status;
    }

    public BackupToolJobDisposition getDisposition()
    {
        return disposition;
    }

    public UUID getJobSetId()
    {
        return jobSetId;
    }

    public Path getJobErrorsFile()
    {
        return jobErrorsFile;
    }

    public UUID getPreviousJobId()
    {
        return previousJobId;
    }

    public FSSBackupHashType getHashType()
    {
        return hashType;
    }

    public List<JobArchiveSummary> getArchives()
    {
        return archives;
    }

    public List<JobError> getErrors()
    {
        return errors;
    }

    private JobState( final JobState obj,
                      final UUID jobId,
                      final Path path,
                      final Instant start,
                      final Instant end,
                      final FSSBackupType type,
                      final String systemId,
                      final BackupToolJobStatus status,
                      final BackupToolJobDisposition disposition,
                      final UUID jobSetId,
                      final Path jobErrorsFile,
                      final UUID previousJobId,
                      final FSSBackupHashType hashType,
                      final List<JobArchiveSummary> archives,
                      final List<JobError> errors )
    {
        if( jobId != null )
        {
            // Parameter takes precedence
            this.jobId = jobId;
        }
        else if( obj != null )
        {
            // Then supplied object
            this.jobId = obj.getJobId();
        }
        else
        {
            // Finally the default value
            this.jobId = null;
        }

        if( path != null )
        {
            this.path = path;
        }
        else if( obj != null )
        {
            this.path = obj.getPath();
        }
        else
        {
            this.path = null;
        }

        if( start != null )
        {
            this.start = start;
        }
        else if( obj != null )
        {
            this.start = obj.getStart();
        }
        else
        {
            this.start = null;
        }

        if( end != null )
        {
            this.end = end;
        }
        else if( obj != null )
        {
            this.end = obj.getEnd();
        }
        else
        {
            this.end = null;
        }

        if( type != null )
        {
            this.type = type;
        }
        else if( obj != null )
        {
            this.type = obj.getType();
        }
        else
        {
            this.type = null;
        }

        if( systemId != null )
        {
            this.systemId = systemId;
        }
        else if( obj != null )
        {
            this.systemId = obj.getSystemId();
        }
        else
        {
            this.systemId = null;
        }

        if( status != null )
        {
            this.status = status;
        }
        else if( obj != null )
        {
            this.status = obj.getStatus();
        }
        else
        {
            this.status = BackupToolJobStatus.NONE;
        }

        if( disposition != null )
        {
            this.disposition = disposition;
        }
        else if( obj != null )
        {
            this.disposition = obj.getDisposition();
        }
        else
        {
            this.disposition = BackupToolJobDisposition.NONE;
        }

        if( jobErrorsFile != null )
        {
            this.jobErrorsFile = jobErrorsFile;
        }
        else if( obj != null )
        {
            this.jobErrorsFile = obj.getJobErrorsFile();
        }
        else
        {
            this.jobErrorsFile = null;
        }

        if( previousJobId != null )
        {
            this.previousJobId = previousJobId;
        }
        else if( obj != null )
        {
            this.previousJobId = obj.getPreviousJobId();
        }
        else
        {
            this.previousJobId = null;
        }

        if( hashType != null )
        {
            this.hashType = hashType;
        }
        else if( obj != null )
        {
            this.hashType = obj.getHashType();
        }
        else
        {
            this.hashType = FSSBackupHashType.NONE;
        }

        if( archives != null )
        {
            this.archives = archives;
        }
        else if( obj != null )
        {
            this.archives = obj.getArchives();
        }
        else
        {
            this.archives = new ArrayList<>();
        }

        if( errors != null )
        {
            this.errors = errors;
        }
        else if( obj != null )
        {
            this.errors = obj.getErrors();
        }
        else
        {
            this.errors = new ArrayList<>();
        }

        if( jobSetId != null )
        {
            this.jobSetId = jobSetId;
        }
        else if( obj != null )
        {
            this.jobSetId = obj.getJobSetId();
        }
        else
        {
            this.jobSetId = null;
        }
    }

    public static JobState setStart( JobState obj, Instant start )
    {
        return from( obj,
                null,
                null,
                start,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null );
    }

    public static JobState setEnd( JobState obj, Instant end )
    {
        return from( obj,
                null,
                null,
                null,
                end,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null );
    }

    public static JobState from( final JobState obj,
                                 final UUID jobId,
                                 final Path path,
                                 final Instant start,
                                 final Instant end,
                                 final FSSBackupType type,
                                 final String systemId,
                                 final BackupToolJobStatus status,
                                 final BackupToolJobDisposition disposition,
                                 final UUID jobSetId,
                                 final Path jobErrorsFile,
                                 final UUID previousJobId,
                                 final FSSBackupHashType hashType)
    {
        List<JobArchiveSummary> arch = new ArrayList<>();
        List<JobError> err = new ArrayList<>();

        if( obj != null )
        {
            arch = obj.getArchives();
            err = obj.getErrors();
        }

        return from( obj, jobId,
                    path,
                    start,
                    end,
                    type,
                    systemId,
                    status,
                    disposition,
                    jobSetId,
                    jobErrorsFile,
                    previousJobId,
                    hashType,
                    arch,
                    err
                    );
    }

    public static JobState from( final JobState obj,
                                 final String jobId,
                                 final String path,
                                 final String start,
                                 final String end,
                                 final String type,
                                 final String systemId,
                                 final String status,
                                 final String disposition,
                                 final String jobSetId,
                                 final String jobErrorsFile,
                                 final String previousJobId,
                                 final String hashType,
                                 final List<JobArchiveSummary> archives,
                                 final List<JobError> errors)
    {
        JobState res;

        UUID currJobId = null;
        UUID currJobSetId = null;
        UUID currPrevJobId = null;

        if( StringUtils.isNoneBlank(jobId) )
        {
            currJobId = UUID.fromString(jobId);
        }

        if( StringUtils.isNoneBlank(jobSetId))
        {
            currJobSetId = UUID.fromString(jobSetId);
        }

        if( StringUtils.isNoneBlank(previousJobId))
        {
            currPrevJobId = UUID.fromString(previousJobId);
        }

        Path currPath = null;
        Path currErrorsFile = null;

        if( StringUtils.isNoneBlank(path) )
        {
            currPath = Paths.get(path);
        }

        if( StringUtils.isNoneBlank(jobErrorsFile))
        {
            currErrorsFile = Paths.get(jobErrorsFile);
        }

        Instant currStart = null;
        Instant currEnd   = null;

        if( StringUtils.isNoneBlank(start))
        {
            currStart = Instant.parse(start);
        }

        if( StringUtils.isNoneBlank(end))
        {
            currEnd = Instant.parse(end);
        }

        FSSBackupType currType = null;
        BackupToolJobStatus currStatus = null;
        BackupToolJobDisposition currDispo = null;
        FSSBackupHashType currHash = null;

        try
        {
            currType = FSSBackupType.from(type);
        }
        catch( BackupToolException ex )
        {
            log.debug("Parsing backup type failed", ex);
        }

        if( StringUtils.isNoneBlank(status) )
        {
            currStatus = BackupToolJobStatus.from(status);
        }

        if( StringUtils.isNoneBlank(disposition) )
        {
            currDispo = BackupToolJobDisposition.from(disposition);
        }

        if( StringUtils.isNoneBlank(hashType) )
        {
            currHash = FSSBackupHashType.from(hashType);
        }

        res = from(obj,
                currJobId,
                currPath,
                currStart,
                currEnd,
                currType,
                systemId,
                currStatus,
                currDispo,
                currJobSetId,
                currErrorsFile,
                currPrevJobId,
                currHash,
                archives,
                errors);

        return res;
    }

    public static JobState from( final JobState obj,
                                final UUID jobId,
                                final Path path,
                                final Instant start,
                                final Instant end,
                                final FSSBackupType type,
                                final String systemId,
                                final BackupToolJobStatus status,
                                final BackupToolJobDisposition disposition,
                                final UUID jobSetId,
                                final Path jobErrorsFile,
                                final UUID previousJobId,
                                final FSSBackupHashType hashType,
                                final List<JobArchiveSummary> archives,
                                final List<JobError> errors)
    {
        JobState res;

        res = new JobState(obj, jobId, path, start, end, type, systemId, status, disposition,
                jobSetId, jobErrorsFile, previousJobId, hashType, archives, errors);

        return res;

    }

    public static void mergeErrors(
            final JobState job,
            final List<JobError> newErrs ) throws BackupToolException
    {
        if( job == null || job.getErrors() == null )
        {
            throw new BackupToolException("Error Job object cannot be null");
        }

        List<JobError> res = job.getErrors();

        for( JobError err : newErrs )
        {
            JobError currErr = err;

            // TODO : fix the errors by adding missing info
            if( err.getJobId() == null )
            {
                // TODO : Add JobID
                ;
            }

            res.add(currErr);
        }
    }

    /**
     * Check that two objects are equivalent.
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }

        if (obj == this)
        {
            return true;
        }

        // getClass() so it checks inheritance.
        // No cross-class equals()
        if (obj.getClass() != getClass())
        {
            return false;
        }

        boolean res;
        JobState rhs = (JobState)obj;

        EqualsBuilder eb = new EqualsBuilder();

        // FIXME : Add more fields in comparison

        res = eb.append(jobId, rhs.getJobId())
                .append(status, rhs.getStatus())
                .append(disposition, rhs.getDisposition())
                .append(start, rhs.getStart())
                .append(end, rhs.getEnd())
                .isEquals();

        return res;
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder hcb = new HashCodeBuilder();

        int res = hcb.append(jobId)
                .append(status)
                .append(disposition)
                .append(start)
                .append(end)
                .toHashCode();

        return res;
    }
}
