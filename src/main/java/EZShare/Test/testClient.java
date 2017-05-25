package EZShare.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class testClient {
    public static void main(String[] arstring) {
        try {
            Socket connection = new Socket("localhost", 8080);

            //Create buffered reader to read input from the console
            InputStream inputstream = System.in;
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

            //Create buffered writer to send data to the server
            OutputStream outputstream = connection.getOutputStream();
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter(outputstream);
            BufferedWriter bufferedwriter = new BufferedWriter(outputstreamwriter);

            String string = null;
            //Read line from the console
            Boolean flag = true;
            while (flag) {
                if ((string = bufferedreader.readLine()) != null) {
                    System.out.println(string);
                    String[] array = string.split("");
                    for (int i = 0; i < array.length; i++) {
                        System.out.println(array[i]);
                        if (array[i].equals('\n')) {
                            System.out.println("Break!");
                            flag = false;
                            break;
                        }
                    }
                }
                //Send data to the server
                bufferedwriter.write("inside");
                bufferedwriter.flush();
            }
            System.out.println("out");
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}