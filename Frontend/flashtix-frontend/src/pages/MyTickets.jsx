import { useState, useEffect, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import { Link } from 'react-router-dom';
import api from '../api/axiosConfig';

export default function MyTickets() {
    const { user, logout } = useContext(AuthContext);
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [downloadingId, setDownloadingId] = useState(null);

    useEffect(() => {
        const fetchOrders = async () => {
            try {
                const response = await api.get('/api/orders/my-tickets');
                
                // NEW: Sort the orders sequentially by orderDate (newest first)
                const sortedOrders = response.data.sort((a, b) => 
                    new Date(b.orderDate) - new Date(a.orderDate)
                );
                
                setOrders(sortedOrders);
            } catch (error) {
                console.error("Failed to fetch orders", error);
            } finally {
                setLoading(false);
            }
        };

        if (user) fetchOrders();
    }, [user]);

    const handleDownload = async (orderId) => {
        setDownloadingId(orderId);
        try {
            // Using your exact download URL endpoint
            const response = await api.get(`/api/orders/${orderId}/download`, {
                responseType: 'blob' 
            });

            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', `FlashTix_Ticket_${orderId}.pdf`);
            document.body.appendChild(link);
            link.click();
            
            link.parentNode.removeChild(link);
            window.URL.revokeObjectURL(url);

        } catch (error) {
            console.error("Download failed", error);
            alert("Failed to download ticket. Please try again.");
        } finally {
            setDownloadingId(null);
        }
    };

    if (!user) return <div style={{ textAlign: 'center', marginTop: '50px' }}>Please log in.</div>;

    return (
        <div style={{ padding: '20px', fontFamily: 'sans-serif', maxWidth: '1000px', margin: '0 auto' }}>
            <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '2px solid #eee', paddingBottom: '15px', marginBottom: '30px' }}>
                <h1 style={{ margin: 0, color: '#333' }}>🎫 My Tickets</h1>
                <div style={{ display: 'flex', gap: '15px', alignItems: 'center' }}>
                    <Link to="/dashboard" style={{ textDecoration: 'none', color: '#007bff', fontWeight: 'bold' }}>
                        ← Back to Live Events
                    </Link>
                    <button onClick={logout} style={{ padding: '8px 15px', backgroundColor: '#dc3545', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer', fontWeight: 'bold' }}>
                        Log Out
                    </button>
                </div>
            </header>

            {loading ? (
                <p>Loading your ticket history...</p>
            ) : orders.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '50px', backgroundColor: '#f8f9fa', borderRadius: '8px' }}>
                    <h3>You haven't purchased any tickets yet!</h3>
                    <Link to="/dashboard" style={{ display: 'inline-block', marginTop: '15px', padding: '10px 20px', backgroundColor: '#28a745', color: 'white', textDecoration: 'none', borderRadius: '5px', fontWeight: 'bold' }}>
                        Browse Concerts
                    </Link>
                </div>
            ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                    {orders.map(order => (
                        // Changed order.id to order.orderId to match your JSON
                        <div key={order.orderId} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', border: '1px solid #ddd', borderRadius: '8px', padding: '20px', backgroundColor: '#fff' }}>
                            <div>
                                <h3 style={{ margin: '0 0 10px 0', color: '#0056b3' }}>Order #{order.orderId} - {order.eventName}</h3>
                                
                                {/* Format the date beautifully */}
                                <p style={{ margin: '5px 0', color: '#555' }}><strong>Date:</strong> {new Date(order.orderDate).toLocaleString()}</p>
                                
                                <p style={{ margin: '5px 0', color: '#555' }}><strong>Quantity:</strong> {order.quantity} Ticket(s)</p>
                                
                                {/* Changed totalPrice to totalAmount */}
                                <p style={{ margin: '5px 0', color: '#555' }}><strong>Total Paid:</strong> ${order.totalAmount}</p>
                                
                                {/* Changed COMPLETED to CONFIRMED */}
                                <p style={{ margin: '5px 0', fontSize: '14px', color: order.status === 'CONFIRMED' ? 'green' : '#ffc107' }}>
                                    Status: <strong>{order.status}</strong>
                                </p>
                            </div>
                            
                            <button 
                                onClick={() => handleDownload(order.orderId)}
                                // Button is now checking for CONFIRMED to unlock
                                disabled={downloadingId === order.orderId || order.status !== 'CONFIRMED'}
                                style={{ 
                                    padding: '12px 20px', 
                                    backgroundColor: (downloadingId === order.orderId || order.status !== 'CONFIRMED') ? '#6c757d' : '#17a2b8', 
                                    color: 'white', 
                                    border: 'none', 
                                    borderRadius: '5px', 
                                    fontSize: '16px',
                                    fontWeight: 'bold',
                                    cursor: (downloadingId === order.orderId || order.status !== 'CONFIRMED') ? 'not-allowed' : 'pointer',
                                    transition: 'background-color 0.2s'
                                }}
                            >
                                {downloadingId === order.orderId ? 'Downloading...' : '📥 Download PDF'}
                            </button>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}