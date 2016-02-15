import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.Timer;

public class ChatClient 
{
	DataOutputStream out = null;
	DataInputStream  in  = null;
	String name = "Piker";

	public ChatClient() throws IOException
	{
		Socket cs = new Socket("localhost",7777);
		out = new DataOutputStream(cs.getOutputStream());
		in  = new DataInputStream(cs.getInputStream());
		Scanner sc = new Scanner(System.in);
		out.writeUTF("login: " + name);
		Timer tm = new Timer(50, new ActionRead() );
		tm.start();
		while(true)
		{
			String str = sc.nextLine();
			if(str.equals("exit"))
			{
				out.writeUTF("exit:");
				System.exit(0);
			}
			out.writeUTF("msg:" + str);

		}
		
	}
	class ActionRead implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			try 
			{
				if(in.available() > 0 )
				{
					String str = in.readUTF();
					System.out.println(str);
				}
			}
			catch (IOException e1) 
			{
				e1.printStackTrace();
			}
		}
	}
}
