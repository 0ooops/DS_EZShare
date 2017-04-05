/**
 * This is used for testing prof's server.
 */

import net.sf.json.JSONObject;

import java.io.*;
import java.net.*;


public class ClientTest {
    public static void main(String[] args) {
        String domainName = "sunrise.cis.unimelb.edu.au";
        int serverPort = 3780;
        JSONObject msg = new JSONObject();
        JSONObject src = new JSONObject();
        String sendData;
        String receiveData;

        try {
            Socket connection = new Socket(domainName, serverPort);
            DataInputStream in = new DataInputStream(connection.getInputStream());
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            System.out.println(connection.getRemoteSocketAddress());
            msg.put("command", "QUERY");
            msg.put("relay", true);
            src.put("name", "");
            src.put("description", "");
            msg.put("resourceTemplate", src);
            sendData = msg.toString();
            System.out.println(sendData);

            out.writeUTF(sendData);
            out.flush();

            do {
                receiveData = in.readUTF();
                System.out.println(receiveData);
            }while (in.available() > 0);
            in.close();
            out.close();
            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
