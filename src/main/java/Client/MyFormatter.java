//package Client;
package main.java.Client;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by jiangyiming on 4/9/17.
 */
public class MyFormatter extends Formatter {
    public String format(LogRecord record) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
        StringBuilder logformat = new StringBuilder();
        logformat.append(dateFormat.format(new Date(record.getMillis()))).append(" - ");
        logformat.append("[").append(record.getSourceClassName()).append(".");
        logformat.append(record.getSourceMethodName()).append("] - ");
        logformat.append("[").append(record.getLevel()).append("] - ");
        logformat.append(record.getMessage());
        logformat.append("\n");
        return logformat.toString();
    }
}
