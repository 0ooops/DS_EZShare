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
    private static ArrayList<Resource> resourceList = new ArrayList<>();
    private static ArrayList<HashMap> serverList = new ArrayList<>();
    private static KeyList keys = new KeyList();

    public static void main(String[] args) {
        try{
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
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

            while(in.available() > 0) {
                receiveData = in.readUTF();
                cmd = JSONObject.fromObject(receiveData);
                switch(cmd.get("command").toString()) {
                    case "PUBLISH":
                        publish(cmd);
                        break;
                    case "REMOVE":
                        remove(cmd);
                        break;
                    case "SHARE":
                        share(cmd);
                        break;
                    case "FETCH":
                        fetch(cmd);
                        break;
                    case "QUERY":
                        sendMsg = query(cmd);
                        break;
                    case "EXCHANGE":
                        sendMsg = exchange(cmd);
                        break;
                    default:
                        msg.put("response", "error");
                        msg.put("errorMessage", "invalid command");
                        sendMsg.add(msg);
                        break;
                }
                sendMsg.add(sendMsg);
                out.writeUTF(sendMsg.toString());
                out.flush();
                in.close();
                out.close();
                clientSocket.close(); //test下有没有问题
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * publish resource in the server
     *
     * @param obj
     * json object contain the resource
     */
    public static void publish(JSONObject obj) {
        //json object contains command respond
        JSONObject response = new JSONObject();

        //check if json has a resource
        if (obj.containsKey("resource")) {
            //get the resources json
            JSONObject resJSON = (JSONObject) obj.get("resource");
            if (resJSON.containsKey("uri")) {
                String uri = (String) resJSON.get("uri");
                String owner = "";
                if (resJSON.containsKey("owner")) {
                    owner = (String) resJSON.get("owner");
                }

                //check if resource is valid
                if (uri.equals("") || owner.equals("*")) {
                    response.put("response", "error");
                    response.put("errorMessage", "invalid resource");
                } else if (uri.startsWith("file:\\/\\/\\/")) {
                    response.put("response", "error");
                    response.put("errorMessage", "cannot publish resource");
                } else {
                    //create a resource and add to the resource list
                    Resource res = getResource(resJSON);
                    int index = keys.put(res);
                    //System.out.println(index);
                    if (index >= -1) {
                        if (index >= 0) {
                            resources.remove(index);
                            resources.add(index, res);
                        }
                        resources.add(res);
                        response.put("response", "success");
                    } else {
                        response.put("response", "error");
                        response.put("errorMessage", "invalid resource");
                    }
                }
            } else {
                response.put("response", "error");
                response.put("errorMessage", "invalid resource");
            }
        } else {
            response.put("response", "error");
            response.put("errorMessage", "missing resource");
        }

        //respond to command
        System.out.println(response);
    }

    /**
     * publish resource with a file type uri in the server
     *
     * @param obj
     * json object contain the resource
     */
    public static void share(JSONObject obj) {
        //json object contains command respond
        JSONObject response = new JSONObject();

        //check if json contains a secret
        if (obj.containsKey("secret")) {
            //check if the secret is valid
            if (!((String)obj.get("secret")).equals("")) {
                //check if there is a resource
                if (obj.containsKey("resource")) {
                    //get the resources json
                    JSONObject resJSON = (JSONObject) obj.get("resource");
                    System.out.println(resJSON);
                    if (resJSON.containsKey("uri")) {
                        String uri = (String) resJSON.get("uri");
                        String owner = "";
                        if (resJSON.containsKey("owner")) {
                            owner = (String) resJSON.get("owner");
                        }

                        //check if resource is valid
                        if (uri.equals("") || owner.equals("*")) {
                            response.put("response", "error");
                            response.put("errorMessage", "invalid resource");
                        } else if (!uri.startsWith("file:\\/\\/\\/")) {
                            response.put("response", "error");
                            response.put("errorMessage", "cannot share resource");
                        } else {
                            //create a resource and add to the resource list
                            Resource res = getResource(resJSON);
                            int index = keys.put(res);
                            if (index >= -1) {
                                if (index >= 0) {
                                    resources.remove(index);
                                    resources.add(index, res);
                                }
                                resources.add(res);
                                response.put("response", "success");
                            } else {
                                response.put("response", "error");
                                response.put("errorMessage", "invalid resource");
                            }
                        }
                    } else {
                        response.put("response", "error");
                        response.put("errorMessage", "invalid resource");
                    }
                } else {
                    response.put("response", "error");
                    response.put("errorMessage", "missing resource and\\/or secret");
                }
            } else {
                response.put("response", "error");
                response.put("errorMessage", "incorrect secret");
            }
        } else {
            response.put("response", "error");
            response.put("errorMessage", "missing resource and\\/or secret");
        }


        //respond to command
        System.out.println(response);
    }


    /**
     * parse json object into a resource object
     *
     * @param obj
     * json object contain the resource
     */
    private static Resource getResource(JSONObject obj) {
        //get resource parameters
        String uri = (String)obj.get("uri");

        String channel = "";
        if (obj.containsKey("channel")) {
            channel = (String) obj.get("channel");
        }

        String owner = "";
        if (obj.containsKey("owner")) {
            owner = (String) obj.get("owner");
        }

        String name = "";
        if (obj.containsKey("name")) {
            name = (String) obj.get("name");
        }

        String des = "";
        if (obj.containsKey("description")) {
            des = (String) obj.get("description");
        }

        ArrayList<String> tags = new ArrayList<String>();
        if (obj.containsKey("tags")) {
            tags = (ArrayList<String>)obj.get("tags");
        }

        String server = "";
        if (obj.containsKey("ezserver")) {
            server = (String) obj.get("ezserver");
        }

        //new resource
        Resource res = new Resource(uri, channel, owner, name, des, tags, server);

        //add resource to resource list
        return res;
    }

    //This function is mainly for parsing the "relay" argument and overall control.
    public static JSONArray query (JSONObject command) {
        Boolean relay = (Boolean)command.get("relay");
        JSONObject response = new JSONObject();
        JSONObject size = new JSONObject();
        JSONArray fullQueryList = new JSONArray();
        if(!command.containsKey("resourceTemplate")) {
            response.put("response","error");
            response.put("errorMessage","missing resourceTemplate");
            fullQueryList.add(response);
            return fullQueryList;
        } else {
            response.put("response", "success");
            fullQueryList.add(response);
            if (!relay) {
                fullQueryList.add(selfQuery(command));
            } else {
                command.put("relay", false);
                fullQueryList = selfQuery(command);
                for(int i = 0; i < serverList.size(); i++) {
                    fullQueryList.addAll(otherQuery(serverList.get(i), command));
                }
            }
            size.put("resultSize",fullQueryList.size() - 1);
            fullQueryList.add(size);
            return fullQueryList;
        }
    }

    //This function is used for query the resource on this server.
    public static JSONArray selfQuery(JSONObject command) {
        JSONArray queryList = new JSONArray();
        JSONObject cmd = JSONObject.fromObject(command.get("resourceTemplate"));

        for(int i = 0; i < resourceList.size(); i++) {
            Boolean channel = false, owner = false, tags = true, uri = false;
            Boolean name = false, description = false, nameDescription = false;
            Resource src = resourceList.get(i);
            String[] cmdTags = (String[])cmd.get("tags");
            String cmdName = cmd.get("name").toString();
            String cmdDescription = cmd.get("description").toString();

            if (cmd.get("channel").equals(src.getChannel())) {
                channel = true;
            }
            if (cmd.get("owner").equals(src.getOwner())) {
                owner = true;
            }
            if (cmdTags.length != 0) {
                for(int j = 0; j < cmdTags.length; j++) {
                    if (!src.getTags().contains(cmdTags[j])) {
                        tags = false;
                    }
                }
            }
            if (cmd.get("uri").equals(src.getUri())) {
                uri = true;
            }
            if ((!cmdName.equals("")) && src.getName().contains(cmdName)) {
                name = true;
            }
            if ((!cmdDescription.equals("")) && src.getDescription().contains(cmdDescription)) {
                description = true;
            }
            if (cmdName.equals("") && cmdDescription.equals("")) {
                nameDescription = true;
            }
            if (channel && owner && tags && uri && (name || description || nameDescription)) {
                queryList.add(src.toJSON());
            }
        }
        return queryList;
    }

    // This function is for one server to query the resource on another server.
    public static JSONArray otherQuery(HashMap serverPort, JSONObject command) {
        String server = serverPort.get("hostname").toString();
        int port = (int)serverPort.get("port");
        String sendData = command.toString(); //我这里相当于假定Client端负责解析，Server端收到的是toString过的JSON
        JSONArray queryList;

        String receiveData = serverSend(server, port, sendData);
        queryList = JSONArray.fromObject(receiveData);

        return queryList;
    }

    // This function is for one server to send data to another server.
    public static String serverSend(String server, int port, String data) {
        String receiveData = "";
        try {
            Socket connection = new Socket(server, port);
            DataInputStream in = new DataInputStream(connection.getInputStream());
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            out.writeUTF(data);
            out.flush();
            receiveData = in.readUTF();

            connection.close();

        } catch (IOException e){
            e.printStackTrace();
        } finally {
            return receiveData;
        }
    }

    public static JSONArray exchange (JSONObject command) {
        ArrayList<HashMap> newList = (ArrayList<HashMap>)command.get("serverList");
        JSONArray msgArray = new JSONArray();
        JSONObject msg = new JSONObject();

        for (int i = 0; i < newList.size(); i++) {
            if (!serverList.contains(newList.get(i))) { // double check whether contains work for this case
                serverList.add(newList.get(i));
                String host = newList.get(i).get("hostname").toString();
                int port = (int)newList.get(i).get("port");
                Query.serverSend(host, port, command.toString());
            }
        }

        msg.put("response", "success");
        msgArray.add(msg);

        return msgArray;
    }
}
