import java.util.HashMap;

/**
 * Created by Peter on 6/4/17.
 */
public class ResourceList {
    private HashMap<String, HashMap<String, HashMap<String, Resource>>> resources;

    public ResourceList() {
        resources = new HashMap<String, HashMap<String, HashMap<String, Resource>>>();
    }

    public void put(Resource res) {
        String channel = res.getChannel();
        String owner = res.getOwner();
        String uri = res.getUri();

        /*
        HashMap<String, Resource> r = new HashMap<String, Resource>();
        HashMap<String, HashMap<String, Resource>> uris = new HashMap<String, HashMap<String, Resource>>();
        r.put(owner, res);
        uris.put(uri, r);
        */
        resources.put(channel, new HashMap<String, HashMap<String, Resource>>());
        resources.get(channel).put(uri, new HashMap<String, Resource>());
        resources.get(channel).get(uri).put(owner, res);
    }

    public Resource get(String owner, String channel, String uri) {
        return resources.get(channel).get(uri).get(owner);
    }

    public boolean contains(Resource res) {
        if(!resources.isEmpty()) {
            System.out.println(1);
            if (resources.containsKey(res.getChannel())) {
                System.out.println(2);
                if (resources.get(res.getChannel()).containsKey(res.getUri())) {
                    System.out.println(3);
                    if (!resources.get(res.getChannel()).get(res.getUri()).containsKey(res.getOwner())) {
                        System.out.println(4);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
