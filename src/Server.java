/** Project description to be added.
 *
 */
//import org.json.simple.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


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
        System.out.println(resources.toString());
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
        String channel = (String) obj.get("channel");

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
            //if(!resources.contains(res)) {
            if(true){
                resources.put(res);
                response.put("response", "success");
                System.out.println(res.toJSON());
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
                System.out.println(res.toJSON());
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


        //new resource with uri
        Resource res = new Resource((String)obj.get("uri"));

        //array to store tags
        //ArrayList<String> tags = new ArrayList<String>();

        //set variables in Resource
        res.setName((String) obj.get("name"));
        res.setChannel((String)obj.get("channel"));
        res.setDescription((String) obj.get("description"));
        res.setOwner((String)obj.get("owner"));
        res.setOwner((String) obj.get("ezserver"));

        //iterate through the tags array and set the tags in Resource
        ArrayList<String> arr = (ArrayList<String>) obj.get("tags");
        /*Iterator it = arr.iterator();

        while (it.hasNext()) {
            tags.add((String) it.next());
        }
        if (!tags.isEmpty()) {
            res.setTags(tags);
        }*/
        res.setTags(arr);
        //add resource to resource list
        return res;
    }

//    Placeholder for fetch function
//    Placeholder for remove function


//    Placeholder for query function
//    Placeholder for exchange function

}
