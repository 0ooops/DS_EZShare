package Server;
//package main.java.Server;
/**
 * This class is used as server side in client-server model. The server class
 * basically takes responsibility for accepting connection with client, and
 * creates new thread for each client. You need to specify the port number in
 * command line while running the server.
 * @author: Jiayu Wang
 * @date: April 5, 2017
 */

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import javax.net.ServerSocketFactory;
import java.net.ServerSocket;
import java.io.*;
import java.net.*;
import java.util.HashMap;


public class Server {
    private static HashMap<Integer, Resource> resourceList = new HashMap<>();
    private static JSONArray serverList = new JSONArray();
    private static KeyList keys = new KeyList();
    private static final String secret = "abc";
    private static int exchangeSecond = 600;

    public static void main(String[] args) {
        try{
            int serverPort = Integer.parseInt(args[0]);
            boolean active = true;
            JSONObject localHost = new JSONObject();
            localHost.put("hostname", "localhost");
            localHost.put("port", serverPort);
            serverList.add(localHost);

            ServerSocketFactory factory = ServerSocketFactory.getDefault();
            ServerSocket server = factory.createServerSocket(serverPort);

            Thread tExchange = new Thread(() -> timingExchange());
            tExchange.start();

            while(active) {
                Socket client = server.accept();
                Thread t = new Thread(() -> serveClient(client));
                t.start();
            }

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void serveClient(Socket client) {
        String receiveData;
        JSONObject cmd;
        JSONObject msg = null;
        JSONArray fileResponse=null;//yiming: add null here
        JSONArray sendMsg = new JSONArray();

        try(Socket clientSocket = client) {
            System.out.println("The connection with " + clientSocket.toString() + " has been established.");
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            FileInputStream file = null;

            do {
                receiveData = in.readUTF();
                System.out.println("Received command from client: " + receiveData);
                cmd = JSONObject.fromObject(receiveData);
                switch(cmd.get("command").toString()) {
                    case "PUBLISH":
                        System.out.println("ResouceList Before Change:" + resourceList);
                        sendMsg.add(PublishNShare.publish(cmd, resourceList, keys,
                                clientSocket.getLocalAddress().getHostAddress().toString(),
                                clientSocket.getLocalPort()));
                        System.out.println("ResourceList After Change:" + resourceList);
                        break;
                    case "REMOVE":
                    	sendMsg.add(RemoveAndFetch.remove(cmd, resourceList, keys));
                        break;
                    case "SHARE":
                        sendMsg.add(PublishNShare.share(cmd, resourceList, keys, secret,
                                clientSocket.getLocalAddress().getHostAddress().toString(),
                                clientSocket.getLocalPort()));
                        break;
                    case "FETCH":
                        fileResponse = RemoveAndFetch.fetch(cmd, resourceList);
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
                        sendMsg.addAll(exchange(cmd, clientSocket));
                        break;
                    default:
                        msg.put("response", "error");
                        msg.put("errorMessage", "invalid command");
                        sendMsg.add(msg);
                        break;
                }
                for (Resource src: resourceList.values()) {
                    System.out.println(src);
                }
                System.out.println("Sending data to client: " + sendMsg.toString());
                out.writeUTF(sendMsg.toString());
                Thread.sleep(3000);
                out.flush();
                if (cmd.get("command").toString().equals("FETCH") && fileResponse.getJSONObject(0).get("response").equals("success")) {
                    byte[] buffer = new byte[4000];
                    while (file.read(buffer) > 0) {
                        out.write(buffer);
                    }
                    out.flush();
//                    file.close();
                }
            } while(in.available() > 0);

            out.close();
            clientSocket.close();
            System.out.println("The connection with " + clientSocket.toString() + " has been closed.");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static JSONArray exchange (JSONObject command, Socket clientSocket) {
        JSONArray newList;
        JSONArray msgArray = new JSONArray();
        JSONObject msg = new JSONObject();

        if (command.containsKey("serverList")) {
            if (command.getJSONArray("serverList").size() != 0) {
                newList = command.getJSONArray("serverList");
                for (int i = 0; i < newList.size(); i++) {
                    if (!serverList.contains(newList.getJSONObject(i))) {
                        serverList.add(newList.getJSONObject(i));
                    }
                }
                msg.put("response", "success");
            } else {
                msg.put("response", "error");
                msg.put("errorMessage", "missing or invalid serverList");
            }
        } else {
            msg.put("response", "error");
            msg.put("errorMessage", "missing serverList");
        }
        msgArray.add(msg);
        return msgArray;
    }

    public static void timingExchange () {
        String receiveData;

        try {
            while (true) {
                System.out.println("Auto exchange start!");
                System.out.println("Current ServerList: " + serverList.toString());
                if (serverList.size() > 1) {
                    int select = 1 + (int) (Math.random() * (serverList.size() - 1));
                    String host = serverList.getJSONObject(select).get("hostname").toString();
                    int port = Integer.parseInt(serverList.getJSONObject(select).get("port").toString());
                    JSONObject cmd = new JSONObject();
                    cmd.put("command", "EXCHANGE");
                    cmd.put("serverList", serverList);
                    receiveData = QueryNExchange.serverSend(host, port, cmd.toString());
                    if (receiveData.equals("connection failed")) {
                        serverList.remove(select);
                    }
                    System.out.println("ServerList after exchange: " + serverList.toString());
                }
                Thread.sleep(exchangeSecond * 1000);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}