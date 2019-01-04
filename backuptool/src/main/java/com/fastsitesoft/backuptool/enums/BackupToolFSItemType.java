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

package com.fastsitesoft.backuptool.enums;

/**
 * The type of any file-system object.
 *
 * Created by kervin on 2015-08-15.
 */
public enum BackupToolFSItemType
{
    NONE,
    GENERIC_FILE,
    DIRECTORY,
    SOFT_LINK,
    SOFT_LINK_TO_FILE,
    SOFT_LINK_TO_DIR,
    HARD_LINK
}
