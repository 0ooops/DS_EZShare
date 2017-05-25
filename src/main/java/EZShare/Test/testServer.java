package EZShare.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.net.Socket;

public class testServer {

    public static void main(String[] arstring) {

        try {
            //Create SSL server socket
            ServerSocketFactory factory = ServerSocketFactory.getDefault();
            ServerSocket server = factory.createServerSocket(8080);

            //Accept client connection
            Socket hello = server.accept();

            //Create buffered reader to read input from the client
            InputStream inputstream = hello.getInputStream();
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

            String string = null;
            //Read input from the client and print it to the screen
            while ((string = bufferedreader.readLine()) != null) {
                System.out.println(string);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
