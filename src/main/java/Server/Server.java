package main.java.Server;

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
import java.util.ArrayList;
import java.util.HashMap;


public class Server {
    private static HashMap<Integer, Resource> resourceList = new HashMap<Integer, Resource>();
    private static JSONArray serverList = new JSONArray();
    private static KeyList keys = new KeyList();

    public static void main(String[] args) {
        try{
            //Test cases, can delete later
            Resource src1 = new Resource("http://src1.com", "my", "fibby", "lala", "something", new ArrayList<>(),"");
            Resource src2 = new Resource("", "", "", "lala", "something", new ArrayList<>(),"");
            resourceList.put(1, src1);
            resourceList.put(2, src2);

            int serverPort = Integer.parseInt(args[0]);
            boolean active = true;
            ServerSocketFactory factory = ServerSocketFactory.getDefault();
            ServerSocket server = factory.createServerSocket(serverPort);

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
        JSONArray sendMsg = new JSONArray();

        try(Socket clientSocket = client) {
            System.out.println("The connection with " + clientSocket.toString() + " has been established.");
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

             do {
                receiveData = in.readUTF();
                 System.out.println("Received command from client: " + receiveData);
                cmd = JSONObject.fromObject(receiveData);
                switch(cmd.get("command").toString()) {
                    case "PUBLISH":
                        sendMsg.add(PublishNShare.publish(cmd, resourceList, keys));
                        break;
                    case "REMOVE":
                    	sendMsg.add(RemoveAndFetch.remove(cmd, resourceList));
                        break;
                    case "SHARE":
                        sendMsg.add(PublishNShare.share(cmd, resourceList, keys));
                        break;
                    case "FETCH":
                    	sendMsg.add(RemoveAndFetch.fetch(cmd, resourceList));
                        break;
                    case "QUERY":
                        sendMsg.addAll(QueryNExchange.query(cmd, resourceList, serverList));
                        break;
                    case "EXCHANGE":
                        sendMsg.addAll(QueryNExchange.exchange(cmd, serverList, clientSocket));
                        break;
                    default:
                        msg.put("response", "error");
                        msg.put("errorMessage", "invalid command");
                        sendMsg.add(msg);
                        break;
                }
                out.writeUTF(sendMsg.toString());
                out.flush();
            } while(in.available() > 0);

            in.close();
            out.close();
            clientSocket.close();
            System.out.print("The connection with " + clientSocket.toString() + " has been closed.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
