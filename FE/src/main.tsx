import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import Router from './app/routes';
import { Header } from '@widget/header';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <div className="app-layout">
      <Header />
      <main>
        <Router />
      </main>
    </div>
  </StrictMode>,
)
