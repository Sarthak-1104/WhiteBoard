Jetpack Compose Interactive Whiteboard:
A high-performance, interactive digital whiteboard engine built with Jetpack Compose.
Designed for Interactive Flat Panels (IFP) and tablets, this project provides a fluid drawing experience with support for vector-based strokes,
geometric shapes, and persistent storage.

Features:
Fluid Drawing Engine: Real-time rendering using Compose Canvas with optimized path processing.
Intelligent Erasing: Support for "Pixel Clear" erasing using BlendMode.Clear for natural interactions.
Geometric Shapes: Vector-based rendering of Rectangles, Circles, Lines, and Polygons (Pentagons).
Text Annotation: Tap-to-place text input with customizable colors.
Infinite History: Robust Undo/Redo stack management for complex workflows.
Canvas Transformation: Integrated support for sharing Screen as .json files.
Media Export: High-resolution PNG export via GraphicsLayer with direct integration into the Android MediaStore.
Persistence Layer: Save and load boards as JSON files for cross-session editing.

Architecture Overview:
The project follows modern MVVM (Model-View-ViewModel) and Clean Architecture principles to ensure the UI remains reactive
and the business logic remains testable.

Key Components:
WhiteboardViewModel: The "Brain" of the operation. It manages the MutableStateFlow of the board, handles history stacks,
and coordinates with storage services.

WhiteboardState: A single source of truth containing immutable lists of StrokeModel, ShapeModel, and TextModel.

GraphicsLayer Rendering: We utilize rememberGraphicsLayer() to record the drawing commands.
This allows for high-performance exports without re-rendering the entire view hierarchy manually.

Hilt Dependency Injection: Manages the lifecycle of the WhiteBoardSavingServices.

WhiteBoardSavingServices: Handles all the logic regarding current state of screen as .json file locally over disk.

File Format Explanation:
Boards are saved in a custom JSON-based vector format. Unlike flat images,
this format preserves the mathematical properties of every stroke and shape, allowing for infinite scalability and post-load editing.

Example Schema:
{
  "strokes": [
    {
      "points": [[10, 10], [15, 20], [20, 30]],
      "color": "#FF0000",
      "width": 3
    }
  ],
  "shapes": [
    {
      "type": "rectangle",
      "topLeft": [50, 50],
      "bottomRight": [150, 100],
      "color": "#0000FF"
    }
  ],
  "texts": [
    {
      "text": "Hello IFP!",
      "position": [300, 400],
      "color": "#000000",
      "size": 24
    }
  ]
}
