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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

/**
 * Created by kervin on 2015-08-11.
 */
public final class JobError
{
    private static final Logger log = LogManager.getLogger(JobError.class);

    private final UUID jobId;
    private final UUID errorId;
    private final BackupToolJobDisposition disposition;
    private final Integer errorCode;
    private final Path archivePath;
    private final Type errorCodeType;
    private final Path itemPath;
    private final String text;
    private final String summary;
    private final Exception exception;
    private final Instant timestamp;

    public Exception getException()
    {
        return exception;
    }

    public UUID getJobId()
    {
        return jobId;
    }

    public UUID getErrorId()
    {
        return errorId;
    }

    public BackupToolJobDisposition getDisposition()
    {
        return disposition;
    }

    public Integer getErrorCode()
    {
        return errorCode;
    }

    public Path getArchivePath()
    {
        return archivePath;
    }

    public Type getErrorCodeType()
    {
        return errorCodeType;
    }

    public Path getItemPath()
    {
        return itemPath;
    }

    public String getText()
    {
        return text;
    }

    public String getSummary()
    {
        return summary;
    }

    public Instant getTimestamp()
    {
        return timestamp;
    }

    private JobError( final UUID jobId,
                     final UUID errorId,
                     final BackupToolJobDisposition disposition,
                     final Integer errorCode,
                     final Path archivePath,
                     final Type errorCodeType,
                     final Path itemPath,
                     final String text,
                     final String summary,
                     final Instant timestamp,
                     final Exception exception)
    {
        this.jobId = jobId;
        this.errorId = errorId;
        this.disposition = disposition;
        this.errorCodeType = errorCodeType;
        this.errorCode = errorCode;
        this.archivePath = archivePath;
        this.itemPath = itemPath;
        this.text = text;
        this.summary = summary;
        this.timestamp = timestamp;
        this.exception = exception;
    }

    public static JobError from( final UUID jobId,
                            final UUID errorId,
                            final BackupToolJobDisposition disposition,
                            final Integer errorCode,
                            final Path archivePath,
                            final Type errorCodeType,
                            final Path itemPath,
                            final String text,
                            final String summary,
                            final Instant timestamp,
                            final Exception exception)
    {
        JobError res;

        res = new JobError(jobId, errorId, disposition, errorCode, archivePath, errorCodeType, itemPath,
                text, summary, timestamp, exception);

        return res;
    }
}
