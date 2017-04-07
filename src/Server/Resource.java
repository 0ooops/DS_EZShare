/**
 * The class Resource is an entity used for storing resources records.
 * The constructor of the class requires the value of uri, because uri
 * is a compulsory element of a resource.
 * The class provides getters and setters for all attributes, and a
 * toJSON function to return the whole resource in form of JSON.
 * @author: Jiayu Wang
 * @date: April 1, 2017
 */
import net.sf.json.JSONObject;
import java.util.ArrayList;


public class Resource {
    private String name;
    private String description;
    private ArrayList<String> tags;
    private String uri;
    private String channel;
    private String owner;
    private String ezServer;

    //Constructor: A URI is a compulsory element for resource.
    public Resource(String uri, String channel, String owner, String name,
                    String description, ArrayList<String> tags, String ezserver) {
        this.uri = uri;
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.channel = channel;
        this.owner = owner;
        this.ezServer = ezserver;
    }

    //Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getEzServer() {
        return ezServer;
    }

    public void setEzServer(String ezServer) {
        this.ezServer = ezServer;
    }

    //toJSON function
    public JSONObject toJSON() {
        JSONObject myResource = new JSONObject();

        myResource.put("name", getName());
        myResource.put("tags", getTags());
        myResource.put("description", getDescription());
        myResource.put("uri", getUri());
        myResource.put("channel", getChannel());
        if (getOwner().equals("")) {
            myResource.put("owner", getOwner());
        } else {
            myResource.put("owner", "*");
        }
        myResource.put("ezserver", getEzServer());

        return myResource;
    }

}

