# Architecture

```text
   Plugin                            Backend
+-----------+                     +-----------+
|           |                     |           |
|  Kotlin   |    HTTP Request     |    C#     |
|           |-------------------->|           |
| JetBrains |<--------------------|   .NET    |
|  Runtime  |    HTTP Response    |  Runtime  |
|           |                     |           |
+-----------+                     +-----------+
```
