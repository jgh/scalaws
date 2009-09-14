package org.jgh.scalaws
//'_' is wildcard in scala
import scala.xml._
import javax.servlet.http._
import javax.servlet._
import java.sql._
import javax.sql._
import javax.naming._

//Mixin inheritance
trait DBAdapter {
  def datasource:DataSource  //A dependency that is abstract needs to be supplied by classes that use this trait
  abstract case class Param(sqlType:Int, value:Any)
  case class StringParam(s:Any) extends Param(Types.VARCHAR, s.toString) 
  case class LongParam(s:BigInt) extends Param(Types.BIGINT, s)
  //Functions are objects and can be passed as parameters . c is a function that takes a Conn as a parameter and returns a sequence of Nodes. 
  def withConn(c:Conn => Seq[Node]):Seq[Node] = { //[] for type parameters (generics).
    val connection =  datasource.getConnection    
    val conn = new Conn(connection)    
    try {
    	c(conn)
    } finally { connection.close }    
  }  
  class Conn(c:Connection) {
    private def prepareStatement(sql:String, params:Param*) = {
      val statement = c.prepareStatement(sql)      
      params.toList.zipWithIndex.foreach(z => statement.setObject(z._2 +1 , z._1.value, z._1.sqlType))
      statement
    }
	def insert(sql:String, params:Param*):Int = {
	  prepareStatement(sql, params:_*).executeUpdate; 
	}   
    def select(query:String, params:Param*)(r:ResultSet => Seq[Node]):Seq[Node] = {
      val result = prepareStatement(query, params:_*).executeQuery;      
      //nested function
      def doRow(acc:Seq[Node], row:ResultSet):Seq[Node] = if (!row.next) acc else doRow(acc ++ r(row), row)  
      try {
    	  doRow(Seq(), result);
      } finally { result.close}      
    }
  }  
}
//Interact natively with java libraries. Here scala is being invoked by java as a servlet and scala is invoking java jdbc libraries
abstract class WebserviceServlet extends HttpServlet {
  //XML literal syntax checked by compiler. If xml is not wellformed the class will not compile. code can be embedded using {}
	def soapEnvelope(body:Node) = (<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
		<soapenv:Header/>
    	<soapenv:Body>{body}</soapenv:Body>
    </soapenv:Envelope>)

   override def doPost(req:HttpServletRequest, resp:HttpServletResponse) = {  
    //Type inference - No need to declare types. Most of the time the compiler can infer what that type a value is by how it is used.
    val response = try {
    	val in = XML.load( req.getInputStream) 
    	//Xpath like queries on XML. 
    	(in  \ "Body" \ "_").firstOption match {
    		case Some(payload) => handlePayload(payload)
    		case None => throw error("Payload element not found")
    	}
    } catch {
    	case _@e => e.printStackTrace
    	  (<soapenv:Fault>
         	<faultcode>soapenv:Server</faultcode>
         	<faultstring>{e}</faultstring>
         	<faultactor>soapenv:Server</faultactor>
         </soapenv:Fault>)      
    }    
    XML.write(resp.getWriter,soapEnvelope(response),"UTF-8", true,  null)    
   }
  def handlePayload(payload:Node):Node  
}

class UserDetailsWSServlet extends WebserviceServlet with DBAdapter {
  //lazy value - evaluated once when it is first used
  lazy val datasource = (new InitialContext).lookup("java:/comp/env/jdbc/test").asInstanceOf[DataSource]
  //Partially applied functions (currying) using multiple parameter set stntax. Calling mapUser(c) will return a function that takes a ResultSet and returns Seq[Node]   
  def mapUser(c:Conn)(r:ResultSet):Seq[Node] = 
    (<User id={"" + r.getLong("id")} firstName={r.getString("firstName")} lastName={r.getString("lastName")} >
     <Roles>
      {c.select("select * from role_t where owner = ?", LongParam(r.getLong("id"))) ((r2) => { 
    	 (<Role name={r2.getString("name")}><Description>{r2.getString("description")}</Description></Role>)
       	})
      }                                                                      	     	 
      </Roles>
     </User>)  
  
  //pattern matching to extract data from objects 
  def handlePayload(payload:Node):Node = payload match {
    case (<FindUsersRequest>{x}</FindUsersRequest>) => {
      val search = StringParam("%" + x + "%")
      (<Users xmlns="urn:www.scalaws.org:UserDetails:v1">
      	{withConn(c =>{
      	  c.select("select * from user_t where firstName like ? or lastName like ?", search,search)(mapUser(c))          	    
      	  })             
      	}      	
      </Users>)    
    }
    case (<AllUsersRequest/>) => {    	
    	(<Users xmlns="urn:www.scalaws.org:UserDetails:v1">
    	{withConn(c => c.select("select * from user_t")(mapUser(c))          	    
    	)}      	
    	</Users>)    
    }
    case (<User>{roles@_*}</User>) => withConn(c =>  { //Anonymous function
      	  val params = Seq( LongParam( BigInt (payload \ "@id" text)),
                            StringParam( payload \ "@firstName"),
                       		StringParam(payload \ "@lastName")                            )
      	  c.insert("insert into user_t (id, firstName, lastName) values ( ?, ?, ?)", params:_*) //_* tells compiler that params is already a Seq (vararg)
          (<Done/>)     //inserting roles left as exercise for reader 	      
    	})(0) //Get first node    
    }    
}