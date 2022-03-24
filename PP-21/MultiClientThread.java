import java.io.*; 
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.net.*; 
import java.text.*; 
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.NoSuchFileException;
import java.nio.file.DirectoryNotEmptyException;

public class MultiClientThread extends Thread
{
	public String cmdline,smsg,rest,rmsg;
	public DataInputStream d_in_st;
	public DataOutputStream d_out_st;

	
	
	public MultiClientThread(String cmdline,String smsg, String rest,String rmsg, DataInputStream d_in_st, DataOutputStream d_out_st) {
		this.cmdline = cmdline;
		this.smsg = smsg;
		this.rmsg = rmsg;
		this.rest=rest;
		this.d_in_st=d_in_st;
		this.d_out_st=d_out_st;
	}
	
	public void run() {
		
		if(this.cmdline.equals("get")) 
		{
			System.out.println("Running function of the thread" + this.cmdline + "-->" + smsg);
			try 
			{
				d_out_st.writeUTF(smsg);
				String s = d_in_st.readUTF();
				if(s.equals("true")) 
				{ 
					String current_temp_commandID= d_in_st.readUTF();
					System.out.println(current_temp_commandID);
					int bytesRead;  
					int current = 0;	
					String fileName = d_in_st.readUTF();
					String currentdir_temp=System.getProperty("user.dir");  
					String mkdr_temp=currentdir_temp+"/"+fileName;
					File statText = new File(mkdr_temp);    
					OutputStream output = new FileOutputStream(statText);     
					long size = d_in_st.readLong();     
					byte[] buffer = new byte[1000];     
					boolean socket2=false;
					while (size > 0 && ((socket2=d_in_st.readBoolean())!=false) && (bytesRead = d_in_st.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) 
					{ 
					    output.write(buffer, 0, bytesRead);     
						size -= bytesRead; 
						d_out_st.flush();
						TimeUnit.MILLISECONDS.sleep(6);    
					  }
					  if(socket2==false)
					  {
						  System.out.println("File is being deleted ");
						  Files.deleteIfExists(Paths.get(mkdr_temp)); 
					  }
					
					output.flush();
					output.close();  
					d_out_st.flush();
					System.out.println("Command is Complete");
					d_out_st.writeUTF("Command is Complete");
				}
				else
				{
				    System.out.println("File not Found");
				}          
				d_out_st.flush();
			}
			catch(Exception e) 
			{
				System.out.println(e);
			}
		}
		else if(this.cmdline.equals("put")) 
		{
			System.out.println("Running the thread function: " + this.cmdline + "-->" + smsg);
			String currentdir_temp=System.getProperty("user.dir");
			try
			{
				System.out.println("Send message is : " +smsg);
				d_out_st.writeUTF(smsg);
				File myFile = new File(currentdir_temp+"/"+rest);
				byte[] mybytearray = new byte[(int) myFile.length()];
			
				if(myFile.exists()) 
				{
					d_out_st.writeUTF("true");
					String command_id_string=d_in_st.readUTF();
					System.out.println(command_id_string);
					FileInputStream fis = new FileInputStream(myFile);  
					d_out_st.writeUTF((myFile.getName()));
					d_out_st.writeLong(mybytearray.length);
					d_out_st.flush();  
					int read;
					boolean temp2;
					byte[] buffer = new byte[1000];
					while ((read=fis.read(buffer)) > 0) 
					{	
						d_out_st.write(buffer,0,read);
						TimeUnit.MILLISECONDS.sleep(6);
						temp2=d_in_st.readBoolean();
						
						if(!temp2)
						{	System.out.println("Deleting the files in Server");
							TimeUnit.MILLISECONDS.sleep(10);
							break;
						}
					}
					d_out_st.flush();
					fis.close();
					if((rmsg = d_in_st.readUTF()) != null) 
					{   
						System.out.println(rmsg); 
					}
				} 
				else 
				{
					d_out_st.writeUTF("false");
					System.out.println("File not Found");
				}
			} 
			catch(Exception e) 
			{
				System.out.println(e);
			}
			try 
			{
				d_out_st.flush();
				currentdir_temp=null;
			}
			catch(Exception e) 
			{
				System.out.println(e);
			}
			
		}
	}
}