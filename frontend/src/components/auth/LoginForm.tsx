/**
 * Login Form Component
 * User authentication with client-side validation
 */

import React, { useState, useEffect } from 'react';
import { useAuth } from '@/lib/context/AuthContext';
import { validateLoginForm } from '@/lib/validation/auth';
import type { LoginRequest } from '@/lib/api/types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Alert } from '@/components/ui/alert';

interface LoginFormProps {
  redirectTo?: string;
}

export function LoginForm({ redirectTo }: LoginFormProps) {
  const { login } = useAuth();

  const [formData, setFormData] = useState<LoginRequest>({
    username: '',
    password: '',
  });

  const [fieldErrors, setFieldErrors] = useState<Record<string, string[]>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [apiError, setApiError] = useState<string | null>(null);
  const [sessionExpired, setSessionExpired] = useState(false);

  // Check for session expiration message
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    if (params.get('session') === 'expired') {
      setSessionExpired(true);
    }
  }, []);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));

    // Clear field error on change
    if (fieldErrors[name]) {
      setFieldErrors((prev) => {
        const newErrors = { ...prev };
        delete newErrors[name];
        return newErrors;
      });
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setApiError(null);
    setSessionExpired(false);

    // Validate form
    const validation = validateLoginForm(formData);
    if (!validation.isValid) {
      setFieldErrors(validation.fieldErrors);
      return;
    }

    setIsSubmitting(true);

    try {
      await login(formData.username, formData.password);

      // Redirect to specified page or default to /generate
      const destination = redirectTo || '/generate';
      window.location.href = destination;
    } catch (error: any) {
      setApiError(error.message || 'Login failed. Please try again.');
      setIsSubmitting(false);

      // Clear password field for security
      setFormData((prev) => ({ ...prev, password: '' }));
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6 w-full max-w-md">
      {sessionExpired && (
        <Alert variant="destructive" className="block">
          <p className="text-sm leading-relaxed">Your session has expired. Please log in again.</p>
        </Alert>
      )}

      {apiError && (
        <Alert variant="destructive" className="block">
          <p className="text-sm leading-relaxed">{apiError}</p>
        </Alert>
      )}

      <div className="space-y-2">
        <Label htmlFor="username">Username</Label>
        <Input
          id="username"
          name="username"
          type="text"
          value={formData.username}
          onChange={handleChange}
          disabled={isSubmitting}
          className={fieldErrors.username ? 'border-red-500' : ''}
          aria-invalid={!!fieldErrors.username}
          aria-describedby={fieldErrors.username ? 'username-error' : undefined}
          autoFocus
        />
        {fieldErrors.username && (
          <p id="username-error" className="text-sm text-red-500">
            {fieldErrors.username[0]}
          </p>
        )}
      </div>

      <div className="space-y-2">
        <Label htmlFor="password">Password</Label>
        <Input
          id="password"
          name="password"
          type="password"
          value={formData.password}
          onChange={handleChange}
          disabled={isSubmitting}
          className={fieldErrors.password ? 'border-red-500' : ''}
          aria-invalid={!!fieldErrors.password}
          aria-describedby={fieldErrors.password ? 'password-error' : undefined}
        />
        {fieldErrors.password && (
          <p id="password-error" className="text-sm text-red-500">
            {fieldErrors.password[0]}
          </p>
        )}
      </div>

      <Button type="submit" className="w-full" disabled={isSubmitting}>
        {isSubmitting ? 'Signing in...' : 'Login'}
      </Button>

      <p className="text-center text-sm text-muted-foreground">
        Don't have an account?{' '}
        <a href="/register" className="text-primary hover:underline">
          Register
        </a>
      </p>
    </form>
  );
}
