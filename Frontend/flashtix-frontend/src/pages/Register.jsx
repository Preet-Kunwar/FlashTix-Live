import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api/axiosConfig';

export default function Register() {
    const [formData, setFormData] = useState({ username: '', email: '', password: '' });
    const [status, setStatus] = useState({ type: '', message: '' });
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleRegister = async (e) => {
        e.preventDefault();
        setLoading(true);
        setStatus({ type: '', message: '' });

        try {
            await api.post('/api/auth/register', formData);
            setStatus({ type: 'success', message: 'Registration successful! Redirecting to login...' });
            setTimeout(() => navigate('/login'), 2000);
        } catch (error) {
            setStatus({ type: 'error', message: 'Registration failed. Username or email might be taken.' });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={styles.pageContainer}>
            <div style={styles.card}>
                <div style={styles.header}>
                    <h2 style={styles.title}>Join FlashTix</h2>
                    <p style={styles.subtitle}>Create an account to grab live tickets.</p>
                </div>

                {status.message && (
                    <div style={{ ...styles.alert, backgroundColor: status.type === 'success' ? '#d4edda' : '#f8d7da', color: status.type === 'success' ? '#155724' : '#721c24' }}>
                        {status.message}
                    </div>
                )}

                <form onSubmit={handleRegister} style={styles.form}>
                    <div style={styles.inputGroup}>
                        <label style={styles.label}>Username</label>
                        <input type="text" name="username" value={formData.username} onChange={handleChange} required style={styles.input} placeholder="e.g., anuj" />
                    </div>
                    <div style={styles.inputGroup}>
                        <label style={styles.label}>Email Address</label>
                        <input type="email" name="email" value={formData.email} onChange={handleChange} required style={styles.input} placeholder="anuj@example.com" />
                    </div>
                    <div style={styles.inputGroup}>
                        <label style={styles.label}>Password</label>
                        <input type="password" name="password" value={formData.password} onChange={handleChange} required style={styles.input} placeholder="••••••••" />
                    </div>
                    <button type="submit" disabled={loading} style={styles.primaryButton}>
                        {loading ? 'Creating Account...' : 'Sign Up'}
                    </button>
                </form>
                <p style={styles.footerText}>
                    Already have an account? <Link to="/login" style={styles.link}>Log In here</Link>
                </p>
            </div>
        </div>
    );
}

// Premium Inline Styles
const styles = {
    pageContainer: { minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'linear-gradient(135deg, #fdfbfb 0%, #ebedee 100%)', fontFamily: "'Inter', sans-serif" },
    card: { backgroundColor: '#ffffff', padding: '40px', borderRadius: '16px', boxShadow: '0 10px 30px rgba(0,0,0,0.08)', width: '100%', maxWidth: '400px' },
    header: { textAlign: 'center', marginBottom: '30px' },
    title: { margin: 0, fontSize: '28px', color: '#1a1a1a', fontWeight: '700' },
    subtitle: { margin: '8px 0 0 0', color: '#666', fontSize: '15px' },
    alert: { padding: '12px', borderRadius: '8px', marginBottom: '20px', fontSize: '14px', textAlign: 'center', fontWeight: '500' },
    form: { display: 'flex', flexDirection: 'column', gap: '20px' },
    inputGroup: { display: 'flex', flexDirection: 'column', gap: '6px' },
    label: { fontSize: '14px', color: '#333', fontWeight: '600' },
    input: { padding: '12px 16px', borderRadius: '8px', border: '1px solid #ddd', fontSize: '15px', outline: 'none', transition: 'border 0.2s', backgroundColor: '#fafafa' },
    primaryButton: { padding: '14px', backgroundColor: '#6366f1', color: '#fff', border: 'none', borderRadius: '8px', fontSize: '16px', fontWeight: '600', cursor: 'pointer', transition: 'transform 0.1s, backgroundColor 0.2s', boxShadow: '0 4px 12px rgba(99, 102, 241, 0.3)' },
    footerText: { textAlign: 'center', marginTop: '25px', fontSize: '14px', color: '#666' },
    link: { color: '#6366f1', textDecoration: 'none', fontWeight: '600' }
};