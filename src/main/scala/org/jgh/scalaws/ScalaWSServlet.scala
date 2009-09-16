package org.jgh.scalaws
//'_' is wildcard in scala
import scala.xml._
import javax.servlet.http._
import java.sql._

//Mixin inheritance. The DBAdapter trait is mixed into the UserDetailsServlet using 'with DBAdapter'
trait DBAdapter {
  def datasource:javax.sql.DataSource  //A dependency. Declared as abstract, needs to be supplied by classes that use this trait
  //consise. this line declares a class with two immutable fields (use var for variables) and a constructor
  class Param(val sqlType:Int,val value:Any)
  //object keyword declares a singleton. Singleton pattern is part of the language. lazily instanciated. No statics in scala.
  object Param {
    //Objects can be functions by implementing an apply method.
    def apply(s:String) = new  Param(Types.VARCHAR, s)
    def apply(x:NodeSeq) = new  Param(Types.VARCHAR, x.text)
    def apply(i:BigInt) = new  Param(Types.BIGINT, i)
  }

  //Functions are objects and can be passed as parameters . c is a function that takes a Conn as a parameter and returns a sequence of Nodes.
  def withConn(c:Conn => Seq[Node]):Seq[Node] = { //use [] for type parameters (generics).
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
      statement //return optional if not supplied the result of that last expression is returned.
    }
	def insert(sql:String, params:Param*):Int = {
	  prepareStatement(sql, params:_*).executeUpdate;
	}
    def select(query:String, params:Param*)(r:ResultSet => Seq[Node]):Seq[Node] = {
      val result = prepareStatement(query, params:_*).executeQuery;
      //nested function.  Scalac will actually do tail call optimisation on this recursive method. means no stack overflow
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
		//Xpath like queries on XML. the \ operator is actually a method. in \ "Body' same as in.\("Body")
		(in  \ "Body" \ "_").firstOption match {
			case Some(payload) => handlePayload(payload)
			case None => throw error("Payload element not found")
		}
    } catch {
		case _@e => e.printStackTrace //() optional on no args methods
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
  lazy val datasource = (new javax.naming.InitialContext).lookup("java:/comp/env/jdbc/test").asInstanceOf[javax.sql.DataSource]
  //Partially applied functions (currying) using multiple parameter set stntax. Calling mapUser(c) will return a function that takes a ResultSet and returns Seq[Node]
  def mapUser(c:Conn)(r:ResultSet):Seq[Node] =
    (<User id={"" + r.getLong("id")} firstName={r.getString("firstName")} lastName={r.getString("lastName")} >
     <Roles>
      {c.select("select * from role_t where owner = ?", Param(r.getLong("id"))) ((r2) => {
		 (<Role name={r2.getString("name")}><Description>{r2.getString("description")}</Description></Role>)
			})
      }</Roles>
     </User>)

  //pattern matching to extract data from objects
  def handlePayload(payload:Node):Node = payload match {
    case (<FindUsersRequest>{x}</FindUsersRequest>) => {
      val search = Param("%" + x + "%")
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
			  val params = Seq( Param( BigInt (payload \ "@id" text)),
                            Param( payload \ "@firstName"),
                            Param(payload \ "@lastName")                            )
			c.insert("insert into user_t (id, firstName, lastName) values ( ?, ?, ?)", params:_*) //_* tells compiler that params is already a Seq (vararg)
          (<Done/>)     //TODO:inserting roles left as exercise for reader
		})(0) //Get first node
    }
}