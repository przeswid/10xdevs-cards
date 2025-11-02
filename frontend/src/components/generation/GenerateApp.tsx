import { GenerationProvider } from '@/lib/context/GenerationContext';
import { GenerateView } from './GenerateView';

/**
 * Root komponent aplikacji generowania
 * Wrappuje GenerateView w GenerationProvider
 */
export const GenerateApp = () => {
  return (
    <GenerationProvider>
      <GenerateView />
    </GenerationProvider>
  );
};
