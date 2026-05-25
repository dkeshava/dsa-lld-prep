
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

// enum LogLevel{
//     INFO,
//     DEBUG,
//     ERROR,
//     FATAL
// }

// class LogMessage{
//     String message;
//     LogLevel level;
//     long timestamp;
//     public LogMessage(String msg, LogLevel level){
//         this.message=msg;
//         this.level=level;
//         this.timestamp=System.currentTimeMillis();
//     }
//     public void getMessage(){
//         System.out.println("current LogMessage: "+message);
//     }  
//     public String toString(){
//         return "["+level+"] "+timestamp+" - "+message;
//     } 
// }

// interface LogAppender{
//     void append(LogMessage message); 
//     LogLevel getLevel();
// }

// class ConsoleAppender implements LogAppender{
//     //should only print info logs
//     LogLevel level;
//     public ConsoleAppender(LogLevel level){
//         this.level=level;
//     }
//     public LogLevel getLevel(){
//         return level;
//     }
//     public void append(LogMessage msg){
//         //if(msg.level!=LogLevel.INFO) return;
//         if(msg.level.ordinal()<level.ordinal()) return;
//         System.out.println(msg.level+": "+msg.message+": being printed in console");
//     }
// }

// class FileAppender implements LogAppender{
//     //should only print error and fatal logs
//     private String filepath;
//     private LogLevel level;
//     public FileAppender(String filepath,LogLevel level){
//         this.filepath=filepath;
//         this.level=level;
//     }
//     public LogLevel getLevel(){
//         return level;
//     }
//     public void append(LogMessage message){
//         //if(message.level!=LogLevel.ERROR && message.level!=LogLevel.FATAL) return;
//         if(message.level.ordinal()<level.ordinal()) return;
//         try(FileWriter writer=new FileWriter(filepath,true)){
//             writer.write(message.toString());
//             writer.write("\n");
//         } catch (Exception e) {
//             System.out.println(
//                 "An error occurred while writing"
//                 + " to the file: " + e.getMessage());
//         }
//     }
// }

// class LoggerConfig{
//     LogLevel level;
//     List<LogAppender> appenders;
//     public LoggerConfig(LogLevel lvl, List<LogAppender> appenders){
//         this.level=lvl;
//         this.appenders=appenders;
//     }
//     public LogLevel getLevel(){
//         return level;
//     }
//     public void setConfig(LogLevel lvl){
//         this.level=lvl;
//     }
//     public List<LogAppender> getAppenders(){
//         return appenders;
//     }
// }

// class Logger{
//     private static Logger instance;
//     private LoggerConfig config;
//     private BlockingQueue<LogMessage> queue;
//     private volatile boolean isRunning=true;

//     private Logger(LoggerConfig loggerConfig){
//         this.config=loggerConfig;
//         this.queue=new LinkedBlockingQueue<>();

//         startWorker();
//     }
//     public static Logger getInstance(LoggerConfig config){
//         if(instance==null){
//             instance=new Logger(config);
//         }
//         return instance;
//     }
//     private void startWorker(){
//         Thread worker=new Thread(()->{
//             while (isRunning || !queue.isEmpty()) { 
//                 try {
//                     LogMessage msg=queue.poll(500, TimeUnit.MILLISECONDS);
//                     if(msg!=null){
//                         for(LogAppender appender: config.getAppenders()){
//                             appender.append(msg);       
//                         }
//                     }
//                 } catch (InterruptedException e) {
//                     Thread.currentThread().interrupt();
//                     break;
//                 }
//             }});
//         //worker.setDaemon(true);
//         worker.start();
//     }
//     public void log(String message, LogLevel lvl){
//         //Global filter
//         if(lvl.ordinal()<config.getLevel().ordinal()) return;
//         LogMessage logMessage=new LogMessage(message, lvl);
//         queue.offer(logMessage);
//     }
//     public void shutdown(){
//         isRunning=false;
//     }
// }

// public class Logger_basic {
//     public static void main(String[] args) {
//         List<LogAppender> appenders;
//         appenders=new ArrayList<>();
//         LogAppender consoleAppender=new ConsoleAppender(LogLevel.INFO);
//         String filepath="/Users/dhondikeshava/Documents/Nicco Vid/logs.log";
//         LogAppender fileAppender=new FileAppender(filepath,LogLevel.ERROR);
//         appenders.add(consoleAppender);
//         appenders.add(fileAppender);
//         LoggerConfig config=new LoggerConfig(LogLevel.INFO, appenders);

//         Logger logger=Logger.getInstance(config);

//         //config.setConfig(LogLevel.ERROR);
//         //System.out.println("second config");
//         logger.log("Info new log", LogLevel.INFO);
//         logger.log("Error new log", LogLevel.ERROR);
//         try {
//             Thread.sleep(1000);     
//         } catch (InterruptedException e) {}
//         logger.shutdown();
//     }
// }

enum LogLevel{
    INFO, 
    DEBUG,
    ERROR,
    FATAL
}

class LogMessage{
    String message;
    LogLevel level;
    long timestamp;
    public LogMessage(String msg, LogLevel lvl){
        this.message=msg;
        this.level=lvl;
        this.timestamp=System.currentTimeMillis();
    }
    public LogLevel getLevel(){
        return level;
    }
    public String toString(){
        return "[ "+level+" ] - "+timestamp+" - "+message;
    }
}

interface LogAppender{
    void append(LogMessage msg);
    LogLevel getLevel();
}

class ConsoleAppender implements LogAppender{
    LogLevel level;
    public ConsoleAppender(LogLevel lvl){
        this.level=lvl;
    }
    public void append(LogMessage msg){
        if(msg.level.ordinal()<level.ordinal()) return;
        System.out.println(msg.message+" - being printed in console");
    }
    public LogLevel getLevel(){
        return level;
    }
}

class FileAppender implements LogAppender{
    String filepath;
    LogLevel level;
    public FileAppender(String filepath, LogLevel level){
        this.filepath=filepath;
        this.level=level;
    }
    public void append(LogMessage msg){
        if(msg.level.ordinal()<level.ordinal()) return;
        try (FileWriter writer = new FileWriter(filepath, true)) { 
                // true for append mode
                writer.write(msg.toString());
                writer.write("\n");
        } 
        catch (Exception e) {
            System.out.println(
                    "An error occurred while writing"
                    + " to the file: " + e.getMessage());
            }
        }
    public LogLevel getLevel(){
        return level;
    }
}

class LoggerConfig{
    LogLevel level;
    List<LogAppender> appenders;
    public LoggerConfig(LogLevel level, List<LogAppender> appenders){
        this.level=level;
        this.appenders=appenders;
    }
    public void setLevel(LogLevel lvl){
        this.level=lvl;
    }
    public List<LogAppender> getAppenders(){
        return appenders;
    }
}

class Logger{
    private static Logger instance;
    private LoggerConfig config;
    private BlockingQueue<LogMessage> queue;
    private volatile boolean isRunning=true;
    private Thread worker;
    public static Logger getInstance(LoggerConfig config){
        if(instance==null){
            synchronized (Logger.class) {
                if(instance==null) instance=new Logger(config);
            }
        }
        return instance;
    }
    private Logger(LoggerConfig config){
        this.config=config;
        this.queue=new LinkedBlockingQueue<>();

        startWorker();
    }
    public void log(LogLevel level, String msg){
        if(level.ordinal()<config.level.ordinal()) return;
        LogMessage logmsg=new LogMessage(msg, level);
        queue.offer(logmsg);
    }
    private void startWorker(){
        worker=new Thread(()->{
            while(isRunning || !queue.isEmpty()){
                try {
                    LogMessage msg=queue.poll(500, TimeUnit.MILLISECONDS);
                    if(msg!=null){
                        for(LogAppender appender: config.getAppenders()){
                            appender.append(msg);
                        }
                    }
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        worker.start();
    }
    public void shutdown(){
        isRunning=false;
        try {
            worker.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class Logger_basic{
    public static void main(String[] args) {
        LogAppender consoleAppender=new ConsoleAppender(LogLevel.INFO);
        LogAppender fileAppender=new FileAppender("/Users/dhondikeshava/Documents/Nicco Vid/logs.log", LogLevel.ERROR);
        List<LogAppender> appenders=new ArrayList<>();
        appenders.add(fileAppender);
        appenders.add(consoleAppender);
        LoggerConfig config=new LoggerConfig(LogLevel.DEBUG, appenders);
        Logger logger=Logger.getInstance(config);

        logger.log(LogLevel.INFO, "info log");
        logger.log(LogLevel.DEBUG, "Debug log");
        logger.log(LogLevel.ERROR, "Error log");
        logger.log(LogLevel.FATAL, "Fatal log");
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }
        logger.shutdown();
    }
}