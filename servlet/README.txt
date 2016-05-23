Adding the JCR Shell Servlet to a web application
=================================================

* Add the following dependencies to the maven project:

   <dependencies>
     <dependency>
      <groupId>org.onehippo.forge.jcrshell</groupId>
      <artifactId>jcrshell-servlet</artifactId>
      <version>1.01.03-SNAPSHOT</version>
      <type>war</type>
    </dependency>

    <dependency>
      <groupId>javax.ws.rs</groupId>
      <artifactId>jsr311-api</artifactId>
      <version>1.1.1</version>
    </dependency>
  </dependencies>


* Overlay the servlet in the maven-war-plugin configuration

  <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-war-plugin</artifactId>
    <configuration>
        ...
      <overlays>
          ...
        <overlay>
          <groupId>org.onehippo.forge.jcrshell</groupId>
          <artifactId>jcrshell-servlet</artifactId>
          <type>war</type>
          <excludes>
            <exclude>WEB-INF/web.xml</exclude>
          </excludes>
        </overlay>
      </overlays>
    </configuration>
  </plugin>


* Add the CXF servlet and the JcrShellSessionFilter to the web.xml

  <context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>classpath:/applicationContext.xml</param-value>
  </context-param>

  <listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
  </listener>
  <listener>
    <listener-class>org.onehippo.forge.jcrshell.servlet.JcrShellSessionAttacher</listener-class>
  </listener>

  <filter>
    <filter-name>JcrSessionFilter</filter-name>
    <filter-class>org.onehippo.forge.jcrshell.servlet.JcrShellSessionFilter</filter-class>
  </filter>

  <servlet>
    <servlet-name>JcrShellResources</servlet-name>
    <servlet-class>org.apache.cxf.transport.servlet.CXFServlet</servlet-class>
    <load-on-startup>1</load-on-startup>
  </servlet>

  <filter-mapping>
    <filter-name>JcrSessionFilter</filter-name>
    <url-pattern>/rest/*</url-pattern>
  </filter-mapping>
  <servlet-mapping>
    <servlet-name>JcrShellResources</servlet-name>
    <url-pattern>/rest/*</url-pattern>
  </servlet-mapping>


* After starting the application, access the jcr shell at 'index.html'.

