import java.util.HashMap;

/**
 * A class that represent the list of resources
 */
public class ResourceList {
    private HashMap<String, HashMap<String, HashMap<String, Resource>>> resources;

    public ResourceList() {
        resources = new HashMap<String, HashMap<String, HashMap<String, Resource>>>();
    }

    /**
     * put a resource into the list
     *
     * @param res
     * resource
     */
    public void put(Resource res) {
        String channel = res.getChannel();
        String owner = res.getOwner();
        String uri = res.getUri();

        resources.put(channel, new HashMap<String, HashMap<String, Resource>>());
        resources.get(channel).put(uri, new HashMap<String, Resource>());
        resources.get(channel).get(uri).put(owner, res);
    }

    /**
     * get a resource from the list using the primary keys
     *
     * @param owner
     * owner of resource
     * @param channel
     * channel resource is on
     * @param uri
     * resource's uri
     * @return the resource
     */
    public Resource get(String owner, String channel, String uri) {
        return resources.get(channel).get(uri).get(owner);
    }

    /**
     * @return the size of the resource list
     */
    public int size() {
        return resources.size();
    }

    /**
     * @param res
     * resource to check
     *
     * @return if the list contains same resource with same channel and uri but different owner
     */
    public boolean contains(Resource res) {
        if(!resources.isEmpty()) {
            if (resources.containsKey(res.getChannel())) {
                if (resources.get(res.getChannel()).containsKey(res.getUri())) {
                    if (!resources.get(res.getChannel()).get(res.getUri()).containsKey(res.getOwner())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
