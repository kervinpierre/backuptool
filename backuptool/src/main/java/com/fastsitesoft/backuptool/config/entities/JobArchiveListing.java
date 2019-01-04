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

import com.fastsitesoft.backuptool.enums.BackupToolFSItemType;
import com.fastsitesoft.backuptool.enums.BackupToolJobDisposition;
import com.fastsitesoft.backuptool.enums.BackupToolJobStatus;
import com.fastsitesoft.backuptool.enums.FSSBackupHashType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.time.Instant;

/**
 * Represents a single Archive Listing on disk.
 *
 * Created by kervin on 2015-08-15.
 */
public final class JobArchiveListing
{
    private static final Logger log = LogManager.getLogger(JobArchiveListing.class);

    private final Path path;
    private final BackupToolJobStatus status;
    private final BackupToolJobDisposition disposition;
    private final Instant lastMod;
    private final FSSBackupHashType hashType;
    private final String hashValue;
    private final BackupToolFSItemType type;

    public Path getPath()
    {
        return path;
    }

    public BackupToolJobStatus getStatus()
    {
        return status;
    }

    public BackupToolJobDisposition getDisposition()
    {
        return disposition;
    }

    public Instant getLastMod()
    {
        return lastMod;
    }

    public FSSBackupHashType getHashType()
    {
        return hashType;
    }

    public String getHashValue()
    {
        return hashValue;
    }

    public BackupToolFSItemType getType()
    {
        return type;
    }

    private JobArchiveListing(final Path path,
                              final BackupToolJobStatus status,
                              final BackupToolJobDisposition disposition,
                              final Instant lastMod,
                              final FSSBackupHashType hashType,
                              final String hashValue,
                              final BackupToolFSItemType type)
    {
        this.path = path;
        this.status = status;
        this.disposition = disposition;
        this.lastMod = lastMod;
        this.hashType = hashType;
        this.hashValue = hashValue;
        this.type = type;
    }

    public static JobArchiveListing from(final Path path,
                             final BackupToolJobStatus status,
                             final BackupToolJobDisposition disposition,
                             final Instant lastMod,
                             final FSSBackupHashType hashType,
                             final String hashValue,
                             final BackupToolFSItemType type)
    {
        JobArchiveListing res;

        res = new JobArchiveListing(path,
                status,
                disposition,
                lastMod,
                hashType,
                hashValue,
                type);

        return res;
    }
}
