interface WeatherResult {
  country: string
  temperature: number
  windspeed: number
  time: string
}

interface WeatherResponse {
  id: number
  location: string
  status: string
  result?: string
}

interface CurrentWeather {
  temperature: number
  wind_speed: number
  windspeed?: number
  time: string
}

interface OpenMeteoResponse {
  current_weather: CurrentWeather
}

export async function requestWeather(location: string): Promise<WeatherResponse> {
  const response = await fetch(`/api/weather`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(location),
  })

  if (!response.ok) {
    throw new Error(`Failed to request weather: ${response.statusText}`)
  }

  const data = await response.json()
  return data as WeatherResponse
}

export async function getWeatherStatus(id: number): Promise<string> {
  const response = await fetch(`/api/weather/${id}/status`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
  })

  if (!response.ok) {
    throw new Error(`Failed to get weather status: ${response.statusText}`)
  }

  const status = await response.text()
  return status
}

export async function getWeatherResult(id: number, country: string): Promise<WeatherResult> {
  const response = await fetch(`/api/weather/${id}/result`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
  })

  if (!response.ok) {
    throw new Error(`Failed to get weather result: ${response.statusText}`)
  }

  const result = await response.text()
  const apiResponse = JSON.parse(result) as OpenMeteoResponse

  // Transform OpenMeteo API response to WeatherData format
  const weatherData: WeatherResult = {
    country: country,
    temperature: apiResponse.current_weather?.temperature ?? 0,
    windspeed: apiResponse.current_weather?.wind_speed ?? apiResponse.current_weather?.windspeed ?? 0,
    time: apiResponse.current_weather?.time,
  }

  return weatherData
}

export async function pollWeatherResult(
  id: number,
  country: string,
  maxAttempts: number = 30,
  interval: number = 1000
): Promise<WeatherResult> {
  for (let attempt = 0; attempt < maxAttempts; attempt++) {
    const status = await getWeatherStatus(id)

    if (status === "SUCCESS") {
      return await getWeatherResult(id, country)
    }

    if (status === "FAILED") {
      throw new Error("Weather request failed on the server")
    }

    // Wait before polling again
    await new Promise((resolve) => setTimeout(resolve, interval))
  }

  throw new Error("Weather request took too long to complete")
}
