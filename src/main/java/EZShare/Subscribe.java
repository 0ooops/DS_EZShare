package EZShare;

/**
 * This class is used for subscribing and unsubscribing functions on EZShare System.
 *
 * @author: Jiacheng Chen
 * @date: May, 2017
 */

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.cli.CommandLine;

import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

public class Subscribe {

    /**
     * Initiate the subscription
     *
     * @param cmd JSON command
     * @return the response
     */
    public static void init(JSONObject cmd, Socket clientSocket, HashMap<Integer, Resource> resourceList,
                            boolean secure, Logger logr_debug, String ip,
                            int port, boolean debug, JSONArray serverList)
            throws IOException, InterruptedException {
        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
        JSONObject response = new JSONObject();
        ArrayList<JSONObject> resTempList = new ArrayList<>();
        boolean relay = false;

        //must contain resource template
        if (cmd.containsKey("resourceTemplate")) {
            //must contain an id
            if (cmd.containsKey("id")) {
                response.put("response", "success");
                response.put("id", cmd.get("id"));
                send(out, logr_debug, response);
                resTempList.add((JSONObject) cmd.get("resourceTemplate"));
                sleep(2000);
                if (cmd.containsKey("relay")) {
                    if (cmd.get("relay").equals("true")) {
                        relay = true;
                    }
                }
                HashMap<String, ArrayList<JSONObject>> sub = new HashMap<>();
                sub.put((String) cmd.get("id"), resTempList);
                Server.updateSubList(clientSocket, sub);
                subscribe(cmd,clientSocket, resourceList, secure, logr_debug, resTempList,
                            relay, ip, port, debug, serverList);
            } else {
                response.put("response", "error");
                response.put("errorMessage", "missing ID");
                send(out, logr_debug, response);
            }
        } else {
            response.put("response", "error");
            response.put("errorMessage", "missing resourceTemplate");
            send(out, logr_debug, response);
        }
    }


    private static void subscribe(JSONObject cmd, Socket clientSocket, HashMap<Integer, Resource> resourceList,
                                 boolean secure, Logger logr_debug, ArrayList<JSONObject> resTempList,
                                 boolean relay, String ip, int port, boolean debug, JSONArray serverList) {
        boolean flag = true;

        try {
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            JSONArray sendMsg = new JSONArray();
            if (!relay) {
                //System.out.println(selfSubscribe());
                resTempList.add((JSONObject) cmd.get("resourceTemplate"));
                for (Resource src : resourceList.values()) {
                    JSONObject m = checkTemplate(src, resTempList);

                    if (!m.has("null")) {
                        sendMsg.add(m);
                        Server.incrementCounter(clientSocket);
                    }

                }
                send(out, logr_debug, sendMsg);

                while (flag) {
                    //check if unsubscribe
                    sleep(2000);
                    if (in.available() > 0) {
                        String recv = in.readUTF();
                        System.out.println(recv);
                        if (recv.contains("UNSUBSCRIBE")) {
                            logr_debug.fine("RECEIVED: " + recv);
                            JSONObject unsubmsg = new JSONObject();
                            unsubmsg.put("resultSize", Server.getCounter(clientSocket));
                            sendMsg.clear();
                            sendMsg.add(unsubmsg);
                            send(out, logr_debug, sendMsg);

                            flag = false;
                        }
                    }

                    //debug message
                    if (debug) {
                        FileReader file = new FileReader("./debug_" + ip + "_" + port +".log");
                        BufferedReader br = new BufferedReader(file);
                        String dCurrentLine;
                        while ((dCurrentLine = br.readLine()) != null) {
                            System.out.println(dCurrentLine);
                        }
                        Server.setupDebug();
                        br.close();
                        file.close();
                    }
                }
            } else {
                relay(cmd, serverList, secure);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void send(DataOutputStream out, Logger logr_debug, JSONArray sendMsg) {
//        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

        //System.out.println(sendMsg.toString());
        try {
            for (int i = 0; i < sendMsg.size(); i++) {
                out.writeUTF(sendMsg.getJSONObject(i).toString());
            }
            logr_debug.fine("SENT: " + sendMsg.toString());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void send(DataOutputStream out, Logger logr_debug, JSONObject sendMsg) {
//        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

        //System.out.println(sendMsg.toString());
        try {
            out.writeUTF(sendMsg.toString());
            logr_debug.fine("SENT: " + sendMsg.toString());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * check if the resource matches resource template
     * @param src resource to be check
     * @param resTempList resource templates
     * @return the resource if match or with a null tag if unmatch
     */
    public static JSONObject checkTemplate(Resource src, ArrayList<JSONObject> resTempList) {

        for (JSONObject resTemp : resTempList) {
            JSONArray cmdTagsJson = resTemp.getJSONArray("tags");
            String[] cmdTags = cmdTagsJson.toString().substring(1,
                    cmdTagsJson.toString().length() - 1).split(",");
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
                for (int j = 0; j < cmdTags.length; j++) {
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
                return src.toJSON();
            }
        }
        JSONObject nul = new JSONObject();
        nul.put("null", "true");
        return nul;
    }

    private static void relay(JSONObject cmd, JSONArray serverList, boolean secure) {
//        cmd.put("relay", "false");
//        String sendData = cmd.toString();
//        String receiveData = "";
//        boolean unSubscribe = false;
//        try {
//
//            for (int i = 0; i < serverList.size(); i++) {
//                Socket connection;
//                String host = serverList.getJSONObject(i).get("hostname").toString();
//                int newPort = (int) serverList.getJSONObject(i).get("port");
//                if (secure) {
//                    System.setProperty("javax.net.ssl.trustStore", "clientKeyStore/client-keystore.jks");
//                    SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
//                    connection = sslsocketfactory.createSocket(host, newPort);
//                } else {
//                    connection = new Socket(host, newPort);
//                }
//            }
//            DataInputStream in = new DataInputStream(connection.getInputStream());
//            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
//            BufferedReader enterRead = new BufferedReader(new InputStreamReader(System.in));
//            out.writeUTF(sendData);
//            out.flush();
//            logr.fine("SENT:" + sendData);
//
//            while (!unSubscribe) {
//                Thread.sleep(2000);
//                while (in.available() > 0) {
//                    String read = in.readUTF();
////                    System.out.println(read);
//                    receiveData += read + ",";
//                    logr.fine("RECEIVED:" + receiveData);
//                }
//                if (!receiveData.equals("")) {
//                    receiveData = "[" + receiveData.substring(0, receiveData.length() - 1) + "]";
////                System.out.println(receiveData);
//                    JSONArray recv = (JSONArray) JSONSerializer.toJSON(receiveData);
//                    JSONObject resp = recv.getJSONObject(0);
//                    if (resp.has("response")) {
//                        String respTpye = (String) resp.get("response");
//                        if (respTpye.equals("error")) {
//                            System.out.print("error,");
//                            System.out.println(resp.get("errorMessage") + "!");
//                        } else {
//                            System.out.println("success!");
//                            recv.clear();
//                            receiveData = "";
//                        }
//                    } else {
//                        printSubResult(recv);
//                        receiveData = "";
//                        recv.clear();
//                    }
//                }
//                if (enterRead.ready())  {
//                    if (enterRead.read() == '\n'){
//                        String unsubMsg = unsubscribeCommand().toString();
//                        out.writeUTF(unsubMsg);
//                        logr.fine("SENT:" + unsubMsg);
//                        unSubscribe = true;
//                    }else {
//                        enterRead.read();
//                    }
//                }
//                if (cmd.hasOption("debug")) {
//                    //print logfile
//                    FileReader file = new FileReader("./logfile.log");
//                    BufferedReader br = new BufferedReader(file);
//                    String sCurrentLine;
//                    while ((sCurrentLine = br.readLine()) != null) {
//                        System.out.println(sCurrentLine);
//                    }
//                    setupLogger();
//                    br.close();
//                    file.close();
//                }
//            }
//            if (unSubscribe){
//                do {
//                    Thread.sleep(1000);
//                    String read = in.readUTF();
////                    System.out.println(read);
//                    receiveData += read + ",";
//                }while (in.available() > 0);
//
//                if (!receiveData.equals("")) {
//                    receiveData = "[" + receiveData.substring(0, receiveData.length() - 1) + "]";
//                    JSONArray recv = (JSONArray) JSONSerializer.toJSON(receiveData);
//                    if (recv.size()==1){
//                        System.out.println(recv.toString());
//                    }else {
//                        String finalRecv =recv.get(recv.size()-1).toString();
//                        recv.remove(recv.size()-1);
//                        printSubResult(recv);
//                        System.out.println(finalRecv);
//                    }
//                }
//            }
//            if (cmd.hasOption("debug")) {
//                //print logfile
//                FileReader file = new FileReader("./logfile.log");
//                BufferedReader br = new BufferedReader(file);
//                String sCurrentLine;
//                while ((sCurrentLine = br.readLine()) != null) {
//                    System.out.println(sCurrentLine);
//                }
//                br.close();
//                file.close();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

}
