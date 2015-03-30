package team.xinyuan519.wxsearch.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {
	private final String END_MARK = "#end";
	private final String SEPERATOR = "&AND&";
	private String msg;
	private String ip;
	private int port;
	private String charset;

	public Client(String msg, String ip, int port, String charset) {
		super();
		this.msg = msg;
		this.ip = ip;
		this.port = port;
		this.charset = charset;
	}

	public String sendCustomizedMsg(String msg, String ip, int port,
			String charset) {
		Socket socket = null;
		BufferedReader inputReader = null;
		PrintWriter printWriter = null;
		String response = "";
		try {
			socket = new Socket(ip, port);
			inputReader = new BufferedReader(new InputStreamReader(
					socket.getInputStream(), charset));
			printWriter = new PrintWriter(socket.getOutputStream());

			JustInTimeCrawler jitc = new JustInTimeCrawler();
			String accountInfo = jitc.getJsonInfo(msg);
			if (accountInfo == null) {
				throw new Exception("Got no extra account info");
			}
			printWriter.print(msg + SEPERATOR + accountInfo + END_MARK);
			printWriter.flush();

			System.out.println("Client send msg");

			int length = -1;
			boolean READ_OVER = false;
			while (true) {
				char[] buffer = new char[1024];
				length = inputReader.read(buffer);

				if (length == -1) {
					READ_OVER = false;
					break;
				}
				response += new String(buffer);
				if (response.contains(END_MARK)) {
					READ_OVER = true;
					break;
				}
			}
			if (!READ_OVER) {
				response = null;
				throw new Exception(String.format("Socket to %s:%dinterrupt",
						ip, port));
			}
			System.out.println(response);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		} finally {
			try {
				if (printWriter != null) {
					printWriter.close();
				}
				if (inputReader != null) {
					inputReader.close();
				}
				if (socket != null) {
					socket.close();
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
		return response;
	}

	public String sendMsg() {
		return sendCustomizedMsg(msg, ip, port, charset);
	}

	@Override
	public void run() {
		// JSONObject jsonObject = new JSONObject();
		// JSONArray jsonArray = new JSONArray();
		// try {
		// jsonObject.append("Title", "title").append("Content", "content")
		// .append("Date", "date").append("Time", "time");
		// jsonArray.put(jsonObject);
		// jsonArray.put(jsonObject);
		// } catch (JSONException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
		//
		// for (int i = 0; i < 5; i++) {
		// sendCustomizedMsg(jsonArray.toString() + "#end", "localhost", 4399,
		// "utf-8");
		// try {
		// Thread.sleep(3 * 1000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
	}

	public static void main(String[] args) {
		Client c = new Client("Hello", "192.168.1.106", 4399, "utf-8");
		Thread t = new Thread(c);
		c.sendMsg();
	}
}
