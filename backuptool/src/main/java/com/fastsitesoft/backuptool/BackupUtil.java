package com.fastsitesoft.agent.backup;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Misc. utilities for helping backup processes.
 * 
 * @author Kervin Pierre <info@sludev.com>
 */
public class BackupUtil
{
    private static final Logger log 
                                = LogManager.getLogger(BackupUtil.class);
    /**
     * This function is an entry point for backup.
     * 
     * @param sourcePath
     * @param chunksize
     * @param chunkSizeInKB
     * @param chunkSizeInMB
     * @param encrypt
     * @param gzip
     * @param excludes
     * @param outputfolder
     * @param outputfilename
     * @throws Exception
     */
    public static void backup(File srcFile, int chunksize, boolean chunkSizeInKB, boolean chunkSizeInMB, boolean encrypt, boolean gzip, String[] excludes, File outputFolderPath, String outputfilename) 
    {

        String sourcePath = srcFile.getAbsolutePath();
        srcFile = new File(sourcePath);

        String outputPath = outputFolderPath.getAbsolutePath() + File.separator + "tmp" + File.separator;
        System.out.println("Output folder : " + outputPath);

        File outputFile = new File(outputPath);
        outputPath = outputFile.getAbsolutePath();
        outputFile = new File(outputPath);
        outputFile.mkdirs();

        // Filter and Gzip files
 //       copyFilesForProcess(srcFile, outputFile, excludes);
 //      generateTarFile(outputFile, outputfilename);
        if (chunksize > 0)
        {
            int calcualatedChunkSize = 0;
            if (chunkSizeInMB)
            {
                calcualatedChunkSize = chunksize * 1024 * 1024;
            }
            else
            {
                calcualatedChunkSize = chunksize * 1024;
            }
 //           generateSplit(outputFile, outputfilename, calcualatedChunkSize);
        }
        if (outputFile.isDirectory())
        {
//            deleteFile(outputFile);
        }
        if (encrypt)
        {
 //           generateEncrypt(outputFolderPath);
        }
    }

    /**
     * This method will delete file/directory recursive
     * 
     * @param file
     * @throws Exception
     */
    public static void deleteFile(File file) throws Exception
    {
        if (file.isDirectory())
        {
            FileUtils.deleteDirectory(file);
        }
        else
        {
            file.delete();
        }
    }

    /**
     * This method will return true/false for file is permitted to process
     * 
     * @param fileName
     * @param excludes
     * @param type
     * @return
     */
    public static boolean isIncludeForProcess(String fileName, String[] excludes, String type)
    {
        boolean isInclude = true;
        if (excludes != null)
        {
            for (String exclude : excludes)
            {
                if ("DIR".equalsIgnoreCase(type))
                {
                    exclude = exclude.replaceAll("/", "");
                    exclude = exclude.replaceAll("\\.", "");
                }
                exclude = exclude.replaceAll("\\*", "(.*)");
                Pattern r = Pattern.compile(exclude);
                Matcher m = r.matcher(fileName);
                if (m.find())
                    return false;
            }
        }
        return isInclude;
    }

    private static IOFileFilter prepareFilters(String[] excludes)
    {
        IOFileFilter finalFilter = null;
        if (excludes != null && excludes.length > 0)
        {
            List<IOFileFilter> filters = new ArrayList<IOFileFilter>();

            for (String regEx : excludes)
            {
                IOFileFilter fileFilter = new RegexFileFilter(regEx);
                filters.add(fileFilter);
                finalFilter = FileFilterUtils.or(filters.toArray(new IOFileFilter[] {}));

                finalFilter = FileFilterUtils.notFileFilter(finalFilter);
            }
        }
        else
        {
            finalFilter = new IOFileFilter()
            {
                public boolean accept(File arg0, String arg1)
                {
                    return true;
                }

                public boolean accept(File arg0)
                {
                    return true;
                }
            };
        }

        return finalFilter;
    }

    /**
     * Copy all the files for process that matches criteria
     * 
     * @param srcFile
     * @param outputFile
     * @param sourcePath
     * @param outputPath
     * @param excludes
     * @throws Exception
     */
    private static void copyFilesForProcess(File srcFile, File outputFile, String[] excludes) throws Exception
    {
        Collection<File> allFiles = null;
        if (srcFile.isDirectory())
        {
            IOFileFilter fileFilter = prepareFilters(excludes);
            allFiles = FileUtils.listFilesAndDirs(srcFile, fileFilter, TrueFileFilter.INSTANCE);

            // remove source folder from the list of files filtered
            Iterator<File> itr = allFiles.iterator();
            while (itr.hasNext())
            {
                File currentFile = itr.next();
                if (currentFile.getAbsoluteFile().equals(srcFile.getAbsoluteFile()))
                {
                    itr.remove();
                }
            }
        }
        else
        {
            allFiles = new ArrayList<File>();
            allFiles.add(srcFile);
        }

        for (File file : allFiles)
        {
            if (file.isDirectory())
            {
                String outputFilePath = getOutputFilePath(file.getAbsolutePath(), srcFile, outputFile);
                File tempFile = new File(outputFilePath);
                if (!tempFile.exists())
                {
                    tempFile.mkdirs();
                }
            }
            else
            {
                String outputFilePath = getOutputFilePath(file.getAbsolutePath(), srcFile, outputFile);
                File tempFile = new File(outputFilePath);
                if (!tempFile.exists())
                {
                    tempFile.createNewFile();
                }
                // Create the output stream for the output file
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                FileOutputStream fos = new FileOutputStream(tempFile);
                GZIPOutputStream gzipos = new GZIPOutputStream(new BufferedOutputStream(fos));
                IOUtils.copy(bis, gzipos);
                gzipos.close();
                fos.flush();
                fos.close();
                bis.close();
            }
        }
    }

    /**
     * Get output file path
     * 
     * @param sourceFile
     * @param sourcePath
     * @param outputPath
     * @return
     */
    public static String getOutputFilePath(String currentFile, File sourcePath, File outputPath)
    {
        String outputFilePath = "";
        if (currentFile.equals(sourcePath))
        {
            outputFilePath = (new File(outputPath, (new File(currentFile)).getName())).getAbsolutePath();
        }
        else
        {
            StringBuffer sb = new StringBuffer(currentFile);
            outputFilePath = outputPath + sb.substring(sourcePath.getAbsolutePath().length(), sb.length());
        }
        return outputFilePath;
    }

    /**
     * This method will generate Tar file
     * 
     * @param srcFile
     * @param outputFile
     * @param sourcePath
     * @param outputPath
     * @param outputfilename
     * @throws Exception
     */
    public static void generateTarFile(File outputFile, String outputfilename) throws Exception
    {
        if (outputFile.isDirectory())
        {
            String outputTarPath = outputFile.getParentFile().getAbsolutePath() + File.separator + outputfilename + ".tar.gz";
            System.out.println("Tar File : " + outputTarPath);
            FileOutputStream fos = new FileOutputStream(outputTarPath);
            // Wrap the output file stream in streams that will tar and gzip
            // everything
            TarArchiveOutputStream taos = new TarArchiveOutputStream(new BufferedOutputStream(fos));
            // TAR has an 8 gig file limit by default, this gets around that
            taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
            // TAR originally didn't support long file names, so enable the
            // support
            // for it
            taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            // Get to putting all the files in the compressed output file
            for (File compressfile : outputFile.listFiles())
            {
                addFilesToTar(taos, compressfile, outputFile.getAbsolutePath());
            }
            // Close everything up
            taos.close();
            fos.close();
        }
    }

    /**
     * Add files to Tar file
     * 
     * @param taos
     * @param compressfile
     * @param sourcePath
     * @param outputPath
     * @throws IOException
     */
    private static void addFilesToTar(TarArchiveOutputStream taos, File compressfile, String outputPath) throws IOException
    {
        // Create an entry for the file
        String tarDirectory = getTarDirectory(compressfile.getAbsolutePath(), outputPath);
        taos.putArchiveEntry(new TarArchiveEntry(compressfile, tarDirectory));
        if (compressfile.isFile())
        {
            // Add the file to the archive
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(compressfile));
            IOUtils.copy(bis, taos);
            taos.closeArchiveEntry();
            bis.close();
        }
        else if (compressfile.isDirectory())
        {
            // close the archive entry
            taos.closeArchiveEntry();
            for (File childFile : compressfile.listFiles())
            {
                addFilesToTar(taos, childFile, outputPath);
            }
        }
    }

    /**
     * This method will return path for tar directory
     * 
     * @param compressFilePath
     * @param outputPath
     * @return
     */
    private static String getTarDirectory(String compressFilePath, String outputPath)
    {
        StringBuffer sb = new StringBuffer(compressFilePath);
        String tarDirectoryPath = sb.substring(outputPath.length(), sb.length());
        return tarDirectoryPath;
    }

    /**
     * It return Year,month,date,minute,second format for file
     * 
     * @return
     */
    public static String getDateStringForFile()
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        return cal.get(Calendar.YEAR) + "" + cal.get(Calendar.MONTH) + "" + cal.get(Calendar.DATE) + "-" + cal.get(Calendar.MINUTE) + "" + cal.get(Calendar.SECOND);
    }

    /**
     * This method will split tar file
     * 
     * @param srcFile
     * @param outputFile
     * @param sourcePath
     * @param outputPath
     * @param outputfilename
     * @param chunkSizeInKB
     * @throws Exception
     */
    public static void generateSplit(File outputFile, String outputfilename, int chunkSizeInKB) throws Exception
    {
        if (outputFile.isDirectory())
        {
            String outputTarPath = outputFile.getParentFile().getAbsolutePath() + File.separator + outputfilename + ".tar.gz";
            File inputTarFile = new File(outputTarPath);
            String FILE_NAME = outputFile.getParentFile().getAbsolutePath() + File.separator + outputfilename + "-" + getDateStringForFile() + "-";
            System.out.println("File Name : " + FILE_NAME);
            String newFileName;
            FileInputStream inputStream = null;
            FileOutputStream filePart = null;
            int nChunks = 0, readLength = chunkSizeInKB;
            try
            {
                inputStream = new FileInputStream(inputTarFile);
                byte[] readByte = new byte[readLength];
                int readCount = 0;
                while ((readCount = inputStream.read(readByte)) != -1)
                {

                    newFileName = FILE_NAME + "" + Integer.toString(nChunks) + ".tar.gz";
                    nChunks++;
                    try
                    {
                        filePart = new FileOutputStream(new File(newFileName));
                        filePart.write(readByte, 0, readCount);
                        filePart.flush();
                        filePart.close();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        try
                        {
                            if (filePart != null)
                            {
                                filePart.flush();
                                filePart.close();
                                filePart = null;
                            }
                        }
                        catch (Exception e)
                        {
                            filePart = null;
                        }
                    }
                }
                inputStream.close();
                // delete tar file
                inputTarFile.delete();
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
            }
        }
    }

    /**
     * Encrypt the files with AES algorithm
     * 
     * @param outputFile
     * @throws Exception
     */
    public static void generateEncrypt(File outputFile) throws Exception
    {
        if (outputFile.isDirectory())
        {
            for (File inputFile : outputFile.listFiles())
            {
                generateEncrypt(inputFile);
            }
        }
        else
        {
            performAESEncryption(outputFile);
        }
    }

    /**
     * This method will perform AES encryption
     * 
     * @param inputFile
     * @throws Exception
     */
    public static void performAESEncryption(File inputFile) throws Exception
    {
        File newEncryptedFile = new File(inputFile.getAbsolutePath() + "_enc");
        FileInputStream fin = null;
        FileOutputStream fout = null;
        byte[] output = null;
        try
        {
//            SecretKey skey = new SecretKeySpec(BackupConstants.SEC_KEY.getBytes("UTF-8"), "AES");
   //         AlgorithmParameterSpec paramSpec = new IvParameterSpec(BackupConstants.iv);
  //          Cipher dcipher = Cipher.getInstance(BackupConstants.ALGORITHUM);
  //          dcipher.init(Cipher.ENCRYPT_MODE, skey, paramSpec);

            fin = new FileInputStream(inputFile);
            fout = new FileOutputStream(newEncryptedFile);

            byte[] readByte = FileUtils.readFileToByteArray(inputFile);
 //          output = dcipher.update(readByte, 0, readByte.length);
            fout.write(output, 0, output.length);
            fout.flush();

            fout.close();
            fin.close();
            inputFile.delete();
            newEncryptedFile.renameTo(inputFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (fin != null)
                    fin.close();
                if (fout != null)
                    fout.close();
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Return a list of folders or files.
     * 
     * @param cwd
     * @param currPath
     * @return 
     */
    public static List<Path> expandPathList(Path cwd, Path currPath)
    {
        List<Path> res = new ArrayList<>();
        
        return res;
    }
    
    /**
     * Return a list of folders or files.
     * 
     * @param cwd
     * @param currPatt
     * @return 
     */
    public static List<Path> expandPathList(Path cwd, Pattern currPatt)
    {
        List<Path> res = new ArrayList<>();
        
        return res;
    }
}
