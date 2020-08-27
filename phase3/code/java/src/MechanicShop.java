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
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class MechanicShop{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public MechanicShop(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
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
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
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
	}
	
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
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
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
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
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
		if (rs.next()) return rs.getInt(1);
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
				"Usage: " + "java [-classpath <classpath>] " + MechanicShop.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		MechanicShop esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new MechanicShop (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. AddCustomer");
				System.out.println("2. AddMechanic");
				System.out.println("3. AddCar");
				System.out.println("4. InsertServiceRequest");
				System.out.println("5. CloseServiceRequest");
				System.out.println("6. ListCustomersWithBillLessThan100");
				System.out.println("7. ListCustomersWithMoreThan20Cars");
				System.out.println("8. ListCarsBefore1995With50000Milles");
				System.out.println("9. ListKCarsWithTheMostServices");
				System.out.println("10. ListCustomersInDescendingOrderOfTheirTotalBill");
				System.out.println("11. < EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddCustomer(esql); break;
					case 2: AddMechanic(esql); break;
					case 3: AddCar(esql); break;
					case 4: InsertServiceRequest(esql); break;
					case 5: CloseServiceRequest(esql); break;
					case 6: ListCustomersWithBillLessThan100(esql); break;
					case 7: ListCustomersWithMoreThan20Cars(esql); break;
					case 8: ListCarsBefore1995With50000Milles(esql); break;
					case 9: ListKCarsWithTheMostServices(esql); break;
					case 10: ListCustomersInDescendingOrderOfTheirTotalBill(esql); break;
					case 11: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

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
	
	public static void AddCustomer(MechanicShop esql){//1
	
	 String first_name;

        do {
                System.out.print("Enter First name: ");									
                try {
                        first_name = in.readLine();
			//Check if user input matches the constraints of the database
                        if(first_name.length() <= 0 || first_name.length() > 32) {
                                throw new RuntimeException("First name cannot be null or exceed 32 characters");			
                        }break; // Break out of the loop if user input is correct

                }catch (Exception e) {
                        System.out.println (e);
                        continue;
                }
        }while (true);

        String last_name;

        do {
                System.out.print("Enter Last name: ");
                try {
                        last_name = in.readLine();
			//Check if user input matches the constraints of the database
                        if(last_name.length() <= 0 || last_name.length() > 32) {
                        throw new RuntimeException("Last name cannot be null or exceed 32 characters");
                }break; // Break out of the loop if user input is correct

                }catch (Exception e) {
                        System.out.println(e);
                        continue;
                }
        }while (true);

        String ID;

        do {
		
                System.out.print("Enter Customer ID: ");
                try {
                        ID = in.readLine();
			// Check if ID is already in the database
                        String query = "SELECT id FROM Customer WHERE id=";
			query += ID + ";";
			int customerExists = esql.executeQuery(query); 
			// If ID exists in the database prompt the user to input a new ID
			if (customerExists != 0) throw new RuntimeException("Customer ID is in use, please enter a new ID.");
			//Check if user input matches the constraints of the database
			if(ID.length() <= 0) throw new RuntimeException("Customer ID cannot be null");
                        break; // Break out of the loop if user input is correct
                }catch (Exception e) {
                        System.out.println(e);
                        continue;
                }
        }while (true);

        String phone_num;

        do {
                System.out.print("Enter Phone number: ");
                try {
                        phone_num = in.readLine();
			//Check if user input matches the constraints of the database
                        if(phone_num.length() <= 0 || phone_num.length() > 13) {
                        throw new RuntimeException("Phone number cannot be null or exceed 13 characters");
                }break; // Break out of the loop if user input is correct

                }catch (Exception e) {
                        System.out.println(e);
                        continue;
                }
        }while (true);

        String address;

        do {
                System.out.print("Enter Address: ");
                try {
                        address = in.readLine();
			//Check if user input matches the constraints of the database
                        if(address.length() <= 0 || address.length() > 256) {
			throw new RuntimeException("Address cannot be null or exceed 256 characters");

                }break; // Break out of the loop if user input is correct
                }catch (Exception e) {
                        System.out.println(e);
                        continue;
                }
        }while (true);

        try {
		//Insert user inputs into the Customer table
                String query = "INSERT INTO Customer (id, fname, lname, phone, address) VALUES (" + ID + ", \'" + first_name + "\', \'" + last_name + "\',  \'" + phone_num  + "\',  \'" + address + "\' );";

                        esql.executeUpdate(query);
		// Display the new information added to the table
		System.out.println("------------------------------------------------");
				System.out.println("New Customer added.");
				query = "\\";
				query += "x";
				esql.executeUpdate(query);
				query = "SELECT id AS Customer_ID, fname AS First_Name, lname AS Last_Name, phone AS Phone_Number, address FROM Customer WHERE id='";
				query+= ID + "';";
				esql.executeQueryAndPrintResult(query);
				System.out.println("------------------------------------------------");
        }catch (Exception e) {
                System.err.println (e.getMessage());
        }



        }

        public static void AddMechanic(MechanicShop esql){//2

        String ID;

        do {
                System.out.print("Enter Employee ID: ");
                try {
                        ID = in.readLine();
			// Check if ID is already in the database
                        String query = "SELECT id FROM Mechanic WHERE id=";
			query += ID + ";";
			int customerExists = esql.executeQuery(query); 
			// If it exists then prompt the user to enter a new ID
			if (customerExists != 0) throw new RuntimeException("Employee ID is in use, please enter a new ID.");
			//Check if user input matches the constraints of the database
                        if(ID.length() <= 0) throw new RuntimeException("Employee ID cannot be null");
                        break; // Break out of the looop if user input is correct
                }catch (Exception e) {
                        System.out.println(e);
                        continue;
                }
        }while (true);


        String first_name;

        do {
                System.out.print("Enter First name: ");
                try {
                        first_name = in.readLine();
			//Check if user input matches the constraints of the database
                        if(first_name.length() <= 0 || first_name.length() > 32) {
                                throw new RuntimeException("First name cannot be null or exceed 32 characters");
                        }break; // Break out of the loop if user input is correct

                }catch (Exception e) {
                        System.out.println (e);
                        continue;
                }
        }while (true);

        String last_name;

        do {
                System.out.print("Enter Last name: ");
                try {
                        last_name = in.readLine();
			//Check if user input matches the constraints of the database
                        if(last_name.length() <= 0 || last_name.length() > 32) {
                        throw new RuntimeException("Last name cannot be null or exceed 32 characters");
                }break; // Break out of the loop if user input is correct

                }catch (Exception e) {
                        System.out.println(e);
                        continue;
                }
        }while (true);

	int exp;

        do {
                System.out.print("Enter employee's experience(no. of years): ");
                try {
                        exp = Integer.parseInt(in.readLine());
			//Check if user input matches the constraints of the database
                        if(exp < 0 || exp >= 100) throw new RuntimeException("Employee's experience cannot be null or greater or equal to 100");
                        break; // Break out of the loop if user input is correct
                }catch (Exception e) {
                        System.out.println(e);
                        continue;
                }
        }while (true);

        try {
		// Insert user inputs into the Mechanic table
                String query = "INSERT INTO Mechanic (id, fname, lname, experience) VALUES (" + ID + ", \'" + first_name + "\', \'" + last_name + "\',  " + exp  + " );";

                        esql.executeUpdate(query);
		// Display the new information added to the table
		query = "\\";
				query += "x";
				esql.executeUpdate(query);
		System.out.println("------------------------------------------------");
				System.out.println("New Mechanic added.");
				query = "SELECT id AS employee_id, fname AS First_Name, lname AS Last_Name, experience FROM Mechanic WHERE id='";
				query+= ID + "';";
				esql.executeQueryAndPrintResult(query);
				System.out.println("------------------------------------------------");
        }catch (Exception e) {
                System.err.println (e.getMessage());
        }
		
	}
		
	public static void AddCar(MechanicShop esql){//3
	
	    String in1;
                do{
                        System.out.print("\tEnter VIN: ");
			
                        try{
				in1 = in.readLine();
				/*// Check if VIN is already in the database
                       		String query = "SELECT vin FROM Car WHERE vin=";
				query += in1 + ";";
				int carExists = esql.executeQuery(query); 
				// If it is then prompt the user to input a new VIN
				if (carExists != 0) throw new RuntimeException("Car VIN is in use, please enter a new VIN.");  */
				//Check if user input matches the constraints of the database
                                if(in1.length() <= 0 || in1.length() > 16) {
                                throw new RuntimeException("VIN cannot be null or exceed 16 characters");
                                }
                                break; // Break out of the loop if user input is correct
                        } catch(Exception e) {
                                System.out.println(e);
                                continue;
                        }
                } while(true);

                String in2;
                do{
                        System.out.print("\tEnter make: ");
                        try{
                                in2 = in.readLine();
				//Check if user input matches the constraints of the database
                                if(in2.length() <=0 || in2.length() > 32) {
                                        throw new RuntimeException("Make cannot be null or exceed 32 characters");
                                }
                                break; // Break out of the loop if user input is correct
                        } catch(Exception e) {
                                System.out.println(e);
                                continue;
                        }
                } while(true);

                String in3;
                do{
                        System.out.print("\tEnter model: ");
                        try{
                                in3 = in.readLine();
				//Check if user input matches the constraints of the database
                                if(in3.length() <= 0 || in3.length() > 32) {
                                        throw new RuntimeException("Model cannot be null or exceed 32 characters");
                                }
                                break; // Break out of the loop if user input is correct
                        } catch(Exception e) {
                                System.out.println(e);
                                continue;
                        }
                } while(true);

                int in4;
                do{
                        System.out.print("\tEnter year: ");
                        try{
                                in4 = Integer.parseInt(in.readLine());
				//Check if user input matches the constraints of the database
                                if(in4 < 1970) {
                                        throw new RuntimeException("Year cannot be less than 1970");
                                }
                                break; // Break out of the loop if user input is correct
                        } catch(Exception e) {
                                System.out.println(e);
                                continue;
                        }
                } while(true);

                try{
			// Inser user inputs into the Car table
                        String query = "INSERT INTO Car(vin, make, model, year) VALUES(\'" + in1 + "\',\'" + in2 + "\',\'" + in3 + "\'," + in4 +")";
                        esql.executeUpdate(query);
			query = "\\";
				query += "x";
				esql.executeUpdate(query);
               		// Display new information added to the database
			System.out.println("------------------------------------------------");
                                System.out.println("New Car added.");
                            query = "SELECT * FROM Car WHERE vin='";
                                query+= in1 + "';";
			esql.executeQueryAndPrintResult(query);
                                System.out.println("------------------------------------------------");
			
		}	catch(Exception e) {
                        System.err.println(e.getMessage());
              		
       		 }

	}
		
	
	
	public static void InsertServiceRequest(MechanicShop esql){//4
       		try{
        		
			// todaysdate will be the current date when the user creates a service request
   			Date date = new Date();
			SimpleDateFormat DateFor = new SimpleDateFormat("MM/dd/yyyy");
			String todaysdate = DateFor.format(date);
			String cust_ID = "", car_ID = "";
			System.out.print("Enter the last name of the customer: ");
                        String lastName = in.readLine();
			// Search for the last name in Customer table and display matching results
			String query = "SELECT * FROM Customer WHERE lname='";
			query += lastName + "';";
			esql.executeQueryAndPrintResult(query);
			int customerExists = esql.executeQuery(query); 
			// If there are matching customers in the database prompt the user to select an existing customer from the
			// results or create a new customer 
			if (customerExists != 0){
				String user_input; 
				do{
					System.out.println("Choose an option below:\n 1. Select an existing customer\n 2. Create a new customer\n ");
					user_input = in.readLine();
					switch(user_input) {
						case "1":
						System.out.println("Enter the customer ID: ");
                        			cust_ID = in.readLine();
						break;
						case "2":
						// To create a new customer call the add customer function
						AddCustomer(esql);
						// Prompt the user to reenter the customer ID to update the cust_ID variable in this function
						System.out.println("Reenter the customer ID: ");
						cust_ID = in.readLine();
						break;
						default :
						System.out.println("Invalid input");
					}
				} while (!(user_input.equals("1")) && !(user_input.equals("2")));
			}
			else{ 
				// Since there are no results matching that last name prompt the user to add a new customer
				System.out.println("There are no customers with that last name. Please add a new customer.");
				AddCustomer(esql);
				// Prompt the user to reenter the customer ID to update the cust_ID variable in this function
				System.out.println("Reenter the customer ID: ");
				cust_ID = in.readLine();
			}	
			
			// Check if the customer owns any cars from the Owns table
			query = "SELECT * FROM Owns WHERE customer_id='";
			query += cust_ID + "';";
				
			esql.executeQueryAndPrintResult(query);
		
			int carExists = esql.executeQuery(query); 
			
			// If customer owns any number of cars, then prompt the user to select a VIN from the matching results
			if (carExists !=0){
				System.out.println("Enter the VIN: ");
				car_ID = in.readLine();
			}
			else { 
				// If customer doesn't own any cars then prompt the user to add a new car
				System.out.println("The customer doesn't own a car. Please add a new car.");
				AddCar(esql);
				// Update the car_ID with the new VIN created by the user
				System.out.println("Reenter the VIN: ");
				car_ID = in.readLine();
				/*query = "SELECT COUNT(ownership_id) FROM Owns";
				int maxOwnership = esql.executeQuery(query);
				query = "INSERT INTO Owns(ownership_id, customer_id, car_vin) VALUES ( " + maxOwnership  + ", " + cust_ID + "," + car_ID + ");";
				esql.*/
			}	
			
			/*query = "SELECT * FROM Owns WHERE car_vin='";
			query += car_ID + "' AND customer_id='";
			query += cust_ID + "';";*/
			
				
				// Insert user inputs into the Service_Request table
				query = "INSERT INTO Service_Request(rid, customer_id, car_vin, date, odometer, complain) VALUES ('";
				System.out.println("Enter the Service Request ID: ");
				int rid = Integer.parseInt(in.readLine());
				query += rid + "', '";
				query += cust_ID + "', '" + car_ID + "','" + todaysdate + "', '";
				System.out.println("Enter the odometer reading: ");
				String odometer = in.readLine();
				query += odometer + "', '";
				System.out.println("What is the issue? ");
				String complain = in.readLine();
				query += complain + "');";
						
				esql.executeUpdate(query);
				query = "\\";
				query += "x";
				esql.executeUpdate(query);
				
				// Display the new information added to the database
				System.out.println("------------------------------------------------");
				System.out.println("New service request created.");
				query = "SELECT * FROM Service_Request WHERE rid='";
				query+= rid + "';";
				esql.executeQueryAndPrintResult(query);
				System.out.println("------------------------------------------------");
	
		
		} catch(Exception e){
				System.err.println(e.getMessage());
		}
	
	}
	
	public static void CloseServiceRequest(MechanicShop esql) throws Exception{//5
		try{
                     
			String wid,rid, mid, comments, query;
	
			// closing date is the current date when the user closes a service request
   			Date date = new Date();
                        SimpleDateFormat DateFor = new SimpleDateFormat("MM/dd/yyyy");
                        String closingdate = DateFor.format(date);
			int bill;
			do {
                		System.out.print("Enter the service request ID: ");
               			 try {
                      		 	rid = in.readLine();
					// Check if the rid entered by the user exists in the database
					query = "SELECT * FROM Service_Request WHERE rid = " + rid + ";";
					int rid_exists = esql.executeQuery(query);
					// If the rid doesn't exist then inform the user that the rid is invalid
					if(rid_exists == 0) {
						throw new RuntimeException("Service Request does not exist");
					}break;
       
             	   		}catch (Exception e) {
                   	     		System.out.println (e);
                   	     		continue;
           	    	 	}
			}while (true);
		
			do {
                		System.out.print("Enter the Employee's ID: ");
              		  	try {
                        		mid = in.readLine();
					// Check if the mid entered by the user exists in the database
                       			query = "SELECT * FROM Mechanic WHERE id = " + mid + ";";
					int mid_exists = esql.executeQuery(query);
					// If the mid doesn't exist then inform the user that the mid is invalid
					if(mid_exists == 0) {
						throw new RuntimeException("Mechanic does not exist");
					}break;

				}catch (Exception e) {
                        		System.out.println(e);
                       			continue;
               		 	}
        		}while (true);
		
		
			System.out.println("Enter the Close request ID: ");
			wid = in.readLine();
			System.out.println("Any comments?");
			comments = in.readLine();
			System.out.println("What's the total amount due? ");
			bill = Integer.parseInt(in.readLine());
			// Insert user inputs into the Closed_Request table
			query = "INSERT INTO Closed_Request(wid, rid, mid, date, comment, bill) VALUES (" + wid + ", " + rid + "," + mid + ", '" + closingdate + "' , '" + comments +"', " + bill + ");" ;
			esql.executeUpdate(query);
			// Display the new information added to the database
			System.out.println("------------------------------------------------");
			System.out.println("Service request closed.");
			query = "SELECT wid AS close_request_id, rid AS service_request_id, mid as employee_id, date AS closing_date, comment as comments, bill FROM Closed_Request WHERE wid='";
			query+= wid + "';";
			esql.executeQueryAndPrintResult(query);
			System.out.println("------------------------------------------------");

		}catch(Exception e){
			System.err.println(e.getMessage());
		}


		      
	}
	
	public static void ListCustomersWithBillLessThan100(MechanicShop esql){//6
		try{
			// Display customers who have bills less than $100
			String query = "EXPLAIN SELECT c.fname AS First_Name, c.lname AS Last_Name, c.id AS Customer_ID, b.bill FROM Customer c, Service_Request a, Closed_Request b WHERE c.id = a.customer_id AND a.rid = b.rid AND b.bill < 100;";
			System.out.println("------------------------------------------------");
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
			System.out.println("------------------------------------------------");
		}
	 	catch(Exception e){
			System.err.println(e.getMessage());
		}	
	}
	
	public static void ListCustomersWithMoreThan20Cars(MechanicShop esql){//7
		try{
			// Display customers who own more than 20 cars
                        String query = "EXPLAIN SELECT fname AS First_Name, lname AS Last_Name, COUNT(a.ownership_id) FROM Customer, Owns a WHERE id IN (SELECT customer_id FROM Owns GROUP BY customer_id HAVING COUNT(customer_id) > 20) AND id = a.customer_id";
			System.out.println("------------------------------------------------");
                        int rowCount = esql.executeQuery(query);
                        esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
			System.out.println("------------------------------------------------");
                } catch(Exception e) {
                        System.err.println(e.getMessage());
                }
	
	}
	
	public static void ListCarsBefore1995With50000Milles(MechanicShop esql){//8
		try{
			// Display cars before 1995 that have 50000 miles odometer reading
			String query = "SELECT DISTINCT vin, make AS Make, model AS Model, year AS Year FROM Car a, Service_Request b WHERE year < 1995 and b.car_vin = a.vin and b.odometer < 50000;";
			System.out.println("------------------------------------------------");
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
			System.out.println("------------------------------------------------");
		}
	 	catch(Exception e){
			System.err.println(e.getMessage());
		}	
	}
	
	public static void ListKCarsWithTheMostServices(MechanicShop esql){//9
		//
		try{
			// Display the first K cars with the most service requests. K is an integer that the user inputs.
			String query = "SELECT make, model, a.number_of_requests FROM Car c, (SELECT car_vin, COUNT(rid) AS number_of_requests FROM Service_Request GROUP BY car_vin ) AS a WHERE a.car_vin = c.vin ORDER BY a.number_of_requests DESC LIMIT "	;
			System.out.println("Enter the number of cars you want to view: ");
			String num = in.readLine();
			query += num + ";";
			System.out.println("------------------------------------------------");
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
			System.out.println("------------------------------------------------");
		}
	 	catch(Exception e){
			System.err.println(e.getMessage());
		}	
	}
	
	public static void ListCustomersInDescendingOrderOfTheirTotalBill(MechanicShop esql){//10
		//
		try{
			// Display customers in descending order of their total bill
			String query = "EXPLAIN SELECT a.fname AS first_name, a.lname AS last_name, Total_Bill FROM Customer a,(SELECT sr.customer_id, SUM(cr.bill) AS Total_Bill FROM Closed_Request cr, Service_Request sr WHERE cr.rid = sr.rid GROUP BY sr.customer_id) AS b WHERE a.id=b.customer_id ORDER BY b.Total_Bill DESC;";
			System.out.println("------------------------------------------------");
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
			System.out.println("------------------------------------------------");
		}
	 	catch(Exception e){
			System.err.println(e.getMessage());
		}			
	}
	
}
