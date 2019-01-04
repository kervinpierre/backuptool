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

package com.fastsitesoft.backuptool.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for generating System IDs used to help identify instances.
 *
 * Created by kervin on 2015-08-14.
 */
public final class BackupToolSystemID
{
    private static final Logger log = LogManager.getLogger(BackupToolSystemID.class);

    public static String getType1()
    {
        String res;
        String currHostname;
        String currOSName;
        String currOSArch;
        String currOSVersion;
        String currUserName;

        StringBuilder resBuilder = new StringBuilder();

        currHostname = System.getenv("HOSTNAME");
        if( StringUtils.isBlank(currHostname) )
        {
            currHostname = System.getenv("COMPUTERNAME");
        }

        currOSName = System.getProperty("os.name");
        currOSArch = System.getProperty("os.arch");
        currOSVersion = System.getProperty("os.version");
        currUserName = System.getProperty("user.name");

        resBuilder.append(String.format("type=1;hostname=\"%s\";", currHostname));
        resBuilder.append(String.format("os.name=\"%s\";", currOSName));
        resBuilder.append(String.format("os.arch=\"%s\";", currOSArch));
        resBuilder.append(String.format("os.version=\"%s\";", currOSVersion));
        resBuilder.append(String.format("user.name=\"%s\";", currUserName));

        res = resBuilder.toString();

        return res;
    }
}
