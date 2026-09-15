# pausepay android client

The Android client receives transaction SMS, filters and parses relevant messages, and sends parsed transactions to the Spring Boot API.

## flow

SMS → SmsReceiver → SmsFilter → SmsParser → Retrofit → Spring Boot → Kafka

Kafka processing is asynchronous. The current backend transaction endpoint returns before categorization and behavior evaluation finish. The existing Android client therefore does not receive the Kafka-generated nudge in that HTTP response.

For asynchronous notifications after Kafka processing, add FCM (or another push channel) and have the backend publish the resulting nudge through it.

## files

```text
android/app/src/main/
├── AndroidManifest.xml
├── java/com/financeautopilot/
│   ├── MainActivity.java
│   ├── filter/SmsFilter.java
│   ├── receiver/SmsReceiver.java
│   ├── parser/SmsParser.java
│   ├── network/
│   │   ├── ApiClient.java
│   │   ├── ApiService.java
│   │   └── TokenManager.java
│   ├── model/
│   │   ├── TransactionRequest.java
│   │   └── NudgeResponse.java
│   └── notification/NudgeNotificationHelper.java
└── res/layout/activity_main.xml
```

The Android source files are included in this directory and are intentionally kept separate from the Spring Boot `src/main` tree.
