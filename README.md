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


# Project Structure
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
