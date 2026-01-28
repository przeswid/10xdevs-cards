/**
 * Login Form Wrapper
 * Wraps LoginForm with AuthProvider for use in Astro pages
 */

import React from "react";
import { AuthProvider } from "@/lib/context/AuthContext";
import { LoginForm } from "./LoginForm";

interface LoginFormWrapperProps {
  redirectTo?: string;
}

export function LoginFormWrapper({ redirectTo }: LoginFormWrapperProps) {
  return (
    <AuthProvider>
      <LoginForm redirectTo={redirectTo} />
    </AuthProvider>
  );
}
