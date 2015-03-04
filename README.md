# keshlib-android-demo (kesh-Framework und Demo-Projekt)
In den folgenden Abschnitten wird erläutert, wie das Framework (jar-Paket) in Ihr Projekt einzubinden ist und wie die grundlegenden Funktionen der Library verwendet werden können.

## Zugangsdaten und weiterführende Dokumentationen
Sollten Sie einen Zugang zu unserem Demo-System und später für die Produktionsumgebung wünschen, [wenden Sie sich bitte an unseren Support.](http://kesh.de/details-partnerintegration)
Dort erhalten Sie die für den Zugang benötigten Zertifikate.

Die Schnittstellendokumentation finden Sie unter folgendem Link: [kesh - Schnittstellenbeschreibung für Fremd-Apps.pdf](https://github.com/xcom-ag/keshlib-ios-demo/blob/master/kesh%20-%20Schnittstellenbeschreibung%20für%20Fremd-Apps.pdf?raw=true)

Sollten Sie auch die Registrierung für kesh einbinden wollen, finden Sie eine Beschreibung hierzu in folgendem Dokument: [kesh - Einbindung der Registrierung Fremd-App.pdf](https://github.com/xcom-ag/keshlib-ios-demo/blob/master/kesh%20-%20Einbindung%20der%20Registrierung%20Fremd-App.pdf?raw=true)

## Installation
1. Name des JAR-Pakets: keshlib-\[version\].jar (im Demo-Projekt enthalten im „libs“-Verzeichnis)
2. Fügen Sie das JAR-Paket in den Projekt-Einstellungen dem Java Build Path (über den Reiter "Libraries") hinzu.
3. Die "JRE System Library" sollte ebenfalls im Projekt verlinkt sein.

## Konfiguration
Um die Funktionen der kesh-Schnittstelle nutzen und Anfragen erfolgreich an den Server schicken zu können, muss zunächst der KeshServiceManager konfiguriert werden.

### Logging
Der KeshServiceManager bietet einige statische Methoden an, über die sich das Loggingverhalten der Library konfigurieren lässt.
```java
// Show all messages (Initial level is java.util.logging.Level.OFF)
KeshServiceManager.setLogLevel(Level.FINEST);

// Add a custom log handler (The library uses a java.util.logging.ConsoleHandler initial)
MyCustomHandler myHandler = new MyCustomHandler();
KeshServiceManager.addLogHandler(myHandler);

// Remove a log handler
KeshServiceManager.removeLogHandler(myHandler);

// The default handler can also be replaced
Handler[] myHandlers = { new MyCustomHandler1(), new MyCustomHandler2() };
KeshServiceManager.setLogHandlers(myHandlers);
```

### KeshServiceManagerConfiguration
Um die Funktionen der kesh-Schnittstelle nutzen und Anfragen erfolgreich an den Server schicken zu können, muss zunächst der KeshServiceManager konfiguriert werden.
Hierzu wird zunächst eine KeshServiceManagerConfiguration mit den entsprechenden Appdaten erstellt. Beispiel:
```java
String appVersion = “0.0.1“;  // Version of your app
String appType = “myApp“;     // String identifier for your app

KeshServiceManagerConfiguration conf = new 
KeshServiceManagerConfiguration(androidContext, appVersion, appType);

// By default, the library trys to reconnect itself after an error.
// Turn off auto reconnect ( Can also be done later with 
// KeshServiceManager.getInstance().setAutoReconnectEnabled(false) )
conf.setAutoReconnectEnabled(false);

// Initialize KeshServiceManager
KeshServiceManager.initializeManager(conf);

// After initialization, you can get the instance
KeshServiceManager serviceManager = KeshServiceManager.getInstance();
```

##  Verbindungsaufbau
Einmal konfiguriert, kann anschließend der Verbindungsaufbau erfolgen:
```java
private void connect() {
    X509Certificate cert = null;
    KeyStore store = loadClientKeyStore(); // Load a KeyStore with the client cert
    String storePasswd = "storepasswd";    // Password for the key store

    try {
        cert = loadCertificatFromFile();   // Load the server cert to identify the server
    } catch (Exception e) {
        e.printStackTrace();
    }
    // Connect using client authentication
    serviceManager.connectToServer(SERVER_URL, cert, store, storePasswd);
}
```
In der Methode loadClientKeyStore() wird ein KeyStore mittels der benötigten .p12-Datei erstellt, loadCertificateFromFile() lädt das X.509-Server-Zertifikat. Die Implementierung dieser Methoden ist im Demo-Projekt ersichtlich.
Die Zertifikate und das zugehörige Passwort erhalten Sie [unter o. g. Ansprechstelle](#zugangsdaten-und-weiterführende-dokumentationen). 

## Abschicken von Requests
Der nächste Schritt ist  der Login des Nutzers. Durch den Login erhält die kesh-Bibliothek ein Session-Token, welches dann automatisch für die weiteren Requests genutzt wird:
```java
public void sendLogin(String phoneNumber, String password) {
    serviceManager.sendLoginRequest(phoneNumber, password, callback);
}
```
Über das Callback wird der Erfolg oder Misserfolg des Requests mitgeteilt, die Response-Daten der jeweiligen Requests können im obigen Dokument eingesehen werden. Aufbau des Callbacks:
```java
private OnResponseReceivedCallback callback = new OnResponseReceivedCallback() {
    public void onRequestFinshedSuccessful(AbstractResponseData arg0) {
        System.out.println("Request finished successful");
    }
    public void onRequestFinishedWithError(AbstractErrorData arg0) {
        System.out.println("Request finished with error");
        ((ErrorDataV2) arg0).getMessage();
    }
};
```
Eine Implementierung aller Requests ist in diesem Demo-Eclipse-Projekt ersichtlich.

## Notifications
Bei einigen Ereignissen schickt der Kesh-Service eine Nachricht an einen CLient, ohne dass dieser eine Anfrage gesendet hat. Um diese Nachrichten verarbeiten zu können bietet der KeshServiceManager Methoden mit denen OnNotificationReceivedListener an- und abgemeldet werden können. Alle registrierten Listener werden über eingehende Notifications informiert.
```java
public void registerForNotification() {
    // Assume current class implements OnNotificationReceivedListener, 
    // serviceManager is our KeshServiceManager instance
    serviceManager.registerNotificationListener(this);
}

public void unregisterForNotification() {
    // Assume current class implements OnNotificationReceivedListener, 
    //serviceManager is our KeshServiceManager instance
    serviceManager.unregisterNotificationListener(this);
}
```

##  Verbindungsstatus
Die Library bietet die Möglichkeit einen Listener für Änderungen am Verbindungsstatus zu registrieren. Wie auch die OnNotificationReceivedListener können OnConnectivityChangedListener an- und abgemeldet werden. Alle registrierten Listener werden über Änderungen informiert.
```java
public void registerForConnectionChanges() {
    // Assume current class implements OnConnectivityChangedListener, 
    // serviceManager is our KeshServiceManager instance
    serviceManager.registerConnectionStateChangedListener(this);
}

public void unregisterForConnectionChanges() {
    // Assume current class implements OnConnectivityChangedListener, 
    // serviceManager is our KeshServiceManager instance
    serviceManager.unregisterConnectionStateChangedListener(this);
}
```

