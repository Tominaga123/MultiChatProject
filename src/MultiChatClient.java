import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;


class MyFrame  extends JFrame implements ActionListener, Runnable, WindowListener{
	
	String nickname = "C48の誰かさん";
	JTextField textField = new JTextField(20);
	JTextField nickTextField = new JTextField(nickname, 20);
	JButton connectButton = new JButton("接続");
	JButton acquireButton = new JButton("サーバ情報取得");
	JLabel IPLabel = new JLabel("IPアドレス");
	JLabel PortLabel = new JLabel("ポート番号");
	JTextField IPTextField = new JTextField("127.0.0.1", 10);
	JTextField PortTextField = new JTextField("5000", 10);
	JButton nickButton = new JButton("ニックネーム設定");
	JTextArea textArea = new JTextArea(30, 20);
	JTextArea entryTextArea = new JTextArea(5, 20);
	JButton button = new JButton("送信する");
	JPanel panel = new JPanel();
	JPanel panel2 = new JPanel();
	JPanel panel3 = new JPanel();
	JPanel panel4 = new JPanel();
	
	PrintWriter writer;
	BufferedReader reader;
	String message;
	Socket socket = null;
	String IP; //IPアドレスを格納する
	int port; //ポート番号を格納する
	JFileChooser chooser = new JFileChooser(); //他のファイルからサーバ情報（IPアドレスとポート番号）を取得するために使用
	File fileChoose; //同上
	FileReader fr; //同上
	File file; //同上
	
	MyFrame(){
		setTitle("チャット欄");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		JScrollPane scrollPane = new JScrollPane(textArea);
		JScrollPane scrollPane2 = new JScrollPane(entryTextArea);
		//テキストを表示するエリアをコンテナに追加
		getContentPane().add(scrollPane);
		//テキストの入力フォームを送信ボタン、クリアボタンと合わせてパネルに配置しコンテナに追加
		panel.setLayout(new FlowLayout());
		panel.add(button);
		panel.add(scrollPane2);
		getContentPane().add(panel);
		//ニックネームの入力フォームと決定ボタンをパネル2に配置しコンテナに追加
		panel2.setLayout(new FlowLayout());
		panel2.add(nickButton);
		panel2.add(nickTextField);
		getContentPane().add(panel2);
		//接続/接続解除ボタンをパネルに配置しコンテナに追加
		panel3.setLayout(new FlowLayout());
		panel3.add(acquireButton);
		panel3.add(connectButton);
		getContentPane().add(panel3);
		//IPアドレス/ポート番号ボタンをパネルに配置しコンテナに追加
		panel4.setLayout(new FlowLayout());
		panel4.add(IPLabel);
		panel4.add(IPTextField);
		panel4.add(PortLabel);
		panel4.add(PortTextField);
		getContentPane().add(panel4);
		//イベント設定
		textArea.setEditable(false);
		button.setEnabled(false);
		connectButton.setEnabled(false);
		acquireButton.setEnabled(false);
		IPTextField.setEnabled(false);
		PortTextField.setEnabled(false);
		button.addActionListener(this);
		connectButton.addActionListener(this);
		acquireButton.addActionListener(this);
		nickButton.addActionListener(this);
		IPTextField.addActionListener(this);
		PortTextField.addActionListener(this);
		this.addWindowListener(this);
		setSize(700,700);
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == button) {
			String  chat = entryTextArea.getText();
			writer.println(nickname + " : " + chat);
			System.out.println("サーバへ[" + nickname + " : " + chat + "]" + "のチャットを送りました。");
			entryTextArea.setText("");
			writer.flush();
		} else if(e.getSource() == nickButton) {
			if(nickButton.getText() == "ニックネーム設定"){
				nickname = nickTextField.getText();
				nickButton.setText("ニックネーム変更");
				connectButton.setEnabled(true);
				acquireButton.setEnabled(true);
				IPTextField.setEnabled(true);
				PortTextField.setEnabled(true);
				textArea.append("ニックネームを [" + nickname + "] に設定しました" + "\r\n");
				textArea.setCaretPosition(textArea.getText().length());
				nickButton.setEnabled(false);
			} else if(nickButton.getText() == "ニックネーム変更") {
				String preNickname = nickname;
				nickname = nickTextField.getText();
				writer.println("[" + preNickname + "] さんのニックネームが [" + nickname + "] に変更されました");
				writer.flush();
			}
		} else if(e.getSource() == connectButton) {
			if(connectButton.getText() == "接続"){
				connectButton.setText("接続解除");
				button.setEnabled(true);
				nickButton.setEnabled(true);
				acquireButton.setEnabled(false);
				IPTextField.setEnabled(false);
				PortTextField.setEnabled(false);
				IP = IPTextField.getText();
				String p = PortTextField.getText();
				port = Integer.parseInt(p);

				connect();
			} else if(connectButton.getText() == "接続解除") {
				writer.println("[" + nickname + "] さんが退室しました");
				writer.flush();
				connectButton.setText("接続");
				button.setEnabled(false);
				nickButton.setEnabled(false);
				acquireButton.setEnabled(true);
				IPTextField.setEnabled(true);
				PortTextField.setEnabled(true);
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				socket = null;
			}
		}else if(e.getSource() == acquireButton) {
			//他のファイルからサーバ情報（IPアドレスとポート番号）を取得する
		    FileNameExtensionFilter filter = new FileNameExtensionFilter("テキストファイル(*.txt)", "txt");
		    chooser.setFileFilter(filter);
			if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { //新ウィンドウを開き、「開く」ボタンが押された場合
				fileChoose = chooser.getSelectedFile();
				if(fileChoose != null) { 
					try {
						 file = new File(fileChoose.getPath());
						 fr = new FileReader(file);
						 reader = new BufferedReader(fr);
						 IPTextField.setText(reader.readLine());
						 PortTextField.setText(reader.readLine());
						 reader.close();
					 }catch(IOException e1) {
						 e1.printStackTrace();
					 }
				}else {
					System.out.println("ファイルが選択されていません"); 
					
				}
			}
		}
	}
	
    public void windowClosing(WindowEvent e){
    	if(connectButton.getText() == "接続解除") {
    		System.out.println("退室します");
	    	writer.println("[" + nickname + "] さんが退室しました");
			writer.flush();
    	}
    	System.exit(EXIT_ON_CLOSE);
     }
	@Override
	public void windowOpened(WindowEvent e) {
		textArea.append("[" + nickname + "]" + " さん、チャットへようこそ" + "\r\n" + 
				"チャットに参加するには、[ニックネーム設定ボタン]→[接続]を押します" + "\r\n" + 
				"※複数行まとめて送れますが、他の人のメッセージと混線する可能性大なので１行ずつ送ることを推奨します※" + "\r\n" + 
				"==========================================================================================" + "\r\n");
		textArea.setCaretPosition(textArea.getText().length());
	}

	public void windowClosed(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	
	public void connect() {
		try {
		socket = new Socket(IP, port);
		writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writer.println("[" + nickname + "] さんがチャットに参加しました");
		writer.flush();
		System.out.println("ここまで来た");
		} catch(Exception e) {
			System.out.println(e);
			System.out.println("connectメソッドでエラー");
			textArea.append("接続できませんでした。" + "\r\n");
			textArea.setCaretPosition(textArea.getText().length());
		}
	}
	
	public void run() {
		System.out.println("スレッドを起動");
		while(true) {
			while(socket == null || !socket.isConnected()) {
				System.out.println("未接続");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("抜けました");
			try {
				while(socket != null && socket.isConnected()) {
					System.out.println("繋がってる");
					message = reader.readLine();
					System.out.println("メッセを受け取り");
					textArea.append( message + "\r\n");
					textArea.setCaretPosition(textArea.getText().length());
					System.out.println("サーバから[" + message + "]" + "のチャットを受け取りました。");
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
//			try {
//				socket.close();
//				socket= null;
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			System.out.println(socket != null);
		}
	}


}
public class MultiChatClient {
	public static void main(String[] args) {
		MyFrame mcc = new MyFrame();
		Thread thread = new Thread(mcc);
		thread.start();
	}
}