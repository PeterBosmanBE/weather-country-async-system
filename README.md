# Weather & Country Application (Weather Country App)

This project is a distributed web application based on a microservices architecture. The system fetches external data about countries and the current weather, processes it asynchronously, and presents it to the user via a modern web interface.

![Banner](.github/assets/ui.png)

## Architecture & Components

The project consists of the following main components:

- **Weather Country UI**: The frontend built with **Next.js / React** and TypeScript. It communicates directly with the internal API to display the data graphically.
- **Weather Country API**: A backend service built with **Quarkus** (Java). This API handles requests from the interface and reads/writes data to the database.
- **Weather Country Worker**: A decoupled background service (also **Quarkus**) that communicates with external services like the *REST Countries API* and the *Open-Meteo API* to fetch or update data messages.
- **Infrastructure**:
  - **PostgreSQL**: A relational database for stable data storage.
  - **Apache ActiveMQ Artemis**: A message broker (AMQP) that ensures fast and asynchronous communication between the API and the Worker.

## How to install & start (Quick Start)

First you need to have this project saved locally to your desktop, this can be done by opening the terminal and entering the following command:

```
git clone https://github.com/PeterBosmanBE/weather-country-async-system
```

After pulling the repository, you'd need to go into the new folder created:

```
cd weather-country-async-system
```

When you're inside the folder, you'd need to run the docker compose file inside this folder. This can be done with the following command:

```
docker compose up --build -d
```
## Accessing the application
Once all the containers are successfully running, you can access the application at:

- Frontend / UI: http://localhost:3000
- Backend API: http://localhost:8080
- Backend Worker: http://localhost:8081

## API Reference

### Internal API (Weather Country API)

These are the REST endpoints exposed by the backend Quarkus application, accessible by default via `http://localhost:8080`.

#### Create a new weather request

```http
  POST /weather
```

| Parameter | Location | Type     | Description                |
| :-------- | :------- | :------- | :------------------------- |
|  `body`   | `body`   | `string` | **Required**. The name of the country/location |

Action: Creates a new weather request in the database and pushes a message to the ActiveMQ broker for the worker to process. It returns the database object containing a unique id.

#### Get the status of a request

```http
  GET /weather/${id}/status
```

| Parameter | Location | Type     | Description                       |
| :-------- | :------- | :------- | :-------------------------------- |
| `id`      | `path`   | `string` | **Required**. ID of the weather request to check |

Action: Returns the current processing status of the request (e.g., PENDING, COMPLETED, FAILED).

#### Get the final weather result

```http
  GET /weather/${id}/result
```

| Parameter | Location | Type     | Description                |
| :-------- | :------- | :------- | :------------------------- |
|  `id`   | `path`   | `string` | **Required**. ID of the weather request to fetch
 |

Action: Returns the actual fetched weather and country data in JSON format once the worker has successfully completed processing the message.

## External APIs (used by Weather Country Worker)
The background worker automatically calls these public APIs during the processing of a request. You don't need any API keys, as they are public:

### REST Countries API

```http
  GET https://restcountries.com/v3.1/name/${location}
```

Action: Fetches the country details (like latitude and longitude coordinates) based on the location name provided by the user.

### Open-Meteo API

```http
  GET https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&current_weather=true
```

Action: Uses the extracted coordinates from the REST Countries API to fetch the current live weather conditions for that specific geographical location.