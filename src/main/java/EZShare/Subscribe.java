package EZShare;

/**
 * This class is used for subscribing and unsubscribing functions on EZShare System.
 * @author:
 * @date: May 12, 2017
 */

import net.sf.json.JSONObject;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Logger;


public class Subscribe {
    public static void subscribe (JSONObject cmd, Socket clientSocket, HashMap<Integer, Resource>
            resourceList, Boolean secure, Logger logr_debug) throws IOException {

        //以下内容是我写着玩的，随便你们删
//        DataInputStream in = new DataInputStream(clientSocket.getInputStream());
//        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
//        HashMap<Integer, Resource> returnResouce = new HashMap<>();
////        JSONObject stop;
//
//        while (true) {
//            if (in.available() > 0) {
//                stop = JSONObject.fromObject(in.readUTF());
//                if (stop.containsKey("command") && stop.containsKey("id")) {
//                    stop.get("command").toString().equals("UNSUBSCRIBE")
//                }
//            }
//        }

        if (cmd.containsKey("resourceTemplate")) {

        } else {

        }
    }

    public static void selfSubscribe (JSONObject cmd, Socket clientSocket, HashMap<Integer, Resource>
            resourceList, Boolean secure, Logger logr_debug) {

    }

    public static void otherSubscribe (JSONObject cmd, Socket clientSocket, HashMap<Integer, Resource>
            resourceList, Boolean secure, Logger logr_debug) {

    }

}
