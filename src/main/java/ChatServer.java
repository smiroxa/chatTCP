import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.Timer;

public class ChatServer 
{
	ArrayList<NetClient> clients = new ArrayList<>();
	
	public ChatServer() throws IOException
	{
		ServerSocket ss = new ServerSocket(7777);
		Timer tm = new Timer(50, new ActionRead() );
		tm.start();
		System.out.println("[Trace] Server stated. Waiting for a client...");
		while(true)
		{
			Socket cs = ss.accept();
			System.out.println("[Trace] Client connected");
			clients.add( new NetClient(cs) );
		}
	}

	class ActionRead implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			try 
			{
				for (NetClient nc : clients) 
				{
					if( nc.in.available() > 0 )
					{
						String str = nc.in.readUTF();
						String cmd = str.substring(0, str.indexOf(":"));
						String inf = str.substring(str.indexOf(":") + 1);
						String msg = "";
						switch (cmd)
						{
							case "login":
								msg = "join new user - " + inf;
								nc.login = inf;
								break;
							case "msg":
								msg = "message from "+ nc.login + " => " + inf;
								break;
							case "exit":
								System.out.println(cmd);
								msg = "user " + nc.login + " disconnected ";
								clients.remove(nc);
								break;
						}
						System.out.println("[Trace] message => " + str);
						for (NetClient nn : clients)
						{
							if(nc != nn)
							{
								nn.out.writeUTF(msg);
							}
						}
					}
				}
			}
			catch (IOException e1) 
			{
				e1.printStackTrace();
			}
		}
	}

	class  NetClient
	{
		Socket cs = null;
		DataOutputStream out = null;
		DataInputStream  in  = null;
		String login  = null;
		public NetClient(Socket cs) throws IOException
		{
			this.cs = cs;
			out = new DataOutputStream(cs.getOutputStream());
			in  = new DataInputStream(cs.getInputStream());
		}
	}
}
