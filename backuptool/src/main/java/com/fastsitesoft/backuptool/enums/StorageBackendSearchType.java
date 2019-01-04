/*
 *   SLU Dev Inc. CONFIDENTIAL
 *   DO NOT COPY
 *  
 *  Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 *  All Rights Reserved.
 *  
 *  NOTICE:  All information contained herein is, and remains
 *   the property of SLU Dev Inc. and its suppliers,
 *   if any.  The intellectual and technical concepts contained
 *   herein are proprietary to SLU Dev Inc. and its suppliers and
 *   may be covered by U.S. and Foreign Patents, patents in process,
 *   and are protected by trade secret or copyright law.
 *   Dissemination of this information or reproduction of this material
 *   is strictly forbidden unless prior written permission is obtained
 *   from SLU Dev Inc.
 */
package com.fastsitesoft.backuptool.enums;

/**
 * Specifies the type of search on a storage backend.
 * 
 * @author kervin
 */
public enum StorageBackendSearchType
{
    NONE,
    SELECT_SELF,
    SELECT_SELF_AND_CHILDREN,
    SELECT_CHILDREN,
    EXCLUDE_SELF,
    SELECT_FILES,
    SELECT_FOLDERS,
    SELECT_ALL
}
