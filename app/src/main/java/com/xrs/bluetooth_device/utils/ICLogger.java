package com.xrs.bluetooth_device.utils;

import android.content.Context;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import de.mindpipe.android.logging.log4j.LogConfigurator;


public class ICLogger {

    private static final String TAG = "ICLogger";
    private static boolean isInitConfig = false;
    private static Context mContext = null;

    private static Logger Log = null;
    private static Logger GPS = null;
    private static Logger Nmea = null;
    private static Logger APILog = null;

    private static Logger alarmCycle = null;
    private static Logger Battery = null;
    private static Logger LBS = null;
    //  private static java.util.logging.Logger originLog= java.util.logging.Logger.getLogger(ICLogger.class.getSimpleName());
    private static final boolean DEBUG = true;    //BuildConfig.DEBUG;

    public static void setContext(Context context) {
        mContext = context;
    }

    private static String getFunctionName() {
        StackTraceElement[] currentStack = Thread.currentThread().getStackTrace();
        int stackLen = currentStack.length;
        for (int i = 0; i < stackLen; ++i) {
            StackTraceElement stackElement = currentStack[i];
            if (!stackElement.isNativeMethod() && !stackElement.getClassName().equals(Thread.class.getName()) && !stackElement.getClassName().equals(ICLogger.class.getName())) {
                StringBuilder sb = new StringBuilder();
                sb.append("[ ");
                sb.append(Thread.currentThread().getName());
                sb.append(": ");
                sb.append(stackElement.getFileName());
                sb.append(":");
                sb.append(stackElement.getLineNumber());
                sb.append(" ");
                sb.append(stackElement.getMethodName());
                sb.append(" ]");
                return sb.toString();
            }
        }
        return null;
    }


    private static final Object mLock = new Object();

    /**
     * 初始化与配置Logger   Log4j
     * 日志信息格式中符号所代表的含义
     * （1）－X号: X信息输出时左对齐。
     * （2）%p: 输出日志信息优先级，即DEBUG，INFO，WARN，ERROR，FATAL。
     * （3）%d: 输出日志时间点的日期或时间，默认格式为ISO8601，也可以在其后指定格式，比如：%d{yyy MMM dd HH:mm:ss,SSS}，输出类似：2002年10月18日 22：10：28，921。
     * （4）%r: 输出自应用启动到输出该log信息耗费的毫秒数。
     * （5）%c: 输出日志信息所属的类目，通常就是所在类的全名。
     * （6）%t: 输出产生该日志事件的线程名。
     * （7）%l: 输出日志事件的发生位置，相当于%C.%M(%F:%L)的组合,包括类目名、发生的线程，以及在代码中的行数。举例：Testlog4.main(TestLog4.java:10)。
     * （8）%x: 输出和当前线程相关联的NDC(嵌套诊断环境),尤其用到像java servlets这样的多客户多线程的应用中。
     * （9）%%: 输出一个"%"字符。
     * （10）%F: 输出日志消息产生时所在的文件名称。
     * （11）%L: 输出代码中的行号。
     * （12）%m: 输出代码中指定的消息,产生的日志具体信息。
     * （13）%n: 输出一个回车换行符，Windows平台为"\r\n"，Unix平台为"\n"输出日志信息换行。
     * 可以在%与模式字符之间加上修饰符来控制其最小宽度、最大宽度、和文本的对齐方式。如：
     * (1)%20c：指定输出category的名称，最小的宽度是20，如果category的名称小于20的话，默认的情况下右对齐。
     * (2)%-20c:指定输出category的名称，最小的宽度是20，如果category的名称小于20的话，"-"号指定左对齐。
     * (3)%.30c:指定输出category的名称，最大的宽度是30，如果category的名称大于30的话，就会将左边多出的字符截掉，但小于30的话也不会有空格。
     * (4)%20.30c:如果category的名称小于20就补空格，并且右对齐，如果其名称长于30字符，就从左边交远销出的字符截掉。
     */
    private static void initConfig() {
        //用double check 提高效率
        if (isInitConfig) {
            return;
        }
        synchronized (mLock) {
            if (!isInitConfig) {
                isInitConfig = true;
                LogConfigurator logConfigurator = new LogConfigurator();
                String filename = StorageUtils.getExternalStorageLogDir() + File.separator + "log.txt";
                logConfigurator.setFileName(filename);// 日志存储路径，如果该路径没有会自动创建文件
                logConfigurator.setRootLevel(Level.ALL);
                logConfigurator.setLevel("log", Level.ALL);
                //logConfigurator.setFilePattern("%d %-5p [%c{2}]-[%L] %m%n");// 设置文件输出模式
                logConfigurator.setFilePattern("%d %m%n ");// 设置文件输出模式
                logConfigurator.setMaxBackupSize(2); // 设置最大备份数量
                logConfigurator.setMaxFileSize(1024 * 1024 * 20);// 设置最大文件大小  20MB
                logConfigurator.setImmediateFlush(true);// 设置是否立即刷新
                logConfigurator.setUseFileAppender(true); // 设置用户文件输出器
                logConfigurator.setInternalDebugging(false); //控制console是否输出
                logConfigurator.setUseLogCatAppender(true);
                logConfigurator.setLogCatPattern("%m%n");
                logConfigurator.configure();// 设置提交
//                android.util.Log.d("CATCH", android.util.Log.getStackTraceString(new ThreadDeath()));
                //Log 和log目前是同一个配置
                Log = Logger.getLogger("log");
                Log.info("init Logger " + Log.getName());

                GPS = Logger.getLogger("GPS");
                GPS.addAppender(getRollingFileAppender("GPS", "%d %m%n "));
                GPS.info("init Logger " + GPS.getName() + " " + ((RollingFileAppender) GPS.getAppender("GPS")).getFile());

                Nmea = Logger.getLogger("Nmea");
                Nmea.addAppender(getRollingFileAppender("Nmea", "%m%n"));
                Nmea.info("init Logger " + Nmea.getName() + " " + ((RollingFileAppender) Nmea.getAppender("Nmea")).getFile());

                APILog = Logger.getLogger("APILog");
                APILog.addAppender(getRollingFileAppender("APILog", "%d %m%n "));
                APILog.info("init Logger " + APILog.getName() + " " + ((RollingFileAppender) APILog.getAppender("APILog")).getFile());

                alarmCycle = Logger.getLogger("alarmCycle");
                alarmCycle.addAppender(getRollingFileAppender("alarmCycle", "%d %m%n "));
                alarmCycle.info("init Logger " + alarmCycle.getName() + " " + ((RollingFileAppender) alarmCycle.getAppender("alarmCycle")).getFile());

                Battery = Logger.getLogger("Battery");
                Battery.addAppender(getRollingFileAppender("Battery", "%d %m%n "));
                Battery.info("init Logger " + Battery.getName() + " " + ((RollingFileAppender) Battery.getAppender("Battery")).getFile());


                LBS = Logger.getLogger("LBS");
                LBS.addAppender(getRollingFileAppender("LBS", "%d %m%n "));
                LBS.info("init Logger " + LBS.getName() + " " + ((RollingFileAppender) LBS.getAppender("LBS")).getFile());
                android.util.Log.d(TAG, "init finished...");
            }
        }
    }

    public static void logRAWtoFile(String filename, String msg) {
        if (filename.equals("Nmea") && Nmea != null) {
            Nmea.info(msg);
        } else if (filename.equals("LBS") && LBS != null) {
            LBS.info(msg);
        } else if (filename.equals("GPS") && GPS != null) {
            GPS.info(msg);
        } else if (filename.equals("APILog") && APILog != null) {
            APILog.info(msg);
        } else if (filename.equals("alarmCycle") && alarmCycle != null) {
            alarmCycle.info(msg);
        } else if (filename.equals("Battery") && Battery != null) {
            Battery.info(msg);
        } else if (Log != null) {
            Log.info(msg);
        }

    }

    public static void logToFile(String filename, String msg) {
        if (Nmea != null && filename.equals("Nmea")) {
            Nmea.info(msg);
        } else if (LBS != null && filename.equals("LBS")) {
            LBS.info(msg);
        } else if (GPS != null && filename.equals("GPS")) {
            GPS.info(msg);
        } else if (APILog != null && filename.equals("APILog")) {
            APILog.info(msg);
        } else if (alarmCycle != null && filename.equals("alarmCycle")) {
            alarmCycle.info(msg);
        } else if (Battery != null && filename.equals("Battery")) {
            Battery.info(msg);
        } else if (Log != null) {
            Log.info(msg);
        }
    }

    private static RollingFileAppender getRollingFileAppender(String name, String layoutpattern) {
        PatternLayout fileLayout = new PatternLayout(layoutpattern);
        String filename = StorageUtils.getExternalStorageLogDir() + File.separator + name + ".txt";
        RollingFileAppender rollingFileAppender = null;
        try {
            rollingFileAppender = new RollingFileAppender(fileLayout, filename);
        } catch (IOException e) {
            e.printStackTrace();
            ICLogger.e(e.getMessage(), e);
            return null;
        }
        rollingFileAppender.setName(name);
        rollingFileAppender.setMaxBackupIndex(2);  //最多3个备份文件
        rollingFileAppender.setMaximumFileSize(10 * 1024 * 1024); //10M 大小
        rollingFileAppender.setImmediateFlush(true);

        return rollingFileAppender;
    }

    public static void v(String msg) {
        initConfig();
        if (Log != null)
            Log.debug(buildMessage(msg));
    }

    public static void v(String msg, Throwable thr) {
        initConfig();
        if (Log != null)
            Log.debug(buildMessage(msg), thr);
    }

    public static void d(String msg) {
        initConfig();
        if (Log != null)
            Log.debug(buildMessage(msg));
    }

    public static void d(String msg, Throwable thr) {
        initConfig();
        if (Log != null)
            Log.debug(buildMessage(msg), thr);
    }

    public static void i(String msg) {
        initConfig();
        if (Log != null)
            Log.info(buildMessage(msg));
    }

    public static void i(String msg, Throwable thr) {
        initConfig();
        if (Log != null)
            Log.info(buildMessage(msg), thr);
    }

    public static void w(String msg) {
        initConfig();
        if (Log != null)
            Log.warn(buildMessage(msg));
    }

    public static void w(String msg, Throwable thr) {
        initConfig();
        if (Log != null)
            Log.warn(buildMessage(msg), thr);
    }

    public static void w(Throwable thr) {
        initConfig();
        if (Log != null)
            Log.warn(buildMessage(""), thr);
    }

    public static void e(String msg) {
        initConfig();
        if (Log != null)
            Log.error(buildMessage(msg));
    }

    public static void e(String msg, Throwable thr) {
        initConfig();
        if (Log != null)
            Log.error(buildMessage(msg), thr);
    }

    public static void PrintExceptionStackTrace(Exception e) {

        StringBuffer sb = new StringBuffer();
        sb.append(e.getMessage()).append("\n");
        e.printStackTrace(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                sb.append((char) b);
            }
        }));
        i(sb.toString());
    }

    /**
     * 生成消息
     *
     * @param msg
     * @return
     */
    protected static String buildMessage(String msg) {
        initConfig();
        if (DEBUG && msg != null) {
            String var1 = getFunctionName();
            if (var1 != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(var1);
                sb.append(" - ");
                sb.append(msg);
                return sb.toString();
            } else {
                return "functionName is null";
            }
        } else {
            return "msg is null";
        }
    }

    public static void log(String TAG, String msg) {
        if (DEBUG) {
            if (Log != null) Log.debug(TAG + " " + msg);
        }
    }

    public static void log(String TAG, String funcName, String msg) {
        if (DEBUG) {
            if (Log != null)
                Log.debug(TAG + " [" + funcName + "]" + msg);

        }
    }

}

