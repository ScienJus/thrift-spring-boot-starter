# spring-boot-starter-thrift

Apache Thrift integration with Spring Boot. nonblocking client/server, service discovery, load balancing and more.

# Getting Started

## Client

add `spring-boot-starter-thrift-client` to your `pom.xml`:

```
<dependency>
    <groupId>com.ricebook</groupId>
    <artifactId>spring-boot-starter-thrift-client</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

use `@ThriftClient` injection thrift client on fields or methods:

```
// field injection
@ThriftClient("cal")
private CalService.Client calClient;

// method injection
private CalService.Client calClient;

@ThriftClient("cal")
public void setCalClient(CalService.Client calClient) {
  this.calClient = calClient;
}
```

add thrift client properties to your `application.yml`:

```
thrift:
  client:
    routes:
      cal:  # same as @ThriftClient#value
        address: localhost:9090
    timeout: 500
    retryTimes: 3
```

## Server

add `spring-boot-starter-thrift-server` to your `pom.xml`:

```
<dependency>
    <groupId>com.ricebook</groupId>
    <artifactId>spring-boot-starter-thrift-server</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

use `@ThriftService` on your thrift handler implementations:

```
@ThriftService("cal")   // give a name
public class CalHandler implements CalService.Iface {

  @Override
  public String concat(String str1, String str2) throws TException {
    return str1 + " " + str2;
  }
}
```

add thrift server properties to your `application.yml`:

```
thrift:
  server:
    registries:
      cal:  # same as @ThriftService#value
        port: 9090
    minWorker: 4
    maxWorker: 20
    workerQueueCapacity: 2048
```

