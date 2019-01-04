/*
 *  SLU Dev Inc. CONFIDENTIAL
 *  DO NOT COPY
 * 
 * Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 * All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 *  the property of SLU Dev Inc. and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to SLU Dev Inc. and its suppliers and
 *  may be covered by U.S. and Foreign Patents, patents in process,
 *  and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from SLU Dev Inc.
 */
package com.fastsitesoft.backuptool;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;

/**
 * Identifier for each backup on disk.
 * 
 * @author Kervin Pierre <info@sludev.com>
 */
public final class BackupId
{
    private static final org.apache.logging.log4j.Logger log 
                                = LogManager.getLogger(BackupId.class);
    
    private final Instant timestamp;
    private final UUID uuid;

    public Instant getTimestamp()
    {
        return timestamp;
    }

    public UUID getUuid()
    {
        return uuid;
    }
    
    public BackupId(Instant i, UUID u)
    {
        uuid = u;
        timestamp = i;
    }

    public BackupId()
    {
        this(Instant.now(), UUID.randomUUID());
    }

    public static BackupId from(String u)
    {
        BackupId res;

        UUID currUUID = UUID.fromString(u);

        res = new BackupId(Instant.now(), currUUID);

        return res;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString()
    {
        String res = "";

        return res;
    }
}
