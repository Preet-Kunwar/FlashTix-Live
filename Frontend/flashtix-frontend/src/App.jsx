import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Login from './pages/Login';
import Register from './pages/Register'; // NEW
import Dashboard from './pages/Dashboard';
import MyTickets from './pages/MyTickets';
import CreateEvent from './pages/CreateEvent'; // NEW

function App() {
    return (
        <AuthProvider>
            <Router>
                <Routes>
                    <Route path="/" element={<Navigate to="/login" />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} /> {/* NEW */}
                    <Route path="/dashboard" element={<Dashboard />} />
                    <Route path="/my-tickets" element={<MyTickets />} /> 
                    <Route path="/create-event" element={<CreateEvent />} /> {/* NEW */}
                </Routes>
            </Router>
        </AuthProvider>
    );
}

export default App;