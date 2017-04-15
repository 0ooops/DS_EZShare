package Server;
//package main.java.Server;
import net.sf.json.JSONObject;
import java.util.HashMap;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class RemoveAndFetch {
	
	public static JSONObject remove(JSONObject command, HashMap<Integer, Resource> resourceList, KeyList keys) {

		JSONObject response = new JSONObject();

		if (!command.containsKey("resource")) {
			response.put("response", "error");
			response.put("errorMessage", "missing resource");
		} else if (!command.getJSONObject("resource").containsKey("uri")) {
			response.put("response", "error");
			response.put("errorMessage", "invalid resource");
		} else {
			String cmdUri = command.getJSONObject("resource").getString("uri");
			String cmdOwner = command.getJSONObject("resource").getString("owner");
			String cmdChannel = command.getJSONObject("resource").getString("channel");
			for (Resource src : resourceList.values()) {
				if (cmdChannel.equals(src.getChannel()) && cmdOwner.equals(src.getOwner()) && cmdUri.equals(src.getUri())) {
					while (resourceList.values().remove(src)) ;
					keys.remove(cmdChannel, cmdUri, cmdOwner);
					response.put("response", "success");
					break;
				}
			}
		}
		return response;
	}
	
	
	public static JSONObject fetch(JSONObject json, HashMap<Integer, Resource> resourceList){
		
		JSONObject response = new JSONObject();
		
		if (json.containsKey("resourceTemplate")) {
			
			JSONObject resTemp = (JSONObject) json.get("resourceTemplate");
			
			if (resTemp.containsKey("uri")){
				if (resTemp.containsKey("channal")){
					String uri = (String) resTemp.get("uri");
					String channal = (String) resTemp.get("channal");
					if ((uri.startsWith("http://")||uri.startsWith("files://")) && (!channal.equals(""))){
						
						File file = new File(uri);
						Boolean chk = false;
						int num = 0;
						for(int i = 0; i < resourceList.size(); i++) {
							Resource resource = resourceList.get(i);
							if (uri.equals(resource.getUri()) && channal.equals(resource.getChannel())){
								chk = true;
								num++;
								
								//fetch
								if (uri.startsWith("files://") && file.exists()){
									
									try{
										Socket s = null;
										String ServerInfo = (String) resource.getEzServer();
										int port = Integer.parseInt(ServerInfo.substring(ServerInfo.indexOf(":")));
										ServerSocket ss = new ServerSocket(port);
										while(true){
											s = ss.accept();
											DataInputStream dis = new DataInputStream(new BufferedInputStream(s.getInputStream()));
											dis.readByte();
											DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(uri)));
											DataOutputStream ps = new DataOutputStream(s.getOutputStream());
											ps.writeUTF(file.getName());
											ps.flush();
											ps.writeLong((long) file.length());
											ps.flush();
										
											int bufferSize = 8192;
											byte[] buf = new byte[bufferSize];
										
											while(true){
												int read = 0;
												if (fis != null){
													read = fis.read(buf);
												}
											
												if (read == -1){
													break;
												}
												ps.write(buf, 0, read);
											}
										
											ps.flush();
											fis.close();
											s.close();
										}
									
									}catch (Exception e){
									e.printStackTrace();
									}
								}
								//fetch over
								
							}
						}
						
						//response
						if (chk){
							if (file.exists()){
								resTemp.put("resourceSize", file.length());
							}
							response.put("response", "scuess");
							response.put("resource", resTemp);
				            response.put("resultSize", num);
						}else{
							response.put("response", "error");
				            response.put("errorMessage", "cannot fetch resource");
						}
												
												
					}else{
						response.put("response", "error");
			            response.put("errorMessage", "invalid resourceTemplate");	
					}
				}else{
					response.put("response", "error");
		            response.put("errorMessage", "invalid resourceTemplate");	
				}
			}else{
				response.put("response", "error");
	            response.put("errorMessage", "invalid resourceTemplate");	
			}
		}else{
			response.put("response", "error");
            response.put("errorMessage", "missing resourceTemplate");
		}
		
		return response;
	}
	
	
	/*
	//from PublishNShare.java
	//Delete when Combination
	private static Resource getResource(JSONObject obj){
		 
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
	*/
	
}
