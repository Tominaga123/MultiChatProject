import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;


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
					System.out.println(e);
					socket.close();
					sub.remove(this);
					return;
				}
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	public void talk(String message) {
		for (int i = 0; i < sub.size(); i++) {
			SubChatServer t = (SubChatServer)sub.get(i);
		    if (t.isAlive()) {
			t.talkone(this, message); 
		    }
		}
		
	}
	public void talkone(SubChatServer talker, String message){
		try {
		    PrintWriter writer = new PrintWriter(socket.getOutputStream());
			writer.println(message);
		    writer.flush();
		    System.out.println(message + "を送りました");
		} catch (IOException e) {
		    System.err.println(e);
		}
	}
}

public class MultiChatServer {
	public static void main(String[] args) {
		try {
			ServerSocket severSocket = new ServerSocket(5000);
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