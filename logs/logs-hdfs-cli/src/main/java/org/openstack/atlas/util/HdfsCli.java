package org.openstack.atlas.util;

import java.util.Arrays;
import org.openstack.atlas.util.staticutils.StaticStringUtils;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;
import org.openstack.atlas.config.HadoopLogsConfigs;
import com.hadoop.compression.lzo.LzoIndex;
import java.net.URI;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import com.hadoop.compression.lzo.LzopCodec;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import org.apache.commons.math.linear.Array2DRowFieldMatrix;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.io.compress.CompressionOutputStream;
import org.openstack.atlas.config.LbLogsConfiguration;
import org.openstack.atlas.logs.hadoop.jobs.HadoopJob;
import org.openstack.atlas.logs.hadoop.jobs.HadoopLogSplitterJob;
import org.openstack.atlas.logs.hadoop.writables.LogMapperOutputValue;
import org.openstack.atlas.logs.hadoop.writables.LogReducerOutputValue;
import org.openstack.atlas.util.debug.Debug;
import sun.net.www.http.Hurryable;

public class HdfsCli {

    private static final String HDUNAME = "HADOOP_USER_NAME";
    private static final int PAGESIZE = 4096;
    private static final int HDFSBUFFSIZE = 1024 * 64;
    private static final int ONEMEG = 1024 * 1024;
    private static final int BUFFER_SIZE = 1024 * 128;
    private static List<String> jarFiles = new ArrayList<String>();
    private static URLClassLoader jobClassLoader = null;
    private static String jobJarName = "";

    public static void main(String[] argv) throws IOException, InterruptedException {
        System.out.printf("JAVA_LIBRARY_PATH=%s\n", System.getProperty("java.library.path"));
        String cmdLine;
        String[] args;
        if (argv.length >= 1) {
            System.out.printf("Useing confFile %s\n", argv[0]);
            HadoopLogsConfigs.resetConfigs(argv[0]);
        } else {
            System.out.printf("useing confFile %s\n", LbLogsConfiguration.defaultConfigurationLocation);
        }
        HdfsUtils hdfsUtils = HadoopLogsConfigs.getHdfsUtils();
        String user = HadoopLogsConfigs.getHdfsUserName();
        Configuration conf = HadoopLogsConfigs.getHadoopConfiguration();

        URI defaultHdfsUri = FileSystem.getDefaultUri(conf);
        FileSystem fs = hdfsUtils.getFileSystem();
        System.setProperty(HDUNAME, user);
        FileSystem lfs = hdfsUtils.getLocalFileSystem();

        BufferedReader stdin = HdfsCliHelpers.inputStreamToBufferedReader(System.in);
        System.out.printf("\n");

        while (true) {
            try {
                System.out.printf("lbaas_hadoop_client %s> ", fs.getWorkingDirectory().toUri().toString());
                cmdLine = stdin.readLine();
                if (cmdLine == null) {
                    break; // EOF
                }
                args = stripBlankArgs(cmdLine);
                if (args.length < 1) {
                    System.out.printf("Usage is help\n");
                    continue;
                }
                String cmd = args[0];
                if (cmd.equals("help")) {
                    System.out.printf("\n");
                    System.out.printf("Usage is\n");
                    System.out.printf("help\n");
                    System.out.printf("cat <path>\n");
                    System.out.printf("cd <path>  #Change remote directory\n");
                    System.out.printf("cdin [dateKey] #Change to the input directory\n");
                    System.out.printf("cdout[dateKey] #Change to the output directory\n");
                    System.out.printf("chmod <octPerms> <path>\n");
                    System.out.printf("chown <user> <group> <path>\n");
                    System.out.printf("chuser <userName>\n");
                    System.out.printf("compressLzo <srcPath> <dstFile> [buffSize]#Compress lzo file\n");
                    System.out.printf("countLines <zeusFile> <nTicks> [buffSize]\n");
                    System.out.printf("cpfl <srcPath 1local> <dstPath remote> [reps] [blocksize]#copy from local\n");
                    System.out.printf("cpld <srcDir> <dstDir>  args [reps] [blocksize]\n");
                    System.out.printf("cpLocal <localSrc> <localDst> [buffsize] #None hadoop file copy\n");
                    System.out.printf("cptl <srcPath remote> <dstPath local> #Copy to Local\n");
                    System.out.printf("diffConfig <confA.xml> <confB.xml># Compare the differences between the configs\n");
                    System.out.printf("du #Get number of free space on HDFS\n");
                    System.out.printf("dumpConfig <outFile.xml> <confIn.xml..> #Dump config to outfile\n");
                    System.out.printf("exit\n");
                    System.out.printf("findCp <className> #find class path via reflection\n");
                    System.out.printf("gc\n");
                    System.out.printf("getlzo <DownloadDir> <dateHour> #Download the Lzo for the given hour\n");
                    System.out.printf("getzip <DownloadDir> <h=hourKey> <l=LoadbalancerId> #Download the zip file from Hdfs for the specifie day and loadbalancer\n");
                    System.out.printf("homedir\n");
                    System.out.printf("indexLzo <FileName>\n");
                    System.out.printf("joinPath <path1> ...<pathN> #Test the join the paths together skipping double slashes.\n");
                    System.out.printf("lineIndex <fileName> #Index the line numbers in the file\n");
                    System.out.printf("lslzo [hourKey] #List the lzos in the input directory\n");
                    System.out.printf("ls [path] #List hdfs files\n");
                    System.out.printf("lsr [path] #List hdfs files recursivly\n");
                    System.out.printf("lszip [l=lid] [h=hour] [m=missing]#List all zip files in the HDFS ourput directory for hourh or and the given lid\n");
                    System.out.printf("mem\n");
                    System.out.printf("mkdir <path>\n");
                    System.out.printf("printReducers <hdfsDir> #Display the contents of the reducer output\n");
                    System.out.printf("pwd   #print remote current directory\n");
                    System.out.printf("rebasePath <srcBase> <srcPath> <dstPath> #Show what the rebasePath method in StaticFileUtils would do\n");
                    System.out.printf("recompressIndex <srcFile> <hdfsDstFile> #Recompress and index lzo file and upload to hdfs\n");
                    System.out.printf("rmdir <path>\n");
                    System.out.printf("rmin <hourkey> #Delete the input paths for the specified hour\n");
                    System.out.printf("rmout <hourKey> #Delete the output paths for the specified hour\n");
                    System.out.printf("rm <path>\n");
                    System.out.printf("runJob <jobDriverClass>");
                    System.out.printf("runSplit <lzoFile> #Run the HadoopSplitterJob for the specified hourkey");
                    System.out.printf("runMain <class> args0..N");
                    System.out.printf("uploadLzo <lzoFile> #Upload the the lzo file\n");
                    System.out.printf("scanLines <logFile> <nLines> <nTicks>\n");
                    System.out.printf("setJobJar <jobJar> #set Jar file to classLoader\n");
                    System.out.printf("setReplCount <FilePath> <nReps> #Set the replication count for this file\n");
                    System.out.printf("showCl <className> #Show class loader info via reflection\n");
                    System.out.printf("showConfig #Show hadoop configs\n");
                    System.out.printf("showCrc <fileName> #Show crc value that would be reported by Zip\n");
                    System.out.printf("whoami\n");
                    continue;
                }
                if (cmd.equals("runSplit") && args.length >= 2) {
                    HadoopLogsConfigs.copyJobsJar();
                    String localLzoFilePath = args[1];
                    String localLzoFile = StaticFileUtils.pathTail(localLzoFilePath);
                    Matcher m = HdfsUtils.hdfsLzoPatternPre.matcher(localLzoFile);
                    if (!m.find()) {
                        System.out.printf("%s doesn't look like a properly name lzo file", localLzoFilePath);
                        continue;
                    }
                    String hourKey = m.group(1);

                    // upload the lzo file
                    List<String> hdfsLzoPathComps = new ArrayList<String>();
                    hdfsLzoPathComps.add(HadoopLogsConfigs.getMapreduceInputPrefix());
                    hdfsLzoPathComps.add(hourKey);
                    hdfsLzoPathComps.add("0-" + hourKey + "-access_log.aggregated.lzo");
                    String hdfsLzoPath = StaticFileUtils.splitPathToString(StaticFileUtils.joinPath(hdfsLzoPathComps));
                    String hdfsLzoIdxPath = hdfsLzoPath + ".idx";
                    System.out.printf("Uploading lzo %s to %s\n", localLzoFile, hdfsLzoPath);
                    InputStream lzoIs = StaticFileUtils.openInputFile(localLzoFilePath, BUFFER_SIZE);
                    OutputStream lzoOs = hdfsUtils.openHdfsOutputFile(hdfsLzoPath, false, false);
                    OutputStream lzoIdx = hdfsUtils.openHdfsOutputFile(hdfsLzoIdxPath, false, false);
                    hdfsUtils.recompressAndIndexLzoStream(lzoIs, lzoOs, lzoIdx, System.out);
                    lzoIs.close();
                    lzoOs.close();
                    lzoIdx.close();


                    // Setup outputdir
                    List<String> outDirComps = new ArrayList<String>();
                    outDirComps.add(HadoopLogsConfigs.getMapreduceOutputPrefix());
                    outDirComps.add("lb_logs_split");
                    outDirComps.add(hourKey);
                    String outDir = StaticFileUtils.splitPathToString(StaticFileUtils.joinPath(outDirComps));

                    List<String> logSplitArgs = new ArrayList<String>();
                    logSplitArgs.add(HadoopLogsConfigs.getHdfsJobsJarPath());
                    logSplitArgs.add(outDir);
                    logSplitArgs.add("");
                    logSplitArgs.add(hourKey);
                    logSplitArgs.add(HadoopLogsConfigs.getNumReducers());
                    logSplitArgs.add(HadoopLogsConfigs.getHdfsUserName());
                    logSplitArgs.add(hdfsLzoPath);
                    HadoopJob hadoopClient = new HadoopLogSplitterJob();
                    hadoopClient.setConfiguration(HadoopLogsConfigs.getHadoopConfiguration());
                    int errorCode = hadoopClient.run(logSplitArgs);  // Actually runs the Hadoop Job
                    System.out.printf("Hadoop tun response code was %d\n", errorCode);
                    continue;
                }
                if (cmd.equals("rmin") && args.length >= 2) {
                    String dateHour = args[1];
                    String inputPath = HadoopLogsConfigs.getMapreduceInputPrefix();
                    if (dateHour.length() < 6) {
                        System.out.printf("Will not remove anything greater then one month of data per call\n");
                        continue;
                    }
                    List<String> targetPathComps = new ArrayList<String>();
                    targetPathComps.add(inputPath);
                    targetPathComps.add(dateHour);
                    String targetPath = StaticFileUtils.splitPathToString(StaticFileUtils.joinPath(targetPathComps));
                    System.out.printf("Searching for directories that begin with %s\n", targetPath);
                    FileStatus[] fileStats = fs.listStatus(new Path(inputPath));
                    List<Path> doomedPaths = new ArrayList<Path>();
                    System.out.printf("Files targeted for deletion are:\n");
                    for (FileStatus fileStat : fileStats) {
                        String hdfsPath = fileStat.getPath().toUri().getRawPath();
                        if (hdfsPath.startsWith(targetPath)) {
                            doomedPaths.add(new Path(hdfsPath));
                            System.out.printf("%s\n", hdfsPath);
                        }
                    }

                    System.out.printf("Are you sure you want to delete the above %d files (Y/N)\n", doomedPaths.size());
                    if (stdinMatches(stdin, "Y")) {
                        for (Path doomedPath : doomedPaths) {
                            if (fs.delete(doomedPath, true)) {
                                System.out.printf("Deleted %s\n", doomedPath.toUri().getRawPath());
                            } else {
                                System.out.printf("Could not Delete %s\n", doomedPath.toUri().getRawPath());
                            }
                        }
                    } else {
                        System.out.printf("Not deleting files\n");
                    }

                    continue;
                }


                if (cmd.equals("rmout") && args.length >= 2) {
                    String dateHour = args[1];
                    String outputPath = HadoopLogsConfigs.getMapreduceOutputPrefix();
                    if (dateHour.length() < 6) {
                        System.out.printf("Will not remove anything greater then one month of data per call\n");
                        continue;
                    }
                    List<String> targetPathComps = new ArrayList<String>();
                    targetPathComps.add(outputPath);
                    targetPathComps.add("lb_logs_split");
                    List<String> lbLogSplitPathComps = new ArrayList<String>(targetPathComps);
                    String lbLogsSplitPath = StaticFileUtils.splitPathToString(StaticFileUtils.joinPath(lbLogSplitPathComps));

                    targetPathComps.add(dateHour);
                    String targetPath = StaticFileUtils.splitPathToString(StaticFileUtils.joinPath(targetPathComps));
                    System.out.printf("Searching for directories that begin with %s\n", targetPath);
                    FileStatus[] fileStats = fs.listStatus(new Path(lbLogsSplitPath));
                    List<Path> doomedPaths = new ArrayList<Path>();
                    System.out.printf("Files targeted for deletion are:\n");
                    for (FileStatus fileStat : fileStats) {
                        String hdfsPath = fileStat.getPath().toUri().getRawPath();
                        if (hdfsPath.startsWith(targetPath)) {
                            doomedPaths.add(new Path(hdfsPath));
                            System.out.printf("%s\n", hdfsPath);
                        }
                    }

                    System.out.printf("Are you sure you want to delete the above %d files (Y/N)\n", doomedPaths.size());
                    String[] resp = stripBlankArgs(stdin.readLine());
                    if (resp.length >= 1 && resp[0].equalsIgnoreCase("Y")) {
                        for (Path doomedPath : doomedPaths) {
                            if (fs.delete(doomedPath, true)) {
                                System.out.printf("Deleted %s\n", doomedPath.toUri().getRawPath());
                            } else {
                                System.out.printf("Could not Delete %s\n", doomedPath.toUri().getRawPath());
                            }
                        }
                    } else {
                        System.out.printf("Not deleting files\n");
                    }

                    continue;
                }

                if (cmd.equals("getzip") && args.length > 1) {
                    Map<String, String> kw = argMapper(args);
                    String lid = (kw.containsKey("l")) ? kw.get("l") : null;
                    String hourKey = (kw.containsKey("h")) ? kw.get("h") : null;
                    String downloadDir = args[1];
                    List<FileStatus> zipStatusList = hdfsUtils.listHdfsZipsStatus(hourKey, lid, false);
                    System.out.printf("Attempting to fetch zipfiles\n");
                    for (FileStatus zipFileStatus : zipStatusList) {
                        System.out.printf("%s\n", HdfsCliHelpers.displayFileStatus(zipFileStatus));
                    }
                    System.out.printf("Are you sure you want to download the above files (Y/N)?");
                    if (stdinMatches(stdin, "Y")) {
                        for (FileStatus zipFileStatus : zipStatusList) {
                            String hdfsZipFileStr = zipFileStatus.getPath().toUri().getRawPath();
                            String dstZipFileStr = StaticFileUtils.joinPath(downloadDir, StaticFileUtils.pathTail(hdfsZipFileStr));
                            System.out.printf("Downloading %s to %s\n", zipFileStatus.getPath().toUri().toString(), dstZipFileStr);
                            InputStream is = hdfsUtils.openHdfsInputFile(zipFileStatus.getPath(), false);
                            OutputStream os = StaticFileUtils.openOutputFile(dstZipFileStr, BUFFER_SIZE);
                            StaticFileUtils.copyStreams(is, os, System.out, BUFFER_SIZE);
                            is.close();
                            os.close();
                        }
                    }
                    continue;
                }
                if (cmd.equals("getlzo") && args.length > 2) {
                    String downloadDir = args[1];
                    String dateHour = args[2];
                    System.out.printf("Searching for lzo files matching %s\n", dateHour);
                    List<FileStatus> lzoFileStatusList = hdfsUtils.listHdfsLzoStatus(dateHour);
                    System.out.printf("Attempting to download lzos\n");
                    for (FileStatus lzoFileStatus : lzoFileStatusList) {
                        System.out.printf("%s\n", HdfsCliHelpers.displayFileStatus(lzoFileStatus));
                    }
                    System.out.printf("Are you sure you want to download the lzo files above?(Y/N)?");
                    if (stdinMatches(stdin, "Y")) {
                        for (FileStatus lzoFileStatus : lzoFileStatusList) {
                            String srcLzoFileStr = StaticFileUtils.pathTail(lzoFileStatus.getPath().toUri().getRawPath());
                            Matcher m = HdfsUtils.hdfsLzoPattern.matcher(srcLzoFileStr);
                            if (!m.find()) {
                                System.out.printf("Error srcFile %s didn't match expected LZO file name");
                                continue;
                            }
                            String dstFileName = m.group(1) + "-access_log.aggregated.lzo";
                            String dstFilePath = StaticFileUtils.joinPath(downloadDir, dstFileName);
                            System.out.printf("Downloading %s to %s\n", lzoFileStatus.getPath().toUri().toString(), dstFilePath);
                            InputStream is = hdfsUtils.openHdfsInputFile(lzoFileStatus.getPath(), false);
                            OutputStream os = StaticFileUtils.openOutputFile(dstFilePath, BUFFER_SIZE);
                            StaticFileUtils.copyStreams(is, os, System.out, BUFFER_SIZE);
                            is.close();
                            os.close();
                        }
                    }
                    continue;
                }
                if (cmd.equals("showConfig")) {
                    System.out.printf("HadoopLogsConfig=%s\n", HadoopLogsConfigs.staticToString());
                    continue;
                } else if (cmd.equals("recompressIndex") && args.length >= 3) {
                    String srcLzo = StaticFileUtils.expandUser(args[1]);
                    String dstLzo = args[2];
                    String dstIdx = dstLzo + ".index";
                    FileInputStream lzoInputStream = new FileInputStream(srcLzo);
                    FSDataOutputStream dstLzoStream = hdfsUtils.openHdfsOutputFile(dstLzo, false, true);
                    FSDataOutputStream dstIdxStream = hdfsUtils.openHdfsOutputFile(dstIdx, false, true);
                    hdfsUtils.recompressAndIndexLzoStream(lzoInputStream, dstLzoStream, dstIdxStream, null);
                    System.out.printf("Recompressed and sent\n");
                    lzoInputStream.close();
                    dstLzoStream.close();
                    dstIdxStream.close();
                    continue;
                }
                if (cmd.equals("whoami")) {
                    System.out.printf("your supposed to be %s\n", user);
                    continue;
                }
                if (cmd.equals("chuser") && args.length >= 2) {
                    user = args[1];
                    fs = FileSystem.get(defaultHdfsUri, conf, user);
                    System.setProperty(HDUNAME, user);
                    System.out.printf("Switched to user %s\n", user);
                    continue;
                }
                if (cmd.equals("mem")) {
                    System.out.printf("Memory\n=================================\n%s\n", Debug.showMem());
                    continue;
                }
                if (cmd.equals("runJob") && args.length >= 2) {
                    Class<? extends HadoopJob> jobDriverClass;

                    String jobDriverClassName = "org.openstack.atlas.logs.hadoop.jobs." + args[1];
                    if (jobClassLoader == null) {
                        System.out.printf("No jobJar set cannot load class searching class Path\n");
                        jobDriverClass = (Class<? extends HadoopJob>) Class.forName(jobDriverClassName);
                    } else {
                        jobDriverClass = (Class<? extends HadoopJob>) Class.forName(jobDriverClassName, true, jobClassLoader);
                    }
                    HadoopJob jobDriver = jobDriverClass.newInstance();
                    jobDriver.setConfiguration(conf);
                    List<String> argsList = new ArrayList<String>();
                    for (int i = 2; i < args.length; i++) {
                        argsList.add(args[i]);
                    }
                    // Run job
                    double startTime = Debug.getEpochSeconds();
                    int exitCode = jobDriver.run(argsList);
                    //jobDriver.run(jobArgs);
                    double endTime = Debug.getEpochSeconds();
                    System.out.printf("took %f seconds running job %s\n", endTime - startTime, jobDriverClassName);
                    System.out.printf("Exit status = %d\n", exitCode);
                    continue;
                }

                if (cmd.equals("runMain") && args.length >= 2) {
                    String className = args[1];
                    String[] mainArgs = new String[args.length - 2];
                    System.out.printf("Running %s\n", className);
                    for (int i = 0; i < args.length - 2; i++) {
                        mainArgs[i] = args[i + 2];
                    }
                    Class mainClass = Class.forName(args[1]);
                    Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);
                    mainMethod.invoke(null, (Object) mainArgs);
                    continue;
                }
                if (cmd.equals("gc")) {
                    System.out.printf("Calling garbage collector\n");
                    Debug.gc();
                    continue;
                }
                if (cmd.equals("lszip")) {
                    Map<String, String> kw = argMapper(args);
                    String dateHour = (kw.containsKey("h")) ? kw.get("h") : null;
                    String lid = (kw.containsKey("l")) ? kw.get("l") : null;
                    System.out.printf("Scanning for zips on hour[%s] lid[%s]\n", dateHour, lid);
                    boolean onlyMissing = (kw.containsKey("m")) ? true : false;

                    List<FileStatus> zipStatusList = hdfsUtils.listHdfsZipsStatus(dateHour, lid, onlyMissing);
                    for (FileStatus zipStatus : zipStatusList) {
                        System.out.printf("%s\n", HdfsCliHelpers.displayFileStatus(zipStatus));
                    }
                    continue;
                }
                if (cmd.equals("lslzo")) {
                    String hourKey = (args.length >= 2) ? args[1] : null;
                    System.out.printf("Scanning lzos for hour[%s]\n", hourKey);
                    List<FileStatus> lzoFiles = hdfsUtils.listHdfsLzoStatus(hourKey);
                    for (FileStatus lzoFileStatus : lzoFiles) {
                        System.out.printf("%s\n", HdfsCliHelpers.displayFileStatus(lzoFileStatus));
                    }
                    continue;
                }

                if (cmd.equals("ls")) {
                    long total_file_size = 0;
                    long total_repl_size = 0;
                    Path path = (args.length >= 2) ? new Path(args[1]) : fs.getWorkingDirectory();
                    FileStatus[] fileStatusList = fs.listStatus(path);
                    if (fileStatusList == null) {
                        System.out.printf("Error got null when trying to retrieve file statuses\n");
                    }
                    for (FileStatus fileStatus : fileStatusList) {
                        total_file_size += fileStatus.getLen();
                        total_repl_size += fileStatus.getLen() * fileStatus.getReplication();
                        System.out.printf("%s\n", HdfsCliHelpers.displayFileStatus(fileStatus));
                    }
                    System.out.printf("Total file bytes: %s\n", Debug.humanReadableBytes(total_file_size));
                    System.out.printf("Total file bytes including replication: %s\n", Debug.humanReadableBytes(total_repl_size));
                    System.out.printf("Total file count: %d\n", fileStatusList.length);
                    continue;
                }
                if (cmd.equals("lsr")) {
                    long total_file_size = 0;
                    long total_repl_size = 0;
                    String mntPath = (args.length >= 2) ? args[1] : fs.getWorkingDirectory().toUri().getRawPath();
                    double startTime = Debug.getEpochSeconds();
                    List<FileStatus> fileStatusList = hdfsUtils.listFileStatusRecursively(mntPath, false);
                    for (FileStatus fileStatus : fileStatusList) {
                        total_file_size += fileStatus.getLen();
                        total_repl_size += fileStatus.getLen() * fileStatus.getReplication();
                        System.out.printf("%s\n", HdfsCliHelpers.displayFileStatus(fileStatus));
                    }
                    System.out.printf("Total file bytes: %s\n", Debug.humanReadableBytes(total_file_size));
                    System.out.printf("Total file bytes including replication: %s\n", Debug.humanReadableBytes(total_repl_size));
                    System.out.printf("Total file count: %d\n", fileStatusList.size());
                    double endTime = Debug.getEpochSeconds();
                    double delay = endTime - startTime;
                    System.out.printf("Took %f Seconds to scan\n", delay);
                    continue;
                }
                if (cmd.equals("exit")) {
                    break;
                }
                if (cmd.equals("cd") && args.length >= 2) {
                    Path path = new Path(args[1]);
                    fs.setWorkingDirectory(path);
                    continue;
                }
                if (cmd.equals("cdin")) {
                    List<String> pathComps = new ArrayList<String>();
                    pathComps.add(HadoopLogsConfigs.getMapreduceInputPrefix());
                    if (args.length >= 2) {
                        pathComps.add(args[1]);
                    }
                    String pathStr = StaticFileUtils.splitPathToString(StaticFileUtils.joinPath(pathComps));
                    System.out.printf("Changing directory to %s\n", pathStr);
                    fs.setWorkingDirectory(new Path(pathStr));
                    continue;
                }
                if (cmd.equals("cdout")) {
                    List<String> pathComps = new ArrayList<String>();
                    pathComps.add(HadoopLogsConfigs.getMapreduceOutputPrefix());
                    pathComps.add("lb_logs_split");
                    if (args.length >= 2) {
                        pathComps.add(args[1]);
                    }
                    String pathStr = StaticFileUtils.splitPathToString(StaticFileUtils.joinPath(pathComps));
                    System.out.printf("Changing directory to %s\n", pathStr);
                    fs.setWorkingDirectory(new Path(pathStr));
                    continue;
                }
                if (cmd.equals("pwd")) {
                    System.out.printf("%s\n", fs.getWorkingDirectory().toUri().toString());
                    continue;
                }
                if (cmd.equals("cat") && args.length >= 2) {
                    String pathStr = args[1];
                    Path filePath = new Path(pathStr);
                    FSDataInputStream is = fs.open(filePath);
                    StaticFileUtils.copyStreams(is, System.out, null, PAGESIZE);
                    is.close();
                    continue;
                }
                if (cmd.equals("chmod") && args.length >= 3) {
                    String octMal = args[1];
                    Path path = new Path(args[2]);
                    short oct = (short) Integer.parseInt(octMal, 8);
                    fs.setPermission(path, new FsPermission(oct));
                    System.out.printf("Setting permisions on file %s\n", path.toUri().toString());
                    continue;
                }
                if (cmd.equals("chown") && args.length >= 4) {
                    String fUser = args[1];
                    String fGroup = args[2];
                    String fPath = args[3];
                    fs.setOwner(new Path(fPath), fUser, fGroup);
                    System.out.printf("Setting owner of %s to %s:%s\n", fPath, fUser, fGroup);
                    continue;
                }
                if (cmd.equals("mkdir") && args.length >= 2) {
                    String fPath = args[1];
                    boolean resp = fs.mkdirs(new Path(fPath));
                    System.out.printf("mkdir %s = %s\n", fPath, resp);
                    continue;
                }
                if (cmd.equals("rm") && args.length >= 2) {
                    String fPath = args[1];
                    boolean resp = fs.delete(new Path(fPath), false);
                    System.out.printf("rm %s = %s\n", fPath, resp);
                    continue;
                }
                if (cmd.equals("rmdir") && args.length >= 2) {
                    String fPath = args[1];
                    boolean resp = fs.delete(new Path(fPath), true);
                    System.out.printf("rmdir %s = %s\n", fPath, resp);
                    continue;
                }
                if (cmd.equals("homedir")) {
                    System.out.printf("%s\n", fs.getHomeDirectory().toUri().toString());
                    continue;
                }
                if (cmd.equals("cpld") && args.length >= 3) {
                    String inDirName = StaticFileUtils.expandUser(args[1]);
                    String outDir = args[2];
                    short nReplications = (args.length > 3) ? (short) Integer.parseInt(args[3]) : fs.getDefaultReplication();
                    long blockSize = (args.length > 4) ? Long.parseLong(args[4]) : fs.getDefaultBlockSize();
                    File inDir = new File(inDirName);
                    File[] files = inDir.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        File file = files[i];
                        String inFileName = file.getName();
                        String fullInPath = inDirName + "/" + inFileName;
                        if (file.isDirectory() || !file.isFile()) {
                            System.out.printf("Skipping %s since its not a file\n", fullInPath);
                            continue;
                        }
                        String fullOutPath = String.format("%s/%s", outDir, inFileName);
                        System.out.printf("Copying %s to %s\n", fullInPath, fullOutPath);
                        long fSize = new File(fullInPath).length();
                        InputStream is = StaticFileUtils.openDataInputStreamFile(fullInPath);
                        FSDataOutputStream os = fs.create(new Path(fullOutPath), true, HDFSBUFFSIZE, nReplications, blockSize);
                        StaticFileUtils.copyStreams(is, os, System.out, fSize, ONEMEG);
                        System.out.printf("Finished with file\n");
                        is.close();
                        os.close();
                        continue;
                    }
                    continue;
                }
                if (cmd.equals("cpfl") && args.length >= 3) {
                    String outPathStr = args[2];
                    String inPathStr = args[1];
                    long fSize = new File(StaticFileUtils.expandUser(inPathStr)).length();
                    InputStream is = StaticFileUtils.openInputFile(inPathStr);
                    FSDataOutputStream os;
                    short nReplications = (args.length > 3) ? (short) Integer.parseInt(args[3]) : fs.getDefaultReplication();
                    long blockSize = (args.length > 4) ? Long.parseLong(args[4]) : fs.getDefaultBlockSize();
                    System.out.printf("Copying with %d replications and blocksize of %d\n", nReplications, blockSize);
                    os = fs.create(new Path(outPathStr), true, HDFSBUFFSIZE, nReplications, blockSize);
                    StaticFileUtils.copyStreams(is, os, System.out, fSize, ONEMEG);
                    System.out.printf("copyed %s -> %s\n", inPathStr, outPathStr);
                    is.close();
                    os.close();
                    continue;
                }
                if (cmd.equals("cptl") && args.length >= 3) {
                    FSDataInputStream is = fs.open(new Path(args[1]), HDFSBUFFSIZE);
                    OutputStream os = StaticFileUtils.openOutputFile(args[2]);
                    StaticFileUtils.copyStreams(is, os, System.out, HDFSBUFFSIZE);
                    is.close();
                    os.close();
                    continue;
                }
                if (cmd.equals("findCp")) {
                    if (args.length >= 2) {
                        String className = args[1];
                        String classPath = Debug.findClassPath(className, jobClassLoader);
                        System.out.printf("%s classpath = %s\n", className, classPath);
                        continue;
                    }
                    String classPath = System.getProperties().getProperty("java.class.path");
                    System.out.printf("classpath = %s\n", classPath);
                    continue;
                }
                if (cmd.equals("setJobJar") && args.length >= 2) {
                    String jarName = StaticFileUtils.expandUser(args[1]);
                    if (jobClassLoader != null) {
                        System.out.printf("jobJar already set to %s\n", jobJarName);
                        continue;
                    }
                    File jarFile = new File(jarName).getAbsoluteFile();
                    if (!jarFile.canRead()) {
                        System.out.printf("Can't read file %s\n", jarFile.getAbsolutePath());
                        continue;
                    }
                    URL jarUrl = jarFile.toURI().toURL();
                    jobClassLoader = new URLClassLoader(new URL[]{jarUrl}, HdfsCli.class.getClassLoader());
                    System.out.printf("Loaded %s as jobJar\n", jarName);
                    continue;
                }
                if (cmd.equals("showCl") && args.length >= 2) {
                    String className = args[1];
                    if (jobClassLoader == null) {
                        System.out.printf("jobJar not yet set\n");
                    }
                    Class classIn = Class.forName(className, true, jobClassLoader);
                    String classLoaderInfo = Debug.classLoaderInfo(className);
                    System.out.printf("%s\n", classLoaderInfo);
                    continue;
                }
                if (cmd.equals("countLines") && args.length >= 3) {
                    String fileName = args[1];
                    int nTicks = Integer.valueOf(args[2]);
                    int buffSize = (args.length > 3) ? Integer.valueOf(args[3]) : PAGESIZE * 4;
                    System.out.printf("Counting the lines from file %s with %d ticks", fileName, nTicks);
                    double startTime = Debug.getEpochSeconds();
                    long nLines = HdfsCliHelpers.countLines(fileName, nTicks, buffSize);
                    double endTime = Debug.getEpochSeconds();
                    System.out.printf("Took %f seconds to count %d lines\n", endTime - startTime, nLines);
                    continue;
                }


                if (cmd.equals("compressLzo") && args.length >= 3) {
                    String srcFileName = args[1];
                    String dstFileName = args[2];
                    int buffsize = (args.length >= 5) ? Integer.parseInt(args[4]) : 4096;
                    InputStream fis = StaticFileUtils.openInputFile(srcFileName);
                    OutputStream fos = StaticFileUtils.openOutputFile(dstFileName);
                    System.out.printf("Attempting to compress %s to file %s\n", srcFileName, dstFileName);
                    LzopCodec codec = new LzopCodec();
                    codec.setConf(conf);
                    CompressionOutputStream cos = codec.createOutputStream(fos);
                    double startTime = Debug.getEpochSeconds();
                    StaticFileUtils.copyStreams(fis, cos, System.out, 1024 * 1024 * 64);
                    double endTime = Debug.getEpochSeconds();
                    System.out.printf("Compression took %f seconds\n", endTime - startTime);
                    fis.close();
                    cos.finish();
                    cos.close();
                    fos.close();
                    continue;
                }


                if (cmd.equals("indexLzo") && args.length >= 2) {
                    String srcFileName = args[1];
                    Path filePath = new Path(StaticFileUtils.expandUser(srcFileName));
                    System.out.printf("Indexing file %s\n", srcFileName);
                    double startTime = Debug.getEpochSeconds();
                    LzoIndex.createIndex(lfs, filePath);
                    double endTime = Debug.getEpochSeconds();
                    System.out.printf("Took %f seconds to index file %s\n", endTime - startTime, srcFileName);
                    continue;
                }
                if (cmd.equals("printReducers") && args.length >= 2) {
                    String sequenceDirectory = args[1];
                    List<LogReducerOutputValue> zipFileInfoList = hdfsUtils.getZipFileInfoList(sequenceDirectory);
                    int totalEntryCount = zipFileInfoList.size();
                    int entryNum = 0;
                    for (LogReducerOutputValue zipFileInfo : zipFileInfoList) {
                        System.out.printf("zipFile[%d]=%s\n", entryNum, zipFileInfo.toString());
                        entryNum++;
                    }

                    System.out.printf("Total entries = %d\n", totalEntryCount);
                    continue;
                }
                if (cmd.equals("scanLines") && args.length >= 3) {
                    String fileName = args[1];
                    int nLines = Integer.parseInt(args[2]);
                    int nTicks = Integer.parseInt(args[3]);
                    BufferedReader r = new BufferedReader(new FileReader(StaticFileUtils.expandUser(fileName)), HDFSBUFFSIZE);
                    int badLines = 0;
                    int goodLines = 0;
                    int lineCounter = 0;
                    int totalLines = 0;
                    int totalGoodLines = 0;
                    int totalBadLines = 0;
                    LogMapperOutputValue logValue = new LogMapperOutputValue();
                    double startTime = StaticDateTimeUtils.getEpochSeconds();
                    for (int i = 0; i < nLines; i++) {
                        String line = r.readLine();

                        if (line == null) {
                            break; // End of file
                        }
                        try {
                            LogChopper.getLogLineValues(line, logValue);
                            goodLines++;
                            totalGoodLines++;
                        } catch (Exception ex) {
                            badLines++;
                            totalBadLines++;
                            System.out.printf("BAD=%s\n", line);
                        }
                        lineCounter++;
                        totalLines++;
                        if (i % nTicks == 0) {
                            double stopTime = StaticDateTimeUtils.getEpochSeconds();
                            double lps = (double) lineCounter / (stopTime - startTime);
                            System.out.printf("read %d lines goodlines=%d badlines=%d secs = %f linespersecond=%f\n", lineCounter, goodLines, badLines, stopTime - startTime, lps);
                            startTime = stopTime;
                            lineCounter = 0;
                            goodLines = 0;
                            badLines = 0;
                        }
                    }
                    System.out.printf("Good=%d badLines=%d total = %d\n", totalGoodLines, totalBadLines, totalLines);

                    r.close();
                    continue;
                }
                if (cmd.equals("showCrc") && args.length >= 2) {
                    String fileName = StaticFileUtils.expandUser(args[1]);
                    BufferedInputStream is = new BufferedInputStream(new FileInputStream(fileName), BUFFER_SIZE);
                    long crc = StaticFileUtils.computeCrc(is);
                    System.out.printf("crc(%s)=%d\n", fileName, crc);
                    is.close();
                    continue;
                }
                if (cmd.equals("du")) {
                    long used = fs.getUsed();
                    System.out.printf("Used bytes: %s\n", Debug.humanReadableBytes(used));
                    continue;
                }
                if (cmd.equals("setReplCount") && args.length >= 3) {
                    String fileName = args[1];
                    Path filePath = new Path(fileName);
                    short replCount = Short.parseShort(args[2]);
                    System.out.printf("Setting Replication count for file %s to %d\n", fileName, replCount);
                    fs.setReplication(filePath, replCount);
                    continue;
                }
                if (cmd.equals("dumpConfig") && args.length >= 2) {
                    System.out.printf("Dumping configs\n");
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(StaticFileUtils.expandUser(args[1]))), HDFSBUFFSIZE);
                    Configuration dumpConf = new Configuration();
                    for (int i = 2; i < args.length; i++) {
                        dumpConf.addResource(new Path(StaticFileUtils.expandUser(args[i])));
                    }
                    dumpConf.writeXml(bos);
                    bos.close();
                    dumpConf.writeXml(System.out);
                    continue;
                }
                if (cmd.equals("lineIndex") && args.length >= 2) {
                    String inFileName = StaticFileUtils.expandUser(args[1]);
                    String outFileName = inFileName + ".idx";
                    InputStream is = StaticFileUtils.openInputFile(inFileName);
                    DataOutputStream os = StaticFileUtils.openDataOutputStreamFile(outFileName);
                    System.out.printf("Indexling file %s -> %s\n", inFileName, outFileName);
                    HdfsCliHelpers.indexFile(is, os, PAGESIZE * 8);
                    is.close();
                    os.close();
                    continue;
                }
                if (cmd.equals("rebasePath") && args.length >= 4) {
                    String srcBase = args[1];
                    String srcPath = args[2];
                    String dstPath = args[3];
                    System.out.printf("calling StaticFileUtils.rebasePath(%s,%s,%s)=", srcBase, srcPath, dstPath);
                    System.out.flush();
                    String rebasedPath = StaticFileUtils.rebaseSplitPath(srcBase, srcPath, dstPath);
                    System.out.printf("%s\n", rebasedPath);
                    continue;
                }
                if (cmd.equals("joinPath") && args.length >= 1) {
                    List<String> pathComps = new ArrayList<String>();
                    for (int i = 1; i < args.length; i++) {
                        pathComps.add(args[i]);
                    }
                    List<String> joinedPathList = StaticFileUtils.joinPath(pathComps);
                    String joinPathString = StaticFileUtils.splitPathToString(joinedPathList);
                    System.out.printf("joinedPath = %s\n", joinPathString);
                    continue;
                }
                System.out.printf("Unrecognized command\n");
                continue;
            } catch (Exception ex) {
                System.out.printf("Exception: %s\n", Debug.getExtendedStackTrace(ex));
            }
        }
        System.out.printf("Exiting\n");
    }

    private static String chop(String line) {
        return line.replace("\r", "").replace("\n", "");
    }

    private static String[] stripBlankArgs(String line) {
        int nargs = 0;
        int i;
        int j;
        String[] argsIn = line.replace("\r", "").replace("\n", "").split(" ");
        for (i = 0; i < argsIn.length; i++) {
            if (argsIn[i].length() > 0) {
                nargs++;
            }
        }
        String[] argsOut = new String[nargs];
        j = 0;
        for (i = 0; i < argsIn.length; i++) {
            if (argsIn[i].length() > 0) {
                argsOut[j] = argsIn[i];
                j++;
            }
        }
        return argsOut;
    }

    private static Map<String, String> argMapper(String[] args) {
        Map<String, String> argMap = new HashMap<String, String>();
        for (String arg : args) {
            String[] kwArg = arg.split("=");
            if (kwArg.length == 2) {
                argMap.put(kwArg[0], kwArg[1]);
            }
        }
        return argMap;
    }

    private static boolean stdinMatches(BufferedReader stdin, String val) throws IOException {
        String[] resp = stripBlankArgs(stdin.readLine());
        return (resp.length > 0 && resp[0].equalsIgnoreCase(val));
    }
}