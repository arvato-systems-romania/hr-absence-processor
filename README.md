# HR Absence Processor

This repository contains a full-stack application for processing HR absence data. It consists of a Java Spring Boot backend and a React frontend, designed to read, process, and export absence information from Excel files.

## Project Structure

- **backend/**: Java Spring Boot application for processing absence data.
  - Handles Excel file reading/writing, business logic, and REST API endpoints.
- **frontend/**: React application for user interaction.
  - Allows users to upload files, view results, and download processed data.
- **data/**: Contains input and output Excel files for testing and production.
- **docker-compose.yml**: Orchestrates backend and frontend services using Docker.

## Prerequisites

- Java 17+
- Node.js 18+
- Docker (optional, for containerized deployment)

## Setup Instructions

### Backend
1. Navigate to the `backend` folder:
   ```cmd
   cd backend
   ```
2. Build the project:
   ```cmd
   mvn clean install
   ```
3. Run the application:
   ```cmd
   mvn spring-boot:run
   ```

### Frontend
1. Navigate to the `frontend` folder:
   ```cmd
   cd frontend
   ```
2. Install dependencies:
   ```cmd
   npm install
   ```
3. Start the development server:
   ```cmd
   npm start
   ```

### Docker Compose (Optional)
To run both backend and frontend using Docker:
```cmd
docker-compose up --build
```

## Usage
- Upload an Excel file containing absence data via the frontend.
- The backend processes the file and returns results.
- Download the processed absence report from the frontend.

## Folder Structure
```
backend/         # Java Spring Boot backend
frontend/        # React frontend
  src/components # React components
  public/        # Static assets
  ...
data/            # Input/output Excel files
```

## Testing
- Backend: Run unit and integration tests with Maven:
  ```cmd
  mvn test
  ```
- Frontend: Run tests with npm:
  ```cmd
  npm test
  ```

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
This project is licensed under the MIT License.

## Contact
For questions or support, please contact the repository maintainer.

