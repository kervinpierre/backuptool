/*
 *  CityMSP LLC CONFIDENTIAL
 *  DO NOT COPY
 *
 * Copyright (c) [2012] - [2019] CityMSP LLC <info@citymsp.nyc>
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 *  the property of CityMSP LLC and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to CityMSP LLC and its suppliers and
 *  may be covered by U.S. and Foreign Patents, patents in process,
 *  and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from CityMSP LLC
 */
package com.fastsitesoft.backuptool.enums;

import org.apache.commons.lang3.StringUtils;

public enum BackupToolIgnoreFlags
{
    NONE,
    FILE_ATTRIBUTES_UNREADABLE,
    FILE_ALL_IO_EXCEPTIONS;

    public static BackupToolIgnoreFlags from(String val)
    {
        BackupToolIgnoreFlags res;

        res = BackupToolIgnoreFlags.valueOf( StringUtils.upperCase(val) );

        return res;
    }
}
