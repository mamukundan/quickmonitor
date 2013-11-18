QuickMonitor
------------

QuickMonitor is useful in monitoring services and sending alerts. 
Currently it can be used to monitor HTTP, HTTPS, Socket, SecureSocket based services.

To get started:
---------------
1. Create a .txt file for every service that needs to be monitored.
2. Provide the type of service, url or ip/port, timeout and the status code
3. Define the frequency in which the service needs to be monitored.
4. If you need email notification, enable it and provide the email configurations in the conf.ini file
5. java -jar ./dist/QuickMonitor.jar
6. If you change add a new service file or edit any of existing service files, you can reload by touching the reload.txt
