"use client"

import { useState } from "react"
import { WeatherSearch } from "../components/weather-search"
import { WeatherDisplay } from "../components/weather-display"
import { WeatherData } from "../types/WeatherData"
import { pollWeatherResult } from "../lib/weatherApi"
import { requestWeather } from "../lib/weatherApi"

export default function WeatherApp() {
  const [weather, setWeather] = useState<WeatherData | null>(null)
  const [error, setError] = useState<string>("")
  const [isSearching, setIsSearching] = useState(false)

  const handleSearch = async (country: string) => {
    setIsSearching(true)
    setError("")

    try {
      // Request weather data from the backend
      const response = await requestWeather(country)
      
      // Poll for the result
      const result = await pollWeatherResult(response.id, country)
      setWeather(result)
      setError("")
    } catch (err) {
      setWeather(null)
      setError(
        err instanceof Error
          ? err.message
          : `Failed to fetch weather data for "${country}". Please try again.`
      )
    } finally {
      setIsSearching(false)
    }
  }

  return (
    <main className="min-h-screen bg-background flex items-center justify-center p-4">
      <div className="w-full max-w-md space-y-6">
        <div className="text-center space-y-2">
          <h1 className="text-3xl font-bold text-foreground tracking-tight">Weather App</h1>
          <p className="text-muted-foreground">Enter a country to see the current weather</p>
        </div>
        
        <WeatherSearch onSearch={handleSearch} isSearching={isSearching} />
        
        {error && (
          <div className="bg-destructive/10 text-destructive text-sm p-4 rounded-lg border border-destructive/20">
            {error}
          </div>
        )}
        
        {weather && <WeatherDisplay data={weather} />}
      </div>
    </main>
  )
}
