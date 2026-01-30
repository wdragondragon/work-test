package org.example.worktest.flink;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.example.worktest.flink.config.FlinkProperties;
import org.example.worktest.flink.exception.FlinkJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class FlinkJobSubmitter {

    private static final Logger log = LoggerFactory.getLogger(FlinkJobSubmitter.class);

    public static void main(String[] args) {
        if (args.length == 0) {
            runWithDefaultConfig();
            return;
        }

        if (args.length == 1 && ("--help".equals(args[0]) || "-h".equals(args[0]))) {
            printHelp();
            return;
        }

        try {
            CommandLineArgs cliArgs = parseArgs(args);
            if (cliArgs.isStopJob()) {
                stopJob(cliArgs);
            } else {
                submitJob(cliArgs);
            }
        } catch (Exception e) {
            log.error("Failed to execute command", e);
            printHelp();
            System.exit(1);
        }
    }

    private static void runWithDefaultConfig() {
        log.info("No arguments provided, using default configuration");
        
        Configuration config = new Configuration();
        config.set(RestOptions.ADDRESS, "192.168.188.128");
        config.set(RestOptions.PORT, 8081);

        FlinkJobService service = new FlinkJobService(config);

        String jobId = service.start(
                "test-job",
                "C:\\dev\\ideaProject\\flink-cdc\\target\\flink-cdc-1.0-SNAPSHOT.jar",
                "com.jdragon.flinkcdc.MySqlCdcToMySqlJob"
        );

        log.info("jobId = {}", jobId);
    }

    private static void submitJob(CommandLineArgs args) {
        ConfigurableApplicationContext context = null;
        try {
            context = SpringApplication.run(FlinkJobSubmitter.class, new String[]{});
            
            FlinkJobService service = context.getBean(FlinkJobService.class);
            
            Configuration customConfig = null;
            if (args.getFlinkHost() != null || args.getFlinkPort() > 0) {
                customConfig = new Configuration();
                if (args.getFlinkHost() != null) {
                    customConfig.set(RestOptions.ADDRESS, args.getFlinkHost());
                }
                if (args.getFlinkPort() > 0) {
                    customConfig.set(RestOptions.PORT, args.getFlinkPort());
                }
            }
            
            String jobId;
            if (args.getProgramArgs() != null && args.getProgramArgs().length > 0) {
                jobId = service.startWithArgs(
                    args.getJobName(),
                    args.getJarPath(),
                    args.getEntryClass(),
                    args.getProgramArgs(),
                    args.getParallelism(),
                    args.getSavepoint()
                );
            } else {
                jobId = service.start(
                    args.getJobName(),
                    args.getJarPath(),
                    args.getEntryClass(),
                    args.getParallelism(),
                    args.getSavepoint(),
                    customConfig
                );
            }
            
            log.info("Job submitted successfully. Job ID: {}", jobId);
            
        } finally {
            if (context != null) {
                context.close();
            }
        }
    }

    private static void stopJob(CommandLineArgs args) {
        ConfigurableApplicationContext context = null;
        try {
            context = SpringApplication.run(FlinkJobSubmitter.class, new String[]{});
            
            FlinkJobService service = context.getBean(FlinkJobService.class);
            
            if (StringUtils.isNotBlank(args.getSavepointDir())) {
                service.stop(args.getJobName(), args.getSavepointDir());
                log.info("Job stopped with savepoint");
            } else {
                service.cancel(args.getJobName());
                log.info("Job cancelled");
            }
            
        } finally {
            if (context != null) {
                context.close();
            }
        }
    }

    private static CommandLineArgs parseArgs(String[] args) {
        CommandLineArgs cliArgs = new CommandLineArgs();
        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            
            switch (arg) {
                case "--job-name":
                case "-n":
                    cliArgs.setJobName(getNextArg(args, i++));
                    break;
                case "--jar-path":
                case "-j":
                    cliArgs.setJarPath(getNextArg(args, i++));
                    break;
                case "--entry-class":
                case "-c":
                    cliArgs.setEntryClass(getNextArg(args, i++));
                    break;
                case "--parallelism":
                case "-p":
                    cliArgs.setParallelism(Integer.parseInt(getNextArg(args, i++)));
                    break;
                case "--savepoint":
                case "-s":
                    cliArgs.setSavepoint(getNextArg(args, i++));
                    break;
                case "--savepoint-dir":
                    cliArgs.setSavepointDir(getNextArg(args, i++));
                    break;
                case "--program-args":
                    cliArgs.setProgramArgs(parseProgramArgs(getNextArg(args, i++)));
                    break;
                case "--flink-host":
                    cliArgs.setFlinkHost(getNextArg(args, i++));
                    break;
                case "--flink-port":
                    cliArgs.setFlinkPort(Integer.parseInt(getNextArg(args, i++)));
                    break;
                case "--stop":
                    cliArgs.setStopJob(true);
                    break;
                default:
                    if (arg.startsWith("-")) {
                        throw new IllegalArgumentException("Unknown option: " + arg);
                    }
            }
        }
        
        if (!cliArgs.isStopJob()) {
            if (StringUtils.isBlank(cliArgs.getJobName())) {
                throw new IllegalArgumentException("Job name is required (--job-name)");
            }
            if (StringUtils.isBlank(cliArgs.getJarPath())) {
                throw new IllegalArgumentException("JAR path is required (--jar-path)");
            }
            if (StringUtils.isBlank(cliArgs.getEntryClass())) {
                throw new IllegalArgumentException("Entry class is required (--entry-class)");
            }
        } else {
            if (StringUtils.isBlank(cliArgs.getJobName())) {
                throw new IllegalArgumentException("Job name is required for stop operation (--job-name)");
            }
        }
        
        return cliArgs;
    }
    
    private static String getNextArg(String[] args, int currentIndex) {
        if (currentIndex + 1 >= args.length) {
            throw new IllegalArgumentException("Missing value for option: " + args[currentIndex]);
        }
        return args[currentIndex + 1];
    }
    
    private static String[] parseProgramArgs(String argsString) {
        return argsString.split("\\s+");
    }
    
    private static void printHelp() {
        System.out.println("Flink Job Submitter - Command Line Interface");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  java -jar <jar> [options]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --job-name, -n <name>        Job name (required)");
        System.out.println("  --jar-path, -j <path>        Path to job JAR file (required)");
        System.out.println("  --entry-class, -c <class>    Entry class name (required)");
        System.out.println("  --parallelism, -p <num>      Parallelism (default: 1)");
        System.out.println("  --savepoint, -s <path>       Savepoint path to restore from");
        System.out.println("  --program-args <args>        Program arguments (space separated)");
        System.out.println("  --flink-host <host>          Flink REST API host (default: from config)");
        System.out.println("  --flink-port <port>          Flink REST API port (default: from config)");
        System.out.println();
        System.out.println("Stop/Cancel Options:");
        System.out.println("  --stop                       Stop or cancel the job");
        System.out.println("  --savepoint-dir <dir>        Directory for savepoint (required for stop)");
        System.out.println();
        System.out.println("Other:");
        System.out.println("  --help, -h                   Show this help message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  Submit job: java -jar app.jar --job-name myjob --jar-path /path/to.jar --entry-class com.example.Job");
        System.out.println("  Submit with args: java -jar app.jar -n myjob -j /path.jar -c com.example.Job --parallelism 4 --program-args \"arg1 arg2\"");
        System.out.println("  Stop with savepoint: java -jar app.jar --stop --job-name myjob --savepoint-dir /tmp/savepoints");
        System.out.println("  Cancel job: java -jar app.jar --stop --job-name myjob");
    }
    
    static class CommandLineArgs {
        private String jobName;
        private String jarPath;
        private String entryClass;
        private int parallelism = 1;
        private String savepoint;
        private String savepointDir;
        private String[] programArgs;
        private String flinkHost;
        private int flinkPort = -1;
        private boolean stopJob = false;
        
        public String getJobName() { return jobName; }
        public void setJobName(String jobName) { this.jobName = jobName; }
        
        public String getJarPath() { return jarPath; }
        public void setJarPath(String jarPath) { this.jarPath = jarPath; }
        
        public String getEntryClass() { return entryClass; }
        public void setEntryClass(String entryClass) { this.entryClass = entryClass; }
        
        public int getParallelism() { return parallelism; }
        public void setParallelism(int parallelism) { this.parallelism = parallelism; }
        
        public String getSavepoint() { return savepoint; }
        public void setSavepoint(String savepoint) { this.savepoint = savepoint; }
        
        public String getSavepointDir() { return savepointDir; }
        public void setSavepointDir(String savepointDir) { this.savepointDir = savepointDir; }
        
        public String[] getProgramArgs() { return programArgs; }
        public void setProgramArgs(String[] programArgs) { this.programArgs = programArgs; }
        
        public String getFlinkHost() { return flinkHost; }
        public void setFlinkHost(String flinkHost) { this.flinkHost = flinkHost; }
        
        public int getFlinkPort() { return flinkPort; }
        public void setFlinkPort(int flinkPort) { this.flinkPort = flinkPort; }
        
        public boolean isStopJob() { return stopJob; }
        public void setStopJob(boolean stopJob) { this.stopJob = stopJob; }
    }
}


