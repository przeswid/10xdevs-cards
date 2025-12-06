/**
 * Register Form Wrapper
 * Wraps RegisterForm with AuthProvider for use in Astro pages
 */

import React from "react";
import { AuthProvider } from "@/lib/context/AuthContext";
import { RegisterForm } from "./RegisterForm";

export function RegisterFormWrapper() {
  return (
    <AuthProvider>
      <RegisterForm />
    </AuthProvider>
  );
}
