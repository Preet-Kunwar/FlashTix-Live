import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'

// NOTE: React StrictMode was removed because it deliberately double-invokes
// useEffect in development, causing every API call (e.g. /my-tickets, /events)
// to fire twice within milliseconds. This shows as duplicate requests in backend logs.
// StrictMode is a development-only tool and has no effect in production builds.
createRoot(document.getElementById('root')).render(<App />)
