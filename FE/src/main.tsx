import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

import './index.css';
import Router from './app/routes';
import { consumeOAuthRedirect } from './shared/api/auth';
import { Header } from '@widget/header';

const queryClient = new QueryClient();

consumeOAuthRedirect();

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <div className="app-layout">
        <Header />
        <main>
          <Router />
        </main>
      </div>
    </QueryClientProvider>
  </StrictMode>,
);
