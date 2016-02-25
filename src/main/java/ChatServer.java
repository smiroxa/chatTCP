import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.log4j.Logger;

public class ChatServer 
{
	final static Logger logger = Logger.getLogger(ChatServer.class);
	ArrayList<NetClient> clients       = new ArrayList<>();
	PriorityBlockingQueue<String> messagesQueue = new PriorityBlockingQueue<>();

	public ChatServer() throws IOException
	{
		ServerSocket serverSocket = new ServerSocket(7777);
		Thread sender = new Thread( new Sender() );
		sender.start();
		logger.debug("Server stated. Waiting for a client..");
		while(true)
		{
			Socket socket = serverSocket.accept();
			logger.debug("New client connected..");
			NetClient netClient = new NetClient(socket);
			clients.add(netClient);
			Thread reader = new Thread( new Reader(netClient) );
			reader.start();
		}
	}

	class Reader implements Runnable
	{
		private NetClient netClient;

		public Reader(NetClient netClient)
		{
			this.netClient = netClient;
		}

		@Override
		public void run()
		{
			String str = "";
			while(true)
			{
				try {
					if (this.netClient.in.available() > 0)
                    {
                        try
						{
                            str = this.netClient.in.readUTF();
                        } catch (IOException e)
						{
                            e.printStackTrace();
                        }
                        logger.debug("read msg from client: " + this.netClient.login + " message: " + str);
                        String cmd = str.substring(0, str.indexOf(":"));
                        String inf = str.substring(str.indexOf(":") + 1);
                        String msg;
                        switch (cmd) {
                            case "LOGIN":
                                msg = "Join new user - " + inf;
                                this.netClient.login = inf;
								messagesQueue.offer(msg);
                                break;
                            case "MSG":
                                msg = "message from" + this.netClient.login + " => " + inf;
								messagesQueue.offer(msg);
                                break;
                            case "EXIT":
                                msg = "user " + this.netClient.login + " disconnected ";
                                clients.remove(this.netClient);
								messagesQueue.offer(msg);
								logger.debug("disconnect user, stopping process read..");
								System.exit(0);
								break;
                        }
                    }
				} catch (IOException e) {
					e.printStackTrace();
				}
				try
				{
					Thread.sleep(200);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	class Sender implements Runnable
	{
		@Override
		public void run()
		{
			logger.debug("Start process SENDER. Wait new clients message for..");
			while (true)
			{
				if ( !messagesQueue.isEmpty() )
				{
					String message = messagesQueue.poll();
					for (NetClient netClient : clients)
					{
						try
						{
							netClient.out.writeUTF(message);
							logger.debug("Send message " + message + " to user: " + netClient.login);
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
				}
				try
				{
					Thread.sleep(50);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
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
