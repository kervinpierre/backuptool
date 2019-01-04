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

import com.fastsitesoft.backuptool.utils.BackupToolException;
import java.net.URI;
import java.net.URISyntaxException;
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
public final class BackupConfigStorageBackend
{
    private static final Logger log 
                      = LogManager.getLogger(BackupConfigStorageBackend.class);
    
    private final Boolean enable;
    private final String user;
    private final String pass;
    private final URI url;
    
    public URI getUrl()
    {
        return url;
    }

    /**
     * Create a storage backend configuration object.
     *
     * @param ur URL of storage backend.
     * @param us Username credential if needed.  Otherwise empty string.
     * @param ps Password credential if needed.  Otherwise empty string.
     * @param en Is the backend enabled?
     * @return
     * @throws BackupToolException
     */
    public static BackupConfigStorageBackend from(String ur, String us, String ps, String en) throws BackupToolException
    {
        boolean enb = Boolean.parseBoolean(en);
        return from(ur, us, ps, enb);
    }

    /**
     * Create a storage backend configuration object.
     *
     * @param ur URL of storage backend.
     * @param us Username credential if needed.  Otherwise empty string.
     * @param ps Password credential if needed.  Otherwise empty string.
     * @param en Is the backend enabled?
     * @return
     * @throws BackupToolException
     */
    public static BackupConfigStorageBackend from(String ur, String us, String ps, Boolean en) throws BackupToolException
    {
        BackupConfigStorageBackend res = null;
        URI currUri;

        try
        {
            currUri = new URI(ur);

            res = new BackupConfigStorageBackend(currUri, us, ps, en);

        }
        catch (URISyntaxException ex)
        {
            throw new BackupToolException( String.format("Error setting the URL '%s'.", ur), ex);
        }

        return res;
    }
    
    public Boolean getEnable()
    {
        return enable;
    }
    
    public String getUser()
    {
        return user;
    }

    public String getPass()
    {
        return pass;
    }

    public BackupConfigStorageBackend(URI ur, String u, String p, boolean en)
    {
        enable = en;
        user = u;
        pass = p;
        url = ur;
    }
    
    public static BackupConfigStorageBackend from( Element el ) throws BackupToolException
    {
        BackupConfigStorageBackend res;

        NodeList currElList;
        Element currEl;

        NamedNodeMap currAttrs;
        Node currAttr;
        
        XPathFactory currXPathfactory = XPathFactory.newInstance();
        XPath currXPath = currXPathfactory.newXPath();
        
        currAttrs = el.getAttributes();

        String currUserStr = null;
        try
        {
            currUserStr = currXPath.compile("./user").evaluate(el);
        }
        catch (XPathExpressionException ex)
        {
            log.debug(ex);
        }

        String currPassStr = null;
        try
        {
            currPassStr = currXPath.compile("./pass").evaluate(el);
        }
        catch (XPathExpressionException ex)
        {
            log.debug(ex);
        }

        String currUrlStr = null;
        try
        {
            currUrlStr = currXPath.compile("./url").evaluate(el);
        }
        catch (XPathExpressionException ex)
        {
            log.debug(ex);
        }

        String currEnableStr = null;
        currAttr = currAttrs.getNamedItem("enable");
        if( currAttr != null )
        {
            currEnableStr = currAttr.getTextContent();
        }

        res = from(currUrlStr, currUserStr, currPassStr, currEnableStr);

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
        
        sb.append( String.format("%sBACKUPCONFIGSTORAGEBACKEND\n", indentStr) );
        sb.append( String.format("%s  USER           : '%s'\n", indentStr, user) );
        sb.append( String.format("%s  ENABLE         : '%b'\n", indentStr, enable) );
        sb.append( String.format("%s  URL            : '%s'\n", indentStr, url) );
        res = sb.toString();
        
        return res;
    }
}
