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

import com.fastsitesoft.backuptool.config.entities.JobSetState;
import com.fastsitesoft.backuptool.config.entities.JobState;
import com.fastsitesoft.backuptool.utils.BackupToolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * read a Job Set State Configuration.
 *
 * Created by kervin on 2015-08-12.
 */
public final class JobSetStateParser
{
    private static final Logger log = LogManager.getLogger(JobSetStateParser.class);

    public static JobSetState readConfig(Document doc)
            throws BackupToolException
    {
        JobSetState res = null;

        Element currEl = doc.getDocumentElement();
        NodeList currElList;
        Element tempEl;

        String currJobSetIdStr = null;
        String currLastSuccessfulJobIdStr = null;
        String currLastJobIdStr = null;
        String currPath = null;
        Map<UUID, JobState> currJobs = new HashMap<>();

        XPathFactory currXPathfactory = XPathFactory.newInstance();
        XPath currXPath = currXPathfactory.newXPath();

        try
        {
            currJobSetIdStr = currXPath.compile("./jobSetId").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            currLastSuccessfulJobIdStr = currXPath.compile("./lastSuccessfulJobId").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            currLastJobIdStr = currXPath.compile("./lastJobId").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            currPath = currXPath.compile("./path").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            // Get the list of jobs
            currElList = (NodeList)currXPath.compile("./jobs/job").evaluate(
                    doc.getDocumentElement(), XPathConstants.NODESET);

            if( currElList != null && currElList.getLength() > 0)
            {
                for(int i=0; i<currElList.getLength(); i++)
                {
                    tempEl = (Element)currElList.item(i);
                    JobState tempJS = JobStateParser.readConfig(tempEl);
                    currJobs.put(tempJS.getJobId(), tempJS);
                }
            }
        }
        catch (XPathExpressionException ex)
        {
            String errMsg = "Failed processing jobs in Job Set State File.";

            log.debug(errMsg, ex);
            throw new BackupToolException(errMsg, ex);
        }

        res = JobSetState.from(currJobSetIdStr,
                currLastJobIdStr,
                currLastSuccessfulJobIdStr,
                currPath,
                currJobs
        );

        return res;
    }
}
