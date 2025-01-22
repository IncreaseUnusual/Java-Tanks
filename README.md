# Tanks Game

This is a Java-based "Tanks" game implemented using the Processing library. The game is a turn-based strategy game where players control tanks, aim their turrets, and fire projectiles at opponents, all while accounting for wind and terrain dynamics.

## Features

- **Turn-Based Gameplay**: Players take turns controlling tanks to aim and fire projectiles.
- **Dynamic Terrain**: Terrain can be destructed by projectiles, altering the battlefield.
- **Wind Mechanics**: Wind influences projectile trajectories.
- **Tank Attributes**:
  - Fuel management
  - Repair health
  - Adjustable turret aiming and power
- **Parachutes**: Tanks can use parachutes to reduce fall damage.
- **Multiple Levels**: Progress through levels with increasing challenges.
- **UI Elements**: Displays wind speed, tank health, and other game statistics.

## Getting Started

### Prerequisites

- [Processing IDE](https://processing.org/download/)
- Java Development Kit (JDK) 8 or higher

### Installation

1. Clone the repository or download the source code.
   ```bash
   git clone <repository_url>
   ```
2. Open the project folder in the Processing IDE.
3. Ensure all necessary `.pde` files and resources (images, sound, etc.) are in the `data` folder.
4. Run the `App` class.

### Controls

- **Spacebar**: Fire projectile.
- **R**: Repair tank health.
- **F**: Refuel the tank.
- **Arrow Keys**:
  - **Up**: Aim turret upwards.
  - **Down**: Aim turret downwards.
  - **Left**: Move tank left.
  - **Right**: Move tank right.
- **W**: Increase turret power.
- **S**: Decrease turret power.

### Gameplay Instructions

1. Launch the game by running the `App` class.
2. Players take turns controlling their tanks.
3. Use arrow keys and `W`/`S` to aim and adjust the power of your shot.
4. Press the spacebar to fire at opponents.
5. Manage your tank's health and fuel to stay in the game.
6. Advance through levels by defeating all opponent tanks.

## File Structure

- **`App.java`**: Main game loop and core logic.
- **`levelSetup.java`**: Handles level initialization and data loading.
- **`projectile.java`**: Manages projectile behavior, trajectory, and collision detection.
- **`tank.java`**: Defines tank attributes and actions.
- **`destruction.java`**: Handles terrain destruction.
- **`ui.java`**: Manages the user interface.
- **`sound.java`**: Handles sound effects and music.

## Known Issues

- Tanks may behave unexpectedly when terrain height is altered rapidly.
- Game might crash if projectiles exceed the maximum limit.
- Some UI elements may not scale correctly for different screen resolutions.

## Future Improvements

- Add multiplayer support.
- Enhance tank customization options.
- Introduce more diverse levels and challenges.
- Optimize performance for large-scale projectile simulations.

## Acknowledgments

- Processing library for graphics rendering.
- Contributors and testers for their feedback and support.

## License

This project is licensed under the [MIT License](LICENSE).

---

Enjoy the game!

