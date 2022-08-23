# rd_example
Example how to use [RD-framework](https://github.com/JetBrains/rd) for cross-process communication.
This is small Gradle+Kotlin project. 

Actions:
- **RD** requires you to generate executing kotlin code from *models* (non-executing code written on kotlin dsl, see `Root.kt`).
To achieve this tak run `./gradlew generateProtocolModels`. Classes will be generated into
`build/rdgen` folder.
- Compile code with `./gradlew build`. In future you can insert `generateProtocolModels` task into *build* workflow.
- You can open code in IntellijIDEA and run `Main.kt`. It demonstrates usage of `RD` for interprocess communication. 
