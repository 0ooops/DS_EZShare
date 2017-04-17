package Server;
//package main.java.Server;

/**
 * This class is used as server side in EZShare System. The server class
 * basically takes responsibility for accepting connection with client, and
 * creates new thread for each client. You can specify a few arguments while
 * running the server, or you may use the default settings.
 * @author: Jiayu Wang
 * @date: April 5, 2017
 */

import main.java.Client.MyFormatter;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.cli.*;
import javax.net.ServerSocketFactory;
import java.net.ServerSocket;
import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;


public class Server {
    private final static Logger logr_info = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final static Logger logr_debug = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    /**
     * Central storage for resource, client, server, and primary keys, shared by all functions.
     * Only exist while the server is alive.
     */
    private static HashMap<Integer, Resource> resourceList = new HashMap<>();
    private static HashMap<String, Long> clientList = new HashMap<>();
    private static JSONArray serverList = new JSONArray();
    private static KeyList keys = new KeyList();
    /**
     * Default settings without command line arguments.
     */
    private static String hostname = "Dr. Stranger";
    private static int connectionSecond = 1;
    private static int exchangeSecond = 600;
    private static int port = 8080;
    private static String secret;

    public static void main(String[] args) {
        try{
            boolean active = true;
            setupLogger();
            logr_info.info("Starting the EZShare Server");
            Options options = new Options();
            options.addOption("advertisedhostname", true, "advertisedhostname");
            options.addOption("debug", false, "print debug information");
            options.addOption("connectionintervallimit", true, "connection interval limit in seconds");
            options.addOption("exchangeinterval", true, "exchange interval in seconds");
            options.addOption("port", true, "server port, an integer");
            options.addOption("secret", true, "secret, random string");

            // Parsing command line arguments
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
            if (cmd.hasOption("advertisedhostname")) {
                hostname = cmd.getOptionValue("advertisedhostname");
            }
            if (cmd.hasOption("connectionintervallimit")) {
                try {
                    connectionSecond = Integer.parseInt(cmd.getOptionValue("connectionintervallimit"));
                } catch (NumberFormatException e) {
                    System.out.println("Please give a valid connection interval number in seconds.");
                    System.exit(1);
                }
            }
            if (cmd.hasOption("exchangeinterval")) {
                try {
                    exchangeSecond = Integer.parseInt(cmd.getOptionValue("exchangeinterval"));
                } catch (NumberFormatException e) {
                    System.out.println("Please give a valid exchange interval number in seconds.");
                    System.exit(1);
                }
            }
            if (cmd.hasOption("port")) {
                try {
                    port = Integer.parseInt(cmd.getOptionValue("port"));
                } catch (NumberFormatException e) {
                    System.out.println("Please give a valid port number." + cmd.getOptionValue("port"));
                    System.exit(1);
                }
            }
            if (cmd.hasOption("secret")) {
                secret = cmd.getOptionValue("secret");
            } else {
                secret = randomAlphabetic(26);
            }
            if (cmd.hasOption("debug")) {
                setupDebug();
                logr_debug.info("Setting debug on");
            }

            // Print logfile info when starting
            logr_info.info("Using secret: " + secret);
            logr_info.info("Using advertised hostname: " + hostname);
            logr_info.info("Bound to port " + port);
            logr_info.info("Started");
            BufferedReader br = new BufferedReader(new FileReader("./logfile.log"));
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                System.out.println(sCurrentLine);
            }

            // Add current host and port to serverList.
            JSONObject localHost = new JSONObject();
            Enumeration<NetworkInterface> hostIP = NetworkInterface.getNetworkInterfaces();
            while (hostIP.hasMoreElements()) {
                localHost.put("hostname", hostIP.nextElement());
                localHost.put("port", port);
                serverList.add(localHost);
            }

            ServerSocketFactory factory = ServerSocketFactory.getDefault();
            ServerSocket server = factory.createServerSocket(port);
            // Create thread for periodical exchange.
            Thread tExchange = new Thread(() -> timingExchange(cmd));
            tExchange.start();
            // Create thread for each client.
            while(active) {
                Socket client = server.accept();
                Thread t = new Thread(() -> serveClient(client, cmd));
                t.start();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * This function is used for receiving data from client, calling functions according to client's command,
     * and sending response back to client.
     * @param client this is the socket connection with client.
     * @param args this is the parsed command from command line input when running server.
     */
    private static void serveClient(Socket client, CommandLine args) {
        String receiveData;
        JSONObject cmd;
        JSONObject msg = null;
        JSONArray fileResponse = null;
        JSONArray sendMsg = new JSONArray();
        Date date = new Date();

        try(Socket clientSocket = client) {
            // Check connection interval limit, if less than lower requirement, close the connection with processing.
            String getAddress = clientSocket.getInetAddress().getHostAddress();
            logr_debug.fine("The connection with " + getAddress + ":" + clientSocket.getPort() + " has been established.");
            Long time = date.getTime();
            if (clientList.containsKey(getAddress)) {
                if (time - clientList.get(getAddress) < connectionSecond * 1000) {
                    logr_debug.fine("The request from host " + getAddress + " is too frequent.");
                    clientSocket.close();
                    logr_debug.fine("The connection with " + getAddress + ":" + clientSocket.getPort() + " has been closed by server.");
                }
            }
            clientList.put(getAddress, time);
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            FileInputStream file = null;
            // Call different functions based on client's command.
            do {
                receiveData = in.readUTF();
                logr_debug.fine("RECEIVED: " + receiveData);
                cmd = JSONObject.fromObject(receiveData);
                switch(cmd.get("command").toString()) {
                    case "PUBLISH":
                        sendMsg.add(PublishNShare.publish(cmd, resourceList, keys,
                                clientSocket.getLocalAddress().getHostAddress(),
                                clientSocket.getLocalPort()));
                        break;
                    case "REMOVE":
                    	sendMsg.add(RemoveNFetch.remove(cmd, resourceList, keys));
                        break;
                    case "SHARE":
                        sendMsg.add(PublishNShare.share(cmd, resourceList, keys, secret,
                                clientSocket.getLocalAddress().getHostAddress(),
                                clientSocket.getLocalPort()));
                        break;
                    case "FETCH":
                        fileResponse = RemoveNFetch.fetch(cmd, resourceList);
                        sendMsg.addAll(fileResponse);
                        if (fileResponse.getJSONObject(0).get("response").equals("success")) {
                            String uri = cmd.getJSONObject("resourceTemplate").get("uri").toString();
                            file = new FileInputStream(uri);
                        }
                        break;
                    case "QUERY":
                        sendMsg.addAll(QueryNExchange.query(cmd, resourceList, serverList));
                        break;
                    case "EXCHANGE":
                        sendMsg.addAll(QueryNExchange.exchange(cmd, serverList));
                        break;
                    default:
                        msg.put("response", "error");
                        msg.put("errorMessage", "invalid command");
                        sendMsg.add(msg);
                        break;
                }
                logr_debug.fine("SENT: " + sendMsg.toString());
                out.writeUTF(sendMsg.toString());
                Thread.sleep(3000);
                out.flush();
                // Sending fetched file to client.
                if (cmd.get("command").toString().equals("FETCH") && fileResponse.getJSONObject(0).get("response").equals("success")) {
                    byte[] buffer = new byte[4000];
                    while (file.read(buffer) > 0) {
                        out.write(buffer);
                    }
                    out.flush();
                    file.close();
                }
            } while(in.available() > 0);
            out.close();
            clientSocket.close();
            logr_debug.fine("The connection with " + getAddress + ":" + clientSocket.getPort() + " has been closed.");
            if (args.hasOption("debug")) {
                BufferedReader brDebug = new BufferedReader(new FileReader("./debug.log"));
                String dCurrentLine;
                while ((dCurrentLine = brDebug.readLine()) != null) {
                    System.out.println(dCurrentLine);
                }
                setupDebug();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setupLogger() {
        LogManager.getLogManager().reset();
        logr_info.setLevel(Level.ALL);
        try {
            FileHandler fh = new FileHandler("logfile.log");
            fh.setLevel(Level.FINE);
            logr_info.addHandler(fh);
            MyFormatter formatter = new MyFormatter();
            fh.setFormatter(formatter);
        } catch (java.io.IOException e) {
            logr_info.finer("File logger is not working.");
        }
    }

    private static void setupDebug() {
        LogManager.getLogManager().reset();
        logr_debug.setLevel(Level.ALL);
        try {
            FileHandler fh = new FileHandler("debug.log");
            fh.setLevel(Level.FINE);
            logr_debug.addHandler(fh);
            MyFormatter formatter = new MyFormatter();
            fh.setFormatter(formatter);
        } catch (java.io.IOException e) {
            logr_debug.finer("Debug logger is not working.");
        }
    }

    /**
     * This function is used for periodically exchanging serverList with a random selected server on serverList.
     * @param args this is the parsed command from command line input when running server.
     */
    public static void timingExchange (CommandLine args) {
        String receiveData;
        try {
            while (true) {
                if (serverList.size() > 1) {
                    int select = 1 + (int) (Math.random() * (serverList.size() - 1));
                    String host = serverList.getJSONObject(select).get("hostname").toString();
                    int port = Integer.parseInt(serverList.getJSONObject(select).get("port").toString());
                    JSONObject cmd = new JSONObject();
                    cmd.put("command", "EXCHANGE");
                    cmd.put("serverList", serverList);
                    logr_debug.fine("Auto-exchange is working in every " + exchangeSecond + " seconds.");
                    logr_debug.fine("SENT: " + cmd.toString());
                    receiveData = QueryNExchange.serverSend(host, port, cmd.toString());
                    logr_debug.fine("RECEIVED: " + receiveData);
                    logr_debug.fine("Auto-exchange is finished.");
                    if (receiveData.equals("connection failed")) {
                        serverList.remove(select);
                    }
                    if (args.hasOption("debug")) {
                        BufferedReader brDebug = new BufferedReader(new FileReader("./debug.log"));
                        String dCurrentLine;
                        while ((dCurrentLine = brDebug.readLine()) != null) {
                            System.out.println(dCurrentLine);
                        }
                        setupDebug();
                    }
                }
                Thread.sleep(exchangeSecond * 1000);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}