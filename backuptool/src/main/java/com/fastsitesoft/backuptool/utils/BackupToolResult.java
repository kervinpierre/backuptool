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

package com.fastsitesoft.backuptool.utils;

import com.fastsitesoft.backuptool.enums.BackupToolResultStatus;
import com.fastsitesoft.backuptool.enums.BackupToolResultStatusDetail;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The result of a operation call.
 *
 * @author Kervin
 */
public final class BackupToolResult
{
    private static final Logger log 
                      = LogManager.getLogger(BackupToolResult.class);
    
    private final BackupToolResultStatus status;
    private final BackupToolResultStatusDetail statusDetail;
    private final String statusNotes;
    private final Exception statusException;

    public BackupToolResultStatus getStatus()
    {
        return status;
    }

    public BackupToolResultStatusDetail getStatusDetail()
    {
        return statusDetail;
    }

    public String getStatusNotes()
    {
        return statusNotes;
    }

    public Exception getStatusException()
    {
        return statusException;
    }

    public BackupToolResult(BackupToolResultStatus status)
    {
        this(status, BackupToolResultStatusDetail.NONE, null, null);
    }

    public BackupToolResult(BackupToolResultStatus status, BackupToolResultStatusDetail statusDetail)
    {
        this(status, statusDetail, null, null);
    }

    public BackupToolResult(BackupToolResultStatus status, BackupToolResultStatusDetail statusDetail,
                            String statusNotes)
    {
        this(status, statusDetail, statusNotes, null);
    }

    public BackupToolResult(BackupToolResultStatus status, BackupToolResultStatusDetail statusDetail,
                            String statusNotes, Exception statusException)
    {
        this.status = status;
        this.statusDetail = statusDetail;
        this.statusNotes = statusNotes;
        this.statusException = statusException;
    }

}
