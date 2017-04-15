//package Server;
package main.java.Server;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

public class QueryNExchange {

    //This function is mainly for parsing the "relay" argument and overall control.
    public static JSONArray query (JSONObject command, HashMap<Integer, Resource> resourceList, JSONArray serverList) {
        boolean relay = Boolean.parseBoolean(command.get("relay").toString());
        JSONObject response = new JSONObject();
        JSONObject size = new JSONObject();
        JSONArray fullQueryList = new JSONArray();
        int querySize;

        if(!command.containsKey("resourceTemplate")) {
            response.put("response","error");
            response.put("errorMessage","missing resourceTemplate");
            fullQueryList.add(response);
            return fullQueryList;
        } else {
            response.put("response", "success");
            fullQueryList.add(response);
            if (!relay) {
                fullQueryList.add(selfQuery(command, resourceList));
            } else {
                command.put("relay", false);
                fullQueryList.add(selfQuery(command, resourceList));
                for(int i = 1; i < serverList.size(); i++) {
                    fullQueryList.addAll(otherQuery(serverList.getJSONObject(i), command));
                }
            }
            querySize = fullQueryList.size() - 1;
            if (fullQueryList.getJSONArray(1).size() == 0) {
                querySize = 0;
            }
            size.put("resultSize", querySize);
            fullQueryList.add(size);
            return fullQueryList;
        }
    }

    //This function is used for query the resource on this server.
    public static JSONArray selfQuery(JSONObject command, HashMap<Integer, Resource> resourceList) {
        JSONArray queryList = new JSONArray();
        JSONObject cmd = JSONObject.fromObject(command.get("resourceTemplate"));
        JSONArray cmdTagsJson = cmd.getJSONArray("tags");
        String[] cmdTags = cmdTagsJson.toString().substring(1, cmdTagsJson.toString().length() - 1).split(",");
        String cmdName = cmd.get("name").toString();
        String cmdDescription = cmd.get("description").toString();

        for(Resource src : resourceList.values()) {
            Boolean channel = true, owner = true, tags = true, uri = true;
            Boolean name = false, description = false, nameDescription = false;

            if (!cmd.get("channel").equals("") && !cmd.get("channel").equals(src.getChannel())) {
                channel = false;
            } else if (cmd.get("channel").equals("") && !src.getChannel().equals("")) {
                channel = false;
            }
            if (!cmd.get("owner").equals("") && !cmd.get("owner").equals(src.getOwner())) {
                owner = false;
            }
            if (cmdTags.length != 0 && !cmdTags[0].equals("")) {
                for(int j = 0; j < cmdTags.length; j++) {
                    if (!src.getTags().contains(cmdTags[j])) {
                        tags = false;
                    }
                }
            }
            if (!cmd.get("uri").equals("") && !cmd.get("uri").equals(src.getUri())) {
                uri = false;
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
                src.setOwner("*");
                queryList.add(src.toJSON());
            }
        }
        return queryList;
    }

    // This function is for one server to query the resource on another server.
    public static JSONArray otherQuery(JSONObject serverPort, JSONObject command) {
        String server = serverPort.get("hostname").toString();
        int port = Integer.parseInt(serverPort.get("port").toString());
        String sendData = command.toString();
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

            do {
                receiveData = in.readUTF();
                Thread.sleep(3000);
            } while (in.available() > 0);

            connection.close();

        } catch (IOException e){
            e.printStackTrace();
            receiveData = "connection failed";
        } finally {
            return receiveData;
        }
    }
}