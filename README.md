<img width="816" height="107" alt="Pacmen Title Screen" src="https://github.com/user-attachments/assets/7c4c9cde-b830-405d-8f6b-2d2e488cdbc6" />

# About
Pac-Men is a modern reimagining of the classic Pac-Man arcade game featuring local multiplayer gameplay.

Unlike the original game, Pac-Men adds the ability for two players to navigate the maze together, collect pellets, avoid ghosts, and compete against each other and win through scoring and survival.

The project was built with Java, with JavaFX and CSS being the main components for the graphical interface.

# Features
- Classic Pac-Man inspired gameplay
- Local multiplayer
- Ghost AI with pathfinding behavior
- Score tracking system
- Multiple game states (Menu, Playing, Paused, Game Over)
- JavaFX-based graphics and animations

# Controls
| Action | Player 1 | Player 2 |
|----------|----------|----------|
| Move Up | W | ↑ |
| Move Down | S | ↓ |
| Move Left | A | ← |
| Move Right | D | → |

# Installation
Make sure you have the correct versions of [Java 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html) and [JavaFX](https://gluonhq.com/products/javafx/) 21 JDK installed on your machine.

For Windows users, download the zip of the code base, unzip, then run the .exe file or .jar file found in pac-men-master/pacmen/.

For MacOS/Linux users, download the zip of the code base and unzip (make sure to disable [Gatekeeper](https://en.wikipedia.org/wiki/Gatekeeper_(macOS)) for machines running on recent MacOS versions). Access the directory pac-men-master/pacmen via the terminal through `cd /[your unzipped code base's location]/pac-men-master/pacmen`, then run the command `java --module-path "/[your JavaFX SDK dependency's location]/javafx-sdk-21.0.11/lib" --add-modules javafx.controls,javafx.fxml -jar pacmen.jar`. 

You can also launch the project with an IDE like Visual Studio Code (make sure to reference the JavaFX library if you are running a different OS than Windows).

## Project Structure
### Main program
Main.java starts the game.

### Game characters and objects
Inside src/pacmen/entities and src/pacmen/ghostai.

### Game map
Inside src/pacmen/map.
Map details are stored as a text file (resources/maps/level1.txt).

### Other properties
Other properties, like multiplayerlogic and scene, are inside src/pacmen.

## Team members
- Nguyen Dam Xuan Nguyen
- Nguyen Tien Khoi Nguyen
- Nguyen The Nguyen
- Tran Minh Quang
- Le Tri Duc
