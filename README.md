# Audit Logging Library

This library provides a comprehensive solution for audit logging in Java applications, using annotations and proxies to intercept method calls and log changes to entities.

## Table of Contents

- [Getting Started](#getting-started)
- [Usage](#usage)
- [Advanced Usage](#advanced-usage)
- [Annotations](#annotations)
- [Contributing](#contributing)
- [License](#license)

## Getting Started

### Prerequisites

- Java 8 or higher
- Maven or Gradle for dependency management

### Installation

Add the library to your project's dependencies:

#### Maven
```xml
<dependency>
    <groupId>com.thinkon</groupId>
    <artifactId>common-util</artifactId>
    <version>2.1.0</version>
</dependency>
```
#### Database

- Run these tables in your project  
```sql
CREATE TABLE audit_log (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `table_name` varchar(200) NOT NULL,
  `action` varchar(100) NOT NULL,
  `value_id` varchar(200) NOT NULL,
  `audit_user` varchar(100) NOT NULL,
  `date` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_id_value` (`value_id`,`table_name`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

CREATE TABLE `audit_log_changes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `audit_log_id` int(11) NOT NULL,
  `field_name` varchar(200) NOT NULL,
  `old_value` blob DEFAULT NULL,
  `new_value` blob DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fdk_audit_log` (`audit_log_id`),
  CONSTRAINT `fdk_audit_log` FOREIGN KEY (`audit_log_id`) REFERENCES `audit_log` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=133 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
```

## Usage

### Basic Example

To use the audit logging, follow these steps:

1. Annotate your entity classes and fields with the provided annotations.
2. Create a proxy for your DAO or service classes using the `AuditProxy`.

#### Step 1: Annotate Your Entities

```java
import com.thinkon.productservice.service.audit.annotation.*;
import com.thinkon.productservice.service.audit.processfield.diff.DefaultAuditDiff;
import java.util.Date;
import java.util.List;

@Auditable(tableName = "example_table")
public class ExampleEntity {
    @AuditProperty(label = "CUSTOM_NAME")//if the AuditProperty is not provided the API will parse to NAME
    private String name;

    @AuditProperty(label = "Entity Description", ignoreNullOrEmpty = true)
    private String description;

    @AuditId
    private Long id;

    @AuditUser
    private String updatedBy;

    @AuditProperties(
        labelField = "Details",
        value = {
            @AuditFieldMapping(label = "Field1", field = "field1"),
            @AuditFieldMapping(label = "Field2", field = "field2", ignoreNullOrEmpty = true)
        },
        diff = DefaultAuditDiff.class
    )
    private ComplexField details;

    // Getters and setters
}

public class ComplexField {
    private String field1;
    private String field2;

    // Getters and setters
}
```

#### Step 2: Create a Proxy

```java
import com.thinkon.productservice.service.audit.proxy.AuditProxy;
import com.thinkon.productservice.service.audit.service.AuditLogServiceImpl;
import com.thinkon.productservice.service.audit.JdbiAuditWrapper;

import javax.sql.DataSource;

public class ExampleUsage {
    public static void main(String[] args) {
        DataSource dataSource = // obtain your DataSource
        JdbiAuditWrapper jdbiAuditWrapper = JdbiAuditWrapper.create(dataSource);
        ExampleDao dao = jdbiAuditWrapper.onDemand(ExampleDao.class);
        //Manual proxy
        AnyClass anyClass = jdbi.proxy(new AnyClassImpl());
        
        // Use the DAO as usual
        ExampleEntity entity = dao.findById(1);
        entity.setName("New Name");
        dao.update(entity);
    }
}
```

### DAO Example

```java
import com.thinkon.productservice.service.audit.annotation.*;
@AuditClass //Indicates this class will be proxy
public interface ExampleDao {
    @AuditCreate(action = CreateAuditClassProcessor.class)
    void create(ExampleEntity entity);

    @AuditUpdate(findById = "findById")
    void update(@AuditId int id, ExampleEntity entity, @AuditUser String auditUser );

    @AuditDelete(findById = "findById")
    void delete(@AuditId Long id);

    ExampleEntity findById(Long id);
}
```

## Advanced Usage

### Custom Audit Differentiator

You can create custom diff classes by implementing the `AuditDiff` interface.

```java
import com.thinkon.productservice.service.audit.processfield.diff.AuditDiff;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public class CustomAuditDiff implements AuditDiff<T [Generic Object]> {
    @Override
    public T process(T firstValue, T secondValue) {
        //Implements your own diff
    }
}
```

### Using `AuditProperties` for Complex Fields

```java
import com.thinkon.productservice.service.audit.annotation.*;
import com.thinkon.productservice.service.audit.processfield.diff.CustomAuditDiff;

public class ComplexEntity {
    @AuditProperties(
        labelField = "Complex Fields",
        value = {
            @AuditFieldMapping(label = "FIRST_SUBFIELD", field = "subField1"),
            @AuditFieldMapping(label = "SECOND_SUBFIELD", field = "subField2", ignoreNullOrEmpty = true)
        },
        diff = CustomAuditDiff.class
    )
    private ComplexField details;

    // Other fields, getters, and setters
}

public class ComplexField {
    private String subField1;
    private String subField2;

    // Getters and setters
}
```

## Annotations

### `@Auditable`
Marks a class as auditable and specifies the table name for audit logs.

### `@AuditClass`
Marks a class for proxy and audit logging.

### `@AuditFieldMapping`
Maps a field for audit logging with a specified label, field name, and other properties. This annotation is intended to be used within `@AuditProperties`.

### `@AuditProperty`
Maps a single field for audit logging with additional metadata.

### `@AuditProperties`
Groups multiple `@AuditFieldMapping` annotations for a single field.

### `@AuditId`
Marks a field or parameter as the identifier for audit logging.

### `@AuditUser`
Marks a parameter as the user responsible for the change being audited.

### `@AuditCreate`, `@AuditUpdate`, `@AuditDelete`
Annotations for specifying create, update, and delete audit operations, respectively.

## Contributing

Contributions are welcome! Please open an issue or submit a pull request on GitLab.
