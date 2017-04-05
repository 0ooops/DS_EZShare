/**
 * Created by think on 2017/4/2.
 */

import net.sf.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;


public class Exchange {
    private ArrayList<HashMap> serverList = new ArrayList<>();

    public void exchange (JSONObject command) {
        ArrayList<HashMap> newList = (ArrayList<HashMap>)command.get("serverList");

        for (int i = 0; i < newList.size(); i++) {
            if (!serverList.contains(newList.get(i))) { // double check whether contains work for this case
                serverList.add(newList.get(i));
                String host = newList.get(i).get("hostname").toString();
                int port = (int)newList.get(i).get("port");
                Query.serverSend(host, port, command.toString());
            }
        }
    }

}