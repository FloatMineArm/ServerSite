
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class Lab2_httpfsCommand {

	public static int httpfs_flag = 0;
	
	public static Lab2_MyServer myserver;
	

	// input httpc-like url with command, output format that can use Client Library
	
	public static void main(String[] args) throws IOException {
		
		myserver = new Lab2_MyServer();
		
// Task#1 run Server  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		do {
			System.out.println("Enter c-URL like command starting with \"httpfs\": ");
			
// Task#2 Get Server Set-up Input
// Set-up inputs can be:
//			[httpfs] + [-v] and/or + [-p (int)port] and/or + [-d (String)path]
			
			// 2-a) basic setup for getting user input:
			InputStreamReader isReader = new InputStreamReader(System.in);
			BufferedReader br = new BufferedReader(isReader);

			// 2-b) save user input into var named: [command]
			String command = br.readLine();
			System.out.println();		// user-friendly
			
			// 2-c) pre-process [command] -- split StringLine into words:
			String[] words;
			words = command.split(" ");

			// 2-d) grammar checking for setup input
			// 2-d-1) first word check
			if (!(words[0].toLowerCase().equals("httpfs")) || words.length <= 1) {
				System.out.println("Invalid Input, Please try again. no httpfs||httpfs error||only httpfs"); // error
				httpfs_flag = Httpfs_Continue();
			}
			// 2-d-2) rest words check -- referring at MyServer
			else if (words.length >= 2) {
				for(int i=1; i<words.length; i++) {
					switch (words[i].toLowerCase()) {
					case "-v": {
						myserver.setV();
						httpfs_flag = 0;
						break;
					}
					case "-p": {
						if(words.length > i+1) {
							myserver.setP(Integer.parseInt(words[i+1]));
							httpfs_flag = 0;
							i++;
						} else {
							System.out.println("Illegal -p Input");
							httpfs_flag = Httpfs_Continue();
						}
						break;
					}
					case "-d" : {
						if(words.length > i+1) {
							myserver.setD(words[i+1]);
							httpfs_flag = 0;
							i++;
						} else {
							System.out.println("Illegal -d Input");
							httpfs_flag = Httpfs_Continue();
						}
					break;
					}
					case "help": {
						if("help".equals(words[1].toLowerCase())) {
							System.out.println("Usage:\r\n" + 
									"httpfs is a simple file server.\r\n" + 
									"usage: httpfs [-v] [-p PORT] [-d PATH-TO-DIR]\r\n" + 
									"-v Prints debugging messages.\r\n" + 
									"-p Specifies the port number that the server will listen and serve at.\r\n" + 
									"Default is 8080.\r\n" + 
									"-d Specifies the directory that the server will use to read/write requested files. Default is the current directory when launching the application.");
							httpfs_flag = Httpfs_Continue();
						} else {
							System.out.println("Wrong use of \"help\".");
						}
						break;
					}
					
//					default:
//						System.out.println("Invalid Input, Please try again. other second chunk error");
//						Httpc_Continue();
//						break;
					} // end switch
				} // end for				
			} // end if

		} while (httpfs_flag == 1); // end do-while(httpc_flag)
		
// Task#3 Setup is done, run the Server.
		if(myserver != null && httpfs_flag == 0) {
			System.out.println("Now the Server is running...\n");
			myserver.executeServer();
		}

	}

	public static int Httpfs_Continue() {

		System.out.println("Do you want to try again? (type \"y\" for YES or \"n\" for NO.)");
		Scanner scan = new Scanner(System.in);
		char reader = scan.next().toLowerCase().charAt(0);
		if (reader == 'y') {
			System.out.println("System continue...\n");
			return 1;
		} else if (reader == 'n') {
			System.out.println("Thanks for using HTTPC Clint Service. System terminated.");
			return -1;
		} else {
			System.out.println("Error input. System terminated.");
			return -1;
		}
	}

}

// softw QI