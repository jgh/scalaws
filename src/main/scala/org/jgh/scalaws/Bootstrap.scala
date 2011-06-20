package org.jgh.scalaws

import javax.servlet.{ServletContextEvent, ServletContextListener}


class Bootstrap extends ServletContextListener with DBAdapter {

  lazy val datasource = (new javax.naming.InitialContext).lookup("java:/comp/env/jdbc/test").asInstanceOf[javax.sql.DataSource]
  def contextInitialized(servletContextEvent:javax.servlet.ServletContextEvent ) = {
    withConn(c =>{
      c.insert("CREATE TABLE user_t (id BIGINT IDENTITY, firstname VARCHAR(32) , lastname VARCHAR(32) , email VARCHAR(48) , locale VARCHAR(16) , timezone VARCHAR(32) , password_pw VARCHAR(48) , password_slt VARCHAR(20) )");
      println("Create user table.")
      c.insert("CREATE TABLE role_t (id BIGINT IDENTITY, name VARCHAR(20),  description CLOB , owner BIGINT)");
      println("Create role table.")
      Seq()
    })

  }

  def contextDestroyed(p1: ServletContextEvent) {}
}