import java.util.HashMap;

/**
 * A class that represent the list of resources
 */
public class KeyList {
    private HashMap<String, HashMap<String, String>> keys;

    public KeyList() {
        keys = new HashMap<String, HashMap<String, String>>();
    }

    /**
     * put a resource into the list
     *
     * @param res
     * resource
     */

    public boolean put(Resource res) {
        String channel = res.getChannel();
        String owner = res.getOwner();
        String uri = res.getUri();
        boolean success = true;
        boolean fail = false;
        if (keys.containsKey(channel)) {
            if (keys.get(channel).containsKey(uri)) {
                if (!keys.get(channel).get(uri).equals(owner)) {
                    return fail;
                }
            } else {
                keys.get(channel).put(uri, owner);
            }
        } else {
            keys.put(channel, new HashMap<String, String>());
            keys.get(channel).put(uri, owner);
        }

        return success;
    }


    /**
     * @return the size of the resource list
     */
    public int size() {
        return keys.size();
    }

}
