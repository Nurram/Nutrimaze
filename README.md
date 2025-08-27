# Pacman Game

_This project is an Android implementation of the classic Pac-Man arcade game. While this version includes some personal touches,
the core mechanics remain faithful to the original game._

## About the game
The game contains ten different levels, all of them with its own map.The maps are filled with pellets and four energizers
(from levels one to eight), which Pac-Man must eat to progress. Pacman has three lives, if it loses all of them, the game is lost. 
In five of the ten levels, the red ghost will increase its speed, and the only way for Pac-Man to match that speed is by
eating the five bells that will appear in specific levels.

## About the implementation of the game 📋
When I set out to develop this game for Android, I aimed to keep it simple, avoiding external libraries like OpenGL or SDL. Instead, I chose to use Kotlin's native tools. The game was developed using coroutines and flows to manage game state, and the Canvas class for rendering the UI.
The game features one main activity with six buttons for controlling Pac-Man: up, right, left, down, start, and stop.


#### Here some screenshots of the app (the ten levels)
![level_1](https://github.com/user-attachments/assets/d7bd8345-0d9c-4a6c-a4e6-71c83155c893)
![level_2](https://github.com/user-attachments/assets/4cd4b5de-d3c3-4282-a2d7-edfaef6bc036)
![level_3](https://github.com/user-attachments/assets/db87b007-1e35-4420-933b-2133ba9da33e)
![level_4](https://github.com/user-attachments/assets/98f716c3-216e-4609-b08e-9b8ffbfffe9b)
![level_5](https://github.com/user-attachments/assets/f1e3dfa3-8dae-4b4c-bd16-9f958832f50b)
![level_6](https://github.com/user-attachments/assets/7604bf73-8b12-47e4-adba-11cac7472e1a)
![level_7](https://github.com/user-attachments/assets/b010cfee-7514-450f-9a58-d29b9ce05081)
![level_8](https://github.com/user-attachments/assets/af45e295-f768-411b-a09e-88ffc6963ec9)
![level_9](https://github.com/user-attachments/assets/5a8a236a-91a0-4bcb-9693-a3ef056a4f6f)
![level_10](https://github.com/user-attachments/assets/8480d9b4-16c8-407c-b491-f00afd8c9945)

### Here some video of the game
Here is a video of the game in action. You can also download it directly from the resources directory in the project.
[Link of the video](https://github.com/MauroSerantes/Pacman_Android/blob/main/resources/game_short.mp4)


## Tech Stack and Architecture ⚙️
* **XML** - For building the user interface
* **MVVM (Model-View-ViewModel)** - Architectural pattern to manage UI-related data in a lifecycle-conscious way
* **Kotlin** - Programming language used for all logic and structure
* **Coroutines and Flows** - For asynchronous operations and game state management
* **JSON** - For storing and managing level data

## Authors ✒️

* **Mauro Serantes** - [Mauro Serantes](https://github.com/MauroSerantes)
