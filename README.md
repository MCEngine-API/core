[`GitHub.dev`](https://github.dev/MCEngine/mcengine-api)
[`🐳 Open in Dev Container (VS Code Desktop)`](vscode://vscode-remote/dev-container+https://github.com/MCEngine/mcengine-api)

# 🚀 MCEngine API

This repository serves as the central API for MCEngine projects.

Unlike typical shared libraries, each project will use `implementation` instead of `compileOnly`.  
This ensures that each project can use the API directly without depending on this repository at runtime.

📦 **Usage Tip**: Add this as an `implementation` dependency in your Gradle project to make full use of the API.
