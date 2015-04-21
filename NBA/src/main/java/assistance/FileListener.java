package assistance;

import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import bussinesslogic.match.MatchLogic;
import bussinesslogic.player.PlayerLogic;

public class FileListener {
	PlayerLogic p = new PlayerLogic();
	MatchLogic m = new MatchLogic();
	public void Listen(String path){
		 try{
			 int i = 0;
		        WatchService watchService=FileSystems.getDefault().newWatchService();  
		        Paths.get(path).register(watchService,   
		                StandardWatchEventKinds.ENTRY_CREATE,  
		                StandardWatchEventKinds.ENTRY_DELETE,  
		                StandardWatchEventKinds.ENTRY_MODIFY);  
		        while(true)  
		        {  
		            WatchKey key=watchService.take();  
		            for(WatchEvent<?> event:key.pollEvents())  
		            {  
		            	
		            	if(event.kind().toString().equals("ENTRY_CREATE")){
		            		System.out.println(i);
		            		i++;
		            		String temp = "./迭代一数据/matches/"+event.context().toString();
		            		System.out.println("start "+MatchLogic.getTime());
		            		p.initialize("./迭代一数据/players/info", "12-13");
		            		System.out.println("play  "+MatchLogic.getTime());
		            		m.update(temp);
		            		System.out.println("end   "+MatchLogic.getTime());
		            		
		            		//PlayerDataPO res = p.getInfo("Aaron Brooks", "12-13");
		            		//System.out.println(res.getName()+";"+res.getFieldGoalPercentage()+res.getGP());
		            	}
		                 
		            }  
		            if(!key.reset())  
		            {  
		                break;  
		            }  
		        }
		          }
		          catch(Exception e){
		        	  e.printStackTrace();
		          }
		} 
	   
	
	public static void main(String[] args) {
		FileListener l = new FileListener();
		l.Listen("迭代一数据/matches");
	}
}

