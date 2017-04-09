package main.java.Server;

import net.sf.json.JSONObject;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * public and share funtions
 */
public class PublishNShare {

    /**
     * publish resource in the server
     *
     * @param obj
     * json object contain the resource
     */
    public static JSONObject publish(JSONObject obj, HashMap<Integer, Resource> resourceList, KeyList keys) {
        //json object contains command respond
        //JSONArray r = new JSONArray();
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
                } else if (!(uri.startsWith("http:\\/\\/") || uri.startsWith("ftp:\\/\\/"))) {
                    response.put("response", "error");
                    response.put("errorMessage", "cannot publish resource");
                } else {
                    //create a resource and add to the resource list
                    Resource res = getResource(resJSON);
                    int index = keys.put(res);
                    //System.out.println(index);

                    /*
                            index -2: resource has same channel and uri but different user
                            index -1: put successful
                            index >0: resource with a same primary key as an existing resource,
                                      use this index to locate the file in resource list and overwrites it
                             */
                    if (index >= -1) {
                        if (index >= 0) {
                            resourceList.remove(index);
                            resourceList.put(index, res);
                        }
                        resourceList.put(index, res);
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
        return response;
    }

    /**
     * publish resource with a file type uri in the server
     *
     * @param obj
     * json object contain the resource
     */
    public static JSONObject share(JSONObject obj, HashMap<Integer, Resource> resourceList, KeyList keys) {
        //json object contains command respond
        //JSONArray r = new JSONArray();
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
                        File f = new File(uri);
                        String owner = "";
                        if (resJSON.containsKey("owner")) {
                            owner = (String) resJSON.get("owner");
                        }

                        //check if resource is valid
                        if (uri.equals("") || owner.equals("*")) {
                            response.put("response", "error");
                            response.put("errorMessage", "invalid resource");
                        } else if (!f.exists()) {
                            response.put("response", "error");
                            response.put("errorMessage", "cannot share resource");
                        } else {
                            //create a resource and add to the resource list
                            Resource res = getResource(resJSON);
                            int index = keys.put(res);

                            /*
                            index -2: resource has same channel and uri but different user
                            index -1: put successful
                            index >0: resource with a same primary key as an existing resource,
                                      use this index to locate the file in resource list and overwrites it
                             */
                            if (index >= -1) {
                                if (index >= 0) {
                                    resourceList.remove(index);
                                    resourceList.put(index, res);
                                }
                                resourceList.put(index, res);
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
        //r.add(response);
        return response;
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
}
