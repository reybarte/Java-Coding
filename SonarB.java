import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Topic;
import javax.jms.Destination;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.apache.activemq.ActiveMQConnectionFactory;
// add your own imports below

public class SonarB {

	// add any private vars below 'pfile'
	// add your own methods *below* get_time

	private String pbroker, pqueue, tqueue, puser, ppass; // properties
	private String pfile  = "USS.properties";     // must be in current dir

	//Boat name
	private String boat;

	public static void main(String[] args) throws Exception {

		if (args.length < 1) {
			System.err.println(
					" Usage: java Sonar <shipname>  ## Consumer mode\n");
			System.exit(2);
		}

		SonarB c = new SonarB(args[0]);
		c.run();

	} // main

	// load properties file
	public void loadprop(String filename) { 
		// ADC
		Properties prop = new Properties();
		try {
			FileInputStream in = new FileInputStream(filename);
			prop.load(in);
		} catch (Exception e){
			System.out.println("Error encountered with property file.");
			System.exit(3);
		}
		pbroker = prop.getProperty("broker");
		pqueue = prop.getProperty("pqueue");
		tqueue = prop.getProperty("tqueue");
		puser = prop.getProperty("username");
		ppass = prop.getProperty("password");
	}

	// this is the only Ctor permitted
	public SonarB(String a) {

		loadprop(pfile); //load information from pfile
		// other init stuff: ADC
		boat = a;

	} // ctor CX()

	// the actual worker, do not change the signature!
	int run() throws javax.jms.JMSException
	{
		Connection cx=null; Session sx=null;    // NOC
		ConnectionFactory cf=null;              // NOC
		// ADC additional vars here if needed
		try {
			cf = new ActiveMQConnectionFactory(pbroker);
			cx = cf.createConnection(puser, ppass);
			sx = cx.createSession(false, Session.AUTO_ACKNOWLEDGE); // NOC
			// createSession should not change

			// make Destination, Consumer
			Destination destQ = sx.createQueue(pqueue);    		// ADC, must use session.createXXXXXX
			Destination destT = sx.createTopic(tqueue);
			MessageConsumer consQueue = sx.createConsumer(destQ, "boat ='" + boat + "'");	// ADC, must use session.createConsumer
			MessageConsumer consTopic = sx.createConsumer(destT); 
			// start consumer
			cx.start();
			System.out.println("*DRILL *DRILL *DRILL USS " + boat + " Sonar connected to: " + pbroker);
			System.out.println("*DRILL *DRILL *DRILL USS " + boat + " at " + get_time() + ": READY");
			while (true) {        // NOC: while loop as is

				// ADC - loop body to get all messages
				TextMessage msg;
				if((msg = (TextMessage)consQueue.receive(1000)) != null){}
				else{
					msg = (TextMessage)consTopic.receive(1);	
				}
				if (msg != null) {
					String order = msg.getText();
					String signature = msg.getStringProperty("signature");
					// break gracefully when done
					if (order.equals("END")){
						if(isAuthentic(order, signature)) {
							break;	
						}
					}
					String orderCopy = order;
					if(signature == null || !isAuthentic(order, signature)) {
						orderCopy += " (NOT AUTHENTIC,";
					} else {
						orderCopy += " (Authentic,";	
					}
					//note - must use isValid() to validate message
					if(!isValid(order)){
						orderCopy += " BAD)";
					} else {
						orderCopy += " OK)";	
					}
					System.out.println(get_time() + " " + boat + ": " + orderCopy);
				}

			} // while

			// loop gracefully exited, print message
			System.out.println(get_time() + " " + boat + ": **TERMINATE DRILL");
			// and fall thru, gracefully, to "finally"

		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			// ADC - to close resources etc
			sx.close();
			cx.close();		
		} // try {} ends here

		return 1; // add appropriate return here
	} // run() run - Consumer

	// ADC isValid - but do not change the signature
	public boolean isValid(String message)
	{
		// ADC -- add your code here!
		if (message.length() < 33){
			return false;
		}
		// check for boilerplate message
		if (!message.substring(0, 25).equals("EAM DRILL-TARGET-PACKAGE-")) {
			return false;
		}
		// check for alpha numeric code
		String target_package = message.substring(25, 31);
		if (!target_package.matches("^[a-zA-Z0-9]{6,}$")) {
			return false;
		}
		// check for slash and number
		if (!message.substring(31, 32).equals("/")) {
			return false;
		}
		// check if there is a space (and thus optional message)
		String rest_of_msg = message.substring(32);
		int next_space = rest_of_msg.indexOf(" ");
		boolean has_opt_msg = true;
		if (next_space == -1) {
			// there is no space, no optional message
			next_space = rest_of_msg.length();
			has_opt_msg = false;
		}
		// check reference_number
		String ref_num = rest_of_msg.substring(0, next_space);
		if (ref_num.length() > 3) {
			return false;
		}
		// verify reference_number is an integer less than or equal to 15
		try {
			int ref_num_int = Integer.parseInt(ref_num);
			if (ref_num_int > 15) {
				return false;
			}
		} catch (NumberFormatException exc) {
			return false;
		}
		// nothing else to check if there is no optional message
		if (!has_opt_msg) {
			return true;
		}
		// verify optional message is 20 characters less
		String opt_msg = rest_of_msg.substring(next_space + 1);
		if (opt_msg.length() > 20) {
			return false;
		}
		// verify optional message is alphanumeric
		if (!opt_msg.matches("^[a-zA-Z0-9-]{1,20}")) {
			return false;
		}

		return true;
	}
	boolean isAuthentic(String msg, String signature) {
		Auth mykey = new Auth("AABECZT");
		String msgSignature = mykey.sign(msg);
		if( msgSignature.equals(signature)) {
			return true;	
		}
		else {
			return false;	
		}
	}


	String get_time() {
		// ADC use SimpleDateFormat to obtain the desired timestamp
		// This method comes in handy above when printing timestamp/s
		SimpleDateFormat sdformat = new SimpleDateFormat("MM-dd-yy HH-mm-ss");
		return sdformat.format(new Date());
	}

}

