/**
 * API endpoint to clear authentication cookie
 * Called when user logs out
 */

import type { APIRoute } from 'astro';
import { clearAuthCookie } from '@/lib/server/auth';

export const POST: APIRoute = async ({ cookies }) => {
  try {
    // Clear the auth cookie
    clearAuthCookie(cookies);

    return new Response(
      JSON.stringify({ success: true }),
      { status: 200, headers: { 'Content-Type': 'application/json' } }
    );
  } catch (error) {
    console.error('Error clearing auth cookie:', error);
    return new Response(
      JSON.stringify({ error: 'Failed to clear cookie' }),
      { status: 500, headers: { 'Content-Type': 'application/json' } }
    );
  }
};
