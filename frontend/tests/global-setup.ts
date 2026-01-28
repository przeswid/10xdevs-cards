/**
 * Global Setup
 * Runs once before all tests start
 * Cleans up any existing Docker containers and volumes from previous runs
 */

import { exec } from "child_process";
import { promisify } from "util";

const execAsync = promisify(exec);

async function globalSetup() {
  console.log("\n🧹 Running global setup...");

  try {
    // Stop and remove any existing Docker containers and volumes
    console.log("Cleaning up any existing Docker services and volumes...");
    await execAsync("docker compose -f docker-compose.e2e.yml down -v");
    console.log("✅ Cleaned up existing Docker services");

    // Start fresh Docker services
    console.log("Starting fresh Docker services...");
    await execAsync("docker compose -f docker-compose.e2e.yml up -d --wait");
    console.log("✅ Docker services started and healthy");
  } catch (error) {
    console.error("❌ Error during setup:", error);

    // Print backend logs to help diagnose the failure
    try {
      console.log("\n📋 Backend container logs:");
      const { stdout: logs } = await execAsync(
        "docker compose -f docker-compose.e2e.yml logs backend"
      );
      console.log(logs);
    } catch {
      console.log("Could not retrieve backend logs");
    }

    throw error;
  }
}

export default globalSetup;
