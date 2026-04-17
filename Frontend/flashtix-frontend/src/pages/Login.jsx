import { useState, useContext } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import api from '../api/axiosConfig';

export default function Login() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    
    const { login } = useContext(AuthContext);
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            // API Call matching your logic
            const response = await api.post('/api/auth/login', {
                username,
                password
            });

            // Extract tokens
            const { accessToken, refreshToken } = response.data;

            // Update AuthContext state
            login(accessToken, refreshToken);

            // Redirect to dashboard
            navigate('/dashboard');

        } catch (err) {
            setError('Invalid username or password. Please try again.');
            console.error("Login failed", err);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={styles.pageContainer}>
            <div style={styles.card}>
                <div style={styles.header}>
                    <h2 style={styles.title}>Welcome Back</h2>
                    <p style={styles.subtitle}>Log in to access live ticket drops.</p>
                </div>

                {error && (
                    <div style={styles.errorAlert}>
                        {error}
                    </div>
                )}

                <form onSubmit={handleLogin} style={styles.form}>
                    <div style={styles.inputGroup}>
                        <label style={styles.label}>Username</label>
                        <input 
                            type="text" 
                            value={username} 
                            onChange={(e) => setUsername(e.target.value)} 
                            required 
                            style={styles.input} 
                            placeholder="Enter your username" 
                        />
                    </div>
                    
                    <div style={styles.inputGroup}>
                        <label style={styles.label}>Password</label>
                        <input 
                            type="password" 
                            value={password} 
                            onChange={(e) => setPassword(e.target.value)} 
                            required 
                            style={styles.input} 
                            placeholder="••••••••" 
                        />
                    </div>

                    <button type="submit" disabled={loading} style={styles.primaryButton}>
                        {loading ? 'Verifying...' : 'Log In'}
                    </button>
                </form>

                <p style={styles.footerText}>
                    Don't have an account? <Link to="/register" style={styles.link}>Sign Up here</Link>
                </p>
            </div>
        </div>
    );
}

const styles = {
    pageContainer: { 
        minHeight: '100vh', 
        display: 'flex', 
        alignItems: 'center', 
        justifyContent: 'center', 
        background: 'linear-gradient(135deg, #fdfbfb 0%, #ebedee 100%)', 
        fontFamily: "'Inter', sans-serif" 
    },
    card: { 
        backgroundColor: '#ffffff', 
        padding: '40px', 
        borderRadius: '16px', 
        boxShadow: '0 10px 30px rgba(0,0,0,0.08)', 
        width: '100%', 
        maxWidth: '400px' 
    },
    header: { textAlign: 'center', marginBottom: '30px' },
    title: { margin: 0, fontSize: '28px', color: '#1a1a1a', fontWeight: '700' },
    subtitle: { margin: '8px 0 0 0', color: '#666', fontSize: '15px' },
    errorAlert: { 
        padding: '12px', 
        borderRadius: '8px', 
        marginBottom: '20px', 
        fontSize: '14px', 
        textAlign: 'center', 
        fontWeight: '500', 
        backgroundColor: '#f8d7da', 
        color: '#721c24' 
    },
    form: { display: 'flex', flexDirection: 'column', gap: '20px' },
    inputGroup: { display: 'flex', flexDirection: 'column', gap: '6px' },
    label: { fontSize: '14px', color: '#333', fontWeight: '600' },
    input: { 
        padding: '12px 16px', 
        borderRadius: '8px', 
        border: '1px solid #ddd', 
        fontSize: '15px', 
        outline: 'none', 
        backgroundColor: '#fafafa' 
    },
    primaryButton: { 
        padding: '14px', 
        backgroundColor: '#6366f1', 
        color: '#fff', 
        border: 'none', 
        borderRadius: '8px', 
        fontSize: '16px', 
        fontWeight: '600', 
        cursor: 'pointer', 
        boxShadow: '0 4px 12px rgba(99, 102, 241, 0.3)' 
    },
    footerText: { textAlign: 'center', marginTop: '25px', fontSize: '14px', color: '#666' },
    link: { color: '#6366f1', textDecoration: 'none', fontWeight: '600' }
};