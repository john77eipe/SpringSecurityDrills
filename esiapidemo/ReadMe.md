Utilize SLF4J
Ref:https://stackoverflow.com/questions/34295088/esapi-getting-noclassdeffounderror-loggerfactory-with-banned-dependency

If you get this error
`org.owasp.esapi.errors.ConfigurationException: SecurityConfiguration for Logger.UserInfo not found in ESAPI.properties`
add this into the ESAPI properties
Logger.UserInfo=false
Logger.ClientInfo=false

To build fat jar

```xml
<build>
        <finalName>esiapidemo</finalName>
        <plugins>

            <!-- other Maven plugins ... -->

            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <configuration>
                            <mainClass>org.xss.demo.Application</mainClass>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

Then use this command to package,
```shell
mvn clean package spring-boot:repackage
java -jar esiapidemo.jar --spring.config.location=application.properties,classpath:/ESAPI.properties,classpath:/validation.properties
```

Use `classpath:` to refer to the packaged file or an absolute path could be directly specified. 