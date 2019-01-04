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
import com.fastsitesoft.backuptool.utils.BackupToolException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.fastsitesoft.backuptool.utils.FSSValidateConfigResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The configuration object from backup File-System items.
 * 
 * @author Kervin Pierre <info@sludev.com>
 */
public class BackupConfigItem
{
    private static final Logger log 
                        = LogManager.getLogger(BackupConfigItem.class);
    
    private final String pathString;
    private final Set<FSSBackupItemBackupOption> backupOptions;
    private final FSSBackupItemPathTypeEnum pathType;
    private final String groupName;
    private final boolean exclude;

    
    /**
     * This is the string representation of a path retrieved from a configuration.
     * It does not represent a "Path" object because it hasn't necessarily been
     * validated.
     * 
     * @return 
     */
    public String getPathString()
    {
        return pathString;
    }
    
    public Set<FSSBackupItemBackupOption> getBackupOptions()
    {
        return backupOptions;
    }
    
    public FSSBackupItemPathTypeEnum getPathType()
    {
        return pathType;
    }

    public String getGroupName()
    {
        return groupName;
    }

    public boolean isExclude()
    {
        return exclude;
    }
       
    /**
     * Create a new BackupConfigItem.
     * 
     * @param pathString The path to the backup item.  Can be a regex.
     * @param backupOptions Options used during calls
     * @param pathType Literal path or Regex.
     * @Param groupName
     * @param exclude Should we include this path or exclude it from the backup?
     */
    protected BackupConfigItem(String pathString,
                            Set<FSSBackupItemBackupOption> backupOptions,
                            FSSBackupItemPathTypeEnum pathType,
                               String groupName,
                            boolean exclude)
    {
        this.pathString = pathString;

        this.groupName = groupName;

        if( backupOptions == null )
        {
            this.backupOptions = new HashSet<>();
        }
        else
        {
            this.backupOptions = backupOptions;
        }

        this.pathType   = pathType;

        this.exclude    = exclude;
    }

    /**
     * Create a configuration File-System Item from an XML Element.
     * 
     * @param <T>
     * @param el
     * @return
     * @throws BackupToolException
     */
    public final static <T extends BackupConfigItem> T from(final Element el) throws BackupToolException
    {
        T res;
          
        String currbackupOptionsStr = null;
        String currGroupName = null;
        String currPathTypeStr = null;       
        String currPathStr = null;
        
        Set<FSSBackupItemBackupOption> currbackupOptions = new HashSet<>();
        FSSBackupItemPathTypeEnum currPathType = FSSBackupItemPathTypeEnum.NONE;
        boolean currExclude = false;
                            
        XPathFactory currXPathfactory = XPathFactory.newInstance();
        XPath currXPath = currXPathfactory.newXPath();
        
        NamedNodeMap currAttrs;
        Node currAttr;
        
        currPathStr = el.getTextContent();
        
        currAttrs = el.getAttributes();

        /**
         * TODO : Multiple/repeatable attributes
         */
        currAttr = currAttrs.getNamedItem("backupOptions");
        if( currAttr != null )
        {
            currbackupOptionsStr = currAttr.getTextContent();
        }

        currAttr = currAttrs.getNamedItem("groupName");
        if( currAttr != null )
        {
            currGroupName = currAttr.getTextContent();
        }

        try
        {
            Element tempEl = (Element)currXPath.compile("./path").evaluate(el, XPathConstants.NODE);
            currPathStr = tempEl.getTextContent();
            
            NamedNodeMap tempAttrs = tempEl.getAttributes();
            currAttr = tempAttrs.getNamedItem("type");
            if( currAttr != null )
            {
                currPathTypeStr = currAttr.getTextContent();
            }
            
            currAttr = tempAttrs.getNamedItem("exclude");
            if( currAttr != null )
            {
                currExclude = Boolean.parseBoolean( 
                        StringUtils.trim(
                                StringUtils.upperCase( currAttr.getTextContent() )));
            }
        }
        catch (XPathExpressionException ex)
        {
            log.debug(ex);
        }

        List<BackupConfigDirectory> currChildDirs = new ArrayList<>();
        
        // Nested relative directories
        NodeList currElList = el.getElementsByTagName("directory");
        if( currElList != null && currElList.getLength() > 0)
        {
            for(int i=0; i<currElList.getLength(); i++)
            {

                Element tempEl = (Element)currElList.item(i);
                if( tempEl.getNodeType() == Node.ELEMENT_NODE )
                {
                    BackupConfigDirectory currItem = from(tempEl);
                    currChildDirs.add(currItem);
                }
            }
        }

        List<BackupConfigFile> currChildFiles = new ArrayList<>();
        
        // Nested relative files
        currElList = el.getElementsByTagName("file");
        if( currElList != null && currElList.getLength() > 0)
        {
            for(int i=0; i<currElList.getLength(); i++)
            {

                Element tempEl = (Element)currElList.item(i);
                if( tempEl.getNodeType() == Node.ELEMENT_NODE )
                {
                    BackupConfigFile currItem = from(tempEl);
                    currChildFiles.add( currItem );
                }
            }
        }
        
        if( StringUtils.isNoneBlank(currPathTypeStr) )
        {
           currPathType = FSSBackupItemPathTypeEnum.valueOf(
                   StringUtils.upperCase(currPathTypeStr));
        }
        
        if( StringUtils.isNoneBlank(currbackupOptionsStr) )
        {
            /**
             * FIXME : This should be an addAll()
             */
            currbackupOptions.add(FSSBackupItemBackupOption.valueOf(
                    StringUtils.upperCase(currbackupOptionsStr)));
        }

        String currTagName = StringUtils.trim(StringUtils.lowerCase(el.getTagName()));
        switch(currTagName)
        {
            case "directory":
                res = (T)new BackupConfigDirectory(currPathStr,
                        currbackupOptions, currPathType, currChildDirs,
                        currChildFiles, currGroupName, currExclude );
                break;
                
            case "file":
                res = (T)new BackupConfigFile(currPathStr, currbackupOptions,
                        currPathType, currGroupName, currExclude );
                break;
                
            default:
                throw new BackupToolException(
                        String.format("Invalid tag '%s' when creating configuration item.", currTagName));
        }
        
        return res;
    }

    public FSSValidateConfigResult validate()
    {
        FSSValidateConfigResult res = new FSSValidateConfigResult();

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
        
        sb.append( String.format("%sBACKUPCONFIGITEM\n", indentStr) );
        sb.append(String.format("%s  PATH  : '%s'\n", indentStr, pathString) );
        
        res = sb.toString();
        
        return res;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) 
        { 
            return false; 
        }
        
        if (obj == this) 
        { 
            return true; 
        }
        
        // getClass() so it checks inheritance.
        // No cross-class equals()
        if (obj.getClass() != getClass()) 
        {
            return false;
        }
   
        boolean res;
        BackupConfigItem rhs = (BackupConfigItem)obj;
        
        EqualsBuilder eb = new EqualsBuilder();
        
        res = eb.append(pathString, rhs.pathString)
                .append(pathType, rhs.pathType)
                .append(backupOptions, rhs.backupOptions)
                .isEquals();
        
        return res;
    }
    
    @Override
    public int hashCode()
    {
        HashCodeBuilder hcb = new HashCodeBuilder();
        
        int res = hcb.append(pathString)
                        .append(pathType)
                        .append(backupOptions)
                        .toHashCode();
        
        return res;
    }
}
