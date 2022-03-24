import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ClientCall {

	public static boolean ck_ter= true;
	
	public static void main(String[] args) throws Exception {
		Scanner sc = new Scanner(System.in);
		Socket socket1 = new Socket(args[0],Integer.parseInt(args[1]));
		Socket socket2 = new Socket(args[0],Integer.parseInt(args[2]));
		DataInputStream d_in_st= new DataInputStream(socket1.getInputStream());
		DataOutputStream d_out_st=new DataOutputStream(socket1.getOutputStream());
		DataOutputStream d_out_st2=new DataOutputStream(socket2.getOutputStream());
		System.out.println("Connected to the server  "+ args[1] +" & "+ args[2]);
		String smsg, rmsg="", c, word="", rest;
		smsg = "";
		for(;;) 
		{
			smsg = sc.nextLine(); 
			
			String []cmdLine = smsg.split(" ");
			
			if(cmdLine.length == 1) 
			{
				word = cmdLine[0];
				rest = "";
				if(word.equals("pwd") || word.equals("ls") || word.equals("cd")) 
				{
					d_out_st.writeUTF(smsg);
					rmsg=d_in_st.readUTF(); 
					if(rmsg != null) 
					{
						System.out.println(rmsg);
					}  
					d_out_st.flush();
				} 
				else if(word.equals("quit")) 
				{
					d_out_st.writeUTF(smsg); 
					break;
				} 
				else 
				{
					System.out.println("Please enter a valid command");
				}
			} 
			else if(cmdLine.length == 2) 
			{
				word = cmdLine[0];
				rest = cmdLine[1];
				if(word.equals("cd") || word.equals("delete") || word.equals("mkdr")) 
				{
					d_out_st.writeUTF(smsg); 
					if((rmsg = d_in_st.readUTF()) != null) 
					{
						System.out.println(rmsg); 
					}  
					d_out_st.flush();
				} 
				else if(word.equals("get")) 
				{
					d_out_st.writeUTF(smsg);
					String s=d_in_st.readUTF();
					
					if(s.equals("true")) 
					{ 
						int bytesRead; 
						int current = 0;
						
						String fileName = d_in_st.readUTF();
						String currentdir_temp=System.getProperty("user.dir");  
						String mkdr_temp=currentdir_temp+"/"+fileName;
						File statText = new File(mkdr_temp);    
						OutputStream output = new FileOutputStream(statText);     
						long size = d_in_st.readLong();     
						byte[] buffer = new byte[1024];     
						
						while (size > 0 && (bytesRead = d_in_st.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) 
						{     
							output.write(buffer, 0, bytesRead);     
							size -= bytesRead;     
						}
					
						output.flush();
						output.close();
						d_out_st.flush();

						System.out.println("File Transfer complete");
						d_out_st.writeUTF("Transfer Completed");
					} 
					else 
					{
						System.out.println("File not Found");
					}
					 
					d_out_st.flush();
				} 
				else if(word.equals("put")) 
				{
					d_out_st.writeUTF(smsg);
					String currentdir_temp=System.getProperty("user.dir");
					File myFile = new File(currentdir_temp+"/"+rest);
					
					byte[] mybytearray = new byte[(int) myFile.length()];
					try
					{
						if(myFile.exists()) {
							d_out_st.writeUTF("true");
							FileInputStream fis = new FileInputStream(myFile);  
							
							byte[] buffer = new byte[4096];
							d_out_st.writeUTF(myFile.getName());     
							d_out_st.writeLong(mybytearray.length);     
							d_out_st.flush();  
							int read;
							while ((read=fis.read(buffer)) > 0) 
							{
								d_out_st.write(buffer,0,read);
							}
							
							d_out_st.flush();
							
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

					d_out_st.flush();
					currentdir_temp=null;
				}
				else if(word.equals("terminate"))
				{
					d_out_st2.writeUTF(smsg);
					System.out.println("Terminating");
					d_out_st2.flush();
				}
				else 
				{
					System.out.println("Please enter a valid command");
				}
			
			} 
			else if(cmdLine.length == 3) 
			{
				word = cmdLine[0];
				rest = cmdLine[1];
				String keyword = cmdLine[2];
				smsg = word + " " + rest + " " + keyword;
				if(keyword.equals("&")) 
				{
					ck_ter=true;
					Thread t = new MultiClientThread(word, smsg,rest,rmsg, d_in_st, d_out_st );
					t.start();
				}
			} 
			else 
			{
				System.out.println("Please enter valid command");
			}
		}
		socket1.close();
		socket2.close();
	}
}