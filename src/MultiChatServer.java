import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

class ServerFrame extends JFrame implements ActionListener{

	JLabel label1 = new JLabel("<html>ポート番号を入力し、作成ボタンを押してください。<br>クライアントに渡すサーバ情報ファイルを作成します<html>");
	JLabel label2 = new JLabel("ポート番号");
	static JTextField textField = new JTextField("5000", 10);
	JButton button = new JButton("ファイル作成");
	JButton OnOffButton = new JButton("サーバ起動");
	JPanel panel1 = new JPanel();
	JPanel panel2 = new JPanel();
	JPanel panel3 = new JPanel();
	//IPアドレス、ポート番号の出力に使用
	File file; 
	FileWriter fw; 
	PrintWriter pw;
	BufferedWriter bw;
	String path;
	InetAddress addr;
	//flagが0の間（ファイル作成ボタンが押されるまで）ソケット生成しない
	static int flag = 0;
	
	ServerFrame(){
		setTitle("ファイル作成");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		//ラベル1をパネルに配置しコンテナに追加
		panel1.setLayout(new FlowLayout());
		panel1.add(label1);
		getContentPane().add(panel1);
		//ラベル2とテキストフィールドをパネルに配置しコンテナに追加
		panel2.setLayout(new FlowLayout());
		panel2.add(label2);
		panel2.add(textField);
		getContentPane().add(panel2);
		//ボタンをパネルに配置しコンテナに追加
		panel3.setLayout(new FlowLayout());
		panel3.add(button);
		panel3.add(OnOffButton);
		getContentPane().add(panel3);
		//イベント設定等
		button.addActionListener(this);
		OnOffButton.addActionListener(this);
		OnOffButton.setEnabled(false);
		//フレーム表示
		setSize(500,200);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == button) {
			try {
				addr = InetAddress.getLocalHost();
				file = new File("サーバ情報.txt");
				path = file.getAbsolutePath();
				file = new File(path);
				fw = new FileWriter(file);
				bw = new BufferedWriter(fw);
				bw.write(addr.getHostAddress());
				bw.newLine();
				bw.write(textField.getText());
				bw.flush();
				bw.close();
				System.out.println("サーバ情報ファイルに" + addr.getHostAddress() + "と" + textField.getText() + "を書き込みました");
				OnOffButton.setEnabled(true);
				
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}else if(e.getSource() == OnOffButton) {
			if(OnOffButton.getText() == "サーバ起動") {
				flag = 1;
				System.out.println("サーバを起動しました");
				OnOffButton.setText("サーバ終了");
			} else if(OnOffButton.getText() == "サーバ終了") {
				flag = 2;
				SubChatServer.talk("サーバが終了しました");
			}
		}
	}
	
}

class SubChatServer extends Thread{
	Socket socket;
	static Vector sub;
	
	SubChatServer(Socket socket) {
		super();
		this.socket = socket;
		if(sub == null) {
			sub = new Vector();
		}
		sub.add(this);
	}

	public void run() {
		try {
			PrintWriter writer = new PrintWriter(socket.getOutputStream());
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			while(true) {
				try {
					String chat = reader.readLine();
					if(chat == null) {
						socket.close();
						sub.remove(this);
						return;
					}
					System.out.println("読み取ったのは" + chat);
					talk(chat);
				} catch(IOException e) {
					e.printStackTrace();
					socket.close();
					sub.remove(this);
					return;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void talk(String message) {
		int i = 1;
		for (i = 0; i < sub.size(); i++) {
			SubChatServer t = (SubChatServer)sub.get(i);
		    if (t.isAlive()) {
		    	t.talkone(message); 
		    }
		}
		if(ServerFrame.flag == 2 && i == sub.size()) {
			System.out.println("サーバを終了します");
			System.exit(0);
		}
		
	}
	public void talkone(String message){
		try {
		    PrintWriter writer = new PrintWriter(socket.getOutputStream());
			writer.println(message);
		    writer.flush();
		    System.out.println(message + "を送りました");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

public class MultiChatServer{
	public static void main(String[] args) {
		new ServerFrame();
		while(ServerFrame.flag == 0) {
			System.out.println("サーバ情報ファイル作成待ち");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//サーバ情報ファイルが作成されたらポート番号を取得してクライアントからの接続を待つ
		try {
			int port = Integer.parseInt(ServerFrame.textField.getText());
			ServerSocket severSocket = new ServerSocket(port);
			Socket socket = null;
			while(true) {
				socket = severSocket.accept();
				SubChatServer t = new SubChatServer(socket);
				t.start();
			}	
		} catch(IOException e){
			System.out.println(e);
			e.printStackTrace();
		}
	}

}