Gemini handwriting recognition for Math and Chinese. 

## Key Features
- By default it recognizes and evaluates math expressions.

- Switch to Chinese mode from the dropdown list in the top left to write Chinese characters and get feedback for your writing.

## Installation

### Run the Gemini Agent server
- cd handwriting/agents
- Add GOOGLE_API_KEY key is in .env
- . .env 
- mvn compile exec:java -Dexec.mainClass="agents.recognition.writing.WritingRecognitionAgent"

### Run the frontend web server
cd handwriting/frontend
mvn clean package
java -jar target/writingrecognition-1.0-SNAPSHOT.jar

## Usage
Browse to http://localhost:8080 and you can write anything in the canvas. Then click **Eval** to send screenshot to Gemini for evaluation.

