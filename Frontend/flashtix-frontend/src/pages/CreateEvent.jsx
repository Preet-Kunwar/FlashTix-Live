import { useState, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import { Link } from 'react-router-dom';
import api from '../api/axiosConfig';

export default function CreateEvent() {
    const { user, logout } = useContext(AuthContext);
    const [formData, setFormData] = useState({
        name: '', description: '', eventDate: '', flashSaleStartTime: '', totalTickets: '', ticketPrice: ''
    });
    const [status, setStatus] = useState({ type: '', message: '' });

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setStatus({ type: '', message: '' });

        try {
            // Ensure numbers are parsed correctly before sending the payload
            const payload = {
                ...formData,
                totalTickets: parseInt(formData.totalTickets),
                ticketPrice: parseFloat(formData.ticketPrice)
            };

            await api.post('/api/events', payload);
            setStatus({ type: 'success', message: 'Event successfully created and published!' });
            setFormData({ name: '', description: '', eventDate: '', flashSaleStartTime: '', totalTickets: '', ticketPrice: '' });
        } catch (error) {
            setStatus({ type: 'error', message: 'Failed to create event. Ensure you have ADMIN privileges.' });
        }
    };

    if (!user) return <div style={{ textAlign: 'center', marginTop: '50px' }}>Please log in.</div>;

    return (
        <div style={styles.dashboardContainer}>
            <header style={styles.navBar}>
                <h1 style={styles.logo}>⚡ FlashTix Admin</h1>
                <div style={styles.navLinks}>
                    <Link to="/dashboard" style={styles.textLink}>Live Dashboard</Link>
                    <button onClick={logout} style={styles.logoutBtn}>Log Out</button>
                </div>
            </header>

            <div style={styles.contentWrapper}>
                <div style={styles.card}>
                    <h2 style={styles.title}>Create New Event</h2>
                    <p style={styles.subtitle}>Fill in the details to launch a new concert or flash sale.</p>
                    
                    {status.message && (
                        <div style={{ ...styles.alert, backgroundColor: status.type === 'success' ? '#d4edda' : '#f8d7da', color: status.type === 'success' ? '#155724' : '#721c24' }}>
                            {status.message}
                        </div>
                    )}

                    <form onSubmit={handleSubmit} style={styles.formGrid}>
                        <div style={{ ...styles.inputGroup, gridColumn: 'span 2' }}>
                            <label style={styles.label}>Event Name</label>
                            <input type="text" name="name" value={formData.name} onChange={handleChange} required style={styles.input} placeholder="e.g., KK - Mumbai" />
                        </div>
                        
                        <div style={{ ...styles.inputGroup, gridColumn: 'span 2' }}>
                            <label style={styles.label}>Description</label>
                            <textarea name="description" value={formData.description} onChange={handleChange} required style={{ ...styles.input, minHeight: '80px' }} placeholder="Live at Mumbai Stadium..." />
                        </div>

                        <div style={styles.inputGroup}>
                            <label style={styles.label}>Event Date & Time</label>
                            {/* Uses datetime-local to format nicely for Spring Boot ISO-8601 */}
                            <input type="datetime-local" name="eventDate" value={formData.eventDate} onChange={handleChange} required style={styles.input} />
                        </div>

                        <div style={styles.inputGroup}>
                            <label style={styles.label}>Flash Sale Start Time</label>
                            <input type="datetime-local" name="flashSaleStartTime" value={formData.flashSaleStartTime} onChange={handleChange} required style={styles.input} />
                        </div>

                        <div style={styles.inputGroup}>
                            <label style={styles.label}>Total Tickets</label>
                            <input type="number" name="totalTickets" value={formData.totalTickets} onChange={handleChange} required min="1" style={styles.input} placeholder="1000" />
                        </div>

                        <div style={styles.inputGroup}>
                            <label style={styles.label}>Ticket Price ($)</label>
                            <input type="number" step="0.01" name="ticketPrice" value={formData.ticketPrice} onChange={handleChange} required min="0" style={styles.input} placeholder="2500.00" />
                        </div>

                        <div style={{ gridColumn: 'span 2', marginTop: '10px' }}>
                            <button type="submit" style={styles.primaryButton}>Publish Event</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
}

const styles = {
    dashboardContainer: { minHeight: '100vh', backgroundColor: '#f4f6f8', fontFamily: "'Inter', sans-serif" },
    navBar: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '15px 40px', backgroundColor: '#ffffff', boxShadow: '0 2px 10px rgba(0,0,0,0.05)' },
    logo: { margin: 0, fontSize: '22px', color: '#1a1a1a', fontWeight: '800' },
    navLinks: { display: 'flex', gap: '20px', alignItems: 'center' },
    textLink: { color: '#6366f1', textDecoration: 'none', fontWeight: '600' },
    logoutBtn: { padding: '8px 16px', backgroundColor: '#ef4444', color: '#fff', border: 'none', borderRadius: '6px', fontWeight: '600', cursor: 'pointer' },
    contentWrapper: { padding: '40px', display: 'flex', justifyContent: 'center' },
    card: { backgroundColor: '#ffffff', padding: '40px', borderRadius: '16px', boxShadow: '0 10px 30px rgba(0,0,0,0.04)', width: '100%', maxWidth: '800px' },
    title: { margin: 0, fontSize: '26px', color: '#1a1a1a', fontWeight: '700' },
    subtitle: { margin: '8px 0 25px 0', color: '#666', fontSize: '15px' },
    alert: { padding: '12px', borderRadius: '8px', marginBottom: '20px', fontSize: '14px', textAlign: 'center', fontWeight: '500' },
    formGrid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' },
    inputGroup: { display: 'flex', flexDirection: 'column', gap: '6px' },
    label: { fontSize: '14px', color: '#444', fontWeight: '600' },
    input: { padding: '12px 16px', borderRadius: '8px', border: '1px solid #ddd', fontSize: '15px', outline: 'none', backgroundColor: '#fafafa' },
    primaryButton: { width: '100%', padding: '14px', backgroundColor: '#10b981', color: '#fff', border: 'none', borderRadius: '8px', fontSize: '16px', fontWeight: '700', cursor: 'pointer', transition: 'background-color 0.2s', boxShadow: '0 4px 12px rgba(16, 185, 129, 0.3)' }
};