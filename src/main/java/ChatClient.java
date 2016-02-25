import org.apache.log4j.Logger;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient extends JFrame implements ActionListener
{
	JPanel panel;
	JTextField NewMsg;
	JTextArea ChatHistory;
	JButton Send;
	Socket socket;

	DataOutputStream out = null;
	DataInputStream  in  = null;

	final static Logger logger = Logger.getLogger(ChatServer.class);

	String name = "Piker";

	public ChatClient() throws IOException
	{
		ChatHistory = new JTextArea();
		NewMsg      = new JTextField();
		panel       = new JPanel();
		Send        = new JButton("Send");

		this.setSize(500, 500);
		this.setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		panel.setLayout(null);
		this.add(panel);
		ChatHistory.setBounds(20, 20, 450, 360);
		panel.add(ChatHistory);
		NewMsg.setBounds(20, 400, 340, 30);
		panel.add(NewMsg);
		Send.setBounds(375, 400, 95, 30);
		panel.add(Send);

		Send.addActionListener(this);
		ChatHistory.setText("Connected to server\n");
		this.setTitle("chat");

		socket = new Socket("localhost", 7777);
		logger.debug("Connected to chat SERVER");

		out = new DataOutputStream(socket.getOutputStream());
		in  = new DataInputStream(socket.getInputStream());

		Scanner sc = new Scanner(System.in);
		out.writeUTF("LOGIN: " + name);
		Thread thread = new Thread(new ReadInputMsg());
		thread.start();

		while(true)
		{
			String str = sc.nextLine();
			if( str.equals("exit") )
			{
				out.writeUTF("EXIT:" + name);
				System.exit(0);
			}
			out.writeUTF("MSG:" + str);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if ((e.getSource() == Send) && (NewMsg.getText() != ""))
		{
			ChatHistory.setText( ChatHistory.getText() + '\n' + "Me:" + NewMsg.getText() );
			try
			{
				DataOutputStream dataOutputStream = new DataOutputStream( socket.getOutputStream() );
				dataOutputStream.writeUTF("MSG:" + NewMsg.getText());

			} catch (Exception e1)
			{
				ChatHistory.setText(ChatHistory.getText() + 'n' + "Message sending fail: Network Error");
				try
				{
					Thread.sleep(1000);
					System.exit(0);
				} catch (InterruptedException e2)
				{
					e2.printStackTrace();
				}
			}
			NewMsg.setText("");
		}
	}


	class ReadInputMsg implements Runnable
	{
		@Override
		public void run()
		{
			while(true)
			{
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try
				{
					if (in.available() > 0 )
					{
						String str = in.readUTF();
						ChatHistory.setText(ChatHistory.getText() + '\n' + "From server:" + str);
						logger.debug(str);
					}
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
		}
	}
}
