package EZShare;

import java.io.*;
import java.net.Socket;

public class ClientThread extends Thread {
    private String receiveData = "";
    //    boolean unSubscribe = false;
    private Socket connection;

    public ClientThread(String host, int port, String sendData) {
        try {
            connection = new Socket(host, port);
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
//            out.writeUTF("{\"resourceTemplate\":{\"name\":\"\",\"tags\":[],\"description\":\"\",\"uri\":\"\",\"channel\":\"\",\"owner\":\"\"},\"command\":\"QUERY\",\"relay\":\"false\"}");
            out.writeUTF(sendData);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.start();
    }

    public void run() {
        try {
            DataInputStream in = new DataInputStream(connection.getInputStream());
            while (true) {
                Thread.sleep(1000);
                while (in.available() > 0) {
                    String read = in.readUTF();
                    System.out.println(read);
                    receiveData += read + ",";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}