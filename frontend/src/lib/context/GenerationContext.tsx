import { createContext, useContext, type ReactNode } from "react";
import { useGenerationWorkflow } from "@/lib/hooks/useGenerationWorkflow";
import type { GenerationContextType } from "@/lib/api/types";

/**
 * Context dla workflow generowania fiszek AI
 */
const GenerationContext = createContext<GenerationContextType | undefined>(undefined);

/**
 * Hook do używania GenerationContext
 * @throws Error jeśli użyty poza GenerationProvider
 */
export const useGeneration = (): GenerationContextType => {
  const context = useContext(GenerationContext);
  if (!context) {
    throw new Error("useGeneration must be used within GenerationProvider");
  }
  return context;
};

/**
 * Provider zarządzający stanem workflow generowania AI
 */
export const GenerationProvider = ({ children }: { children: ReactNode }) => {
  const workflow = useGenerationWorkflow();

  return <GenerationContext.Provider value={workflow}>{children}</GenerationContext.Provider>;
};
