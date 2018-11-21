
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Date;
//import javax.json.JsonObject; not useful
import org.json.*;
import org.omg.Messaging.SyncScopeHelper;

public class Lab2_MyServer{

	// Task#1 Variable Setting

	// 1-?) got from Client
	protected String client_method;
	protected String user_agent;
	protected String[] key_value;
	// protected String httpVersion;
	protected Socket socket; // a connection btw Client&Server to be made to ServerSocket
	// 1-a) Socket Based Var
	private ServerSocket serverSocket;
	private boolean isUrgentClose = false;
	private int timeout_timer = 60; // average timer for maintaining a connection
	private int max_timer = timeout_timer * 2; // after max_timer, the connection end
	private Date sys_creat_date;

	// 1-b) JsonObject Based Var
	protected JSONObject js_obj;
	protected JSONObject args; // http://127.0.0.1/test.txt?assignment=1 "args": {"assignment":"1"},
	protected JSONObject headers; //
	protected String origin;
	protected String url;
	protected URL url2;

	// 1-c) Server "httpfs" Based Var
	protected String debug_msg;
	protected int port;
	protected String file_directory = ""; // file directory
	protected String file_content = ""; // files content

	protected boolean optionV;
	protected boolean optionP;
	protected boolean optionD;

	// 1-d) Verbose related Var
	protected String[] sCode = { "200 OK", "404 Not Found", "400 Bad Request" };
	protected int content_length = 0;

	// Task#2 Constructor

	// 2-a) default constructor of myServer
	public Lab2_MyServer() {

		optionV = false;
		optionP = false;
		optionD = false;

		js_obj = new JSONObject();
		args = new JSONObject();
		headers = new JSONObject();
		headers.put("Connection", "close");
		headers.put("Host", origin);
		headers.put("User-Agent","Concordia-HTTP/1.0");
		headers.put("(File) Length", file_content.length());
		headers.put("Content-Type", "json");
		this.port = 8080;
		file_directory = System.getProperty("user.dir"); // directory where server will operate, user.dir = current
															// workspace directory

		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			debug_msg += "#301. Incorrect port number format. Fail to establish ServerSocket.\n\n";
			if(optionV)
				System.out.println(debug_msg);
		}
		// System.out.println("Server is listening on default port " + port);
		// set timeout?
	}

	// 2-b) constructor of myServer with port
	public Lab2_MyServer(int port) throws IOException {

		optionV = false;
		optionP = false;
		optionD = false;

		js_obj = new JSONObject();
		args = new JSONObject();
		headers = new JSONObject();

		this.port = port;
		file_directory = System.getProperty("user.dir");

		serverSocket = new ServerSocket(port);
	}

	// Task#3 "Functioning" Method

	// 3-a == Task#4) an always listening Method, potentially a "multi-thread"
	// Method.
	// @@ for multi-thread tasks:
	// A: Server should be responsible to Client
	// B: prevent client hogging to long
	// after coding the whole chunck moved to a new class called ServerThread.
	public void executeServer() {

		if (serverSocket == null) {
			debug_msg += "#315. ServerSocket have not been establish. Fail to continue.\n\n";
//			System.out.println("Inner Error. Need to establish a Server first.");
			return;
		} else {
			// while(serverSocket!=null) {
			debug_msg += "Server is running. Waiting for any connection...\n\n";
			if(optionV)
				System.out.println("Waiting for client...\n");

			// 4-a) detect if any Client is trying to connect -- ServerSocket.accept()
			// method.
			// $$$$$$ multi-threaded server: CREAT A NEW THREAD TO HANDLE CLIENT SOCKET...
			// $$$$$$
			try {
				socket = serverSocket.accept();
				socket.setSoTimeout(timeout_timer * 1000);
				setOrigin(socket);
				if(optionV) {
					debug_msg += "Connection established with client: " + socket.getLocalSocketAddress() + "\n\n";
					System.out.println("Connected with a client: " + socket.getLocalSocketAddress() + "\n");
				}
			} catch (SocketTimeoutException se) {
				// time out setup is using: socket.setTimeOut(n*1000ms)
				debug_msg += "#303. ServerSocket has timed out.\n\n";
				if(optionV) {
					System.out.println(debug_msg);
					System.out.println("Connection Time out...");
				}
			} catch (IOException e) {
				debug_msg += "#304. An I/O error occurs when waiting for a connection.\n\n";
				if(optionV)
					System.out.println(debug_msg);
			}

			// 4-b) ready to get Client input... //InputStreamReader isReader = new
			// InputStreamReader(socket.getInputStream()); //int character =
			// isReader.read(); ##this or below
			// Client input is expected to be within 50 lines...
			// use while-loop to identify each line Client passes...

			try {
				InputStream input = socket.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(input));

				String line;
				String[] sentences = new String[50];
				String[][] words_matrix = new String[50][];
				int sentencePtr = 0;

				// at the meantime, setup Server Reply Info based on scenario...
				OutputStream output = socket.getOutputStream();
				PrintWriter writer = new PrintWriter(output, true); // ## "true": auto-flush the writer

				// save Client 1st - last input in sentences[][].
				/*
				 * eg. 
				 * [0][get/post+.* URL httpVersion]
				 * [1][host: HOST]
				 * [2][User-Agent: String]
				 * [3][key-value: ??]
				 */
				while (reader.ready() && sentencePtr < words_matrix.length) {
					line = reader.readLine();
					String[] words = line.split(" ");
					sentences[sentencePtr] = line;
					words_matrix[sentencePtr] = words;
					sentencePtr++;
				}

				// 4-c) analyze each [line of words] = [sentence] from Client
				// Client input is splitted in [words][0-i], now deal with each words...]

				// 1st line
//				System.out.println("Server is analyzing 1st line from Client...");
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				client_method = words_matrix[0][0].toLowerCase();		//@@@@ You Mao Bing
				url = words_matrix[0][1];	//$$$$ THERE ARE changes in Client
//				URL netUrl;
//				try {
//					netUrl = new URL(url);
//					setFile_Directory(netUrl.getFile());
//				} catch (MalformedURLException e) {
//					debug_msg += "#305. Bad format of Client passing URL.\n\n";
//				}	// not sure if it is better here to handle or later below handleGet/Post

				// 2ed->last line
				// Extract K-V Pairs from client command
				for (int i = 1; sentences[i] != null && i < sentences.length; i++) {
//					System.out.println(sentences[i]);
					if(sentences[i].matches("Host: ")) {
						String host = sentences[i].substring(sentences[i].indexOf(":") + 2);
						String myself = socket.getLocalAddress().toString();
						myself = '/'+myself;
						if(myself.equals(host)) {
							System.out.println("host EQUAL, visiting the right site.");
						} else {
							System.out.println("host Not EQUAL.");
							System.out.println(host+" ==== "+myself);
						}
					} else if(sentences[i].matches("User-Agent: ")) {
						System.out.println("match User-Agent!!");
						user_agent = sentences[i].substring(sentences[i].indexOf(":") + 2);
					} 
					else if (sentences[i].matches(":")) {
						String key = sentences[i].substring(0, sentences[i].indexOf(":"));
						String value = sentences[i].substring(sentences[i].indexOf(":") + 2);
						headers.put(key, value);
					}
				}
//				writer.println("Responding...(from Server)");
				writer.println("You've requested: "+ client_method +" method...(from Server)\n\n");
				if ("get".equals(client_method)) {
					handleGet(socket);
					writer.println("Conversation end...(from Server)");

				} else if ("post".equals(client_method)) {
					handlePost(sentences, writer);
					writer.println("Conversation end...(from Server)");

				} else {
					writer.println(Info_BadRequest());
					writer.println("Conversation end...(from Server)");
					debug_msg += "#302. Client bad request(not GET neither POST). Service terminated...\n\n";
					return;
				}
			} catch (IOException e) {
				debug_msg += "#306. Nothing to read / No bytes buffered on the socket. Fail to read from Client.\n\n";
				if(optionV)
					System.out.println(debug_msg);
			}

			// general Server Goodbye Reply...
			if (isUrgentClose) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					debug_msg += "#316. An I/O error occurs when closing the socket.\n\n";
					if(optionV)
						System.out.println(debug_msg);
				} // Server usually should be always running. This is for in case the server
					// must be stopped for some reasons.
			} else {
				OutputStreamWriter osWriter;
				try {
					osWriter = new OutputStreamWriter(socket.getOutputStream());
					osWriter.write("Connection closed... Thank you for connecting to:" + socket.getLocalSocketAddress());
					socket.close();
				} catch (IOException e) {
					debug_msg += "#307. Socket is not connected / An I/O error occurs Client-Server socket.\n\n";
					if(optionV)
						System.out.println(debug_msg);
				}
			}
		} // LONG else statement -- if (serverSocket is not null)

	} // end run()

	// 3-b) handleGet
	public void handleGet(Socket socket) {	//PrintWriter writer||Socket socket//$$$$ passing writer from main class MAY not help. writer is not initialized?

		/*
		 * eg. 
		 * [0][get/post+.* URL httpVersion]
		 * [1][host: HOST]
		 * [2][User-Agent: String]
		 * [3][key : value]
		 * [..][key : value]
		 * [n][key : value]
		 */
		OutputStream output = null;
		PrintWriter writer = null;
		try {
			output = socket.getOutputStream();
			writer = new PrintWriter(output, true); // ## "true": auto-flush the writer
		} catch (IOException e1) {
			debug_msg += "#310. An I/O error occurs when creating the output stream.\n\n";
			if(optionV)
				System.out.println(debug_msg);
		}

//		writer.println("Responding...");
		// SCENARIO1 -- [http://127.0.0.1/] -- get / -- Return the directory list
		if (url.matches("http://127.0.0.1/") || url.matches("/")) {
			writer.println(getDirFileList(file_directory));
			if(optionV) {
				System.out.println("Delivering File List to Client: ");
				System.out.println(getDirFileList(file_directory));
			}
		}
		// SCENARIO2 -- [http://127.0.0.1/COMP6461] -- get /foo -- Return the sub-dir
		// list OR File OR with query arguments
		// [http://127.0.0.1/COMP6461/]
		// [http://127.0.0.1/COMP6461/test.txt]
		// [http://127.0.0.1/COMP6461/test.txt?assignment=2]
		//

		// foo://example.com:8042/over/there?name=ferret#nose
		// \_/  \______________/ \_________/ \_________/ \__/
		//	|			|			|			|		   |
		// scheme 	authority 	  path		 query 		fragment

		// else if (url.matches("http://127.0.0.1/.+$") || url.matches("/.+$")) {
		else if (url.matches("http://127.0.0.1/(.*)") || url.matches("/(.*)")) {
			try {
				String fileName = null;
				String fileDirectory = null;
				URL temp_url = new URL(url);

				System.out.println();
				String temp_str = temp_url.getFile(); 									// [/COMP6461/test.txt?assignment=2]
															// @@@@ getPath()??

				if (url.indexOf("?") == -1) {

//					fileDirectory = temp_url.getPath();							 			// [/COMP6461/test.txt]
					String st = temp_url.getPath();											// @@@@ if(-d), do nothing
					fileDirectory = st.substring(0, st.lastIndexOf("/") );					// @@@@[/COMP6461/] -> [/COMP6461]
					System.out.println("Here1: "+fileDirectory);
					// $$$$ Security Issue. Handle Later.
					setFile_Directory(fileDirectory);
//					fileName = temp_url.getFile();
					fileName = url.substring(url.lastIndexOf("/") + 1); 				// [test.txt] or [comp6461]
					System.out.println("Here2: "+fileName);
					// optional: fileName = Path.getName()								// $$$$ String FileName is lame, try save the Path Object to carry more information.
					writer.println(getDirFileData(file_directory, fileName));
					//@@@@ Should handle Info_OK
					
				} else {
					// $$$$ record the args from Client HERE.
					JSONObject jObject = new JSONObject();
					String[] key = new String[10];
					String[] value = new String[10];
					String subString = temp_str.substring(temp_str.indexOf("?") + 1);	// [assignment=2&Content-Type=json]
					int i = 0;
					while(subString.contains("=")) {									// [assignment=2&Content-Type=json]
						key[i]= subString.substring(subString.indexOf("=") - 1);		//       |	   |
						value[i] = subString.substring(subString.indexOf("=") + 1);		//			   |
						if (subString.indexOf("&") != -1) {									// if there are another args pair.
							subString = subString.substring(subString.indexOf("&") + 1);	// new subString: [Content-Type=json]
						} else {
							break;
						}
					}
					for (int j = 0; j < value.length; j++) {
						jObject.put(key[j], value[j]);
					}
					setArgs(jObject);
					fileName = url.substring(url.lastIndexOf("/") + 1, url.indexOf("?")); // [test.txt] with '?' condition
				}
			} catch (MalformedURLException e) {
				debug_msg += "#305. Bad format of Client passing URL.\n\n";
				if(optionV)
					System.out.println(debug_msg);
			} catch (FileNotFoundException e) {
				debug_msg += "#404. File Not Found.\n\n";
				writer.println(Info_BadRequest());
				if(optionV)
					System.out.println(debug_msg);
			} catch (IOException e) {
				debug_msg += "";
				if(optionV)
					System.out.println(debug_msg);
			}
		}
	}

	// 3-c) handlePost
	public void handlePost(String[] sentences, PrintWriter writer) {

	}

	// 3-d) handle200Get
	public String Info_OK_Get() {
		setJson();
		content_length = js_obj.toString().length();
		String content = "\nHTTP /1.0 " + sCode[0] // sCode[0]--200 OK
				+ "\nConnection: closed" 	// closed -- means every connection closed after delivery msg.
												// Keep-Alive -- since this server is dedicated server, and serve only
												// small amount of user, we assume the server will run till the client say "bye".
				+ "\nServer: 6461_DIY_Server/1.0.0" 
				+ "\nDate: " + new Date().toString() // System clock time/date
				+ "\nContent-Type: DIY_json" 
				+ "\nContent-Length: " + content_length // non-verbose text msg length.
				+ "\nKeep-Alive: timeout=10, max=20\n\n" // ## NEED TO IMPLEMENT--TIMEOUT CLOCK
				+ js_obj +"\n\n"
				+ file_content;

		return content;
	}
	
	// 3-e) handle200Post
		public String Info_OK_Post() {
			setJson();
			content_length = js_obj.toString().length();
			String content = "\nHTTP /1.0 " + sCode[0] // sCode[0]--200 OK
					+ "\nConnection: closed" 	// closed -- means every connection closed after delivery msg.
													// Keep-Alive -- since this server is dedicated server, and serve only
													// small amount of user, we assume the server will run till the client say "bye".
					+ "\nServer: 6461_DIY_Server/1.0.0" 
					+ "\nDate: " + new Date().toString() // System clock time/date
					+ "\nContent-Type: DIY_json" 
					+ "\nContent-Length: " + content_length // non-verbose text msg length.
					+ "\nKeep-Alive: timeout=10, max=20\n\n" // ## NEED TO IMPLEMENT--TIMEOUT CLOCK
					+ "\n" + js_obj +"\n"+"\n"
					+ file_content;

			return content;
		}

	// 3-f) handle404
	public String Info_NotFound() {
		setJson();
		content_length = js_obj.toString().length();
		String content = "\nHTTP /1.0 " + sCode[1] // sCode[1]--404 NotFound
				+ "\nConnection: closed" 	// closed -- means every connection closed after delivery msg.
												// Keep-Alive -- since this server is dedicated server, and serve only
												// small amount of user, we assume the server will run till the client say "bye".
				+ "\nServer: 6461_DIY_Server/1.0.0" 
				+ "\nDate: " + new Date().toString() // System clock time/date
				+ "\nContent-Type: DIY_json"
				+ "\nContent-Length: " + content_length // non-verbose text msg length.
				+ "\nKeep-Alive: timeout=10, max=20\n\n" // ## NEED TO IMPLEMENT--TIMEOUT CLOCK
				+ "\n" + js_obj +"\n";

		return content;
	}

	// 3-g) handle other 400 Bad Request
	public String Info_BadRequest() {
		setJson();
		content_length = js_obj.toString().length();
		String content = "\nHTTP /1.0 " + sCode[2] // sCode[2]--400 BadRequest
				+ "\nConnection: closed" 
				+ "\nServer: 6461_DIY_Server/1.0.0" 
				+ "\nDate: " + new Date().toString() // System
				+ "\nContent-Type: DIY_json" + "\nContent-Length: " + content_length // non-verbose text msg length.
				+ "\nKeep-Alive: timeout=10, max=20\n\n" // ## NEED TO IMPLEMENT--TIMEOUT CLOCK
				+ js_obj + "\n";

		return content;
	}

	// Task#5 Getter && Setter Method

	// 5-a) Option V/P/D, debug_msg/port/directory
	public boolean isV() {
		if (optionV == false)
			return false;
		else
			return true;
	}

	public boolean isP() {
		if (optionP == false)
			return false;
		else
			return true;
	}

	public boolean isD() {
		if (optionD == false)
			return false;
		else
			return true;
	}
	
	public String getV() {
		return debug_msg;
	}

	public void setV() {
		optionV = true;
		debug_msg = "This Server has a time-out timer for " + timeout_timer + " sec, " + "and a maxmum timer for "
				+ max_timer + " sec.\n" + "This Server is running from: " + sys_creat_date + ".\n\n";
	}

	public void setP(int port) {
		optionP = true;
		this.port = port;
	}

	public void setD(String dir) {
		optionD = true;
		this.file_directory = dir;
	}

	// 5-b) file_directory, file_content
	public String getFile_Directory() {
		return file_directory;
	}

	public void setFile_Directory(String dir) {
		file_directory = dir;
	}

	public String getFile_Content() {
		return file_content;
	}

	public void setFile_Content(String content) {
		file_content = content;
	}

	// 5-c) args, headers, origin, url, json...ect
	public void setArgs(JSONObject args) {
		this.args = args;
	}

	public void setHeaders(JSONObject headers) {
		this.headers = headers;
	}

	public void setOrigin(Socket socket) {
		this.origin = socket.getLocalSocketAddress().toString();
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setJson() { // prepare for json output
		js_obj.put("args", args);
		js_obj.put("headers", headers);
		js_obj.put("origin", origin);
		js_obj.put("url", url);
	}

	// 5-c) file related getter & setter
	public String getDirFileList(String directory) {
		
		String dir = "The File(s) inside your requested directory is(are): \n";
		File[] files;
		try{
			files = new File(directory).listFiles();
		} catch (SecurityException e) {
			debug_msg += "#311. Warning! Client is trying to write without permission. System denied.\n\n";
			if(optionV)
				System.out.println(debug_msg);
			return "Error";
		}if(files == null) {
			return Info_NotFound();
		}else {
			for (File f : files) {
				if(f.isDirectory())
					dir += "Directory: " + f.getName() + " ,\n";
				if (f.isFile()) {
					dir += "File: " + f.getName() + " ,\n";
				}
			}
			file_content = dir;
			return Info_OK_Get();
		}
		
//		System.out.println("default output: "+dir);
	}

	public String getDirFileData(String directory, String fileName) throws FileNotFoundException, IOException {

		StringBuilder f_content = new StringBuilder();
		File file = new File(directory + fileName); // $$$$ lame here, try use the Path Object. SAME BELOW!!!
		
		if (!file.exists())
			return Info_NotFound();
		else if(file.isDirectory()) {
			return getDirFileList(directory);
		} else if(file.isFile()) {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			while (line != null) {
				f_content.append(line + "\r\n");
				line = br.readLine();
			}
			file_content = f_content.toString();
			return f_content.toString();
		}
		return "Neither Directory, nor File, Error.\n\n";
	}

	public int setDirFileData(String directory, String fileName) throws FileNotFoundException, IOException {
		StringBuilder f_content = new StringBuilder();
		File file = new File(directory + File.separator + fileName);

		// @@new-write
		if (!file.exists()) {
			file.createNewFile();
			PrintWriter writer = new PrintWriter(directory, "UTF-8");
			writer.println(file_content);
			writer.close();
			return 0;
		}
		// @@overwrite
		else if (file.exists()) {
			file.createNewFile();
			PrintWriter writer = new PrintWriter(directory, "UTF-8");
			writer.println(file_content);
			writer.close();
			return 1;
		}
		// @@error
		else
			return -1;

	}

//	public static void main(String[] args) throws IOException {
//
//	}

}
