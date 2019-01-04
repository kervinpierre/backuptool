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
import com.fastsitesoft.backuptool.config.entities.JobError;
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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * read a Job State Configuration.
 *
 * Created by kervin on 2015-08-12.
 */
public final class JobStateParser
{
    private static final Logger log = LogManager.getLogger(JobStateParser.class);

    public static JobState readConfig(Document doc)
            throws BackupToolException
    {
        Element currElem = doc.getDocumentElement();

        return readConfig(currElem);
    }

    public static JobState readConfig(Element rootElem)
            throws BackupToolException
    {
        JobState res = null;
        NodeList currElList;
        Element tempEl;

        String currTypeStr = null;
        String currJobIdStr = null;
        String currPathStr = null;
        String currStartStr = null;
        String currEndStr = null;
        String currSystemIdStr = null;
        String currDispositionStr = null;
        String currStatusStr = null;
        String currJobSetStr = null;
        String currPrevJobStr = null;
        String currJobErrorsFile = null;
        String currHashStr = null;
        Map<Path, JobArchiveSummary> jobArchivesSet = new HashMap<>();
        List<JobError> jobErrors = null;

        XPathFactory currXPathfactory = XPathFactory.newInstance();
        XPath currXPath = currXPathfactory.newXPath();

        try
        {
            currTypeStr = currXPath.compile("./type").evaluate(rootElem);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            currJobIdStr = currXPath.compile("./jobId").evaluate(rootElem);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            currPathStr = currXPath.compile("./path").evaluate(rootElem);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            currStartStr = currXPath.compile("./start").evaluate(rootElem);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            currEndStr = currXPath.compile("./end").evaluate(rootElem);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            currSystemIdStr = currXPath.compile("./systemId").evaluate(rootElem);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            currDispositionStr = currXPath.compile("./disposition").evaluate(rootElem);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            currStatusStr = currXPath.compile("./status").evaluate(rootElem);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            currJobSetStr = currXPath.compile("./jobSetId").evaluate(rootElem);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            currPrevJobStr = currXPath.compile("./previousJobId").evaluate(rootElem);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            currJobErrorsFile = currXPath.compile("./errorsFilePath").evaluate(rootElem);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            currHashStr = currXPath.compile("./hashType").evaluate(rootElem);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("", ex);
        }

        try
        {
            // Get the list of jobs
            currElList = (NodeList)currXPath.compile("./archives/archiveSummary").evaluate(
                    rootElem, XPathConstants.NODESET);

            if( currElList != null && currElList.getLength() > 0)
            {
                for(int i=0; i<currElList.getLength(); i++)
                {
                    tempEl = (Element)currElList.item(i);
                    JobArchiveSummary tempJS = JobArchiveSummaryParser.readConfig(tempEl);
                    jobArchivesSet.put(tempJS.getPath(), tempJS);
                }
            }
        }
        catch (XPathExpressionException ex)
        {
            String errMsg = "Failed processing jobs in Job Set State File.";

            log.debug(errMsg, ex);
            throw new BackupToolException(errMsg, ex);
        }

        List<JobArchiveSummary> jobArchives = new ArrayList<>();
        jobArchives.addAll(jobArchivesSet.values());

        res = JobState.from(null,
                currJobIdStr,
                currPathStr,
                currStartStr,
                currEndStr,
                currTypeStr,
                currSystemIdStr,
                currStatusStr,
                currDispositionStr,
                currJobSetStr,
                currJobErrorsFile,
                currPrevJobStr,
                currHashStr,
                jobArchives,
                jobErrors
        );

        return res;
    }
}
