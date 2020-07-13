# Prova Finale Ingegneria del Software 2020


- ###  Andrea Aspesi ([@AspesiAndrea](https://github.com/AspesiAndrea))<br>andrea1.aspesi@mail.polimi.it
- ###  Matteo Bettini ([@MatteoBettini](https://github.com/MatteoBettini))<br>matteo1.bettini@mail.polimi.it
- ###  Mirko De Vita ([@MirkoDeVita98](https://github.com/MirkoDeVita98))<br>mirko.devita@mail.polimi.it

| Functionality | State |
|:-----------------------|:------------------------------------:|
| Basic rules | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| Complete rules | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| Socket | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| GUI | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| CLI | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| Multiple games | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| Persistence | [![RED](https://placehold.it/15/f03c15/f03c15)](#) |
| Advanced Gods | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| Undo | [![RED](https://placehold.it/15/f03c15/f03c15)](#) |

<!--
[![RED](https://placehold.it/15/f03c15/f03c15)](#)
[![YELLOW](https://placehold.it/15/ffdd00/ffdd00)](#)
[![GREEN](https://placehold.it/15/44bb44/44bb44)](#)
-->

# Santorini

![Santorini Logo](logo.png)

## Setup

- In the [deliveries](deliveries) folder there are two multi-platform jar files, one to set the Server up and the other one to start the Client.
- The Server can be run with the following command, as default it runs on port 4567:
    ```shell
    > java -jar SantoriniServer.jar
    ```
  This command can be followed by these arguments:
  - **-port**: followed by the desired port number between MIN_PORT and MAX_PORT as argument;
  - **-v**: to activate logging on the console;
  - **-log**: followed by a file name, to activate logging both in the console and in the chosen file;
  - **-help**: to get help.
  
- The Client can be run with the following command:
    ```shell
    > java -jar SantoriniClient.jar
    ```
  - This command sets the Client on Graphical User Interface(GUI) mode, but it can be followed by **-cli** if the Command Line Interface(CLI) is preferred.
  - The Server's IP and port to connect to can be specified during the execution.
 
 ## Build
 Use maven to build jar files for both the Client and the Server by choosing the appropriate Maven Profile.  
 
 To build the Server, issue:  
    ```
       > mvn clean    
    ```  
    ```
      > mvn package -P Server    
    ```  
 <br>
 To build the Client, issue:  
    ```
        > mvn clean    
    ```  
    ```
       > mvn package -P Client    
    ```    
  
  After these processes both jars can be found in the builds folder.
 ## Extra
 
 Two game-modes are implemented:
 - **Normal**: the Server sends the possible moves/builds to the Client so that they are displayed to the Player during his/her turn.
 - **Hardcore**: in this mode there are no suggestions and the Player can lose if he/she does not obey to Gods' rules.
 
 ## Tools
 
 * [StarUML](http://staruml.io) - UML Diagram
 * [Maven](https://maven.apache.org/) - Dependency Management
 * [IntelliJ](https://www.jetbrains.com/idea/) - IDE
 * [JavaFX](https://openjfx.io) - Graphical Framework
 
 ## License
 
 This project is developed in collaboration with [Politecnico di Milano](https://www.polimi.it) and [Cranio Creations](http://www.craniocreations.it).
 
