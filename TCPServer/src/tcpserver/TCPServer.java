package tcpserver;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.net.*;

public class TCPServer implements Runnable {
	// Connect status constants
	public final static int NULL = 0;
	public final static int DISCONNECTING = 1;
	public final static int BEGIN_CONNECT = 2;
	public final static int CONNECTED = 3;

	// Other constants
	public final static String statusMessages[] = {
			" Error! Could not connect!", " Disconnecting...",
			" Listening...", " Connected" };
	public final static TCPServer tcpObj = new TCPServer();
	public final static String END_CHAT_SESSION = new Character((char) 0)
			.toString(); // Indicates the end of a session

	// Connection atate info
	public static String hostIP = null;
	public static int port = 1234;
	public static int connectionStatus = BEGIN_CONNECT;
	public static String statusString = statusMessages[connectionStatus];
	public static StringBuffer toAppend = new StringBuffer("");
	public static StringBuffer toSend = new StringBuffer("");

	// Various GUI components and info
	public static JFrame mainFrame = null;
	public static JTextArea chatText = null;
	public static JPanel statusBar = null;
	public static JLabel statusField = null;
	public static JTextField statusColor = null;
	public static JTextField ipField = null;
	public static JTextField portField = null;

	// TCP Components
	public static ServerSocket hostServer = null;
	public static Socket socket = null;
	public static BufferedReader in = null;
	public static PrintWriter out = null;

	// ///////////////////////////////////////////////////////////////

	private static JPanel initOptionsPane() {
		JPanel pane = null;

		// Create an options pane
		JPanel optionsPane = new JPanel(new GridLayout(4, 1));

		// IP address input
		pane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pane.add(new JLabel("Host IP:"));
		ipField = new JTextField(10);
		ipField.setText(hostIP);
		ipField.setEnabled(false);
		
		pane.add(ipField);
		optionsPane.add(pane);

		// Port input
		pane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pane.add(new JLabel("Port:"));
		portField = new JTextField(10);
		portField.setEnabled(false);
		portField.setText((new Integer(port)).toString());

		pane.add(portField);
		optionsPane.add(pane);

		return optionsPane;
	}

	// ///////////////////////////////////////////////////////////////

	// Initialize all the GUI components and display the frame
	private static void initGUI() {
		// Set up the status bar
		statusField = new JLabel();
		statusColor = new JTextField(1);
		statusColor.setBackground(Color.red);
		statusColor.setEditable(false);
		statusBar = new JPanel(new BorderLayout());
		statusBar.add(statusColor, BorderLayout.WEST);
		statusBar.add(statusField, BorderLayout.CENTER);

		// Set up the options pane
		JPanel optionsPane = initOptionsPane();

		// Set up the chat pane
		JPanel chatPane = new JPanel(new BorderLayout());
		chatText = new JTextArea(10, 20);
		chatText.setLineWrap(true);
		chatText.setEditable(false);
		chatText.setForeground(Color.blue);
		JScrollPane chatTextPane = new JScrollPane(chatText,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		chatPane.add(chatTextPane, BorderLayout.CENTER);
		chatPane.setPreferredSize(new Dimension(200, 100));

		// Set up the main pane
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.add(statusBar, BorderLayout.SOUTH);
		mainPane.add(optionsPane, BorderLayout.WEST);
		mainPane.add(chatPane, BorderLayout.CENTER);

		// Set up the main frame
		mainFrame = new JFrame("Servidor TCP");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setContentPane(mainPane);
		mainFrame.setSize(mainFrame.getPreferredSize());
		mainFrame.setLocation(200, 200);
		mainFrame.pack();
		mainFrame.setVisible(true);
	}

	// ///////////////////////////////////////////////////////////////

	// The thread-safe way to change the GUI components while
	// changing state
	private static void changeStatusTS(int newConnectStatus, boolean noError) {
		// Change state if valid state
		if (newConnectStatus != NULL) {
			connectionStatus = newConnectStatus;
		}

		// If there is no error, display the appropriate status message
		if (noError) {
			statusString = statusMessages[connectionStatus];
		}
		// Otherwise, display error message
		else {
			statusString = statusMessages[NULL];
		}

		// Call the run() routine (Runnable interface) on the
		// error-handling and GUI-update thread
		SwingUtilities.invokeLater(tcpObj);
	}

	// ///////////////////////////////////////////////////////////////

	// The non-thread-safe way to change the GUI components while
	// changing state
	private static void changeStatusNTS(int newConnectStatus, boolean noError) {
		// Change state if valid state
		if (newConnectStatus != NULL) {
			connectionStatus = newConnectStatus;
		}

		// If there is no error, display the appropriate status message
		if (noError) {
			statusString = statusMessages[connectionStatus];
		}
		// Otherwise, display error message
		else {
			statusString = statusMessages[NULL];
		}

		// Call the run() routine (Runnable interface) on the
		// current thread
		tcpObj.run();
	}

	// ///////////////////////////////////////////////////////////////

	// Thread-safe way to append to the chat box
	private static void appendToChatBox(String s) {
		synchronized (toAppend) {
			toAppend.append(s);
		}
	}

	// ///////////////////////////////////////////////////////////////

	// Cleanup for disconnect
	private static void cleanUp(int if_server) {
		try {
			if (hostServer != null && if_server == 1) {
				hostServer.close();
				hostServer = null;
			}
		} catch (IOException e) {
			hostServer = null;
		}

		try {
			if (socket != null) {
				socket.close();
				socket = null;
			}
		} catch (IOException e) {
			socket = null;
		}

		try {
			if (in != null) {
				in.close();
				in = null;
			}
		} catch (IOException e) {
			in = null;
		}

		if (out != null) {
			out.close();
			out = null;
		}
	}
	
	private static void guardar_mensaje(String mensaje, int f){		
		StringBuilder msn = new StringBuilder();
		if(f == 1){
			msn.append("FDLV ");
		}else{
			msn.append("DLV ");
		}
		msn.append(mensaje);
		//-------
		FileWriter arch = null; 
        PrintWriter pw = null;
        try {
        	File file = new File("server_msn_nl.txt");
        	if(file.exists()==false){
        		file.createNewFile();
        	}
            arch = new FileWriter("server_msn_nl.txt", true);
            pw = new PrintWriter(arch);
            pw.println(msn);

        } catch (IOException ex) {
            //Logger.getLogger(ManejoArchivos.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (null != arch && null != pw) {
                try {
                    pw.close();
                    arch.close();
                } catch (IOException ex) {
                    ex.printStackTrace();;
                }
            }
        }
		
	}
	

	// Checks the current state and sets the enables/disables
	// accordingly
	public void run() {
		try {
			hostIP = InetAddress.getLocalHost().toString();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			
		}
		switch (connectionStatus) {
		case DISCONNECTING:
			statusColor.setBackground(Color.orange);
			break;

		case CONNECTED:
			statusColor.setBackground(Color.green);
			break;

		case BEGIN_CONNECT:
			statusColor.setBackground(Color.orange);
			break;
		}

		// Make sure that the button/text field states are consistent
		// with the internal states
		ipField.setText(hostIP);
		portField.setText((new Integer(port)).toString());
		statusField.setText(statusString);
		chatText.append(toAppend.toString());
		toAppend.setLength(0);

		mainFrame.repaint();
	}

	// ///////////////////////////////////////////////////////////////
	
	
	// The main procedure
	public static void main(String args[]) {
		String s;

		initGUI();
		changeStatusNTS(BEGIN_CONNECT, true);

		while (true) {
			try { // Poll every ~10 ms
				Thread.sleep(10);
			} catch (InterruptedException e) {
				
			}

			switch (connectionStatus) {
			
			case BEGIN_CONNECT:
				try {
					// Try to set up a server if host
					if(hostServer == null){
						InetAddress inetAddress = InetAddress.getLocalHost();
						hostServer = new ServerSocket(1234, 50, inetAddress);
					}
					socket = hostServer.accept();
					
					in = new BufferedReader(new InputStreamReader(
							socket.getInputStream()));
					out = new PrintWriter(socket.getOutputStream(), true);

					changeStatusTS(CONNECTED, true);
				}
				// If error, clean up and output an error message
				catch (IOException e) {
					cleanUp(1);
					changeStatusTS(NULL, false);
				}
				break;

			case CONNECTED:
				try {
					// Send data
					if (toSend.length() != 0) {
						out.print(toSend);
						out.flush();
						
						toSend.setLength(0);
						changeStatusTS(DISCONNECTING, true);
					}

					// Receive data
					if (in.ready()) {
						s = in.readLine();
						if ((s != null) && (s.length() != 0)) {
							appendToChatBox("IN: " + s + "\n");
							if(s.startsWith("FETCH")){
								send_fetch(s);
							}
							else if(s.startsWith("FILE")){
								recive_file();
								changeStatusTS(DISCONNECTING, true);
							}
							else{
								guardar_mensaje(s, 0);
								changeStatusTS(DISCONNECTING, true);
							}
						}
					}
				} catch (IOException e) {
					cleanUp(1);
					changeStatusTS(NULL, false);
				}
				break;

			case DISCONNECTING:
				// Tell other chatter to disconnect as well
				out.print(END_CHAT_SESSION);
				out.flush();

				// Clean up (close all streams/sockets)
				cleanUp(0);
				changeStatusTS(BEGIN_CONNECT, true);
				break;

			default:
				break; // do nothing
			}
		}
	}

	private static void recive_file() {
		
	    FileOutputStream fos;
		try {
		    String nombre = in.readLine();
		    String tamanno = in.readLine();
		    String destino = in.readLine();

		    byte[] byte_array = new byte[Integer.parseInt(tamanno)];
		    
		    InputStream is = socket.getInputStream();
		    File arch = new File(nombre);
		    if(arch.exists()==false){
		    	arch.createNewFile();
		    }
			fos = new FileOutputStream(arch);

			int count;
            while ((count = is.read(byte_array)) >= 0) {
                fos.write(byte_array, 0, count);

            }
            fos.flush();

		    fos.close();
		    is.close();
		    
		    StringBuilder msn = new StringBuilder();
		    msn.append("mensaje="+nombre);
		    msn.append("&");
		    msn.append("ip_o="+InetAddress.getLocalHost().getHostAddress());
		    msn.append("&");
		    msn.append("ip_d="+destino);
		    guardar_mensaje(msn.toString(), 1);
		    
		} catch (Exception e) {
			System.out.println("error recive_file()");
			e.printStackTrace();
		}

	}

	private static int send_fetch(String content) {
		int fo = content.indexOf("&");
		String ip = content.substring(fo+1);
		
		//--
		
		FileWriter wtr = null;
        PrintWriter pw = null;
        
    	String path;
        FileReader fr = null;
        BufferedReader br = null;
        StringBuilder fin = new StringBuilder();

        try {
            File contenido = new File("server_msn_nl.txt");
            if(contenido.exists()==false){
            	contenido.createNewFile();
            	toSend.append("NOPE\n");
            	return 0;
            }
            wtr = new FileWriter("tmp.txt", true);
            pw = new PrintWriter(wtr);
            
        	path = contenido.getAbsolutePath();
        	
            fr = new FileReader(path);
            br = new BufferedReader(fr);

            // Lectura del fichero
            String linea; 
            
            while ((linea = br.readLine()) != null) {
                if (linea.equals("")){
                	continue;
                	}
                if((linea.startsWith("DLV") || linea.startsWith("FDLV")) && linea.endsWith(ip)){
                	fin.append(linea+"&");
                }
                else{
                	pw.println(linea);
                }
            }
            if(fin.length() == 0){
            	toSend.append("NOPE\n");
            }
            else{
            	toSend.append(fin+"\n");
            }
            pw.close();
            br.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {

            	File aux = new File("server_msn_nl.txt");
            	aux.delete();
            	File aux2 = new File("tmp.txt");
            	boolean result = aux2.renameTo(new File("server_msn_nl.txt"));
                if(result){
                	//System.out.println("Exito");
                }
                else{
                	//System.out.println("Fracaso");
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return 1;

	}
}
