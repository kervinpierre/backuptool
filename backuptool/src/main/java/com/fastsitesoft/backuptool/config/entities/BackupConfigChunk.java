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

import com.fastsitesoft.backuptool.enums.FSSBackupSizeType;
import com.fastsitesoft.backuptool.enums.FSSConfigMembersEnum;
import com.fastsitesoft.backuptool.enums.FSSValidateResultCodeEnum;
import com.fastsitesoft.backuptool.enums.FSSValidateResultErrorEnum;
import com.fastsitesoft.backuptool.utils.BackupToolException;
import com.fastsitesoft.backuptool.utils.FSSValidateConfigError;
import java.util.ArrayList;
import java.util.List;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Kervin Pierre <info@sludev.com>
 */
public final class BackupConfigChunk
{
    private static final Logger log 
                        = LogManager.getLogger(BackupConfigChunk.class);
    
    private final long size;
    private final boolean enable;
    private final FSSBackupSizeType sizeType;
    
    public boolean isEnabled()
    {
        return enable;
    }
    
    public long getSize()
    {
        return size;
    }
    
    public FSSBackupSizeType getSizeType()
    {
        return sizeType;
    }

    public BackupConfigChunk(final long s, final FSSBackupSizeType t, final boolean en)
    {
        enable   = en;
        size     = s;
        sizeType = t;
    }

    public static BackupConfigChunk from( Element el ) throws BackupToolException
    {
        BackupConfigChunk res;
        FSSBackupSizeType sizeType = FSSBackupSizeType.NONE;
        boolean enable = true;
        long size = 0;

        NodeList currElList;
        Element currEl;
          
        NamedNodeMap currAttrs;
        Node currAttr;
        
        XPathFactory currXPathfactory = XPathFactory.newInstance();
        XPath currXPath = currXPathfactory.newXPath();

        if( el == null )
        {
            log.debug("Chunk element is null parsing config file.");

            throw new BackupToolException("Chunk element is null parsing config file.");
        }

        currAttrs = el.getAttributes();
        
        try
        {
            String currChunkSizeStr = currXPath.compile("./size").evaluate(el);
            size = Integer.parseInt(currChunkSizeStr);
        }
        catch (XPathExpressionException ex)
        {
            log.debug(ex);
        }
        
        try
        {
            String currChunkSizeTypeStr = currXPath.compile("./sizeType").evaluate(el);
            sizeType = FSSBackupSizeType.from(currChunkSizeTypeStr);
        }
        catch (XPathExpressionException ex)
        {
            log.debug(ex);
        }
        
        currAttr = currAttrs.getNamedItem("enable");
        if( currAttr != null )
        {
            String currEnableStr = currAttr.getTextContent();
            enable = Boolean.parseBoolean(currEnableStr);
        }

        res = new BackupConfigChunk(size, sizeType, enable);

        return res;
    }
        
    /**
     * Confirm this configuration may run without errors.
     * 
     * @return 
     */
    public static List<FSSValidateConfigError> validate(BackupConfigChunk conf)
    {
        List<FSSValidateConfigError> res = new ArrayList<>();
        
        // Check enable
        if( !conf.enable )
        {
            FSSValidateConfigError currErr = new FSSValidateConfigError(
                    FSSValidateResultCodeEnum.WARNING,
                    String.format("Invalid 'Chunk Enable' value '%b'", conf.enable),
                    conf.enable, FSSConfigMembersEnum.BACKUPCONFIGCHUNK_ENABLE,
                    FSSValidateResultErrorEnum.INVALID_VALUE);
            
            res.add(currErr);
        }

        // Check size
        if( conf.size < 1 )
        {
            FSSValidateConfigError currErr = new FSSValidateConfigError(
                    FSSValidateResultCodeEnum.WARNING, 
                    String.format("Invalid 'Chunk Size' value '%d'", conf.size),
                    conf.size, FSSConfigMembersEnum.BACKUPCONFIGCHUNK_SIZE,
                    FSSValidateResultErrorEnum.INVALID_VALUE);
            
            res.add(currErr);
        }
        
        // Check size type
        switch(conf.sizeType)
        {
            case B:
                break; 
                
            case KB:
                break;
                
            case MB:
                break;
                
            case GB:
                break;
                
            case TB:
                break;
                     
            default:
                FSSValidateConfigError currErr = new FSSValidateConfigError(
                        FSSValidateResultCodeEnum.WARNING,
                        String.format("Invalid 'Chunk Size Type' value '%s'", conf.sizeType),
                        conf.sizeType, FSSConfigMembersEnum.BACKUPCONFIGCHUNK_SIZETYPE,
                        FSSValidateResultErrorEnum.INVALID_VALUE);

                res.add(currErr);
                break;
        }
        
        return res;
    }
    
    public long getSizeBytes() throws BackupToolException
    {
        long res = 0;
        
        switch(sizeType)
        {
            case B:
                res = size;
                break;
            
            case KB:
                res = size * 1000;
                break;
                
            case MB:
                res = size * 1000 * 1000;
                break;
                
            case GB:
                res = size * 1000 * 1000 * 1000;
                break;
                
            case TB:
                res = size * 1000 * 1000 * 1000 * 1000;
                break;
                
            default:
                String errMsg = String.format("Invalid size type '%s", sizeType);
                log.info(errMsg);
                throw new BackupToolException(errMsg);
        }
        
        return res;
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
    
    public String toString(int indent)
    {
        String res;
        String indentStr = "";
        
        if( indent > 0 )
        {
            indentStr = StringUtils.repeat(" ", indent);
        }
        
        StringBuilder sb = new StringBuilder();
        
        sb.append( String.format("%sBACKUPCONFIGCHUNK\n", indentStr) );
        sb.append( String.format("%s  SIZE           : '%d'\n", indentStr, size) );
        sb.append( String.format("%s  ENABLE         : '%b'\n", indentStr, enable) );
        sb.append( String.format("%s  SIZE TYPE      : '%s'\n", indentStr, sizeType) );
        res = sb.toString();
        
        return res;
    }
}
