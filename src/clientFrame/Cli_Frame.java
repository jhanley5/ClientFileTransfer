package clientFrame;
import clientFrame.Login.Client;
import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

/**
 *
 * Class that displays a User interface for client after connecting to server and being verified 
 * Allows Users to display personal files, and files they have been permitted to view by others
 * Allows Users to add friends
 * Allows Users Add comments to comment file
 * Allows Users to chat
 * Allows Users to let friends view file displayed on clients screen if client owns file 
 */
public class Cli_Frame extends javax.swing.JFrame {

    String username, address;   // Declares string variables for username and IP address
    ArrayList<String> friends = new ArrayList<String>();   // Creates new String type Array list for list of friends
    ArrayList<String> MYFiles = new ArrayList<String>();   // Creates new String type Array list for list of personal files
    ArrayList<String> FriendFiles = new ArrayList<String>();    // Creates new String type Array list for list of files user has permission to view 
    ArrayList<String> Allowed = new ArrayList<String>();    // Creates new String type Array list for list of users able to see current file  
    String fileowner = "";  // Initializes string variable for owner of file currently displayed
    String fileName = "";   // Initializes string variable for currently displayed file's name
    Boolean isConnected;    // Declares Boolean variable associated with whether the client is connected to server currently
    Client cli2 = new Client(); // Creates new Client object to copy passed in Client object's address and port for reconnect later if needed
    
    Socket sock;    // Declares Socket object 
    BufferedReader reader;  // Declares BufferedReader object 
    PrintWriter writer; // Declares PrintWriter object
    
    /**
     * Function that Disconnects client from server 
     */ 
    public void Disconnect() 
    {
        try 
        {
            writer.println(username+"&has disconnected&Disconnect");    // PrintWriter writes to server through input stream that client has disconnected and performs action associated with Disconnect command 
            writer.flush(); // Flushes or clears PrintWriter's buffer
            ta_chat.append("You have been Disconnected.\n");    // Appends message to Client's chat
            sock.close();   // Closes socket connection to server
            writer.close(); // Closes output stream writer
            reader.close(); // Closes input stream reader
            isConnected = false;    // Means that client is no longer connected to server
        } catch(Exception ex) 
        {
            ta_chat.append("Failed to disconnect. \n"); // Appends error message to Client's chat 
        }        
    }
    
    /**
     * Function that creates thread for class that's reading incoming messages from input stream
     */ 
    public void ListenThread() 
    {
         Thread IncomingReader = new Thread(new IncomingReader());  // Creates new thread object for Incoming Reader class
         IncomingReader.start();    // Starts new thread
    }
    
    /**
     * Class that reads incoming messages from server
     */ 
    public class IncomingReader implements Runnable
    {        
        /**
        * Function that displays file
        */ 
        public void Display(int s)
        {
            try
            {
                byte[] mybytearray = new byte[s];    // Creates new Byte array with size of file
                InputStream IS = sock.getInputStream(); // Initializes new InputStream object to read from socket connection input stream
                IS.read(mybytearray, 0, mybytearray.length);    // Reads input from input stream storing it into byte array 
                String dsply = new String(mybytearray);  // Creates new string object based on byte array content
                Display.append(dsply);  // Appends string variable to Display text area
                Display.setCaretPosition(Display.getDocument().getLength());  // Sets caret postion all the way at the then of the text area
            }
            catch(IOException ex)
            {
                ta_chat.append("Failed to read input stream and display input\n"); // Appends message to Client's chat 
            }
        }
          
        /**
        * Listens for incoming messages from server 
        */ 
        @Override
        public void run() 
        {
            String[] data;  // Declares string array
            String stream, done = "Done", chat = "Chat";    // Declares/Initializes string variables

            try 
            {
                while ((stream = reader.readLine()) != null) // While message read from input stream is not null
                {
                     data = stream.split("&");  // Initializes string array with string variable split on '&' 
                     if(data[1].equals("Uploaded")) // If element equals Uploaded
                     {
                         MYFiles.add(data[0]);  // Adds file uploaded to list of user's personal files
                     }
                     else if(data[1].equals("FrFile"))  // If element equals FrFile
                     {
                        FriendFiles.clear();    // Clear ArrayList FriendFiles
                        String[] tmp;   
                        tmp = data[0].split("`");   // Initializes string array with string array element parsed on '`'
                        for(String n:tmp)   // For number of elements in string array tmp
                        {
                            FriendFiles.add(n); // FriendFiles adds name of friends' files user is allowed to view
                        }
                        
                        DefaultListModel dlm = new DefaultListModel();  // Creates new list model object dlm 
                        for(String s:FriendFiles)   // For number of elements in FriendFiles
                        {   
                            dlm.addElement(s);  // list model add string s as new element 
                        }
                        lst_AvailFiles.setModel(dlm);   // Set model of JList as list model object dlm  
                        lst_AvailFiles.setVisible(true);    // Set list to visible
                     }
                     else if(data[1].equals("Commented"))   // If element equals Commented
                     {
                         ta_PrevCmnts.append(data[0]+"\n"); // Append comment to Prevcmnts textarea
                         ta_PrevCmnts.setCaretPosition(ta_PrevCmnts.getDocument().getLength()); // Set caret to end of document
                         
                     }
                     else if(data[1].equals("AddedViewer")) // If element equals AddedViewer
                     {
                         Allowed.add(data[0]);  // Add viewer to Arraylist for users allowed to view current file
                     }
                     else if(data[1].equals("NotAdded"))    // If element equals NotAdded
                     {
                        JFrame frame = new JFrame("Error"); // Displays Error frame
                        JOptionPane.showMessageDialog(frame, "User to be added does not exist or is already a friend!"); // Displays error message on frame
                     }
                     else if(data[1].equals("Added"))   // If element equals Added
                     {
                        ta_friends.append(data[0]+"\n");    // Append name of friend added to friend textarea
                        ta_friends.setCaretPosition(ta_friends.getDocument().getLength());  // Set caret to end of friend text area
                        friends.add(data[0]);   // friends ArrayList add friend name to list 
                        tf_addFriend.setText("");   // Set text in add friend textfield to being empty 
                        tf_addFriend.requestFocus();    // Focuses input on add new friend textfield 
                     }
                     else if(data[1].equals("DisplayComments")) // If element equals DisplayComments
                     {    
                         String[] tmp;
                         tmp = data[0].split("`"); // Initializes string array with string element parsed by '`'
                         for(String tmp2:tmp)    // For number of elements in string array
                         {
                             ta_PrevCmnts.append(tmp2+"\n");    // Append string variable initialized with string array element to PrevCmnts textarea
                             ta_PrevCmnts.setCaretPosition(ta_PrevCmnts.getDocument().getLength()); // Set caret position to end of textarea
                         }
                         ta_PrevCmnts.setVisible(true); // Set textarea to visible
                         
                     }
                     else if(data[2].equals("Display")) // If element equals Display
                     {
                        int size = Integer.parseInt(data[1]);   // Convert string element to integer
                        Display(size);  // Call function to display file passing in file size as parameter
                        String[] tmp;
                        tmp = data[0].split("`");   // Initialize string array with string variable parsed by '`'
                        fileowner = tmp[1]; // Initialize string variable with name of fileowner
                        if(!tmp[0].equals("None"))  // If element does not equal None
                        {
                            String[] tmp2;
                            tmp2 = tmp[0].split(":");   // Initializes string array with string element parsed by ':' 
                            for(String s:tmp2)  // For number of elements in string array
                            {
                                Allowed.add(s); // Add string variable to ArrayList<String> for users allowed to view file displayed
                            }
                        }
                     }
                     else if (data[2].equals(chat)) // If element equals Chat
                     {
                        ta_chat.append(data[0] + ": " + data[1] + "\n");    // Display string elements to Chat textarea
                        ta_chat.setCaretPosition(ta_chat.getDocument().getLength());    // Set caret position to end of textarea 
                       
                     } 
                     else if (data[2].equals(done)) // If element equals Done
                     {
                         ta_chat.append(data[0]+" "+data[1]+"\n"); // Appends string array elements to chat text area
                         ta_chat.setCaretPosition(ta_chat.getDocument().getLength());   // Sets caret to end of textarea
                         ta_chat.append("You have been Disconnected.\n");   // Appends string to chat textarea
                         ta_chat.setCaretPosition(ta_chat.getDocument().getLength());   // Sets caret to end of text area
                         sock.close();  // Closes socket connection
                         writer.close(); // Closes output stream writer
                         reader.close();    // Closes input stream reader
                         isConnected = false;   // Means client is no longer connected to server
                         break; // Break from while loop
                     } 
                     else if(data[3].equals("Populate"))    // If element equals Populate
                     {
                        String[] frnds;
                        frnds = data[0].split("`"); // Initialize string array with string element parsed by '`'
                        
                        String[] owned;
                        owned = data[1].split("`"); // Initializes string array with string element parsed by '`'
                        
                        String[] avail;
                        avail = data[2].split("`");  // Initializes string array with string element parsed by '`'
                        
                        
                        for(String s:frnds) // For number of elements in string array frnds
                        {
                            friends.add(s); // Add string s to ArrayList friends 
                            ta_friends.append(s+"\n");  // Appends string s to friends textarea
                            ta_friends.setCaretPosition(ta_friends.getDocument().getLength());  // Sets caret position to end of text area
                        }
                        
                        for(String s:owned) // For number of elements in string array owned
                        {
                            MYFiles.add(s); // Add string s to ArrayList of files user owns
                        }
                 
                        for(String s:avail) // For number of elements in string array avail
                        {
                            FriendFiles.add(s); // Add string s to ArrayList of friends' Files
                        }                     
                     }
                }
           }
           catch(Exception ex) 
           { 
              JFrame frame = new JFrame("Error"); // Displays Error frame
              JOptionPane.showMessageDialog(frame, "Failed to read from stream!"); // Displays error message on frame
           }
        }
    }
     
    /**
     * Creates new form Cli_Frame
     */ 
    public Cli_Frame() {
        initComponents();
    }
    
    /**
     * Creates new form Cli_Frame with Client Object as parameter
     */ 
    public Cli_Frame(Client cli)
    {
        initComponents();
        
        ta_chat.setVisible(true);   // Sets chat textarea visible
        ta_friends.setVisible(true);    // Sets friends textarea visible
        lst_AvailFiles.setVisible(true);    // Sets List of Available file visible
        ta_PrevCmnts.setVisible(false); // Sets previous comments textarea not visible
        ta_PrevCmnts.setEditable(false);    // Sets previous comments textarea not editable
        ta_chat.setEditable(false); // Sets chat textarea not editable
        Display.setEditable(false); // Sets Display textarea not editable
        ta_friends.setEditable(false);  // Sets friends textarea not editable
        
        try
        {
            sock = new Socket(cli.Addr,cli.port);   // Initializes socket object with pass in Client object's address and port
            InputStreamReader streamreader = new InputStreamReader(sock.getInputStream());  // Creates InputStreamReader object that reads from socket input stream
            reader = new BufferedReader(streamreader);  // Initializes BufferedReader object to buffer stream reader content
            writer = new PrintWriter(sock.getOutputStream());   // Initializes PrintWriter object to write content to socket output stream
            username = cli.Usrname; // Initializes username string variable with Client object's username
            isConnected = true; // Means Client is connected to server
            cli2.Addr = cli.Addr;   // Initializes new Client object's address with passed in Client object's address
            cli2.port = cli.port;   // Initializes new Client object's port with passed in Client object's port
            ListenThread(); // Calls function to create thread for listenign to socket input stream
            writer.println(username+"&Ready");  // Writes to server, client username and command that Client is ready for intial information
            writer.flush(); // Flushes PrintWriter buffer
        }
        catch(IOException ex)
        {
            JFrame frame = new JFrame("Error"); // Displays Error frame
            JOptionPane.showMessageDialog(frame, "Failed to create socket connection!"); // Displays error message on frame
        } 
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addViewer = new javax.swing.JFrame();
        b_addViewer2 = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        lst_viewers = new javax.swing.JList<>();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        Display = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lst_AvailFiles = new javax.swing.JList<>();
        jLabel3 = new javax.swing.JLabel();
        b_uploadFile = new javax.swing.JButton();
        b_addFriend = new javax.swing.JButton();
        b_addViewer = new javax.swing.JButton();
        tf_comment = new javax.swing.JTextField();
        b_addCmnt = new javax.swing.JButton();
        b_PrevCmnts = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        ta_chat = new javax.swing.JTextArea();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        ta_PrevCmnts = new javax.swing.JTextArea();
        tf_chat = new javax.swing.JTextField();
        b_send = new javax.swing.JButton();
        tf_addFriend = new javax.swing.JTextField();
        rb_myFiles = new javax.swing.JRadioButton();
        rb_othrUsrFiles = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        b_newDsply = new javax.swing.JButton();
        jScrollPane8 = new javax.swing.JScrollPane();
        ta_friends = new javax.swing.JTextArea();
        b_Reconnect = new javax.swing.JButton();

        addViewer.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addViewer.setTitle("AddViewer");
        addViewer.setBounds(new java.awt.Rectangle(0, 0, 400, 300));
        addViewer.setResizable(false);

        b_addViewer2.setText("Add Viewer");
        b_addViewer2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_addViewer2ActionPerformed(evt);
            }
        });

        jScrollPane6.setViewportView(lst_viewers);

        javax.swing.GroupLayout addViewerLayout = new javax.swing.GroupLayout(addViewer.getContentPane());
        addViewer.getContentPane().setLayout(addViewerLayout);
        addViewerLayout.setHorizontalGroup(
            addViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addViewerLayout.createSequentialGroup()
                .addGroup(addViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(addViewerLayout.createSequentialGroup()
                        .addGap(75, 75, 75)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(addViewerLayout.createSequentialGroup()
                        .addGap(145, 145, 145)
                        .addComponent(b_addViewer2)))
                .addContainerGap(84, Short.MAX_VALUE))
        );
        addViewerLayout.setVerticalGroup(
            addViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addViewerLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(b_addViewer2)
                .addContainerGap(66, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Chat - Client ");
        setBackground(new java.awt.Color(0, 0, 153));
        setForeground(new java.awt.Color(0, 0, 0));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        Display.setColumns(20);
        Display.setRows(5);
        jScrollPane1.setViewportView(Display);

        jLabel1.setFont(new java.awt.Font("Charlemagne Std", 0, 11)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 102));
        jLabel1.setText("Current File:");

        jScrollPane2.setViewportView(lst_AvailFiles);

        jLabel3.setFont(new java.awt.Font("Charlemagne Std", 0, 11)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(0, 0, 102));
        jLabel3.setText("Friends:");

        b_uploadFile.setBackground(new java.awt.Color(255, 255, 0));
        b_uploadFile.setFont(new java.awt.Font("Charlemagne Std", 0, 11)); // NOI18N
        b_uploadFile.setForeground(new java.awt.Color(0, 0, 102));
        b_uploadFile.setText("Upload File");
        b_uploadFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_uploadFileActionPerformed(evt);
            }
        });

        b_addFriend.setBackground(new java.awt.Color(255, 255, 0));
        b_addFriend.setFont(new java.awt.Font("Charlemagne Std", 0, 11)); // NOI18N
        b_addFriend.setForeground(new java.awt.Color(0, 0, 102));
        b_addFriend.setText("Add Friend");
        b_addFriend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_addFriendActionPerformed(evt);
            }
        });

        b_addViewer.setBackground(new java.awt.Color(255, 255, 0));
        b_addViewer.setFont(new java.awt.Font("Charlemagne Std", 0, 11)); // NOI18N
        b_addViewer.setForeground(new java.awt.Color(0, 0, 102));
        b_addViewer.setText("Add Viewers");
        b_addViewer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_addViewerActionPerformed(evt);
            }
        });

        tf_comment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tf_commentActionPerformed(evt);
            }
        });

        b_addCmnt.setBackground(new java.awt.Color(255, 255, 0));
        b_addCmnt.setFont(new java.awt.Font("Charlemagne Std", 0, 10)); // NOI18N
        b_addCmnt.setForeground(new java.awt.Color(0, 0, 102));
        b_addCmnt.setText("Add Comment");
        b_addCmnt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_addCmntActionPerformed(evt);
            }
        });

        b_PrevCmnts.setBackground(new java.awt.Color(255, 255, 0));
        b_PrevCmnts.setFont(new java.awt.Font("Charlemagne Std", 0, 10)); // NOI18N
        b_PrevCmnts.setForeground(new java.awt.Color(0, 0, 102));
        b_PrevCmnts.setText("View Previous Comments");
        b_PrevCmnts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_PrevCmntsActionPerformed(evt);
            }
        });

        ta_chat.setColumns(20);
        ta_chat.setRows(5);
        jScrollPane4.setViewportView(ta_chat);

        jLabel4.setFont(new java.awt.Font("Charlemagne Std", 0, 11)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(0, 0, 102));
        jLabel4.setText("Chat:");

        ta_PrevCmnts.setColumns(20);
        ta_PrevCmnts.setRows(5);
        jScrollPane5.setViewportView(ta_PrevCmnts);

        tf_chat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tf_chatActionPerformed(evt);
            }
        });

        b_send.setBackground(new java.awt.Color(255, 255, 0));
        b_send.setFont(new java.awt.Font("Charlemagne Std", 0, 11)); // NOI18N
        b_send.setForeground(new java.awt.Color(0, 0, 102));
        b_send.setText("Send");
        b_send.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_sendActionPerformed(evt);
            }
        });

        buttonGroup1.add(rb_myFiles);
        rb_myFiles.setFont(new java.awt.Font("Charlemagne Std", 0, 11)); // NOI18N
        rb_myFiles.setForeground(new java.awt.Color(0, 0, 102));
        rb_myFiles.setText("My Files");
        rb_myFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rb_myFilesActionPerformed(evt);
            }
        });

        buttonGroup1.add(rb_othrUsrFiles);
        rb_othrUsrFiles.setFont(new java.awt.Font("Charlemagne Std", 0, 11)); // NOI18N
        rb_othrUsrFiles.setForeground(new java.awt.Color(0, 0, 102));
        rb_othrUsrFiles.setText("Friend Files");
        rb_othrUsrFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rb_othrUsrFilesActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Charlemagne Std", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 0, 102));
        jLabel2.setText("SnD (Share & Discuss)");

        b_newDsply.setBackground(new java.awt.Color(255, 255, 0));
        b_newDsply.setText("Display");
        b_newDsply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_newDsplyActionPerformed(evt);
            }
        });

        ta_friends.setColumns(20);
        ta_friends.setRows(5);
        jScrollPane8.setViewportView(ta_friends);

        b_Reconnect.setBackground(new java.awt.Color(255, 255, 0));
        b_Reconnect.setText("Reconnect");
        b_Reconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_ReconnectActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(b_addCmnt)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tf_comment))
                            .addComponent(jScrollPane1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(30, 30, 30)
                                .addComponent(b_uploadFile)
                                .addGap(27, 27, 27)
                                .addComponent(b_addViewer)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(b_PrevCmnts, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane5)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(rb_myFiles)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(rb_othrUsrFiles)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(b_newDsply)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(tf_addFriend, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(b_addFriend, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(23, Short.MAX_VALUE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(jLabel3)
                                                .addGroup(layout.createSequentialGroup()
                                                    .addComponent(tf_chat, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(b_send)))))
                                    .addGap(14, 14, 14))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addGap(156, 156, 156)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(b_Reconnect)
                                        .addComponent(jLabel4))
                                    .addGap(19, 19, 19)))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(b_Reconnect))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rb_othrUsrFiles)
                    .addComponent(rb_myFiles)
                    .addComponent(b_addViewer)
                    .addComponent(b_uploadFile)
                    .addComponent(jLabel1)
                    .addComponent(jLabel4)
                    .addComponent(b_newDsply))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(tf_chat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(b_send))
                                .addGap(11, 11, 11)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tf_addFriend, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(b_addFriend)
                            .addComponent(b_addCmnt)
                            .addComponent(tf_comment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(b_PrevCmnts)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane5)))
                .addContainerGap(35, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Button that uploads file from client to server
     */ 
    private void b_uploadFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b_uploadFileActionPerformed
        try
        {
            JFileChooser jfc = new JFileChooser();  // Creates FileChooser object
            File myFile = null; // Initializes File object to null
            String path = "";   // Initializes string variable to empty
            int returnValue = jfc.showOpenDialog(null); // Initializes integer variable with value of operations status
            if (returnValue == JFileChooser.APPROVE_OPTION) // If integer for status equals value for a a file being selected
            {
                myFile = jfc.getSelectedFile(); // Initializes File variable with selected file 
                path = myFile.getAbsolutePath();    // Initializes string variable with file path of selected file
                myFile = new File(path);    // Initializes file variable with new file object based on path   
                int size = (int)myFile.length();    // Initializes integer variable with file size
                String[] tmp = path.split("\\\\");  // Initializes string array with file path parsed by '\\'
                int max = tmp.length;   // Initializes integer variable with number of elements in string array tmp
                writer.println(tmp[max-1]+"&"+size+"&"+username+"&Upload"); // PrintWriter object writes to server, name of file selected, size of file, username of client uploading, and upload command
                writer.flush(); // Flushes PrintWriter buffer
                byte[] mybytearray = new byte[size];    // Creates new byte array
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile)); // Creates BufferedInputStream object set to read from file
                bis.read(mybytearray, 0, mybytearray.length);   // Reads file into byte array
                OutputStream os = sock.getOutputStream();   // Creates new OutputStream object set to write to server through socket output stream
                os.write(mybytearray, 0, mybytearray.length);   // Output Stream object writes to server contents of byte array
                os.flush(); // Flushes OutputStream object buffer
                bis.close();    // Closes InputStream freading from file
            }
            else
            {
                JFrame frame = new JFrame("Error"); // Displays Error frame
                JOptionPane.showMessageDialog(frame, "File not selected!"); // Displays error message on frame
            }
            
        }
        catch(IOException ex)
        {
            JFrame frame = new JFrame("Error"); // Displays Error frame
            JOptionPane.showMessageDialog(frame, "Failed to select a File!"); // Displays error message on frame
        }
    }//GEN-LAST:event_b_uploadFileActionPerformed

    /**
     * Text field for adding a comment
     */ 
    private void tf_commentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tf_commentActionPerformed
        
    }//GEN-LAST:event_tf_commentActionPerformed

    /**
     * Button for viewing previous comments
     */ 
    private void b_PrevCmntsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b_PrevCmntsActionPerformed
        ta_PrevCmnts.setText("");   // Sets text of previous comments text area to empty
        if(fileName == "")  // If filename of file currently displayed is empty
        {
            JFrame frame = new JFrame("Error"); // Displays Error frame
            JOptionPane.showMessageDialog(frame, "Please have a file displayed prior to viewing comments"); // Displays error message on frame
        }
        else
        {
            String tmp[];
            tmp = fileName.split(":");  // Initializes string array with name of file displayed parsed by ':' 
            writer.println(tmp[1]+"&Get Comments&PrevComments");    // PrintWriter object writes to server fileID and command to get previous comments
            writer.flush(); // Flushes PrintWriter object buffer
        }
    }//GEN-LAST:event_b_PrevCmntsActionPerformed

    /**
     * Text field for chat messages
     */ 
    private void tf_chatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tf_chatActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tf_chatActionPerformed

    /**
     * Button that sends chat messages
     */ 
    private void b_sendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b_sendActionPerformed
        if ((tf_chat.getText()).equals(""))     // If text entered in chat textfield is empty
        {
            tf_chat.setText("");    // Set text of chat textfield to empty
            tf_chat.requestFocus(); // Resets focus to chat textfield
        } 
        else 
        {
            try 
            {
               writer.println(username + "&" + tf_chat.getText() + "&" + "Chat");   // PrintWriter object writes to server, username of user sending message, message sent, and command to Chat
               writer.flush(); // Flushes PrintWriter object buffer
            } 
            catch (Exception ex)
            {
                ta_chat.append("Message was not sent. \n"); // Appends message to chat textarea 
            }
            tf_chat.setText("");    // Sets chat textfield to empty
            tf_chat.requestFocus(); // Resets focus to chat textfield
        }
    }//GEN-LAST:event_b_sendActionPerformed

    /**
     * Button that adds Comment to comment file
     */ 
    private void b_addCmntActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b_addCmntActionPerformed
        String[] tmp; 
        tmp = fileName.split(":");  // Initializes string array with name of file parsed by ':'
        if ((tf_comment.getText()).equals(""))  // if text entered into comment textfield is empty
        {
            tf_comment.setText(""); // Sets comment textfield to empty 
            tf_comment.requestFocus();  // Resets focus to comment textfield
        } 
        else 
        {
            try 
            {
               writer.println(tmp[1]+"&"+username+"&"+ tf_comment.getText()+"&Comment");    // PrintWriter object writes to server displayed file's fileId, username of client adding comment, comment to be added, and command to comment  
               writer.flush(); // Flushes PrintWriter object buffer
            } 
            catch (Exception ex)
            {
                ta_chat.append("Message was not sent. \n");
            }
            tf_comment.setText(""); // Sets comment textfield to empty 
            tf_comment.requestFocus();  // Resets focus to comment textfield
        }
    }//GEN-LAST:event_b_addCmntActionPerformed

    /**
     * Button that displays selected file
     */ 
    private void b_newDsplyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b_newDsplyActionPerformed
        fileName = lst_AvailFiles.getSelectedValue();   // Initializes filename to name of file selected to be displayed 
        String[] tmp;
        tmp = fileName.split(":");  // Initializes string array with name of file to be displayed parsed by ':'
        writer.println(username+"&"+tmp[1]+"&Display"); // PrintWriter object writes to server username of client requesting to display file, the fileID, and command to Display
        writer.flush();     // Flushes PrintWriter object buffer
        Display.setText("");    // Sets textarea of display to empty
    }//GEN-LAST:event_b_newDsplyActionPerformed

    /**
     * Radio Button that displays list of Users' personal uploaded files
     */ 
    private void rb_myFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rb_myFilesActionPerformed
        DefaultListModel dlm = new DefaultListModel();  // Creates new list model object 
        for(String s:MYFiles)   // For number of elements in MYFiles
        {
            dlm.addElement(s);  // Adds string s as new element in list model 
        }
        lst_AvailFiles.setModel(dlm);   // Sets model of list to list model object dlm
        lst_AvailFiles.setVisible(true);    // Sets list of files to being visible
    }//GEN-LAST:event_rb_myFilesActionPerformed

    /**
     * Radio Button that displays list of Friends' personal uploaded files that user has permission to view
     */ 
    private void rb_othrUsrFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rb_othrUsrFilesActionPerformed
        writer.println(username+"&FriendsFiles");   // PrintWriter object writes to server username, and command to get fiel of friends user is eligible to view
        writer.flush(); // Flushes PrintWriter Object's buffer
    }//GEN-LAST:event_rb_othrUsrFilesActionPerformed

    /**
     * Button that adds a user to list of people able to view current file displayed
     */ 
    private void b_addViewerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b_addViewerActionPerformed
        boolean allowed = false;    // Boolean variable associated with whether or not a user is already allowed to view file displayed
        if(fileName.equals("")) // If fileName is empty
        {
            JFrame frame = new JFrame("Error"); // Displays Error frame
            JOptionPane.showMessageDialog(frame, "Please have a file displayed prior to adding a viewer"); // Displays error message on frame
        }
        else if(!username.equals(fileowner))    // If username of client doesn't equal fileowner's name
        {
            JFrame frame = new JFrame("Error"); // Displays Error frame
            JOptionPane.showMessageDialog(frame, "Only file owners can add a viewer"); // Displays error message on frame
        }
        else if(Allowed.isEmpty())  // If Allowed ArrayList is empty
        {
            DefaultListModel dlm = new DefaultListModel();  // Create new list model object 
            for(String fr:friends)  // For number of elements in friends ArrayList
            {
                dlm.addElement(fr); // List model dlm add string fr as new element
            }
            lst_viewers.setModel(dlm);  // Set model of list to list model object dlm
            addViewer.setVisible(true); // Sets frame to add a viewer to visible
        }
        else
        {
            DefaultListModel dlm = new DefaultListModel(); // Create new list model object   
            for(String fr:friends)  // For number of elements in friends ArrayList
            {
                for(String allow:Allowed)   // For number of elements in Allowed ArrayList
                {
                    if(!fr.equals(allow))   // If string fr equals string allow
                    {
                       continue;    // Continue loop to next element in Allowed ArrayList
                    }
                    else
                    {
                        allowed = true; // Meaning string fr is also in Allowed Arraylist so allowed is true
                        break;  // Break out of for loop
                    }
                }
                if(allowed == false)    // If allowed equals false Meaning string fr is not allowed to view file yet
                {
                    dlm.addElement(fr); // Add string fr as new element in list
                }
                else
                {
                    allowed = false;    // Reset boolean variable to false to test next element in friends ArrayList
                }
            }
            lst_viewers.setModel(dlm);  // Set model of list to list model object dlm 
            addViewer.setVisible(true); // Sets frame to add a viewer to visible
        }
        
    }//GEN-LAST:event_b_addViewerActionPerformed

    /**
     * Button that displays frame with list of friends that are not able to view file yet
     */ 
    private void b_addViewer2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b_addViewer2ActionPerformed
        String newViewer = lst_viewers.getSelectedValue();  // Initializes string variable with selected value from list
        if(newViewer == null) // If newViewer is empty or equals ""
        {
            JFrame frame = new JFrame("Error"); // Display error frame
            JOptionPane.showMessageDialog(frame, "No viewer selected!"); // Display error message on frame
        }
        else
        {
            String[] tmp;
            tmp = fileName.split(":");  // Initializes string array with filename parsed by ':'
            writer.println(newViewer+"&"+tmp[0]+"&"+tmp[1]+"&Viewer");  // PrintWriter Object writes to server name of newViewer, File name, FileID, and command to add viewer
            writer.flush(); // Flushes PrintWriter Object buffer 
            addViewer.dispose();    // Disposes of addViewer frame 
        }

    }//GEN-LAST:event_b_addViewer2ActionPerformed

    /**
     * Button that adds a Friend
     */ 
    private void b_addFriendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b_addFriendActionPerformed
        String newFriend = tf_addFriend.getText();  // Initializes string variable with text entered into textfield
        if(newFriend.equals(username))  // If text entered equals useranme of client
        {
            JFrame frame = new JFrame("Error"); // Displays Error frame
            JOptionPane.showMessageDialog(frame, "Friend to be added cannot be you!"); // Displays error message on frame
        }
        else if(newFriend.length() > 12)    // If text entered is greater than 12 characters
        {
            JFrame frame = new JFrame("Error"); // Displays Error frame
            JOptionPane.showMessageDialog(frame, "Username cannot be longer than 12 characters!"); // Displays error message on frame
        }
        else if(!newFriend.matches("[a-zA-z_!@$#?0-9]{2,12}"))  // If text entered does not follow this pattern
        {
            JFrame frame = new JFrame("Error"); // Display error frame
            JOptionPane.showMessageDialog(frame, "Friend name has unacceptable characters!"); // Display error message on frame
        }
        else
        {
            writer.println(username+"&"+newFriend+"&Friend");   // PrintWriter Object writes to server username of client, name of friend to be added and command to add friend
            writer.flush(); // Flushes PrintWriter Object buffer
            
        }
        
       
    }//GEN-LAST:event_b_addFriendActionPerformed

     /**
     * Actions that occurs when user exits frame
     */ 
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if(isConnected == true) // if client is still connected to server
        {
            Disconnect();   // Call Function to disconnect client from server
        }
        System.exit(0); // Exit program
    }//GEN-LAST:event_formWindowClosing

     /**
     * Button that reconnects a user to server should their be a disconnection
     */ 
    private void b_ReconnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b_ReconnectActionPerformed
        if(isConnected == false)    // If client is not connected to server
        {
            try
            {
                sock = new Socket(cli2.Addr,cli2.port); // Initializes socket object with new socket set to client's stored IP address and port number
                InputStreamReader streamreader = new InputStreamReader(sock.getInputStream());  // Creates new InputStreamReader object set to read from socket input stream
                reader = new BufferedReader(streamreader);  // Creates new BufferedReader that is set to buffer InputSreamReader object
                writer = new PrintWriter(sock.getOutputStream());   // Creates new PrintWriter Object set to write to socket output stream
                
                isConnected = true; // Means Client is connected to server
                ListenThread(); // Calls function that creates thread to listen to input sent from server
            }
            catch(IOException ex)
            {
               JFrame frame = new JFrame("Error"); // Displays Error frame
               JOptionPane.showMessageDialog(frame, "Server not Available!\nTry a different IP Address or Port"); // Display error message on frame
            }
            writer.println(username+"&has&Connected");  // PrintWriter Object writes to server username of client connected and command to tell others client connected
            writer.flush(); // Flushes PrintWriter object buffer
        }
    }//GEN-LAST:event_b_ReconnectActionPerformed

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Cli_Frame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Cli_Frame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Cli_Frame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Cli_Frame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Cli_Frame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea Display;
    private javax.swing.JFrame addViewer;
    private javax.swing.JButton b_PrevCmnts;
    private javax.swing.JButton b_Reconnect;
    private javax.swing.JButton b_addCmnt;
    private javax.swing.JButton b_addFriend;
    private javax.swing.JButton b_addViewer;
    private javax.swing.JButton b_addViewer2;
    private javax.swing.JButton b_newDsply;
    private javax.swing.JButton b_send;
    private javax.swing.JButton b_uploadFile;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JList<String> lst_AvailFiles;
    private javax.swing.JList<String> lst_viewers;
    private javax.swing.JRadioButton rb_myFiles;
    private javax.swing.JRadioButton rb_othrUsrFiles;
    private javax.swing.JTextArea ta_PrevCmnts;
    private javax.swing.JTextArea ta_chat;
    private javax.swing.JTextArea ta_friends;
    private javax.swing.JTextField tf_addFriend;
    private javax.swing.JTextField tf_chat;
    private javax.swing.JTextField tf_comment;
    // End of variables declaration//GEN-END:variables
}
