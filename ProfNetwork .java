/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Scanner;
import javax.xml.stream.events.StartDocument;

import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class ProfNetwork {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of ProfNetwork
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public ProfNetwork (String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end ProfNetwork

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
	 if(outputHeader){
	    for(int i = 1; i <= numCol; i++){
		System.out.print(rsmd.getColumnName(i) + "\t");
	    }
	    System.out.println();
	    outputHeader = false;
	 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
          List<String> record = new ArrayList<String>();
         for (int i=1; i<=numCol; ++i)
            record.add(rs.getString (i));
         result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       if(rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            ProfNetwork.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      ProfNetwork esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the ProfNetwork object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new ProfNetwork (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
               
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. Manage Friends");
                System.out.println("2. Update Profile");
                System.out.println("3. Messenger");
                
                
                System.out.println(".........................");
                System.out.println("9. Log out");
                switch (readChoice()){
                   case 1: FriendList(esql, authorisedUser); break;
                   case 2: UpdateProfile(esql, authorisedUser); break;
                   case 3: messenger(esql,  authorisedUser);break;
                                    
                   
                   case 9: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    * An empty block and contact list would be generated and associated with a user
    **/
   public static void CreateUser(ProfNetwork esql){
      try{
         //System.out.println("\t Enter Name");
         //String name = in.readLine();
         System.out.print("\tEnter username : ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user email: ");
         String email = in.readLine();
         //System.out.println("Enter Date of Birth");
         //String dob = in.readLine();
	 //Creating empty contact\block lists for a user
	 String query = String.format("INSERT INTO USR (userId, password, email,) VALUES ('%s','%s','%s')", login, password, email);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end

   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(ProfNetwork esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USR WHERE userId = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0)
		return login;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end
   /// MANAGE FRIEND LIST
   public static void FriendList(ProfNetwork esql, String currentUser){
      try{
         
         boolean FL = true;
         while(FL){
            System.out.println("****************************");
            System.out.println("Manage Friends Menu ");
            System.out.println("****************************");
            System.out.println("1. Add Friend");
            System.out.println("2. Manage Friend Requests");
            System.out.println("3. Remove Friend");
            System.out.println("4. Search People");
            System.out.println("5. Go to Friend List");
            System.out.println("9. Go back");
            System.out.println("****************************");
            switch(readChoice()){
               case 1:System.out.println("Enter the username you want to add");
                      String user = in.readLine();
                      SendRequest(esql, currentUser, user); break;
               case 2:ManageFriendRequest(esql, currentUser); break;
               case 3:RemoveFriend(esql, currentUser); break;
               case 4:SearchPeople(esql, currentUser); break;
               case 5:Friends(esql, currentUser); break; 
               case 9: FL = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }
         }
         }catch(Exception e){
           System.err.println(e.getMessage());
         }
    }
   public static void Friends(ProfNetwork esql, String currentUser) {
      try{
         String query = String.format("select * from Connection where status = 'Accept' AND userid ='%s'",currentUser);
         esql.executeQueryAndPrintResult(query);
         boolean stay = true;
         while(stay){
            System.out.println("----------------------------------------------");
            System.out.println("Friends Menu");
            System.out.println("----------------------------------------------");
            System.out.println("1. Go to Friends Profile");
            System.out.println("9. Go back");
            System.out.println("----------------------------------------------");
            switch(readChoice()){
               case 1:System.out.println("Enter Friends Username");
                      String friend = in.readLine(); 
                      friendProfile(esql, currentUser,friend);break;
               case 9: stay = false;break;
            }
         }
      }catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   private static void friendProfile(ProfNetwork esql, String currentUser, String friend) {
      try{
         
         String check = String.format("select connectionid from Connection where connectionid ='%s' and userid = '%s' or connectionid ='%s' and userid = '%s' ", friend,currentUser,friend,currentUser);
         int num = esql.executeQuery(check);
         if (num > 0){
            boolean FP = true;
            while(FP){
               System.out.println("-----------Friends Profile-------------");
               System.out.println("1. View Profile");
               System.out.println("2. Send Message");
               System.out.println("3. View Friends");

               System.out.println("9. Go back");

               switch(readChoice()){
                  case 1:String query = String.format("select U.name, U.email, U.userid, W.company, W.role from USR U, Work_Ex W  where U.userid = '%s' and W.userid = '%s'", friend, friend);
                         int num1 = esql.executeQuery(query);
                         if (num1>0){
                           esql.executeQueryAndPrintResult(query);
                        }
                         else{
                            System.out.println("Profile Does not Exist");
                         }
                  case 2: NewMessage(esql, currentUser, friend);
                  case 3:viewFriendsOfFriends(esql,currentUser, friend); break;
                  case 9: FP = false; break;
                  default:System.out.println("Invalid Input.");
            
               }
            }
                       
         }else{
            System.out.println("They are not friends");
         }
         

      }catch(Exception e){
         System.err.println(e.getMessage());
      }
   }

   private static void viewFriendsOfFriends(ProfNetwork esql, String currentUser, String friend) {
      try{
         String query = String.format("select U.name,C.connectionid from Connection C, USR U where userid = '%s' and status = 'Accept' and C.connectionid = U.userid", friend);
         int num = esql.executeQuery(query);
         if(num < 1){
            System.out.println("User does not have Friends");
         }else{
            esql.executeQueryAndPrintResult(query);
            boolean FF = true;
            while(FF){
               System.out.println("---------------------------");
               System.out.println("1. View Profile");
               System.out.println("2. Send Message");
               System.out.println("3. Go to Friend of Friend's profile");
               System.out.println("4. Add this person as friend");
               System.out.println("---------------------------");
               System.out.println("9. Go back");

               switch(readChoice()){
                  case 1:String query1 = String.format("select U.name, U.dateofbirth, U.userid, W.company, W.role, E.major, E.degree from USR U, Work_Ex W, Edu_det E  where U.userid = '%s' and W.userid = '%s'", friend, friend);
                        int num1 = esql.executeQuery(query1);
                        if (num1>0){
                        String q1 = String.format("select * from connection where userid = '%s' and connectionid = '%s' or userid = '%s' and connectionid='%s'",currentUser,friend,friend,currentUser);
                        int num2 = esql.executeQuery(q1);
                        if (num2>0){
                           esql.executeQueryAndPrintResult(query1);
                         }else{ 
                           String q2 = String.format("select U.name,E.major,E.degree,W.company,W.role from USR U, Work_Ex W, Edu_det E where U.userid = '%s' and W.userid = '%s' and E.userid = '%s'",friend,friend,friend);
                           esql.executeQueryAndPrintResult(q2);
                        }
                           
                        }

                         else{
                           System.out.println("Profile Does not Exist");
                         }
                         break;
                  case 2: NewMessage(esql, currentUser, friend);break;
                  case 3:System.out.println("Enter Username of the profile you want to visit");
                         String visit = in.readLine();
                         viewFriendsOfFriends(esql, currentUser, visit);
                         break;
                  case 4: SendRequest(esql, currentUser,friend);break;
                  case 9:FF = false; break;
                  default: System.out.println("Invalid choice");
               }
            }
         }
      }catch(Exception e){

      }
   }

   public static void SendRequest(ProfNetwork esql, String currentUser, String user){
      try{
            if(connectionDepthcheck(esql,currentUser,user)){
               String reqTo = String.format("insert into connection (userid,connectionid,status) values ('%s','%s','Request')", user, currentUser);
               String reqFrom = String.format("insert into connection (userid,connectionid,status) values ('%s','%s','Request')", currentUser, user);
               esql.executeQuery(reqTo);
               esql.executeQuery(reqFrom);
               System.out.println("Request Sent Successfully");
            }
            
      }catch(Exception e){
         System.err.println(e.getMessage());
         }
   
   } 
   private static boolean connectionDepthcheck(ProfNetwork esql, String currentUser, String user) {
      try{
         boolean status = false;
         String query = String.format("SELECT count(*) FROM connection_usr WHERE userid = '%s' AND status = 'Accept' OR connectionid = '%s' AND status = 'Accept'", currentUser, currentUser);
         int count = esql.executeQuery(query);
         if (count < 4){
            return true;
         else{
            if 
         }
      }
      return false;
      } catch(Exception e){
         return false;
      } 
   }

   public static void ManageFriendRequest(ProfNetwork esql, String currentUser){
      try{
         String query = String.format("select U.name,C.connectionid, C.status from Connection C, USR U where C.userid = '%s' AND C.connectionid = U.userid AND status ='Request'",currentUser);
         esql.executeQueryAndPrintResult(query);
         System.out.println("Enter the Username of a Friend you want to Accept or Reject : ");
         String username = in.readLine();
         System.out.println("1. Accept");
         System.out.println("2. Reject");
         
         switch(readChoice()){
            case 1:String AcceptRequest = String.format("update Connection set status = 'Accept' where userid = '%s' AND connectionid = '%s'", currentUser,username);
                   String AcceptRequest1 = String.format("update Connection set status = 'Accept' where userid = '%s' AND connectionid = '%s'", username,currentUser);
                   esql.executeUpdate(AcceptRequest);
                   esql.executeUpdate(AcceptRequest1);
                   System.out.println("Friend Request Accepted");
                   break;
            case 2:String rejectRequest = String.format("update Connection set status = 'Reject' where userid = '%s' AND connectionid = '%s'", currentUser,username);
                   String rejectRequest1 = String.format("update Connection set status = 'Rejectt' where userid = '%s' AND connectionid = '%s'", username,currentUser);
                   esql.executeUpdate(rejectRequest);
                   esql.executeUpdate(rejectRequest1);
                   System.out.println("Friend Request Accepted");
                   break;
            default:System.out.println("Invalid Choice"); break;
         }
 
      }catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   public static void RemoveFriend(ProfNetwork esql, String currentUser){
      try{
         String query = String.format("select U.name,C.connectionid from Connection C, USR U where C.userid = '%s' AND status ='Accept' AND C.connectionid = U.userid",currentUser);
         esql.executeQueryAndPrintResult(query);
         System.out.println("Enter the Username of a Friend you want to Remove : ");
         String username = in.readLine();
         String DeleteRequest = String.format("delete from Connection where userid = '%s' and connectionid = '%s'", currentUser,username);
         String DeleteRequest1 = String.format("delete from Connection where userid = '%s' and connectionid = '%s'", username,currentUser);
         esql.executeQuery(DeleteRequest);
         esql.executeQuery(DeleteRequest1);
         System.out.println("Friend Request Accepted");
      }catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
    //Search People Function                                                                                                                \
   //
   //
   //
   //
   public static void SearchPeople(ProfNetwork esql, String currentUser){
      try{
         System.out.print("\t Enter the Name of the Person : ");
         String SearchName = in.readLine();

        // String query = String.format("select name, userid, email from USR where name like '%'+'%s'+'%'", SearchName);
        String query =  "select name, userid, email from USR where name like %" + SearchName;
        int num = esql.executeQuery(query);
         if (num > 0){
            esql.executeQueryAndPrintResult(query);
            System.out.print("\t Do you want to Add Friend ? \t yes \t no");
            String check = in.readLine();
            if (check == "yes"){
               System.out.println("Enter Username");
               String user = in.readLine();
               SendRequest(esql, currentUser,user);
            }

         }
      }catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   //
   //
   //
   //
   //Search People Function Ends
    //
    //
    // MANAGE FRIEND LIST ENDS

    // UPDATE PROFILE BEGINS
   public static void UpdateProfile(ProfNetwork esql, String currentUser){
      try{
          boolean udProfile = true;
               while(udProfile) {
                 String q1 = String.format("select userid,name,email,dateofbirth from USR where userid = '%s'",currentUser);
                 esql.executeQueryAndPrintResult(q1);
                 System.out.println("****************************");
                 System.out.println("UPATE PROFILE MENU");
                 System.out.println("****************************");
                 
                 System.out.println("1. Change Password");
                 System.out.println("2. Add Work Experience");
                 System.out.println("3. Update Work Experience");
                 System.out.println("4. Add Educational details");
                 System.out.println("5. Update Educational Details");
                 System.out.println("6. Update Email");
                 System.out.println("7. Update Name");
                 System.out.println("8. Update Date of Birth");
                 System.out.println("9. Go back");
                 switch (readChoice()){
                    case 1: ChangePassword(esql, currentUser); break;
                    case 2: AddWorkExp(esql, currentUser); break;
                    case 3: UpdateWorkExp(esql, currentUser); break;
                    case 4: AddEduDet(esql, currentUser); break;
                    case 5: updateEduDet(esql, currentUser); break;
                    case 6: UpdateEmail(esql, currentUser); break;
                    case 7: updateName(esql, currentUser);break;
                    case 8: updateDate(esql, currentUser);break;
                    case 9: udProfile = false; break;
                    default : System.out.println("Unrecognized choice!"); break;
                 }
               }
       }catch(Exception e){
          System.err.println(e.getMessage());
       }
    }
 
   
 
   
   private static void updateDate(ProfNetwork esql, String currentUser) {
      try{
         System.out.println("Enter Date of Birth");
         String dob = in.readLine();
         String query = String.format("update usr set dateofbirth = '%s' where userid = '%s'",dob,currentUser);
         esql.executeUpdate(query);
         
      }catch(Exception e){
         System.err.println(e.getMessage());
      }
   }

   private static void updateName(ProfNetwork esql, String currentUser) {
      try{
         System.out.println("Enter Name");
         String name = in.readLine();
         String query = String.format("update usr set name = '%s' where userid = '%s'",name,currentUser);
         esql.executeUpdate(query);
         
      }catch(Exception e){
         System.err.println(e.getMessage());
      }
   }

   public static void ChangePassword(ProfNetwork esql, String currentUser){
      try{
         System.out.println("Enter New Password");
         String password = in.readLine();
         String query = String.format("update usr set password = '%s' where userid = '%s'",password,currentUser);
         esql.executeUpdate(query);
         System.out.print("\t Password changed");
      }catch(Exception e){
         System.err.println(e.getMessage());
      }
   }

   public static void AddWorkExp(ProfNetwork esql, String currentUser){
      try{
         System.out.println("Which Company do you work at ? ");
         String company = in.readLine();
         System.out.println("Whats your role ? ");
         String role = in.readLine();
         System.out.println("Where is your office located? ");
         String location = in.readLine();
         System.out.println("Enter Start Date? ");
         String startDate = in.readLine();
         System.out.println("Enter End Date");
         String endDate = in.readLine();
         String query = String.format("insert into Work_Ex (userid, company, role, location, startDate, endDate) values ('%s','%s','%s','%s','%s','%s')",currentUser, company , role, location, startDate, endDate);
         esql.executeUpdate(query);
         System.out.print("\t Work Experience Added");
      }catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   public static void UpdateWorkExp(ProfNetwork esql, String currentUser){
      try{
            String displayWorkEx = String.format("select * from Work_Ex where userid = '%s'", currentUser);
            esql.executeQueryAndPrintResult(displayWorkEx);
            System.out.println("Enter Compay");
            String company = in.readLine();
            System.out.println("Whats your role ? ");
            String role = in.readLine();
            System.out.println("Enter Start Date? ");
            String startDate = in.readLine();

            boolean upWorkEx = true;
            while(upWorkEx){
               System.out.println("\nUpdate Work Experience Menu");
               System.out.println("------------------------------------");
               System.out.println("1. Add/Update End Date");
               System.out.println("2. Add/Update Location");
               System.out.println("3. Remove Work Experience");
               System.out.println("------------------------------------");
               System.out.println("9. Go Back");

               switch(readChoice()){
                  case 1: updateWorkExEndDate(esql, currentUser, company, role, startDate);break;
                  case 2: updateWorkExLocation(esql, currentUser, company, role, startDate);break;
                  case 3: removeWorkEx(esql,currentUser, company, role, startDate);break;
                  case 9: upWorkEx = false; break;
                  default:System.out.println("Unrecognized Choice");
               }
            }            
         }catch(Exception e){
            System.err.println(e.getMessage());
      }
         
         
   }
   private static void removeWorkEx(ProfNetwork esql, String currentUser, String company, String role,String startDate) {
      try{
         String ifExists = String.format("select * from Work_Ex where userid = '%s' AND company = '%s' AND role = '%s' AND startDate = '%s'",currentUser, company , role, startDate);
         int res = esql.executeQuery(ifExists);
         if (res > 0){
            String query = String.format("delete from Work_Ex where userid = '%s' AND company = '%s' AND role = '%s' AND startDate = '%s'",currentUser, company , role, startDate);
            esql.executeUpdate(query);
            System.out.print("\t Work Experience removed");
         }else{
            System.out.println("Work Experience Does not Exist");
            }
      }catch(Exception e){
         System.err.println(e.getMessage());
      }
   }

   private static void updateWorkExLocation(ProfNetwork esql, String currentUser, String company, String role, String startDate) {
      try{
         System.out.println("Enter Location");
         String location = in.readLine();
         String ifExists = String.format("select * from Work_Ex where userid = '%s' AND company = '%s' AND role = '%s' AND startDate = '%s'",currentUser, company , role, startDate);
         int res = esql.executeQuery(ifExists);
         if (res > 0){
            String query = String.format("update Work_Ex set location = '%s' where userid = '%s' AND company = '%s' AND role = '%s' AND startDate = '%s'",location,currentUser, company , role, startDate);
            esql.executeUpdate(query);
            System.out.print("\t Work Experience Updated");
         }else{
            System.out.println("Pleas Add Work Experience");
            }
      }catch(Exception e){
         System.err.println(e.getMessage());
      }
   }

   private static void updateWorkExEndDate(ProfNetwork esql, String currentUser, String company, String role,String startDate) {
      try{
         System.out.println("Enter End Date");
         String endDate = in.readLine();
         String ifExists = String.format("select * from Work_Ex where userid = '%s' AND company = '%s' AND role = '%s' AND startDate = '%s'",currentUser, company , role, startDate);
         int res = esql.executeQuery(ifExists);
         if (res > 0){
            String query = String.format("update Work_Ex set endDate = '%s' where userid = '%s' AND company = '%s' AND role = '%s' AND startDate = '%s'",endDate,currentUser, company , role, startDate);
            esql.executeUpdate(query);
            System.out.print("\t Work Experience Updated");
         }else{
            System.out.println("Pleas Add Work Experience");
            }
      }catch(Exception e){
         System.err.println(e.getMessage());
      }
   }

   public static void AddEduDet(ProfNetwork esql, String currentUser){
      try{
         System.out.println("Enter Institution ");
         String institution = in.readLine();
         System.out.println("Enter Major ");
         String major = in.readLine();
         System.out.println("Enter degree");
         String degree = in.readLine();
         System.out.println("Enter Start Date? ");
         String startDate = in.readLine();
         System.out.println("Enter End Date");
         String endDate = in.readLine();
         String query = String.format("insert into Edu_det (userid, institutionname, major, degree, startDate, endDate) values ('%s','%s','%s','%s','%s','%s')",currentUser, institution , major, degree, startDate, endDate);
         esql.executeUpdate(query);
         System.out.print("\t Work Experience Added");
      }catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   public static void updateEduDet(ProfNetwork esql, String currentUser){
      try{     
            String displayEduDet = String.format("select * from Edu_det where userid = '%s'", currentUser);
            esql.executeQueryAndPrintResult(displayEduDet);   
            System.out.println("Enter Major");
            String major = in.readLine();
            System.out.println("Enter Degree ");
            String degree = in.readLine();
            
            boolean upEduDet = true;
            while(upEduDet){
               System.out.println("Update Education Details Menu");
               System.out.println("----------------------------------------------------");
               System.out.println("1. Update Start Date ");
               System.out.println("2. Update End Date");
               System.out.println("3. Remove Degree");
               System.out.println("----------------------------------------------------");
               System.out.println("9. Go back");
               switch(readChoice()){
                  case 1:updateStartDate(esql, currentUser, major,degree);break;
                  case 2:updateEndDate(esql, currentUser,major, degree);break;
                  case 3:removeDegree(esql, currentUser, major, degree);break;
                  case 9:upEduDet = false; break;
                  default : System.out.println("Unrecognized Choice"); break;
               }
            } 
         }catch(Exception e){
            System.err.println(e.getMessage());
      }
         
         
   }
   public static void updateStartDate(ProfNetwork esql, String currentUser, String major, String degree) {
      try{
         System.out.println("Enter Start Date :  ");
         String startDate = in.readLine();
         String ifExists = String.format("select * from Edu_det where userid = '%s' AND major = '%s' AND degree = '%s'", currentUser, major, degree);
         int res = esql.executeQuery(ifExists);
         if (res > 0){
            String query = String.format("update Edu_det set startDate = '%s' where userid = '%s' AND major = '%s' AND degree = '%s'",startDate,currentUser, major , degree);
            esql.executeUpdate(query);
            System.out.print("\n Educational Details Updated");
         }else{
            System.out.println("Education Details Does not Exist");
         }
      }catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   public static void updateEndDate(ProfNetwork esql, String currentUser, String major, String degree) {
      try{
         System.out.println("Enter End Date :  ");
         String endDate = in.readLine();
         String ifExists = String.format("select * from Edu_det where userid = '%s' AND major = '%s' AND degree = '%s'", currentUser, major, degree);
         int res = esql.executeQuery(ifExists);
         if (res > 0){
            String query = String.format("update Edu_det set endDate = '%s' where userid = '%s' AND major = '%s' AND degree = '%s'",endDate,currentUser, major , degree);
            esql.executeUpdate(query);
            System.out.print("\n Educational Details Updated");
         }else{
            System.out.println("Education Details Does not Exist");
         }
      }catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   public static void removeDegree(ProfNetwork esql, String currentUser, String major, String degree) {
      try{
         String ifExists = String.format("select * from Edu_det where userid = '%s' AND major = '%s' AND degree = '%s'", currentUser, major, degree);
         int res = esql.executeQuery(ifExists);
         if (res > 0){
            String query = String.format("delete from Edu_det  where userid = '%s' AND major = '%s' AND degree = '%s'",currentUser, major , degree);
            esql.executeUpdate(query);
            System.out.print("\n Educational Details Removed");
         }else{
            System.out.println("Education Details Does not Exist");
         }
      }catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   public static void UpdateEmail(ProfNetwork esql, String currentUser){
      try{
         System.out.println("Enter New Email");
         String email = in.readLine();
         String query = String.format("update usr set email = '%s' where userid = '%s'",email,currentUser);
         esql.executeUpdate(query);
         System.out.print("\n Email Updated");
      }catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   
   // MESSENGER STARTS
   public static void messenger(ProfNetwork esql, String currentUser) {
      try {
         
         boolean m = true;
         while(m){
            System.out.println("***************************************");
            System.out.println("         Messenger Menu");
            System.out.println("***************************************");
            System.out.println("1. Send Message");
            System.out.println("2. Inbox");
            System.out.println("3. Sent");
            System.out.println("9. Go back");
            System.out.println("***************************************");

            switch(readChoice()){
               case 1:System.out.println("Enter Username of Reciever");
                      String user = in.readLine(); 
                      NewMessage(esql, currentUser, user);break;
               case 2:viewRecievedMessage(esql, currentUser);break;
               case 3:viewSentMessage(esql, currentUser);break;
               case 9:m = false; break;
            }
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
      
   }

   public static void viewRecievedMessage(ProfNetwork esql, String currentUser){
      try{
         String query = String.format("select * from Message where receiverid='%s' and status ='Delivered'", currentUser);
         esql.executeQueryAndPrintResult(query);
         boolean delMsg = true;
         while(delMsg){
            System.out.println("1. Delete Message");
            System.out.println("2. View Message");
            System.out.println("9. Go back");

            switch(readChoice()){
               case 1: System.out.println("enter msgid of the message you want to delete"); String input = in.readLine();
               int mid = Integer.parseInt(input.trim());
      
                       deleteMessage(esql,mid);break;
               case 2: readMessage(esql,currentUser);
                       break;
               case 9: delMsg = false;break;
               default: System.out.println("Invalid choice");
            }
         }

      }catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   
   private static void readMessage(ProfNetwork esql, String currentUser) {
      try {
         System.out.println("Enter Messge id");
         String input = in.readLine();
         int mssgid = Integer.parseInt(input.trim());

         String query = String.format("select content from Messge where msgid = '%d'",mssgid);
         esql.executeQueryAndPrintResult(query);
         String q1 = String.format("update Message set status = 'Read' where msgid = '%d'", mssgid);
         esql.executeUpdate(q1);

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void viewSentMessage(ProfNetwork esql, String currentUser){
      try{
         String query = String.format("select * from Message where senderid='%s' and status ='Delivered'", currentUser,currentUser);
         esql.executeQueryAndPrintResult(query);
         boolean delMsg = true;
         while(delMsg){
            System.out.println("1. Delete Message");
            System.out.println("9. Go back");

            switch(readChoice()){
               case 1: System.out.println("enter msgid of the message you want to delete"); 
                       String input = in.readLine();
                       int mid = Integer.parseInt(input.trim());
                       deleteMessage(esql,mid);break;
               case 9: delMsg = false;break;
               default: System.out.println("Invalid choice");
            }
         }

      }catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   private static void deleteMessage(ProfNetwork esql, int mid) {
      try {
         String query = String.format("delete from Message where msgId = " + mid);
         esql.executeQuery(query);
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void NewMessage(ProfNetwork esql, String currentUser , String reciever){
      try {
         System.out.println("what's the message ?");
         String contents = in.readLine();
         String query = String.format("INSERT INTO Message (senderid,receiverid,contents,sendtime,deletestatus,status) values ( '%s', '%s' , '%s',CURRENT_TIMESTAMP, 0, 'Delivered';",currentUser,reciever,contents);
         esql.executeUpdate(query);
         
         
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }
   

  // MESSENGER ENDS
// Rest of the functions definition go in here

}//end ProfNetwork
