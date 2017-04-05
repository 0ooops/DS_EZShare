/** Project description to be added.
 *
 */
import org.json.simple.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;


public class Server {

    private HashMap<String[], Resource> resources = new HashMap<String[], Resource>();
    private HashMap<String[], String[]> primaryKeys = new HashMap<String[], String[]>();
    private int uri = 2;
    private int channel = 1;
    private int owner = 0;

    /**
     * publish resource in the server
     *
     * @param obj
     * json object contain the resource
     */
    public void publish(JSONObject obj) {
        //json object contains command respond
        JSONObject response = new JSONObject();
        String[] keys = getPrimaryKeys(obj);

        //key contains only channel and uri
        String[] pKey = new String[2];
        pKey[0] = keys[channel];
        pKey[1] = keys[uri];

        //check if resource is valid
        if(keys[uri].equals("")
                || keys[uri] == null
                || keys[owner].equals("*")
                || primaryKeys.containsKey(pKey)) {
            response.put("response", "error");
            response.put("errorMessage", "invalid resource");
        } else if(keys[uri].startsWith("file:\\/\\/\\/\\/")) {
            response.put("response", "error");
            response.put("errorMessage", "cannot publish resource");
        } else {
            //create a resource and add to the resource list
            //Resource res = addResource(obj, keys, pKey);
            response.put("response", "success");
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
    public void share(JSONObject obj) {
        //json object contains command respond
        JSONObject response = new JSONObject();
        String[] keys = getPrimaryKeys(obj);

        //key contains only channel and uri
        String[] pKey = new String[2];
        pKey[0] = keys[channel];
        pKey[1] = keys[uri];

        //check if resource is valid
        if(keys[uri].equals("")
                || keys[uri] == null
                || keys[owner].equals("*")
                || primaryKeys.containsKey(pKey)) {
            response.put("response", "error");
            response.put("errorMessage", "invalid resource");
        } else if(!keys[uri].startsWith("file:\/\/\/\/")) {
            response.put("response", "error");
            response.put("errorMessage", "cannot share resource");
        } else {
            //create a resource and add to the resource list
            //Resource res = addResource(obj, keys, pKey);
            response.put("response", "success");
        }

        //respond to command
        System.out.println(response);
    }

    /**
     * get the primary key of resource
     *
     * @param obj
     * json object contain the resource
     *
     * @return
     * primary keys
     */
    private String[] getPrimaryKeys(JSONObject obj) {
        //primary keys
        String uri = (String) obj.get("uri");
        String channel = (String) obj.get("channel");
        String owner = (String) obj.get("owner");
        String[] keys = new String[3];
        keys[0] = owner;
        keys[1] = channel;
        keys[2] = uri;

        return keys;
    }

    /**
     * parse json object into a resource object
     *
     * @param obj
     * json object contain the resource
     *
     * @param keys
     * primary keys, 0 is owner, 1 is channel, 2 is uri
     */
    private Resource addResource(JSONObject obj, String[] keys, String[] pKey) {


        //new resource with uri
        Resource res = new Resource(keys[uri]);

        //array to store tags
        ArrayList<String> tags = new ArrayList<String>();

        //set variables in Resource
        res.setName((String) obj.get("name"));
        res.setChannel(keys[channel]);
        res.setDescription((String) obj.get("description"));
        res.setOwner(keys[owner]);
        res.setOwner((String) obj.get("ezserver"));

        //iterate through the tags array and set the tags in Resource
        JSONArray arr = (JSONArray) obj.get("tags");
        Iterator it = arr.iterator();

        while (it.hasNext()) {
            tags.add((String) it.next());
        }
        if (!tags.isEmpty()) {
            res.setTags(tags);
        }

        //add resource to resource list
        primaryKeys.put(pKey, keys);
        resources.put(keys, res);
        return res;
    }

//    Placeholder for fetch function
//    Placeholder for remove function


//    Placeholder for query function
//    Placeholder for exchange function

}
