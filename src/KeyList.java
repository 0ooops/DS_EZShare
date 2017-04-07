import java.util.HashMap;

/**
 * A class that represent the list of primary keys
 */
public class KeyList {
    private HashMap<String, HashMap<String, HashMap<String, Integer>>> keys;
    private Integer index;

    public KeyList() {
        keys = new HashMap<String, HashMap<String, HashMap<String, Integer>>>();
        index = 0;
    }

    /**
     * put a key set into the list
     *
     * @param res
     * resource
     */

    public int put(Resource res) {
        String channel = res.getChannel();
        String owner = res.getOwner();
        String uri = res.getUri();

        if (keys.containsKey(channel)) {
            if (keys.get(channel).containsKey(uri)) {
                if (!keys.get(channel).get(uri).containsKey(owner)) {
                    return -2;
                } else {
                    return keys.get(channel).get(uri).get(owner);
                }
            } else {
                keys.get(channel).put(uri, new HashMap<String, Integer>());
                keys.get(channel).get(uri).put(owner, index);
            }
        } else {
            keys.put(channel, new HashMap<String, HashMap<String, Integer>>());
            keys.get(channel).put(uri, new HashMap<String, Integer>());
            keys.get(channel).get(uri).put(owner, index);
        }
        //System.out.println(index);
        index += 1;
        return -1;
    }


    /**
     * @return the size of the key list
     */
    public int size() {
        return keys.size();
    }

}
