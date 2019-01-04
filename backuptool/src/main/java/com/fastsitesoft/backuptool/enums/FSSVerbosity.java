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

package com.fastsitesoft.backuptool.enums;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Kervin Pierre <info@sludev.com>
 */
public enum FSSVerbosity
{
    ALL,
    MINIMUM,
    MAXIMUM,
    DEBUG,
    INFO,
    WARN,
    ERROR;

    private static final Logger log
            = LogManager.getLogger(FSSVerbosity.class);

    /**
     *  Set the application's verbosity level.  Prior to this call here, it's
     *  set to WARN by default, or by the -DFSSVERBOSITY java command line
     *  property.
     *
     *  --verbose has the top precendence.  Using this argument to set the
     *     verbosity on any command run.
     *
     *  -DFSSVERBOSITY can be used as a default system would verbosity.  Since
     *     it sets the logger's verbosity from the start, you should set it so
     *     that you do not lose any messages.
     *
     * @param v The verbosity level. ALL, DEBUG, INFO, WARNING and ERROR.
     */
    public static FSSVerbosity from(String v)
    {
        FSSVerbosity currVerbosity = null;

        try
        {
            String currV = StringUtils.upperCase(v);
            currVerbosity = FSSVerbosity.valueOf(currV);
        }
        catch (Exception ex)
        {
            log.error( String.format("Invalid Verbosity specified '%s'", v), ex);
        }

        return currVerbosity;
    }
}
