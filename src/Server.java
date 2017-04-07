/** Project description to be added.
 *
 */
//import org.json.simple.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import org.json.simple.*;


public class Server {

    private static ArrayList<Resource> resources = new ArrayList<Resource>();
    private static KeyList keys = new KeyList();

    /**
     * testing
     */

    public static void main(String[] args) {

        JSONObject abc = new JSONObject();
        abc.put("name", "ezshare");
        ArrayList<String> arr = new ArrayList<String>();
        arr.add("cmd");
        arr.add("win");
        abc.put("tags", arr);
        abc.put("description", "");
        abc.put("uri", "http://abc.com");
        //abc.put("uri", "file:\\/\\/\\/~/Download/abc.txt");
        abc.put("channel", "");
        abc.put("owner", "");
        abc.put("ezserver", "");

        JSONObject test = new JSONObject();
        //test.put("command", "SHARE");
        //test.put("secret", "a");

        test.put("command", "PUBLISH");
        test.put("resource", abc);
        System.out.println(test);
        publish(test);
        //share(test);

        abc.remove("owner");
        //abc.put("owner", "peter");
        abc.put("owner", "");

        JSONObject test2 = new JSONObject();
        test2.put("command", "PUBLISH");
        test2.put("resource", abc);
        System.out.println(test2);
        publish(test2);

        /*
        String domainName = "sunrise.cis.unimelb.edu.au";
        int serverPort = 3780;
        JSONObject abc = new JSONObject();
        JSONObject test = new JSONObject();
        String sendData;
        String receiveData;

        try {
            Socket connection = new Socket(domainName, serverPort);
            DataInputStream in = new DataInputStream(connection.getInputStream());
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            System.out.println(connection.getRemoteSocketAddress());
            abc.put("name", "ezshare");
            ArrayList<String> arr = new ArrayList<String>();
            arr.add("cmd");
            arr.add("win");
            abc.put("tags", arr);
            abc.put("description", "");
            abc.put("uri", "http://abc.com");
            //abc.put("uri", "file:\\/\\/\\/~/Download/abc.txt");
            abc.put("channel", "");
            abc.put("owner", "");
            abc.put("ezserver", "");
            test.put("command", "SHARE");
            //test.put("secret", "2os41f58vkd9e1q4ua6ov5emlv");
            //test.put("resource", abc);
            sendData = test.toString();
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
        */
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
                    if (keys.put(res)) {
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
                            if (keys.put(res)) {
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

//    Placeholder for fetch function
//    Placeholder for remove function


//    Placeholder for query function
//    Placeholder for exchange function

}
