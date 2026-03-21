"use client"

import { useState } from "react"
import { Search } from "lucide-react"
import { Button } from "../components/ui/button"
import { Input } from "../components/ui/input"

interface WeatherSearchProps {
  onSearch: (country: string) => void
  isSearching: boolean
}

export function WeatherSearch({ onSearch, isSearching }: WeatherSearchProps) {
  const [query, setQuery] = useState("")

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (query.trim()) {
      onSearch(query.trim())
    }
  }

  return (
    <form onSubmit={handleSubmit} className="flex gap-2">
      <div className="relative flex-1">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <Input
          type="text"
          placeholder="Enter country name..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          className="pl-10 h-12 bg-card border-border shadow-sm"
        />
      </div>
      <Button 
        type="submit" 
        className="h-12 px-6 shadow-sm"
        disabled={isSearching || !query.trim()}
      >
        {isSearching ? (
          <span className="flex items-center gap-2">
            <span className="h-4 w-4 border-2 border-primary-foreground/30 border-t-primary-foreground rounded-full animate-spin" />
            <span className="sr-only">Searching</span>
          </span>
        ) : (
          "Search"
        )}
      </Button>
    </form>
  )
}
