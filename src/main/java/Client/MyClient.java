package Client;
//package main.java.Client;
/**
 * Created by jiangyiming on 4/8/17.
 */

import org.apache.commons.cli.*;

import java.util.logging.*;

import net.sf.json.*;

import java.io.*;
import java.net.Socket;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;

public class MyClient {
    private final static Logger logr = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /**
     * default server host and port
     */

//    private static int port = 3300;
//    private static String host = "http://115.146.93.106";
    private static int port = 8000;
    private static String host = "localhost";
//    private static String host = "sunrise.cis.unimelb.edu.au";
//    private static int port = 3780;

//    private static int port = 3780;
//    private static String host = "sunrise.cis.unimelb.edu.au";
//    private static int port = 9999;
//    private static String host = "localhost";

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

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            formatter.printHelp("commands", options);
            System.exit(1);
            return;
        }

        if (cmd.hasOption("port") || cmd.hasOption("host")) {
            if (cmd.hasOption("port") && cmd.hasOption("host")) {
                host = cmd.getOptionValue("host");
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

    private static JSONObject exchangeCommand(CommandLine cmd) {
        JSONObject exchange = new JSONObject();
        JSONArray serverList = new JSONArray();
        String servers = cmd.getOptionValue("servers");
        String[] sServers = servers.split(",");
        for (String sServer : sServers) {
            String[] tempServer = sServer.split(":");
            JSONObject serv = new JSONObject();
            serv.put("hostname", tempServer[0]);
            serv.put("port", tempServer[1]);
            serverList.add(serv);
        }
        exchange.put("command", "EXCHANGE");
        exchange.put("serverList", serverList);
        logr.fine("exchanging");
        return exchange;
    }

    private static JSONObject fetchCommand(CommandLine cmd) {
        JSONObject fetch = queryCommand(cmd);
        fetch.put("command", "FETCH");
        fetch.remove("relay");
        logr.fine("fetching from " + host + ":" + port);
        return fetch;
    }

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

    private static JSONObject removeCommand(CommandLine cmd) {
        JSONObject remove = publishCommand(cmd);
        uri = cmd.getOptionValue("uri");
        remove.put("command", "REMOVE");
        JSONObject resource = (JSONObject) remove.get("resource");
        resource.put("uri", uri);
        logr.fine("removing from " + host + ":" + port);
        return remove;
    }

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

        Socket connection;
        try {
            connection = new Socket(host, port);
            DataInputStream in = new DataInputStream(connection.getInputStream());
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

//            System.out.println("send to server:" + sendData);

            out.writeUTF(sendData);
//            System.out.println("Sending data: " + sendData);
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
                    receiveData += read + "\n";
//                    System.out.println("receive from server:" + read); //打印需要format
                } while (in.available() > 0);
            } else {
                Thread.sleep(1000);
                String readline = in.readUTF();
                logr.fine("RECEIVED:" + readline);
                JSONArray recv = (JSONArray) JSONSerializer.toJSON(readline);
                JSONObject responseType = recv.getJSONObject(0);
                if (responseType.get("response").equals("error")) {
                    receiveData = "error";
                    receiveData += "," + responseType.get("errorMessage");
                } else {
                    JSONObject resource = recv.getJSONObject(1);
                    int filesize = (int) resource.get("resourceSize");
                    String fileName = (String) resource.get("name");
                    String randomName = randomAlphabetic(5);
                    fileName = fileName.equals("") ? randomName : fileName;
                    String fileType = uri.substring(uri.indexOf(".") + 1);
                    FileOutputStream fos = new FileOutputStream(fileName + randomName + "." + fileType);
                    byte[] buffer = new byte[4096];
                    int read;
                    int remaining = filesize;
                    System.out.println("receiving..");
                    while ((read = in.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                        remaining -= read;
//                        System.out.println("read " + totalRead + " bytes.");
                        fos.write(buffer, 0, read);
                    }
                    System.out.println("done");
                }
            }
            if (cmd.hasOption("debug")) {
                //print logfile
                BufferedReader br = new BufferedReader(new FileReader("./logfile.log"));
                //uncomment below if running in maven before build.
                //BufferedReader br = new BufferedReader(new FileReader("src/main/java/Client/logfile.log"));
                String sCurrentLine;
                while ((sCurrentLine = br.readLine()) != null) {
                    System.out.println(sCurrentLine);
                }
            } else {
                //print out
                JSONArray recv = (JSONArray) JSONSerializer.toJSON(receiveData);
                JSONObject resp = recv.getJSONObject(0);
                String respTpye = (String) resp.get("response");
                if (respTpye.equals("error")) {
                    System.out.print("error,");
                    System.out.println(resp.get("errorMessage") + "!");
                } else {
                    System.out.println("success!");
                }
                if (command.equals(QUERY) && !respTpye.equals("error")){
                    for (int i=1;i<recv.size()-1;i++){
                        JSONObject queryList = recv.getJSONObject(i);
                        String qName = (String) queryList.get("name");
                        String qUri= (String) queryList.get("uri");
                        JSONArray qTags = (JSONArray)queryList.get("tags");
                        String qEzserver = (String)queryList.get("ezserver");
                        String qChannel = (String)queryList.get("channel");
                        System.out.println("name: "+qName);
                        System.out.println("tags: "+qTags.toString());
                        System.out.println("uri: "+qUri);
                        System.out.println("channel: "+qChannel);
                        System.out.println("ezserver: "+qEzserver);
                    }
                    System.out.println();
                    System.out.println("hit "+ (recv.size()-2) +" resources");
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
