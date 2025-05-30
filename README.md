[`GitHub.dev`](https://mcengine.github.io/mcengine-website/codespaces/github/dev)
[`🐳 Open in Dev Container (VS Code Desktop)`](https://mcengine.github.io/mcengine-website/codespaces/devcontainer/volume/localhost)

# 🚀 MCEngine API

This repository serves as the central API for MCEngine projects.

Unlike typical shared libraries, each project will use `implementation` instead of `compileOnly`.  
This ensures that each project can use the API directly without depending on this repository at runtime.

📦 **Usage Tip**: Add this as an `implementation` dependency in your Gradle project to make full use of the API.
