package Client; /**
 * Created by jiangyiming on 4/8/17.
 */

import org.apache.commons.cli.*;
import org.apache.commons.logging.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MyClient {

    /**
     * @param args the command line arguments
     */

    //TODO:-debug
    private static Log log = LogFactory.getLog(MyClient.class);
//    private static final Logger LOGG = Logger.getLogger(MyClient.class);

    private static int port = 3780;
    private static String host = "sunrise.cis.unimelb.edu.au";
    private static String channel = "";
    private static String description = "";
    private static String name = "";
    private static String owner = "";
    private static String tags = "";
    private static String uri = "";

    private static String ezserver = null;

    private static final String PUBLISH = "-publish";
    private static final String REMOVE = "-remove";
    private static final String SHARE = "-share";
    private static final String QUERY = "-query";
    private static final String FETCH = "-fetch";
    private static final String EXCHANGE = "-exchange";

    public static void main(String[] args) {

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
//        Option[] cmdoptions = cmd.getOptions();
//        for (Option cmdoption : cmdoptions) {
//            cmdoption.getOpt();
//            System.out.println(cmdoption.getOpt());
//            System.out.println();
//        }

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
                        uri = cmd.hasOption("uri") ? uri = cmd.getOptionValue("uri") : "";
                        JSONObject sendPub = publishCommand(cmd, uri);
                        sendMessage(sendPub, cmd);

                    }
                    break;
                case REMOVE:
                    if (!cmd.hasOption("uri")) {
                        System.out.println("a valid URI is required");
                        System.exit(1);
                        return;
                    } else {
                        uri = cmd.hasOption("uri") ? uri = cmd.getOptionValue("uri") : "";
                        JSONObject sendRem = removeCommand(cmd, uri);
                        sendMessage(sendRem, cmd);
                    }
                    break;
                case SHARE:
                    if (!cmd.hasOption("uri")) {
                        System.out.println("a valid URI is required~");
                        System.exit(1);
                        return;
                    } else {
                        uri = cmd.hasOption("uri") ? uri = cmd.getOptionValue("uri") : "";
                        JSONObject sendShare = shareCommand(cmd, uri);
                        sendMessage(sendShare, cmd);
                    }
                    break;
                case QUERY:
                    JSONObject sendQuery = queryCommand(cmd);
                    sendMessage(sendQuery, cmd);
                    break;
                case FETCH:
                    JSONObject sendFetch = fetchCommand(cmd);
                    sendMessage(sendFetch, cmd);
                    break;
                case EXCHANGE:
                    if (!cmd.hasOption("servers")) {
                        System.out.println("pls give a valid server list");
                        System.exit(1);
                        return;
                    } else {
                        JSONObject sendExchange = exchangeCommand(cmd);
                        sendMessage(sendExchange, cmd);
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
        JSONArray serverlist = new JSONArray();
        String servers = cmd.getOptionValue("servers");
        String[] sServers = servers.split(",");
        for (int i = 0; i < sServers.length; i++) {
            String[] tempServer = sServers[i].split(":");
            JSONObject serv = new JSONObject();
            serv.put("hostname", tempServer[0]);
            serv.put("port", tempServer[0]);
            serverlist.add(serv);
        }
        exchange.put("command", "EXCHANGE");
        exchange.put("serverlist", serverlist);
        return exchange;
    }

    private static JSONObject fetchCommand(CommandLine cmd) {
        JSONObject fromquery = queryCommand(cmd);
        fromquery.put("command", "FETCH");
        fromquery.remove("relay");
        return fromquery;
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
        return query;
    }

    private static JSONObject shareCommand(CommandLine cmd, String uri) {
        JSONObject fromPub = publishCommand(cmd, uri);
        String secret = cmd.hasOption("secret") ? cmd.getOptionValue("secret") : "";
        fromPub.put("command", "SHARE");
        fromPub.put("secret", secret);
        return fromPub;
    }

    private static JSONObject removeCommand(CommandLine cmd, String uri) {
        JSONObject fromPub = publishCommand(cmd, uri);
        fromPub.put("command", "REMOVE");
        return fromPub;
    }

    private static JSONObject publishCommand(CommandLine cmd, String uri) {
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
        return pub;
    }

    private static void sendMessage(JSONObject sendJson, CommandLine cmd) {
        String sendData = sendJson.toString();
        String receiveData;
        if (cmd.hasOption("port") && cmd.hasOption("host")) {
            host = cmd.getOptionValue("host");
            port = Integer.parseInt(cmd.getOptionValue("port"));
        }
        Socket connection;
        try {
            connection = new Socket(host, port);
            DataInputStream in = new DataInputStream(connection.getInputStream());
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
//            System.out.println(connection.getRemoteSocketAddress());

            System.out.println("send to server:" + sendData);

            out.writeUTF(sendData);
            out.flush();
            do {
                receiveData = in.readUTF();
                System.out.println("receive from server:" + receiveData);
            } while (in.available() > 0);
            in.close();
            out.close();
            connection.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
