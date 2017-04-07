/**
 * This class is used as client side in client-server model. The client class
 * basically takes responsibility for establishing connection with server,
 * parses the command line input and sends it to server. The class also receives
 * server response and displays for users.
 * @author: Jiayu Wang, Yiming Jiang
 * @date: April 5, 2017
 */

package Client;
import java.io.*;
import java.net.*;


public class Client {
    public static void main(String[] args) {
        //The domainName and serverPort need to be changed later
        String domainName = "localhost";
        int serverPort = 8080;
        String sendData;
        String receiveData;

        try {
            Socket connection = new Socket(domainName, serverPort);
            DataInputStream in = new DataInputStream(connection.getInputStream());
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            sendData = parseCmd(args);
            //Jiang takes this. Input is args, array of Strings, output is JSONObject.toString(), if there is error, than just print out error message
            if (sendData.equals("")) {
                connection.close();
            } else {
                out.writeUTF(sendData);
                out.flush();
                while (in.available() > 0) {
                    receiveData = in.readUTF();
                    parseBack(receiveData);
                }
                in.close();
                out.close();
                connection.close();
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public static String parseCmd(String[] args) {
        //If there is any error you can check, you can just print out the error message and return ""

        return "JSONObject.toString()";
    }

    public static void parseBack(String receiveData) {
        // Simply change the String format back to JSON and print, maybe pprint is more suitable here.
        // There are two cases, one is the normal msg, the other is file

    }
}
