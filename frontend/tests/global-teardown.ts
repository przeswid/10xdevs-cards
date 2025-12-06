/**
 * Global Teardown
 * Runs once after all tests complete
 * Cleans up database and stops Docker services
 */

import { exec } from "child_process";
import { promisify } from "util";

const execAsync = promisify(exec);

async function globalTeardown() {
  console.log("\n🧹 Running global teardown...");

  try {
    // Stop and remove Docker containers with volume cleanup
    console.log("Stopping Docker services and cleaning up volumes...");
    await execAsync("docker compose -f docker-compose.e2e.yml down -v");
    console.log("✅ Docker services stopped and volumes removed");
  } catch (error) {
    console.error("❌ Error during teardown:", error);
    throw error;
  }
}

export default globalTeardown;
