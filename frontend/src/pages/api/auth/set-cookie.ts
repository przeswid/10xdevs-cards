/**
 * API endpoint to set authentication cookie
 * Called after successful login from client-side
 */

import type { APIRoute } from 'astro';
import { setAuthCookie } from '@/lib/server/auth';

export const POST: APIRoute = async ({ request, cookies }) => {
  try {
    const body = await request.json();
    const { token, expiresIn } = body;

    if (!token || !expiresIn) {
      return new Response(
        JSON.stringify({ error: 'Missing token or expiresIn' }),
        { status: 400, headers: { 'Content-Type': 'application/json' } }
      );
    }

    // Set the auth cookie
    setAuthCookie(cookies, token, expiresIn);

    return new Response(
      JSON.stringify({ success: true }),
      { status: 200, headers: { 'Content-Type': 'application/json' } }
    );
  } catch (error) {
    console.error('Error setting auth cookie:', error);
    return new Response(
      JSON.stringify({ error: 'Failed to set cookie' }),
      { status: 500, headers: { 'Content-Type': 'application/json' } }
    );
  }
};
