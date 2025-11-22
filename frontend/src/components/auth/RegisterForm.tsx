/**
 * Registration Form Component
 * User registration with client-side validation
 */

import React, { useState } from 'react';
import { useAuth } from '@/lib/context/AuthContext';
import { validateRegistrationForm } from '@/lib/validation/auth';
import type { RegisterRequest } from '@/lib/api/types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Alert } from '@/components/ui/alert';

export function RegisterForm() {
  const { register } = useAuth();

  const [formData, setFormData] = useState<RegisterRequest>({
    username: '',
    password: '',
    email: '',
    firstName: '',
    lastName: '',
  });

  const [fieldErrors, setFieldErrors] = useState<Record<string, string[]>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [apiError, setApiError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

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

  const handleBlur = (e: React.FocusEvent<HTMLInputElement>) => {
    const { name } = e.target;

    // Validate single field on blur
    const validation = validateRegistrationForm(formData);
    if (validation.fieldErrors[name]) {
      setFieldErrors((prev) => ({
        ...prev,
        [name]: validation.fieldErrors[name],
      }));
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setApiError(null);
    setSuccessMessage(null);

    // Validate form
    const validation = validateRegistrationForm(formData);
    if (!validation.isValid) {
      setFieldErrors(validation.fieldErrors);
      return;
    }

    setIsSubmitting(true);

    try {
      await register(formData);
      setSuccessMessage('Registration successful! Redirecting...');

      // Redirect to generate page after 1 second
      setTimeout(() => {
        window.location.href = '/generate';
      }, 1000);
    } catch (error: any) {
      setApiError(error.message || 'Registration failed. Please try again.');
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6 w-full max-w-md">
      {apiError && (
        <Alert variant="destructive" className="block">
          <p className="text-sm leading-relaxed">{apiError}</p>
        </Alert>
      )}

      {successMessage && (
        <Alert className="block">
          <p className="text-sm leading-relaxed">{successMessage}</p>
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
          onBlur={handleBlur}
          disabled={isSubmitting}
          className={fieldErrors.username ? 'border-red-500' : ''}
          aria-invalid={!!fieldErrors.username}
          aria-describedby={fieldErrors.username ? 'username-error' : undefined}
        />
        {fieldErrors.username && (
          <p id="username-error" className="text-sm text-red-500">
            {fieldErrors.username[0]}
          </p>
        )}
      </div>

      <div className="space-y-2">
        <Label htmlFor="email">Email</Label>
        <Input
          id="email"
          name="email"
          type="email"
          value={formData.email}
          onChange={handleChange}
          onBlur={handleBlur}
          disabled={isSubmitting}
          className={fieldErrors.email ? 'border-red-500' : ''}
          aria-invalid={!!fieldErrors.email}
          aria-describedby={fieldErrors.email ? 'email-error' : undefined}
        />
        {fieldErrors.email && (
          <p id="email-error" className="text-sm text-red-500">
            {fieldErrors.email[0]}
          </p>
        )}
      </div>

      <div className="space-y-2">
        <Label htmlFor="firstName">First Name</Label>
        <Input
          id="firstName"
          name="firstName"
          type="text"
          value={formData.firstName}
          onChange={handleChange}
          onBlur={handleBlur}
          disabled={isSubmitting}
          className={fieldErrors.firstName ? 'border-red-500' : ''}
          aria-invalid={!!fieldErrors.firstName}
          aria-describedby={fieldErrors.firstName ? 'firstName-error' : undefined}
        />
        {fieldErrors.firstName && (
          <p id="firstName-error" className="text-sm text-red-500">
            {fieldErrors.firstName[0]}
          </p>
        )}
      </div>

      <div className="space-y-2">
        <Label htmlFor="lastName">Last Name</Label>
        <Input
          id="lastName"
          name="lastName"
          type="text"
          value={formData.lastName}
          onChange={handleChange}
          onBlur={handleBlur}
          disabled={isSubmitting}
          className={fieldErrors.lastName ? 'border-red-500' : ''}
          aria-invalid={!!fieldErrors.lastName}
          aria-describedby={fieldErrors.lastName ? 'lastName-error' : undefined}
        />
        {fieldErrors.lastName && (
          <p id="lastName-error" className="text-sm text-red-500">
            {fieldErrors.lastName[0]}
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
          onBlur={handleBlur}
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
        <p className="text-sm text-muted-foreground">
          Must be 8-100 characters with uppercase, lowercase, number, and special character
        </p>
      </div>

      <Button type="submit" className="w-full" disabled={isSubmitting}>
        {isSubmitting ? 'Registering...' : 'Register'}
      </Button>

      <p className="text-center text-sm text-muted-foreground">
        Already have an account?{' '}
        <a href="/login" className="text-primary hover:underline">
          Login
        </a>
      </p>
    </form>
  );
}
