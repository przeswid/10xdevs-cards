/**
 * App Header Navigation Component
 * Authentication-aware navigation bar
 */

import React from "react";
import { useAuth } from "@/lib/context/AuthContext";
import { Button } from "@/components/ui/button";

interface AppHeaderProps {
  showAuthButtons?: boolean;
}

export function AppHeader({ showAuthButtons = true }: AppHeaderProps) {
  const { user, isAuthenticated, logout } = useAuth();

  const handleLogout = async () => {
    await logout();
    window.location.href = "/login";
  };

  return (
    <header className="border-b bg-background">
      <div className="container mx-auto px-4 py-4 flex items-center justify-between">
        <div className="flex items-center space-x-6">
          <a href="/" className="text-xl font-bold">
            10x Cards
          </a>

          {isAuthenticated && (
            <nav className="hidden md:flex items-center space-x-4">
              <a
                href="/generate"
                className="text-sm font-medium text-muted-foreground hover:text-foreground transition-colors"
              >
                Generate
              </a>
              <a
                href="/flashcards"
                className="text-sm font-medium text-muted-foreground hover:text-foreground transition-colors"
              >
                Flashcards
              </a>
            </nav>
          )}
        </div>

        {showAuthButtons && (
          <div className="flex items-center space-x-4">
            {isAuthenticated ? (
              <>
                <span className="text-sm text-muted-foreground hidden sm:inline">
                  {user?.firstName} {user?.lastName}
                </span>
                <Button variant="outline" size="sm" onClick={handleLogout}>
                  Logout
                </Button>
              </>
            ) : (
              <>
                <Button variant="ghost" size="sm" asChild>
                  <a href="/login">Login</a>
                </Button>
                <Button size="sm" asChild>
                  <a href="/register">Register</a>
                </Button>
              </>
            )}
          </div>
        )}
      </div>
    </header>
  );
}
