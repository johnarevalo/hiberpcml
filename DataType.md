# Data Types #

Every element in the pcml file should be represented in a Java field within a POJO class based on the table shown as below:

| **pcml type** | **Java Type** |
|:--------------|:--------------|
| char | java.lang.String |
| packed | java.math.BigDecimal |
| zoned | java.math.BigDecimal |
| int | java.lang.Long |
| float | java.lang.Float |
| byte | byte[.md](.md) |
| struct | <see next section> |

## Struct Types ##