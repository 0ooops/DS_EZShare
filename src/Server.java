/** Project description to be added.
 *
 */
//import org.json.simple.*;

import java.util.ArrayList;
import org.json.simple.*;


public class Server {

    //private static ArrayList<Resource> res = new ArrayList<Resource>();
    private static ResourceList resources = new ResourceList();

    /**
     * testing main
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
        abc.put("channel", "");
        abc.put("owner", "");
        abc.put("ezserver", "");

        publish(abc);
        //abc.remove("owner");
        //abc.put("owner", "chen");
        publish(abc);
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
        String uri = (String) obj.get("uri");
        String owner = (String) obj.get("owner");

        //check if resource is valid
        if(uri.equals("")
                || uri == null
                || owner.equals("*")) {
            response.put("response", "error");
            response.put("errorMessage", "invalid resource");
        } else if(uri.startsWith("file:\\/\\/\\/")) {
            response.put("response", "error");
            response.put("errorMessage", "cannot publish resource");
        } else {
            //create a resource and add to the resource list
            Resource res = getResource(obj);
            if(!resources.contains(res)) {

                resources.put(res);
                response.put("response", "success");
            } else {
                response.put("response", "error");
                response.put("errorMessage", "invalid resource");
            }
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

        String uri = (String) obj.get("uri");
        String owner = (String) obj.get("owner");

        //check if resource is valid
        if(uri.equals("")
                || uri == null
                || owner.equals("*")) {
            response.put("response", "error");
            response.put("errorMessage", "invalid resource");
        } else if(!uri.startsWith("file:\\/\\/\\/")) {
            response.put("response", "error");
            response.put("errorMessage", "cannot share resource");
        } else {
            //create a resource and add to the resource list
            Resource res = getResource(obj);
            if(!resources.contains(res)) {
                resources.put(res);
                response.put("response", "success");
            } else {
                response.put("response", "error");
                response.put("errorMessage", "invalid resource");
            }
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
        String channel = (String)obj.get("channel");
        String owner = (String)obj.get("owner");
        String name = (String) obj.get("name");
        String des = (String) obj.get("description");
        ArrayList<String> tags = (ArrayList<String>)obj.get("tags");
        String server = (String) obj.get("ezserver");

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
