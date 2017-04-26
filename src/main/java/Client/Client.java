package Client;
//package main.java.Client;

/**
 * This class is used as the client side of EZShare System. The client can take legitimate user command as input,
 * send data to server, and print the message received from server. You can review the process of communications
 * if you set the command line arguments '-debug' on.
 * Created by jiangyiming on 4/8/17.
 */

import org.apache.commons.cli.*;

import java.util.logging.*;

import net.sf.json.*;

import java.io.*;
import java.net.Socket;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;

public class Client {
    private final static Logger logr = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    /**
     * default server host and port
     */
//    private static int port = 8080;
//    private static String host = "localhost";
    private static String host = "sunrise.cis.unimelb.edu.au";
    private static int port = 3781;
    private static String channel = "";
    private static String description = "";
    private static String name = "";
    private static String owner = "";
    private static String tags = "";
    private static String uri = "";
    private static String ezserver = null;
    /**
     * all valid commands.
     */
    private static final String PUBLISH = "-publish";
    private static final String REMOVE = "-remove";
    private static final String SHARE = "-share";
    private static final String QUERY = "-query";
    private static final String FETCH = "-fetch";
    private static final String EXCHANGE = "-exchange";

    public static void main(String[] args) {
        /**
         * all client side commands.
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
        options.addOption("remove", false, "remove resources from server");
        options.addOption("secret", true, "secret");
        options.addOption("servers", true, "server list, host1:port1, host2, port2,...");
        options.addOption("share", false, "share resource on server");
        options.addOption("tags", true, "resource tags, tag1,tag2,tag3,...");
        options.addOption("uri", true, "resource URI");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        HelpFormatter formatter = new HelpFormatter();
/**
 * check if command is valid
 */
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            formatter.printHelp("commands", options);
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
                if (strPort.length() > 4) {
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
 * setup log
 */
        setupLogger();
        logr.info("setting debug on");
        /*
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
            String command = args[0];
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
            if (exPort.length() > 4) {
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
        resourceTemplate.put("name", name);
        resourceTemplate.put("tags", tagArray);
        resourceTemplate.put("description", description);
        resourceTemplate.put("uri", uri);
        resourceTemplate.put("channel", channel);
        resourceTemplate.put("owner", owner);
        resourceTemplate.put("ezserver", ezserver);
        query.put("resourceTemplate", resourceTemplate);
        query.put("command", "QUERY");
        query.put("relay", "true");
        logr.fine("querying from " + host + ":" + port);
        return query;
    }

    /**
     * dealing with share command
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
            connection = new Socket(host, port);
            DataInputStream in = new DataInputStream(connection.getInputStream());
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.writeUTF(sendData);
            out.flush();
            logr.fine("SENT:" + sendData);
            try {
                connection.setSoTimeout(10 * 1000);
            } catch (Exception e) {
                System.out.println("connection fail");
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
                    receiveData = readline;
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
                    } catch (Exception E){
                        System.out.println("This should not happan, otherwise, you are using Aaron's server.LOL");
                        System.exit(1);
                    }
                    String fileType = uri.substring(uri.indexOf(".") + 1);
                    String fileName = (String) resource.get("name");
                    String randomName = randomAlphabetic(5)+"."+fileType;
                    fileName = fileName.equals("") ? randomName : fileName;
                    FileOutputStream fos = new FileOutputStream(fileName);
                    byte[] buffer = new byte[4096];
                    int readRes;
                    int remaining = fileSize;
                    System.out.println("receiving...");
                    while ((readRes = in.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                        remaining -= readRes;
                        fos.write(buffer, 0, readRes);
                    }
                    System.out.println("done");
                    fetchSuccess = true;
                }

            }
            if (cmd.hasOption("debug")) {
                //print logfile
                BufferedReader br = new BufferedReader(new FileReader("./logfile.log"));
                String sCurrentLine;
                while ((sCurrentLine = br.readLine()) != null) {
                    System.out.println(sCurrentLine);
                }
            } else if (!fetchSuccess) {
                //print out
                receiveData="["+receiveData.substring(0,receiveData.length()-1)+"]";
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
                    System.out.println("hit " + (recv.size() - 2) + " resource(s)");
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
     * setup log file.
     */
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