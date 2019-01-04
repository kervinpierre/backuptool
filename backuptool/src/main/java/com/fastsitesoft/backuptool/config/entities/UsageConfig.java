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
package com.fastsitesoft.backuptool.config.entities;

import java.nio.file.Path;

/**
 *
 * @author Kervin
 */
public final class UsageConfig
{
    private final String helpString;
    private final int exitCode;
    private final Path lockFile;

    public Path getLockFile()
    {
        return lockFile;
    }
    
    public String getHelpString()
    {
        return helpString;
    }

    public int getExitCode()
    {
        return exitCode;
    }
    
    public UsageConfig(final String h, final int e, final Path lf)
    {
        helpString = h;
        exitCode = e;
        lockFile = lf;
    }
}
