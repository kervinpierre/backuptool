/*
 *  BackupLogic LLC CONFIDENTIAL
 *  DO NOT COPY
 *
 * Copyright (c) [2012] - [2019] BackupLogic LLC <info@backuplogic.com>
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 *  the property of BackupLogic LLC and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to BackupLogic LLC and its suppliers and
 *  may be covered by U.S. and Foreign Patents, patents in process,
 *  and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from BackupLogic LLC
 */
package com.fastsitesoft.backuptool.main;

import com.fastsitesoft.backuptool.config.builders.BackupConfigBuilder;
import com.fastsitesoft.backuptool.config.entities.BackupConfig;
import com.fastsitesoft.backuptool.config.entities.UsageConfig;
import com.fastsitesoft.backuptool.config.parsers.BackupConfigParser;
import com.fastsitesoft.backuptool.config.parsers.ParserUtil;
import com.fastsitesoft.backuptool.constants.BackupConstants;
import com.fastsitesoft.backuptool.enums.BackupToolFileFormats;
import com.fastsitesoft.backuptool.utils.BackupToolException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.logging.log4j.LogManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Entry class for initializing the agent on start via various interfaces.  This
 * class handles all command line processing for the application, converting the
 * command line options to "configuration objects and entities" that the rest of
 * the application uses.
 * 
 * @author Kervin Pierre <kervinpierre@gmail.com>
 */
public class BackupToolInitialize
{
    private static final org.apache.logging.log4j.Logger LOGGER
                                = LogManager.getLogger(BackupToolInitialize.class);
    
    /**
     * Main initialization method.  This method is responsible for parsing the
     * command line arguments and building entities in memory that represent
     * these user options.
     * 
     * @param args Command line arguments originally from the static main method.
     * @return Configuration object representing the entire application configuration.
     */
    public static BackupConfig initialize(String[] args)
    {
        LOGGER.debug( String.format("Initialize() started. %s\n", Arrays.toString(args)) );
        
        CommandLineParser parser = new DefaultParser();
        Options options = ConfigureOptions();

        BackupConfigBuilder backupOptsBuilder = BackupConfigBuilder.from();

        BackupConfig backupOpts = null;

        try
        {
            // Get the command line argument list from the OS
            CommandLine line;
            try
            {
                line = parser.parse(options, args);
            }
            catch (ParseException ex)
            {
                throw new BackupToolException( 
                        String.format("Error parsing command line.'%s'", 
                                ex.getMessage()), ex);
            }
            
            // First check for an argument file, so it can override command
            // line arguments from above.
            //
            // NB: You can't use command line arguments AND argfile at the same
            //     time
            if (line.hasOption("argfile"))
            {
                String argfile = line.getOptionValue("argfile");
                
                try
                {
                    BufferedReader reader = new BufferedReader( new FileReader(argfile));
                    String argLine = "";
                    
                    do
                    {
                        argLine = reader.readLine();
                        if( argLine != null )
                        {
                            argLine = argLine.trim();
                        }
                    }
                    while(  argLine != null 
                                && (argLine.length() < 1 ||  argLine.startsWith("#")) );
                    
                    String[] argArray = new String[0];
                    
                    if( argLine != null )
                    {
                        argArray = argLine.split("\\s+");
                    }
                    
                            
                    LOGGER.debug( String.format("Initialize() : Argfile parsed : %s\n",
                            Arrays.toString(argArray)) );
        
                    try
                    {
                        line = parser.parse(options, argArray);
                    }
                    catch (ParseException ex)
                    {
                        throw new BackupToolException( 
                            String.format("Error parsing command line.'%s'", 
                                ex.getMessage()), ex);
                    }
                }
                catch (FileNotFoundException ex)
                {
                     LOGGER.error( "Error : File not found :", ex);
                    throw new BackupToolException("ERROR: Argument file not found.", ex);
                }
                catch (IOException ex)
                {
                     LOGGER.error( "Error : IO : ", ex);
                    throw new BackupToolException("ERROR: Argument file can not be read.", ex);
                }
            }
            
            if (line.hasOption("conf-file"))
            {
                Path confFile = Paths.get(  line.getOptionValue("conf-file"));
                
                backupOptsBuilder.merge( BackupConfigParser.readConfig(
                        ParserUtil.readConfig(confFile,
                                BackupToolFileFormats.BACKUPCONFIGURATION)) );
            }
            
            if( line.getOptions().length < 1 )
            {
                // At least one option is mandatory
                throw new BackupToolException("No program arguments were found.");
            }

            // Argument order can be important. We may be creating THEN changing a folder's attributes.
            // It would be important to from the folder first.
            Iterator cmdI = line.iterator();
            while( cmdI.hasNext())
            {
                Option currOpt = (Option)cmdI.next();
                String currOptName = currOpt.getLongOpt();

                switch( currOptName )
                {      
                    case "verbose":
                        backupOptsBuilder.setVerbosity(currOpt.getValue());
                        break;
                     
                    case "service":
                        backupOptsBuilder.setRunAsService(true);
                        break;
                        
                    case "version":
                        backupOptsBuilder.setDisplayVersion(true);
                        break;
                        
                    case "dry-run":
                        backupOptsBuilder.setDryRun(true);
                        break;
                        
                    case "output-redirect":
                        backupOptsBuilder.setOutputFile(currOpt.getValue());
                        break;
                        
                    case "run-backup":
                        // Run the Backup utility
                        backupOptsBuilder.setBackup(true);
                        break;
                        
                    case "run-restore":
                        // Run the Backup Restore utility
                        backupOptsBuilder.setRestore(true);
                        break;
                        
                    case "backup-type":
                        backupOptsBuilder.setSetType(currOpt.getValue());
                        break;
                     
                    case "backup-setname":
                        backupOptsBuilder.setSetName(currOpt.getValue());
                        break;
                        
                    case "backup-location-url":
                        {
                           // BackupConfigStorageBackend currBackend
                           //             = backupOpts.getStorageBackend();
                           // currBackend.setUrl(currOpt.getValue());

                           /* currBackendURL = currOpt.getValue(); */
                        }
                        break;
                        
                    case "backup-id":
                        backupOptsBuilder.setBackupId(currOpt.getValue());
                        break;
                        
                    case "backup-folder":
                        backupOptsBuilder.setDirList(currOpt.getValue());
                        break;

                    case "backup-holding-folder":
                        backupOptsBuilder.setHoldingDirectory(currOpt.getValue());
                        break;
                        
                    case "backup-file":
                        backupOptsBuilder.setFileList(currOpt.getValue());
                        break;

                    case "archive-name-regex":
                        backupOptsBuilder.setArchiveFileNamePattern(currOpt.getValue());
                        break;

                    case "archive-name-component":
                        backupOptsBuilder.setArchiveFileNameComponent(currOpt.getValue());
                        break;

                    case "archive-name-template":
                        backupOptsBuilder.setArchiveFileNameTemplate(currOpt.getValue());
                        break;

                    case "job-name-regex":
                        backupOptsBuilder.setJobFileNamePattern(currOpt.getValue());
                        break;

                    case "job-name-component":
                        backupOptsBuilder.setJobFileNameComponent(currOpt.getValue());
                        break;

                    case "job-name-template":
                        backupOptsBuilder.setJobFileNameTemplate(currOpt.getValue());
                        break;

                    case "use-checksum":
                        backupOptsBuilder.setUseChecksum(true);
                        break;
                        
                    case "checksum":
                        backupOptsBuilder.setChecksumType(currOpt.getValue());
                        break;
                        
                    case "use-modified-date":
                        backupOptsBuilder.setUseModifiedDate(true);
                        break;
                        
                    case "chunk-size":
                        {
//                            BackupConfigChunk currChunk = backupOpts.getChunk();
//                            long size = Long.parseLong(currOpt.getValue());
//                            currChunk = new BackupConfigChunk(size, FSSBackupSizeType.B,true);
//                            backupOpts.setChunk(currChunk);
                            try
                            {
                                String tempChunk = currOpt.getValue();
                                Pattern p = Pattern.compile("([0-9]+)([a-zA-Z]+)");
                                Matcher m = p.matcher(tempChunk.trim());

                                backupOptsBuilder.setChunk(m.group(1), m.group(2));
                            }
                            catch(Exception ex)
                            {
                                throw new BackupToolException("Error parsing chunk definition", ex);
                            }
                        }
                        break;
                        
                    case "encryption-cipher":
                        backupOptsBuilder.setEncryptionCipher(currOpt.getValue());
                        break;

                    case "encryption-key":
                        backupOptsBuilder.setEncryptionKey(currOpt.getValue());
                        break;

                    case "backup-compression-type":
                        backupOptsBuilder.setCompression(currOpt.getValue());
                        break;
                        
                    case "backup-report-type":
                        backupOptsBuilder.setBackupReportType(currOpt.getValue());
                        break;
                        
                    case "backup-report-path":
                        backupOptsBuilder.setBackupReportPath(currOpt.getValue());
                        break;
                        
                    case "restore-destination":
                        backupOptsBuilder.setRestoreDestination(currOpt.getValue());
                        break;
                        
                    case "no-preserve-permissions":
                        backupOptsBuilder.setPreservePermissions(false);
                        break;
                        
                    case "no-preserve-ownership":
                        backupOptsBuilder.setPreserveOwnership(false);
                        break;
                        
                    case "no-clobber":
                        backupOptsBuilder.setNoClobber(true);
                        break;

                    case "ignore-lock":
                        backupOptsBuilder.setIgnoreLock(true);
                        break;

                    case "describe":
                        backupOptsBuilder.setBackupDescribe(true);
                        break;
                        
                    case "status":
                        backupOptsBuilder.setBackupStatus(true);
                        break;
                }
            }

            // Do some manual checking to the provided options
            // because CLI options groups are a bit of a pain.
            if( BooleanUtils.isNotTrue(backupOptsBuilder.getRestore())
                    && BooleanUtils.isNotTrue(backupOptsBuilder.getBackup())
                    && BooleanUtils.isNotTrue(backupOptsBuilder.isDisplayVersion())
                    && BooleanUtils.isNotTrue(backupOptsBuilder.isDisplayUsage()) )
            {
                // None of the core functions were selected
                throw new BackupToolException("Expected a core function to be specified.  Please review your command line options. 'Backup'? 'Restore'? 'Usage'?");
            }

            LOGGER.debug("Merging Command Line Arguments...");

            backupOpts = backupOptsBuilder.toConfig();
        }
        catch (BackupToolException ex)
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            
            pw.append(String.format("Error : '%s'\n\n", ex.getMessage()));
            
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( pw, 80,
                                     String.format("\njava -jar backuptool-%s.jar ", BackupConstants.PROD_VERSION), 
                                        "\nThe backuptool application can be used in a variety of options and modes.\n", options,
                                        0, 2, "© BackupLogic LLC All Rights Reserved.",
                                        true);

            final UsageConfig currUsageConf = new UsageConfig(sw.toString(), 1, null);

            backupOpts = BackupConfigBuilder.toUsageConfig(currUsageConf);
        }

        LOGGER.debug( String.format("Initialize() end.\n") );
        
        return backupOpts;
    }

    private static Options ConfigureOptions()
    {     
        Options options = new Options();
        
        options.addOption( Option.builder().longOpt( "version" )
                                .desc( "Display version information." )
                                .build() );
        
        options.addOption( Option.builder().longOpt( "run-backup" )
                                .desc( "Run the backup described in the config file." )
                                .build() );
        
        options.addOption( Option.builder().longOpt( "backup-type" )
                                .desc("The current backup type.")
                                .hasArg()
                                .argName("BACKUPTYPE")
                                .build() );
        
        options.addOption( Option.builder().longOpt( "backup-setname" )
                                .desc("Name of the current backup set.")
                                .hasArg()
                                .argName("BACKUPSETNAME")
                                .build() );
        
        options.addOption( Option.builder().longOpt( "backup-location-url" )
                                .desc( "When creating a backup this is the path to the existing or new backup."
                                        + "When restoring from backup, this flag should point to an existing backup." )
                                .hasArg()
                                .argName("BACKUPLOCATIONURL")
                                .build() );
        
        options.addOption( Option.builder().longOpt( "backup-id" )
                                .desc( "This is a unique ID relating to all backups at a specified location."
                                + "  Please note this ID is not guaranteed to be unique across all backups in entirety.")
                                .hasArg()
                                .argName("BACKUPID")
                                .build() );
        
        options.addOption( Option.builder().longOpt( "backup-folder" )
                                .desc( "A backup folder definition." )
                                .hasArg()
                                .argName("BACKUPFOLDER")
                                .build() );

        options.addOption( Option.builder().longOpt( "backup-file" )
                                .desc( "A backup file definition." )
                                .hasArg()
                                .argName("BACKUPFILE")
                                .build() );

        options.addOption( Option.builder().longOpt( "backup-holding-folder" )
                                .desc( "A folder for holding temporary files." )
                                .hasArg()
                                .argName("BACKUPHOLDINGFOLDER")
                                .build() );
        
        options.addOption( Option.builder().longOpt("use-checksum")
                                .desc("Use file checksums during differential backup related operations."
                                        + "  Default for this flag is normally 'on'.  But if '--use-modified-date' flag is on, then the default"
                                        + " for this flag is 'off'.")
                                .build() );
        
         options.addOption( Option.builder().longOpt( "checksum" )
                                .desc("The type of checksum to be used."
                                        + "  Default is 'SHA2'.")
                                .hasArg()
                                .argName("CHECKSUM")
                                .build() );
         
        options.addOption( Option.builder().longOpt( "use-modified-date" )
                                .desc("Use file-system modified-date during differential backup related operations."
                                        + " Default for this flag is normally 'on'.  But if '--use-checksum' flag is on, then the default"
                                        + " for this flag is 'off'.")
                                .hasArg()
                                .argName("USEMODIFIEDDATE")
                                .build() );
        
        options.addOption( Option.builder().longOpt( "chunk-size" )
                                .desc( "Size of the file-system chunks used when creating backups." )
                                .hasArg()
                                .argName("CHUNKSIZE")
                                .build() );

        options.addOption( Option.builder().longOpt( "archive-name-regex" )
                .desc("The regex used for generating archive file names.")
                .hasArg()
                .argName("ARCHIVENAMEREGEX")
                .build());

        options.addOption(Option.builder().longOpt("archive-name-component")
                .desc("The type of components contained in the regex.")
                .hasArg()
                .argName("ARCHIVENAMECOMP")
                .build() );

        options.addOption(Option.builder().longOpt("archive-name-template")
                .desc("The template for each archive name.")
                .hasArg()
                .argName("ARCHIVENAMETEMPLATE")
                .build() );

        options.addOption( Option.builder().longOpt( "job-name-regex" )
                .desc("The regex used for generating job folder names.")
                .hasArg()
                .argName("JOBNAMEREGEX")
                .build());

        options.addOption(Option.builder().longOpt("job-name-component")
                .desc("The type of components contained in the regex." )
                .hasArg()
                .argName("JOBNAMECOMP")
                .build() );

        options.addOption(Option.builder().longOpt("job-name-template")
                .desc("The template for each job name." )
                .hasArg()
                .argName("JOBNAMETEMPLATE")
                .build() );

        options.addOption( Option.builder().longOpt( "encryption-cipher" )
                                .desc( "Type of Encryption to use on the backup files while storing."
                                + "  Default is 'NONE'.  'AES' is also an option.")
                                .hasArg()
                                .argName("ENCRYPTIONCIPHER")
                                .build() );

        options.addOption( Option.builder().longOpt( "encryption-key" )
                .desc("The encryption key if any.  Please note this may cause your key to remain in your CLI history.")
                .hasArg()
                .argName("ENCRYPTIONKEY")
                .build() );
        
        options.addOption( Option.builder().longOpt( "backup-compression-type" )
                                .desc("Type of Compression to use on the backup files while storing."
                                        + " Default is 'NONE'. 'GZIP', 'GZIP01'-'GZIP10' are all options")
                                .hasArg()
                                .argName("COMPRESSIONTYPE")
                                .build() );
        
        options.addOption( Option.builder().longOpt( "exclude-regex" )
                                .desc( "Regular expression to match against paths and folder names prior to backing up."
                                + "  If a path or folder name matches, then they are excluded.")
                                .hasArg()
                                .argName("EXCLUDEREGEX")
                                .build() );
        
        options.addOption( Option.builder().longOpt( "exclude-path" )
                                .desc( "File-system path match during backups.  Matches are excluded from the current backup." )
                                .hasArg()
                                .argName("EXCLUDEPATH")
                                .build() );
        
        options.addOption( Option.builder().longOpt( "backup-report-type" )
                                .desc( "Type of report to generate. Default 'PLAIN'. Also 'XML' an option" )
                                .hasArg()
                                .argName("BACKUPREPORTTYPE")
                                .build() );
        
        options.addOption( Option.builder().longOpt( "backup-report-path" )
                                .desc( "The location of the backup report." )
                                .hasArg()
                                .argName("BACKUPREPORTPATH")
                                .build() );
        
        options.addOption( Option.builder().longOpt("run-restore")
                                .desc("Restores a backup to the file-system.")
                                .build() );
        
        options.addOption( Option.builder().longOpt( "restore-destination" )
                                .desc("Path for restoring backed up data.")
                                .hasArg()
                                .argName("RESTOREDEST")
                                .build() );

        options.addOption( Option.builder().longOpt("no-preserve-permissions")
                                .desc("Disable restoring file-system permissions.")
                                .build() );
        
                
        options.addOption( Option.builder().longOpt( "no-preserve-ownership" )
                                .desc( "Disable restoring file-system ownership." )
                                .build() );
        
                
        options.addOption( Option.builder().longOpt( "no-clobber" )
                                .desc( "Disable overwriting files." )
                                .build() );

        options.addOption( Option.builder().longOpt( "ignore-lock" )
                .desc( "Ignore an existing lock file.  Mainly for debugging purposes." )
                .build() );

        options.addOption( Option.builder().longOpt( "backup-describe" )
                                .desc( "Describe a backup on disk." )
                                .build() );
        
                
        options.addOption( Option.builder().longOpt( "backup-status" )
                                .desc( "Return the status of a currently running backup or restore." )
                                .build() );
        
        options.addOption( Option.builder().longOpt( "argfile" )
                                .desc( "The name of a file containing"
                 + " the list of command-line arguments.  Only the first uncommented line is read."
                 + "\nAll other command line options are ignored." )
                                .hasArg()
                                .argName("ARGFILENAME")
                                .build() );
                
        options.addOption( Option.builder().longOpt( "conf-file" )
                                .desc( "The name of a file containing"
                 + " the FastSite configuration" )
                                .hasArg()
                                .argName("CONFFILENAME")
                                .build() );
        
        options.addOption( Option.builder().longOpt( "backupset-file" )
                                .desc( "The name of a file containing"
                 + " the full backup configuration" )
                                .hasArg()
                                .argName("BKFILENAME")
                                .build() );
        
        options.addOption( Option.builder().longOpt( "verbose" )
                                .desc( "Verbosity level.  Values include MINIMUM, MAXIMUM, " +
"DEBUG, INFO, WARNING,and ERROR")
                                .hasArg()
                                .argName("VERBOSITY")
                                .build() );
        
        options.addOption( Option.builder().longOpt("output-redirect")
                                .hasArg()
                                .desc("Redirect output to a file.")
                                .argName("OUTPUTREDIRECT")
                                .build());
        
        options.addOption( Option.builder().longOpt( "dry-run" )
                                .desc( "Do not change anything on the server,"
                                    + " just simulate what might happen if we ran the command." )
                                .build() );
        
        options.addOption( Option.builder().longOpt( "update" )
                                .desc( "Update the agent components from the server." )
                                .build() );
        
        options.addOption( Option.builder().longOpt( "service" )
                                .desc( "Signal that the agent should run as a background service." )
                                .build() );
        
        options.addOption( Option.builder().longOpt( "username" )
                                .desc( "The username credential" )
                                .hasArg()
                                .argName("USERNAME")
                                .build() );
        
        options.addOption( Option.builder().longOpt( "password" )
                                .desc( "The password credential.  "
              + "Argument is optional and it is advised to leave it out for security."
              + "Otherwise your password will be saved in your shell history." )
                                .hasArg()
                                .argName("PASSWORD")
                                .build() );
        
        return options;
    }
}
