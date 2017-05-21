package EZShare;

/**
 * This class is used as the client side of EZShare System. The client can take legitimate user command as input,
 * send data to server, and print the message received from server. You can review the process of communications
 * if you set the command line arguments '-debug' on.
 * Created by jiangyiming on 4/8/17.
 */

import org.apache.commons.cli.*;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.*;

import net.sf.json.*;
import org.apache.commons.lang.RandomStringUtils;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;


public class Client {
    private final static Logger logr = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME + "logForNomal");
    private final static Logger logrSub = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME + "logForSub");
    /**
     * default server host and port
     */
    private static int port = 8080;
    private static String host = "localhost";
    private static String channel = "";
    private static String description = "";
    private static String name = "";
    private static String owner = "";
    private static String tags = "";
    private static String uri = "";
    private static String ezserver = null;
    private static String relay = "false";
    private static Boolean secure = false;
    private static String id = "";
    /**
     * all valid commands.
     */
    private static final String PUBLISH = "-publish";
    private static final String REMOVE = "-remove";
    private static final String SHARE = "-share";
    private static final String QUERY = "-query";
    private static final String FETCH = "-fetch";
    private static final String EXCHANGE = "-exchange";
    private static final String SUBSCRIBE = "-subscribe";
    private static String logForSub = "";

    public static void main(String[] args) {
        /**
         * all valid client side command line arguments.
         */
        Options options = new Options();
        options.addOption("channel", true, "channel");
        options.addOption("debug", false, "print debug information");
        options.addOption("description", true, "resource description");
        options.addOption("exchange", false, "exchange server list with server");
        options.addOption("fetch", false, "fetch resources from server");
        options.addOption("host", true, "server host,a domain name or IP address");
        options.addOption("name", true, "resource name");
        options.addOption("owner", true, "owner");
        options.addOption("port", true, "server port, an integer");
        options.addOption("publish", false, "publish source on server");
        options.addOption("query", false, "query for resources from server");
        options.addOption("subscribe", false, "subscribe to server");
        options.addOption("unsubscribe", false, "unsubscribe to server");
        options.addOption("remove", false, "remove resources from server");
        options.addOption("secret", true, "secret");
        options.addOption("servers", true, "server list, host1:port1, host2, port2,...");
        options.addOption("share", false, "share resource on server");
        options.addOption("tags", true, "resource tags, tag1,tag2,tag3,...");
        options.addOption("uri", true, "resource URI");
        options.addOption("relay", true, "whether query from other servers");
        options.addOption("secure", false, "whether to use a secured connection");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        HelpFormatter formatter = new HelpFormatter();
/**
 * check if commands are valid
 */
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            formatter.printHelp("pls choose commands from below", options);
            System.exit(1);
            return;
        }
/**
 * check if port number is valid
 */
        if (cmd.hasOption("port") || cmd.hasOption("host")) {
            if (cmd.hasOption("port") && cmd.hasOption("host")) {
                host = cmd.getOptionValue("host");
                String strPort = cmd.getOptionValue("port");
                if (strPort.length() > 5) {
                    System.out.println("port out of range,please give a valid port number~");
                    System.exit(1);
                }
                try {
                    port = Integer.parseInt(cmd.getOptionValue("port"));
                } catch (NumberFormatException E) {
                    System.out.println("please give a valid port number~");
                    System.exit(1);
                    return;
                }
            } else {
                System.out.println("please give both hostname and port number~");
                System.exit(1);
                return;
            }
        }

/**
 * check if port number is valid
 */
        if (cmd.hasOption("secure")) {
            secure = true;
        }


/**
 1.judge whether the client gives any command.
 2.verify the command.
 3.produce corresponding JSON Objects for sending to the server.
 4.send message.
 */
        if (args == null || args.length == 0) {
            formatter.printHelp("commands", options);
            System.out.println("Please choose commands from above");
            System.exit(1);
        } else {
            String command = searchCommand(args);
            if (!command.equals(SUBSCRIBE)) {
                setupLogger();
                logr.info("setting debug on");
            } else {
                setupSubLogger();
                logrSub.info("setting debug on");
            }
            switch (command) {
                case PUBLISH:
                    if (!cmd.hasOption("uri")) {
                        System.out.println("the published resource must have a valid URI");
                        System.exit(1);
                        return;
                    } else {
                        JSONObject sendPub = publishCommand(cmd);
                        sendMessage(command, sendPub, cmd);

                    }
                    break;
                case REMOVE:
                    if (!cmd.hasOption("uri")) {
                        System.out.println("a valid URI is required");
                        System.exit(1);
                        return;
                    } else {
                        JSONObject sendRem = removeCommand(cmd);
                        sendMessage(command, sendRem, cmd);
                    }
                    break;
                case SHARE:
                    if (!cmd.hasOption("uri")) {
                        System.out.println("a valid URI is required~");
                        System.exit(1);
                        return;
                    } else {
                        JSONObject sendShare = shareCommand(cmd);
                        sendMessage(command, sendShare, cmd);
                    }
                    break;
                case QUERY:
                    JSONObject sendQuery = queryCommand(cmd);
                    sendMessage(command, sendQuery, cmd);
                    break;
                case SUBSCRIBE:
                    JSONObject sendSubscribe = subscribeCommand(cmd);
                    sendSubMessage(sendSubscribe, cmd);
                    break;
                case FETCH:
                    JSONObject sendFetch = fetchCommand(cmd);
                    sendMessage(command, sendFetch, cmd);
                    break;
                case EXCHANGE:
                    if (!cmd.hasOption("servers")) {
                        System.out.println("pls give a valid server list");
                        System.exit(1);
                        return;
                    } else {
                        JSONObject sendExchange = exchangeCommand(cmd);
                        sendMessage(command, sendExchange, cmd);
                    }
                    break;
                default:
                    System.out.println("invalid command");
                    System.exit(1);
            }
        }

    }

    /**
     * dealing with exchange command
     *
     * @param cmd commands
     * @return the JSONObject message to be sent to the server
     */
    private static JSONObject exchangeCommand(CommandLine cmd) {
        JSONObject exchange = new JSONObject();
        JSONArray serverList = new JSONArray();
        String servers = cmd.getOptionValue("servers");
        String[] sServers = servers.split(",");
        for (String sServer : sServers) {
            String[] tempServer = sServer.split(":");
            JSONObject serv = new JSONObject();
            serv.put("hostname", tempServer[0]);
            String exPort = tempServer[1];
            if (exPort.length() > 5) {
                System.out.println("pls input a valid port");
                System.exit(1);
            }
            serv.put("port", tempServer[1]);
            serverList.add(serv);
        }
        exchange.put("command", "EXCHANGE");
        exchange.put("serverList", serverList);
        logr.fine("exchanging");
        return exchange;
    }

    /**
     * dealing with fetch command
     *
     * @param cmd commands
     * @return the JSONObject message to be sent to the server
     */
    private static JSONObject fetchCommand(CommandLine cmd) {
        JSONObject fetch = queryCommand(cmd);
        fetch.put("command", "FETCH");
        fetch.remove("relay");
        logr.fine("fetching from " + host + ":" + port);
        return fetch;
    }

    /**
     * dealing with query command
     *
     * @param cmd commands
     * @return the JSONObject message to be sent to the server
     */
    private static JSONObject queryCommand(CommandLine cmd) {
        JSONObject query = new JSONObject();
        JSONObject resourceTemplate = new JSONObject();
        uri = cmd.hasOption("uri") ? uri = cmd.getOptionValue("uri") : "";
        name = cmd.hasOption("name") ? cmd.getOptionValue("name") : "";
        description = cmd.hasOption("description") ? cmd.getOptionValue("description") : "";
        channel = cmd.hasOption("channel") ? cmd.getOptionValue("channel") : "";
        owner = cmd.hasOption("owner") ? cmd.getOptionValue("owner") : "";
        JSONArray tagArray = new JSONArray();
        if (cmd.hasOption("tags")) {
            tags = cmd.getOptionValue("tags");
            String[] stags = tags.split(",");
            for (int i = 0; i < stags.length; i++) {
                tagArray.add(stags[i]);
            }
        }
        if (cmd.hasOption("relay")) {
            relay = cmd.getOptionValue("relay");
        }
        resourceTemplate.put("name", name);
        resourceTemplate.put("tags", tagArray);
        resourceTemplate.put("description", description);
        resourceTemplate.put("uri", uri);
        resourceTemplate.put("channel", channel);
        resourceTemplate.put("owner", owner);
        resourceTemplate.put("ezserver", ezserver);
        query.put("resourceTemplate", resourceTemplate);
        query.put("command", "QUERY");
        query.put("relay", relay);
        logr.fine("querying from " + host + ":" + port);
        return query;
    }

    /**
     * dealing with subscribe command
     *
     * @param cmd commands
     * @return the JSONObject message to be sent to the server
     */
    private static JSONObject subscribeCommand(CommandLine cmd) {
        JSONObject subscribe = queryCommand(cmd);
        id = RandomStringUtils.randomAlphabetic(10);
        subscribe.put("command", "SUBSCRIBE");
        subscribe.put("id", id);
        logrSub.fine("subscribing from " + host + ":" + port);
        return subscribe;
    }

    /**
     * dealing with unsubscribe command
     *
     * @return the JSONObject message to be sent to the server
     */
    private static JSONObject unsubscribeCommand() {
        JSONObject unsubscribe = new JSONObject();
        unsubscribe.put("command", "UNSUBSCRIBE");
        unsubscribe.put("id", id);
        logrSub.fine("unsubscribing from " + host + ":" + port);
        return unsubscribe;
    }

    /**
     * dealing with share command
     *
     * @param cmd commands
     * @return the JSONObject message to be sent to the server
     */
    private static JSONObject shareCommand(CommandLine cmd) {
        JSONObject share = publishCommand(cmd);
        uri = cmd.getOptionValue("uri");
        String secret = cmd.hasOption("secret") ? cmd.getOptionValue("secret") : "";
        share.put("command", "SHARE");
        share.put("secret", secret);
        JSONObject resource = (JSONObject) share.get("resource");
        resource.put("uri", uri);
        logr.fine("sharing to " + host + ":" + port);
        return share;
    }

    /**
     * dealing with remove command
     *
     * @param cmd commands
     * @return the JSONObject message to be sent to the server
     */
    private static JSONObject removeCommand(CommandLine cmd) {
        JSONObject remove = publishCommand(cmd);
        uri = cmd.getOptionValue("uri");
        remove.put("command", "REMOVE");
        JSONObject resource = (JSONObject) remove.get("resource");
        resource.put("uri", uri);
        logr.fine("removing from " + host + ":" + port);
        return remove;
    }

    /**
     * dealing with publish command
     *
     * @param cmd commands
     * @return the JSONObject message to be sent to the server
     */
    private static JSONObject publishCommand(CommandLine cmd) {
        uri = cmd.getOptionValue("uri");
        name = cmd.hasOption("name") ? cmd.getOptionValue("name") : "";
        description = cmd.hasOption("description") ? cmd.getOptionValue("description") : "";
        channel = cmd.hasOption("channel") ? cmd.getOptionValue("channel") : "";
        owner = cmd.hasOption("owner") ? cmd.getOptionValue("owner") : "";
        JSONArray tagArray = new JSONArray();
        if (cmd.hasOption("tags")) {
            tags = cmd.getOptionValue("tags");
            String[] stags = tags.split(",");
            for (int i = 0; i < stags.length; i++) {
                tagArray.add(stags[i]);
            }
        }
        JSONObject pub = new JSONObject();
        JSONObject resource = new JSONObject();

        resource.put("name", name);
        resource.put("tags", tagArray);
        resource.put("description", description);
        resource.put("uri", uri);
        resource.put("channel", channel);
        resource.put("owner", owner);
        resource.put("ezserver", ezserver);
        pub.put("resource", resource);
        pub.put("command", "PUBLISH");
        logr.fine("publishing to " + host + ":" + port);
        return pub;
    }

    /**
     * connection and transmission.
     *
     * @param sendJson json object to be sent.
     * @param cmd      cmd may specify another server host and port number.
     */
    private static void sendMessage(String command, JSONObject sendJson, CommandLine cmd) {
        String sendData = sendJson.toString();
        String receiveData = "";
        boolean fetchSuccess = false;
        Socket connection;

        try {
            if (secure) {
                System.setProperty("javax.net.ssl.trustStore", "clientKeyStore/client-keystore.jks");
                SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                connection = sslsocketfactory.createSocket(host, port);
            } else {
                connection = new Socket(host, port);
            }
            DataInputStream in = new DataInputStream(connection.getInputStream());
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.writeUTF(sendData);
            out.flush();
            logr.fine("SENT:" + sendData);
            try {
                connection.setSoTimeout(20 * 1000);
            } catch (Exception e) {
                System.out.println("The client side terminates the connection as the server does not response anything");
                System.exit(1);
            }
            if (!command.equals(FETCH)) {
                do {
                    Thread.sleep(1000);
                    String read = in.readUTF();
                    logr.fine("RECEIVED:" + read);
                    receiveData += read + ",";
                } while (in.available() > 0);
            } else {
                Thread.sleep(1000);
                String readline = in.readUTF();
                logr.fine("RECEIVED:" + readline);
                JSONObject recv = JSONObject.fromObject(readline);
                if (recv.get("response").equals("error")) {
                    receiveData = readline + ",";
                } else {
                    String readResource = in.readUTF();
                    logr.fine("RECEIVED:" + readResource);
                    JSONObject resource = JSONObject.fromObject(readResource);
                    int fileSize = 0;
                    try {
                        fileSize = (int) resource.get("resourceSize");
                    } catch (NumberFormatException E) {
                        System.out.println("fail to download ,file oversize ");
                        System.exit(1);
                    } catch (Exception E) {
                        System.out.println("This should not happan, otherwise, you are using Aaron's server.LOL");
                        System.exit(1);
                    }

                    String fileType = uri.substring(uri.lastIndexOf(".") + 1);
                    String fileName = randomAlphabetic(5) + "." + fileType;
                    FileOutputStream fos = new FileOutputStream(fileName);

                    byte[] buffer = new byte[4096];
                    int readRes;
                    int remaining = fileSize;
                    if (!cmd.hasOption("debug")) {
                        System.out.println("success!");
                        System.out.println();
                        System.out.println("receiving...");
                        System.out.println();
                    }
                    while ((readRes = in.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                        remaining -= readRes;
                        fos.write(buffer, 0, readRes);
                    }
                    if (!cmd.hasOption("debug")) {
                        System.out.println("name: " + resource.get("name"));
                        System.out.println("uri: " + uri);
                        System.out.println("description: " + resource.get("description"));
                        System.out.println("channel: " + resource.get("channel"));
                        System.out.println();
                        System.out.println("done!");
                        System.out.println("your file stores in the current path and the file name is: " + fileName);
                    }
                    fetchSuccess = true;
                }

            }
            if (cmd.hasOption("debug")) {
                //print logfile
                printLogFromFile();
            } else if (!fetchSuccess) {
                //print out
                receiveData = "[" + receiveData.substring(0, receiveData.length() - 1) + "]";
                JSONArray recv = (JSONArray) JSONSerializer.toJSON(receiveData);

                JSONObject resp = recv.getJSONObject(0);
                String respTpye = (String) resp.get("response");
                if (respTpye.equals("error")) {
                    System.out.print("error,");
                    System.out.println(resp.get("errorMessage") + "!");
                } else {
                    System.out.println("success!");
                }
                if (command.equals(QUERY) && !respTpye.equals("error")) {
                    printQueryResult(recv);
                }
            }
            in.close();
            out.close();
            connection.close();
        } catch (InterruptedException e) {
            System.out.println("bad things always happen,pls try again.");
        } catch (IOException e) {
            System.out.println("connection fail");
            System.exit(1);
        }
    }

    /**
     * subscribing and maintaining connection
     *
     * @param sendJson json object to be sent.
     */
    private static void sendSubMessage(JSONObject sendJson, CommandLine cmd) {
        String sendData = sendJson.toString();
//        ClientThread clientThread = new ClientThread(host,port,sendData);
        String receiveData = "";
        boolean unSubscribe = false;
        Socket connection;
        try {

            if (secure) {
                System.setProperty("javax.net.ssl.trustStore", "clientKeyStore/client-keystore.jks");
                SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                connection = sslsocketfactory.createSocket(host, port);
            } else {
                connection = new Socket(host, port);
            }
            DataInputStream in = new DataInputStream(connection.getInputStream());
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            BufferedReader enterRead = new BufferedReader(new InputStreamReader(System.in));
            out.writeUTF(sendData);
            out.flush();
            logrSub.fine("SENT:" + sendData);
            int count = -1;

            while (!unSubscribe) {
                Thread.sleep(2000);
                while (in.available() > 0) {
                    String read = in.readUTF();
//                    System.out.println(read);
                    receiveData += read + ",";
                    logrSub.fine("RECEIVED:" + read);
                }
                if (cmd.hasOption("debug")) {
                    //print logfile
                    count = printLogFromFile(count);
                    receiveData = "";
                } else {
                    if (!receiveData.equals("")) {
                        receiveData = "[" + receiveData.substring(0, receiveData.length() - 1) + "]";
//                System.out.println(receiveData);
                        JSONArray recv = (JSONArray) JSONSerializer.toJSON(receiveData);
                        JSONObject resp = recv.getJSONObject(0);
                        if (resp.has("response")) {
                            String respTpye = (String) resp.get("response");
                            if (respTpye.equals("error")) {
                                System.out.print("error,");
                                System.out.println(resp.get("errorMessage") + "!");
                            } else {
                                System.out.println("success!");
                                recv.clear();
                                receiveData = "";
                            }
                        } else {
                            printSubResult(recv);
                            receiveData = "";
                            recv.clear();
                        }
                    }
                }

                if (enterRead.ready()) {
                    if (enterRead.read() == '\n') {
                        String unsubMsg = unsubscribeCommand().toString();
                        out.writeUTF(unsubMsg);
                        logrSub.fine("SENT:" + unsubMsg);
                        unSubscribe = true;
                    } else {
                        enterRead.read();
                    }
                }
            }
            if (unSubscribe) {
                do {
                    Thread.sleep(1000);
                    String read = in.readUTF();
//                    System.out.println(read);
                    receiveData += read + ",";
                    logrSub.fine("RECEIVED:" + read);
                } while (in.available() > 0);
                if (!receiveData.equals("")) {
                    if (cmd.hasOption("debug")) {
                        //print logfile
                        printLogFromFile(count);
                    } else {
                        receiveData = "[" + receiveData.substring(0, receiveData.length() - 1) + "]";
                        JSONArray recv = (JSONArray) JSONSerializer.toJSON(receiveData);
                        if (recv.size() == 1) {
                            System.out.println(recv.toString());
                        } else {
                            String finalRecv = recv.get(recv.size() - 1).toString();
                            recv.remove(recv.size() - 1);
                            printSubResult(recv);
                            System.out.println(finalRecv);
                        }
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * search for the command, and the system only allow one command.
     *
     * @param args all parameters typed by the client
     * @return the command if has one.
     */
    private static String searchCommand(String[] args) {
        String comd = "";
        Set<String> commandSet = new HashSet<>();
        commandSet.add(PUBLISH);
        commandSet.add(REMOVE);
        commandSet.add(SHARE);
        commandSet.add(QUERY);
        commandSet.add(SUBSCRIBE);
        commandSet.add(FETCH);
        commandSet.add(EXCHANGE);
        for (String arg : args) {
            if (commandSet.contains(arg)) {
                if (comd.equals("")) {
                    comd = arg;
                } else {
                    System.out.println("multiple commands detected, pls just give one command");
                    System.exit(1);
                }
            }
        }
        if (comd.equals("")) {
            System.out.println("pls give your command(publish, share, or fetch?)");
            System.exit(1);
        }
        return comd;
    }

    private static void printSubResult(JSONArray recv) {
        for (int i = 0; i < recv.size(); i++) {
            JSONObject queryList = recv.getJSONObject(i);
            String qName = (String) queryList.get("name");
            String qUri = (String) queryList.get("uri");
            JSONArray qTags = (JSONArray) queryList.get("tags");
            String qEzserver = (String) queryList.get("ezserver");
            String qChannel = (String) queryList.get("channel");
            System.out.println("name: " + qName);
            System.out.println("tags: " + qTags.toString());
            System.out.println("uri: " + qUri);
            System.out.println("channel: " + qChannel);
            System.out.println("ezserver: " + qEzserver);
            System.out.println();
        }
        System.out.println();
    }

    private static void printQueryResult(JSONArray recv) {
        for (int i = 1; i < recv.size() - 1; i++) {
            JSONObject queryList = recv.getJSONObject(i);
            String qName = (String) queryList.get("name");
            String qUri = (String) queryList.get("uri");
            JSONArray qTags = (JSONArray) queryList.get("tags");
            String qEzserver = (String) queryList.get("ezserver");
            String qChannel = (String) queryList.get("channel");
            System.out.println("name: " + qName);
            System.out.println("tags: " + qTags.toString());
            System.out.println("uri: " + qUri);
            System.out.println("channel: " + qChannel);
            System.out.println("ezserver: " + qEzserver);
            System.out.println();
        }
        System.out.println();
        int resultSize = (recv.size() - 2) > 0 ? (recv.size() - 2) : 0;
        System.out.println("hit " + resultSize + " resource(s)");
    }

    private static void printLogFromFile() {
        FileReader file = null;
        try {
            file = new FileReader("./logfile.log");
            BufferedReader br = new BufferedReader(file);
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                System.out.println(sCurrentLine);
            }
            br.close();
            file.close();
        } catch (FileNotFoundException e) {
            logr.finer("File logger not working.");
        } catch (IOException e) {
            logr.finer("Exceptions.");
        }
    }

    private static int printLogFromFile(int offset) {
        FileReader file = null;
        try {
            int count = offset;
            file = new FileReader("./" + logForSub + ".log");
            BufferedReader br = new BufferedReader(file);
            String sCurrentLine;
            while (count > -1) {
                br.readLine();
                count--;
            }
            while ((sCurrentLine = br.readLine()) != null) {
                System.out.println(sCurrentLine);
                offset++;
            }
            br.close();
            file.close();

        } catch (FileNotFoundException e) {
            logrSub.finer("File logger not working.");
        } catch (IOException e) {
            logrSub.finer("Exceptions.");
        }
        return offset;
    }

    /**
     * setup log file.
     */

    private static void setupSubLogger() {
        LogManager.getLogManager().reset();
        logr.setLevel(Level.ALL);
        logrSub.setLevel(Level.ALL);
        logForSub = "logForSub" + randomAlphabetic(10);
        try {
            FileHandler fhSub = new FileHandler(logForSub + ".log");
            fhSub.setLevel(Level.FINE);
            logrSub.addHandler(fhSub);
            MyFormatter formatter = new MyFormatter();
            fhSub.setFormatter(formatter);
        } catch (java.io.IOException e) {
            logr.finer("File logger not working.");
        }
    }

    private static void setupLogger() {
        LogManager.getLogManager().reset();
        logr.setLevel(Level.ALL);
        try {
            FileHandler fh = new FileHandler("logfile.log");
            fh.setLevel(Level.FINE);
            logr.addHandler(fh);
            MyFormatter formatter = new MyFormatter();
            fh.setFormatter(formatter);
        } catch (java.io.IOException e) {
            logr.finer("File logger not working.");
        }
    }

}