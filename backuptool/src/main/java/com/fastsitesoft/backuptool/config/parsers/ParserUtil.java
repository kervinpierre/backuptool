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

import com.fastsitesoft.backuptool.enums.BackupToolFileFormats;
import com.fastsitesoft.backuptool.utils.BackupToolException;
import com.fastsitesoft.backuptool.utils.FSSConfigurationFile;
import com.fastsitesoft.backuptool.utils.FSSLSResourceResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by kervin on 2015-08-24.
 */
public final class ParserUtil
{
    private static final Logger log = LogManager.getLogger(ParserUtil.class);

    /**
     * Read and validate a configuration file.
     *
     * @param confPath
     * @return
     * @throws BackupToolException
     */
    public static Document readConfig(Path confPath, BackupToolFileFormats type)
            throws BackupToolException
    {
        FSSConfigurationFile conf = new FSSConfigurationFile();
        Document doc = conf.read(confPath);

        SchemaFactory factory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        // associate the schema factory with the resource resolver, which is responsible for resolving the imported XSD's
        factory.setResourceResolver(new FSSLSResourceResolver());

        // note that if your XML already declares the XSD to which it has to conform, then there's no need to from a validator from a Schema object
        Source schemaFile = new StreamSource(ParserUtil.class.getClassLoader()
                .getResourceAsStream(BackupToolFileFormats.getSchema(type)));

        Schema schema;
        try
        {
            schema = factory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            try
            {
                validator.validate(new DOMSource(doc));
            }
            catch( SAXException ex )
            {
                String errMsg = String.format("Error parsing configuration document.");

                log.error(errMsg, ex);
                throw new BackupToolException(errMsg, ex);
            }
            catch( IOException ex )
            {
                String errMsg = String.format("Error reading configuration document on disk.");

                log.error(errMsg, ex);
                throw new BackupToolException(errMsg, ex);
            }
        }
        catch( SAXException ex )
        {
            String errMsg = String.format("Error parsing schema document.");

            log.error(errMsg, ex);
            throw new BackupToolException(errMsg, ex);
        }

        return doc;
    }
}
