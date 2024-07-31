# external-task-retry-aspect
[![](https://img.shields.io/maven-central/v/de.viadee.bpm.camunda/external-task-retry-aspect-spring-boot-starter)](https://search.maven.org/artifact/de.viadee.bpm.camunda/external-task-retry-aspect-spring-boot-starter)
![GitHub tag (latest by date)](https://img.shields.io/github/v/tag/viadee/external-task-retry-aspect)
[![](https://img.shields.io/badge/External%20Task%20Handler-7.21.0-orange.svg)](https://docs.camunda.org/manual/7.20/user-guide/ext-client/spring-boot-starter)
![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/viadee/external-task-retry-aspect/maven-build.yml)
[![](https://img.shields.io/github/issues/viadee/external-task-retry-aspect)](https://github.com/viadee/external-task-retry-aspect/issues)
[![](https://img.shields.io/github/stars/viadee/external-task-retry-aspect)](https://github.com/viadee/external-task-retry-aspect/stargazers)

## ℹ️ Description
This tool helps to control the retry-behaviour in external-task-handlers based on the
official [java-client](https://docs.camunda.org/manual/latest/user-guide/ext-client/) provided
by [Camunda BPM](https://docs.camunda.org/manual/latest/user-guide/ext-client/).

## ⭐ Features
* Retry-behaviour for external-tasks can be configured in process-models as known from `JavaDelegates`
  like `R3/PT1M`, meaning *three times each after one minute*
* Every `Exception` leads to a retry  - no manual handling within handlers necessary
* Special error-type to force instant incidents - skipping any retry-behaviour
* Additional error-type to create a business-error, which must be handled in process
* Configurable default retry-behaviour

## 🚀 How to use
1. Besides the `camunda-external-task-client` dependency, the following maven-coordinate needs to be added to the `pom.xml`. As
a `spring-boot-starter`, the aspect will be loaded automatically as soon as the handler-application starts:
```xml
<dependencies>
  
    <!-- works either with the original external-task-client... -->
    <dependency>
        <groupId>org.camunda.bpm</groupId>
        <artifactId>camunda-external-task-client</artifactId>
        <version>...</version>
    </dependency>
    
    <!-- ...or with new spring-boot-starter-external-task-client since version 7.15.0 -->
    <dependency>
        <groupId>org.camunda.bpm.springboot</groupId>
        <artifactId>camunda-bpm-spring-boot-starter-external-task-client</artifactId>
        <version>...</version>
    </dependency>

    <!-- finally: the retry-aspect itself  -->
    <dependency>
        <groupId>de.viadee.bpm.camunda</groupId>
        <artifactId>external-task-retry-aspect-spring-boot-starter</artifactId>
        <version>...</version>
    </dependency>
</dependencies>
```
2. Add extension-property to an external-task:
![](docs/external-task-extension-properties.png)
   - The property-name is configurable (see below), default: `RETRY_CONFIG`
   - Possible values might be, default: `R3/PT5M`
     - `R1/P1D`: 1 retry after 1 day
     - `R2/PT2H`: 2 retries after 2 hours each
     - `R3/PT3M`: 3 retries after 3 minutes each
     - `PT5M,PT10M,PT20M,PT1H,PT12H,P1D`: 6 increasing retries; 5, 10, 20 minutes, 12 hours, 1 day


3. Make sure, the `ExternalTaskHandler` is capable to access extension-properties :
```java
public class HandlerConfig {

    @Autowired // e.g. spring component
    private ExternalTaskHandler myExternalTaskHandler;
    
    public void openHandler() {
        new ExternalTaskClientBuilderImpl()
                .baseUrl("http://camunda/engine-rest").build()
                    .subscribe("worker-topic")
                    .handler(myExternalTaskHandler)   // injected spring component
                    .includeExtensionProperties(true) // important, bc. the default: false
                    .open();
    }
}
```
Alternatively, if using `spring-boot-starter-external-task-client`, activate extension-properties e.g. in the application.yaml ([more information](https://docs.camunda.org/manual/latest/user-guide/ext-client/spring-boot-starter/)):
```yaml
camunda.bpm.client:
  subscriptions: 
    worker-topic:
      include-extension-properties: true
```
### Configuration options
These properties are available, they can be set e.g. in `application.properties`:

```properties
# Default retry-behaviour, if no retry is configured. 
# Whenever this property is configured incorrectly, 'R3/PT5M' is also used as fallback
de.viadee.bpm.camunda.external-task.retry-config.default-behavior=R3/PT5M

# Identifier used in bpmn-extension-properties, default=RETRY_CONFIG
de.viadee.bpm.camunda.external-task.retry-config.identifier=RETRY_CONFIG
```

## 🧙 How this might help?
A comparison of some `ConventionalHandler` with an `AspectedHandler` explains how the error-handling 
can be completely left out, because anything is done by the `retry-aspect`:

### `ConventionalHandler` without Retry-Aspect
```java
@Component
public class ConventionalHandler implements ExternalTaskHandler {

    public void execute(ExternalTask task, ExternalTaskService service) {
        try {
            // do some business-logic and complete if fine...
            service.complete(task);

            // ...or maybe end with some bpmn-error, that has to be handled within process
            service.handleBpmnError(task, "bpmn-error-code");

        } catch (Exception error) {
            // catch errors and think about retries and timeout
            service.handleFailure(task,
                    "error-message",        // shown in Camunda Cockpit
                    "error-details",        // e.g. stacktrace, available in Camunda Cockpit
                    task.getRetries() - 1,  // how many retries are left? (initial null)
                    300000L);               // time to next retry in ms
        }
    }
}

```

### `AspectedHandler` using Retry-Aspect

* No `try-catch` needed, this is done automatically, i.e. all errors thrown in an `ExternalTaskHandler` `execute()`-method will be caught automatically and handled as follows:
    * `ExternalTaskBusinessError` can be used to trigger `handleBpmnError()`
    * `InstantIncidentException` can be used to skip retries and create an incident instantly
    * Any other exception leads to the specified retry-behaviour

```java
@Component
@ExternalTaskSubscription("my-topic")
public class AspectedHandler implements ExternalTaskHandler {

    public void execute(ExternalTask task, ExternalTaskService service) {
        // do some business-logic and complete if fine...
        service.complete(task);

        // ...or maybe end with some bpmn-error, that has to be handled within process
        throw ExternalTaskBusinessError("bpmn-error-code");
    }
}
```

## :computer: Versions
The following versions are used. Older versions are probably not maintained, but in most cases, it should be possible to 
use a newer version of the Retry-Aspect in combination with an older version of the External-Task-Client. If you encounter
any issue, please feel free to contact me.

| Retry-Aspect | External-Task-Client | Spring Boot |  
|-------------:|---------------------:|------------:|
|        1.2.x |               7.15.0 |       2.5.x |
|        1.3.x |               7.16.0 |       2.6.x |
|        1.4.x |               7.17.0 |       2.6.x |
|        1.4.2 |               7.17.0 |       2.7.x |
|        1.5.x |               7.18.0 |       2.7.x |
|        1.6.x |               7.19.0 |       2.7.x |
|        1.7.x |               7.19.0 |       2.7.x |
|        1.8.x |               7.20.0 |       3.1.x |
|        1.9.x |               7.21.0 |       3.3.x |

## 🤹 Collaboration
This tool was build by viadee Unternehmensberatung AG. If you are interested to find out what 
else we are doing, check out our website [viadee.de](https://www.viadee.de/en). 
If you have any feedback, ideas or extensions feel free to contact or create a GitHub issue.

## 🏆 Thanks

* Many thanks to [@ChrisSchoe][u_chrisschoe] for making the external-task-retry-aspect spring-boot-3-ready ([#107][i107])

## 🔑 License

[![](https://img.shields.io/github/license/viadee/external-task-retry-aspect)](https://github.com/viadee/external-task-retry-aspect/blob/master/LICENSE)

---  
[i107]: https://github.com/viadee/external-task-retry-aspect/issues/107
[u_ChrisSchoe]: https://github.com/ChrisSchoe
