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

package com.fastsitesoft.backuptool.config.parsers;

import com.fastsitesoft.backuptool.config.entities.JobArchiveSummary;
import com.fastsitesoft.backuptool.config.entities.JobState;
import com.fastsitesoft.backuptool.utils.BackupToolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Created by kervin on 2015-09-28.
 */
public final class JobArchiveSummaryParser
{
    private static final Logger log = LogManager.getLogger(JobArchiveSummaryParser.class);

    public static JobArchiveSummary readConfig(Element rootElem)
            throws BackupToolException
    {
        JobArchiveSummary res = null;

        String currEncryptionType = null;
        String currPath = null;
        String currListingFile = null;
        String currOrderId = null;
        String currCreateStart = null;
        String currCreateEnd = null;
        String currCompressType = null;

        XPathFactory currXPathfactory = XPathFactory.newInstance();
        XPath currXPath = currXPathfactory.newXPath();

        try
        {
            currPath = currXPath.compile("./path").evaluate(rootElem);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            currListingFile = currXPath.compile("./listingFile").evaluate(rootElem);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            currOrderId = currXPath.compile("./orderId").evaluate(rootElem);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            currCreateStart = currXPath.compile("./createStart").evaluate(rootElem);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            currCreateEnd = currXPath.compile("./createEnd").evaluate(rootElem);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            currPath = currXPath.compile("./path").evaluate(rootElem);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            currEncryptionType = currXPath.compile("./encryptionType").evaluate(rootElem);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            currCompressType = currXPath.compile("./compressionType").evaluate(rootElem);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        res = JobArchiveSummary.from(currPath,
                currListingFile,
                currOrderId,
                currCreateStart,
                currCreateEnd,
                currCompressType,
                currEncryptionType);

        return res;
    }
}
