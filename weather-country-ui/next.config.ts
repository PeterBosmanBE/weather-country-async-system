import type { NextConfig } from "next";

const weatherApiBaseUrl = process.env.WEATHER_API_BASE_URL!;

const nextConfig: NextConfig = {
  reactCompiler: true,
  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: `${weatherApiBaseUrl}/:path*`,
      },
    ];
  },
};

export default nextConfig;
