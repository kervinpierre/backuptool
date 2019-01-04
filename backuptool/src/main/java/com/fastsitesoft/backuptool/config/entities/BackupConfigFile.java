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

import com.fastsitesoft.backuptool.enums.FSSBackupItemBackupOption;
import com.fastsitesoft.backuptool.enums.FSSBackupItemPathTypeEnum;
import com.fastsitesoft.backuptool.utils.FSSValidateConfigResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

/**
 *
 * @author Kervin Pierre <info@sludev.com>
 */
public final class BackupConfigFile extends BackupConfigItem
{
    private static final Logger log 
                        = LogManager.getLogger(BackupConfigFile.class);

    public BackupConfigFile(String pathString, Set<FSSBackupItemBackupOption> backupOptions,
                                               FSSBackupItemPathTypeEnum pathType,
                                                String groupName,
                                               boolean exclude)
    {
        super(pathString, backupOptions, pathType, groupName, exclude);
    }
    
     /**
     * Return the String representation of this configuration objection
     * 
     * @return
     */
    @Override
    public String toString()
    {
        return toString(0);
    }
    
    /**
     *
     * @param indent
     * @return
     */
    @Override
    public String toString(int indent)
    {
        String res;
        String indentStr = "";
        
        if( indent > 0 )
        {
            indentStr = StringUtils.repeat(" ", indent);
        }
        
        StringBuilder sb = new StringBuilder();
        
        sb.append( String.format("%sBACKUPCONFIGFILE\n", indentStr) );
        
        sb.append( String.format("%s", super.toString(indent+2)));
        
        //sb.append( String.format("%s  SERVICE URL  : '%s'\n", indentStr, serviceUrl) );
        
        res = sb.toString();
        
        return res;
    }

    @Override
    public FSSValidateConfigResult validate()
    {
        FSSValidateConfigResult res;

        res = super.validate();

        return res;
    }
}
