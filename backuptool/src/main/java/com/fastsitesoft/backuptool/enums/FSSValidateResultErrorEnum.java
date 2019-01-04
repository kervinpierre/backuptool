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
package com.fastsitesoft.backuptool.enums;

/**
 * Provides a code for the specific error encountered during validation.
 * 
 * @author Kervin Pierre <info@sludev.com>
 */
public enum FSSValidateResultErrorEnum
{
    NONE,
    MISSING_FS_OBJECT,
    FS_OBJECT_PRESENT,
    INVALID_VALUE,
    EXPECTED_FOLDER,
    EXPECTED_FILE
}
