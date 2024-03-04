## IntelliJ

### Setup
1. File > Open
2. Select the "feed-me" folder and click "Ok"
3. After IntelliJ recognizes the Gradle project, click "Load build scripts"
4. You may have to download a JDK, ensure that this is Java 21

### Building
1. In the "Gradle" menu on the right side, run the "build" task

### Testing
1. File > Run > Run...
2. Select the "Test" run configuration

### Running
1. File > Run > Run...
2. Select the "Main" run configuration

> [!NOTE]
> These run configurations can be accessed in the top right of IntelliJ next to the play button

## Command line

### Setup
1. Download a JDK for Java 21
2. Set your JAVA_HOME environment variable to the path to the extracted JDK

### Building
1. In the "feed-me" folder, run `./gradlew build` (this will run tests too)

### Testing
1. In the "feed-me" folder, run `./gradlew test`

### Running
1. In the "feed-me" folder, run `./gradlew run -q --console=plain`