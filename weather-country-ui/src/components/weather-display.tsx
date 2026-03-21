"use client"

import { Thermometer, Wind, Clock, MapPin } from "lucide-react"
import { Card, CardContent, CardHeader, CardTitle } from "../components/ui/card"
import type { WeatherData } from "../types/WeatherData"

interface WeatherDisplayProps {
  data: WeatherData
}

export function WeatherDisplay({ data }: WeatherDisplayProps) {
  return (
    <Card className="shadow-lg border-border overflow-hidden">
      <CardHeader className="bg-primary text-primary-foreground pt-6 pb-6">
        <CardTitle className="flex items-center gap-2 text-xl">
          <MapPin className="h-5 w-5" />
          {data.country}
        </CardTitle>
      </CardHeader>
      <CardContent className="p-0">
        <div className="divide-y divide-border">
          <WeatherStat
            icon={<Thermometer className="h-6 w-6 text-primary" />}
            label="Temperature"
            value={`${data.temperature}°C`}
          />
          <WeatherStat
            icon={<Wind className="h-6 w-6 text-primary" />}
            label="Wind Speed"
            value={`${data.windspeed} km/h`}
          />
          <WeatherStat
            icon={<Clock className="h-6 w-6 text-primary" />}
            label="Local Time"
            value={data.time}
          />
        </div>
      </CardContent>
    </Card>
  )
}

interface WeatherStatProps {
  icon: React.ReactNode
  label: string
  value: string
}

function WeatherStat({ icon, label, value }: WeatherStatProps) {
  return (
    <div className="flex items-center justify-between p-4">
      <div className="flex items-center gap-3">
        {icon}
        <span className="text-muted-foreground font-medium">{label}</span>
      </div>
      <span className="text-xl font-semibold text-foreground">{value}</span>
    </div>
  )
}
