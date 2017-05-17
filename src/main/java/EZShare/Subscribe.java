package EZShare;

/**
 * This class is used for subscribing and unsubscribing functions on EZShare System.
 * @author: Jiacheng Chen
 * @date: May, 2017
 */

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class Subscribe {
    //resource Template
    private static JSONObject resTemp = new JSONObject();
    private static boolean relay = false;

    /**
     * Initiate the subscription
     * @param cmd
     * JSON command
     * @return the response
     * @throws IOException
     */
    public static JSONObject init(JSONObject cmd) throws IOException {
        JSONObject response = new JSONObject();

        //must contain resource template
        if (cmd.containsKey("resourceTemplate")) {
            //must contain an id
            if (cmd.containsKey("id")) {
                response.put("response", "success");
                response.put("id", cmd.get("id"));
                resTemp.equals(cmd.get("resourceTemplate"));
                if(cmd.containsKey("relay")) {
                    if (cmd.get("relay").equals("true")) {
                        relay = true;
                    }
                }
            } else {
                response.put("response", "error");
                response.put("errorMessage", "missing ID");
            }
        } else {
            response.put("response", "error");
            response.put("errorMessage", "missing resourceTemplate");
        }
        return response;
    }

    public static void subscribe (JSONObject cmd, Socket clientSocket, HashMap<Integer, Resource> resourceList,
                                  ArrayList<Resource> resQueue, Boolean secure,
                                  Logger logr_debug) throws IOException {
        if(!relay) {
            while (!resQueue.isEmpty()) {
                System.out.println(selfSubscribe(resQueue.get(0)));
                resQueue.remove(0);
            }
        }

    }

    public static JSONObject selfSubscribe (Resource src) {

        JSONArray cmdTagsJson = resTemp.getJSONArray("tags");
        String[] cmdTags = cmdTagsJson.toString().substring(1, cmdTagsJson.toString().length() - 1).split(",");
        String cmdName = resTemp.get("name").toString();
        String cmdDescription = resTemp.get("description").toString();


        Boolean channel = true, owner = true, tags = true, uri = true;
        Boolean name = false, description = false, nameDescription = false;

        if (!resTemp.get("channel").equals("") && !resTemp.get("channel").equals(src.getChannel())) {
            channel = false;
        } else if (resTemp.get("channel").equals("") && !src.getChannel().equals("")) {
            channel = false;
        }
        if (!resTemp.get("owner").equals("") && !resTemp.get("owner").equals(src.getOwner())) {
            owner = false;
        }
        if (cmdTags.length != 0 && !cmdTags[0].equals("")) {
            for(int j = 0; j < cmdTags.length; j++) {
                if (!src.getTags().contains(cmdTags[j])) {
                    tags = false;
                }
            }
        }
        if (!resTemp.get("uri").equals("") && !resTemp.get("uri").equals(src.getUri())) {
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
        }
        return src.toJSON();
    }

    public static void otherSubscribe (JSONObject cmd, Socket clientSocket, HashMap<Integer, Resource>
            resourceList, Boolean secure, Logger logr_debug) {

    }

}
