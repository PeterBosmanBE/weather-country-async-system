import "dotenv/config";
import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  reactCompiler: true,
  async rewrites() {
    const weatherApiBaseUrl = process.env.WEATHER_API_BASE_URL!;
    
    return [
      {
        source: "/api/:path*",
        destination: `${weatherApiBaseUrl}/:path*`,
      },
    ];
  },
};

export default nextConfig;
