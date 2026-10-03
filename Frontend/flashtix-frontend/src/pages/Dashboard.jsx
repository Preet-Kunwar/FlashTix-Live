import { useState, useEffect, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import api from '../api/axiosConfig';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Link } from 'react-router-dom';

export default function Dashboard() {
    const { user, logout } = useContext(AuthContext);
    const [events, setEvents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [quantities, setQuantities] = useState({});
    const [processingEvents, setProcessingEvents] = useState({}); 
    const [messages, setMessages] = useState({}); 

    // 1. Fetch the initial list of events
    useEffect(() => {
        const fetchEvents = async () => {
            try {
                const response = await api.get('/api/events');
                setEvents(response.data);
            } catch (error) {
                console.error("Failed to fetch events", error);
            } finally {
                setLoading(false);
            }
        };

        if (user) fetchEvents();
    }, [user]);

    // 2. Initialize WebSockets to listen for live updates
    useEffect(() => {
        console.log("WebSocket Effect Triggered! Current event count:", events.length);

        if (events.length === 0) {
            console.log("Waiting for events to load from database before connecting WebSockets...");
            return; 
        }

        console.log("Events loaded! Attempting to connect to Spring Boot WebSockets...");

        const stompClient = new Client({
            // Connect to the Monolith port (8080) for WebSockets
            webSocketFactory: () => new SockJS('http://localhost:8080/ws-ticketing'),
            debug: (str) => console.log("STOMP DEBUG: " + str),
            reconnectDelay: 5000, // Auto-reconnect if the server restarts
            onConnect: () => {
                console.log('✅ SUCCESS: Connected to Live Ticket Broker!');
                
                // Subscribe to the live feed for every event on the page
                events.forEach(event => {
                    stompClient.subscribe(`/topic/events/${event.id}`, (message) => {
                        const newTicketCount = parseInt(message.body);
                        console.log(`Live Update Received: Event ${event.id} -> ${newTicketCount} tickets`);
                        
                        // Update React state dynamically without refreshing!
                        setEvents(prevEvents => prevEvents.map(e => 
                            e.id === event.id ? { ...e, availableTickets: newTicketCount } : e
                        ));
                    });
                });
            },
            onStompError: (frame) => {
                console.error('❌ Broker error: ' + frame.headers['message']);
            },
        });

        stompClient.activate();

        // Cleanup: Disconnect when the user leaves the page or logs out
        return () => {
            console.log("Disconnecting WebSockets...");
            stompClient.deactivate();
        };
    }, [events.length]); 

    // Update the quantity state when the dropdown changes
    const handleQuantityChange = (eventId, value) => {
        setQuantities(prev => ({ ...prev, [eventId]: parseInt(value) }));
    };

    // Handle the Ticket Purchase
    const handleBuyTicket = async (eventId) => {
        // Lock the button instantly to prevent spam clicks
        setProcessingEvents(prev => ({ ...prev, [eventId]: true }));
        const selectedQty = quantities[eventId] || 1;

        try {
            // Send the request to your API Gateway
            await api.post('/api/orders/purchase', null, {
                params: { eventId, quantity: selectedQty }
            });

            setMessages(prev => ({ ...prev, [eventId]: `Success! ${selectedQty} ticket(s) queued.` }));
            
            // Clear message after 4 seconds
            setTimeout(() => setMessages(prev => ({ ...prev, [eventId]: null })), 4000);

        } catch (error) {
            // Handle Rate Limiting (429) vs standard errors
            if (error.response?.status === 429) {
                setMessages(prev => ({ ...prev, [eventId]: "Error: Too many requests. Please slow down." }));
            } else {
                setMessages(prev => ({ ...prev, [eventId]: "Error: Could not process order." }));
            }
        } finally {
            // Unlock the button
            setProcessingEvents(prev => ({ ...prev, [eventId]: false }));
        }
    };

    // Wait for the user context to load
    if (!user) return <div style={{ textAlign: 'center', marginTop: '50px' }}>Please log in.</div>;

    return (
        <div style={{ padding: '20px', fontFamily: 'sans-serif', maxWidth: '1000px', margin: '0 auto' }}>
            {/* Header Section */}
            {/* Header Section */}
<header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '2px solid #eee', paddingBottom: '15px', marginBottom: '30px', flexWrap: 'wrap', gap: '15px' }}>
    <h1 style={{ margin: 0, color: '#1a1a1a', fontWeight: '800' }}>🎟️ FlashTix Live</h1>
    
    <div style={{ display: 'flex', gap: '15px', alignItems: 'center', flexWrap: 'wrap' }}>
        <span style={{ fontSize: '15px', color: '#555' }}>
            Welcome, <strong style={{ color: '#1a1a1a' }}>{user.username}</strong>
        </span>
        
        {/* Only show the Create Event button if the user has the ADMIN role */}
        {user.roles?.includes('ROLE_ADMIN') && (
            <Link to="/create-event" style={{ textDecoration: 'none', padding: '10px 16px', backgroundColor: '#10b981', color: 'white', borderRadius: '8px', fontWeight: '600', fontSize: '14px', boxShadow: '0 4px 12px rgba(16, 185, 129, 0.2)' }}>
                ➕ Create Event
            </Link>
        )}

        <Link to="/my-tickets" style={{ textDecoration: 'none', padding: '10px 16px', backgroundColor: '#6366f1', color: 'white', borderRadius: '8px', fontWeight: '600', fontSize: '14px', boxShadow: '0 4px 12px rgba(99, 102, 241, 0.2)' }}>
            🎫 My Tickets
        </Link>
        
        <button onClick={logout} style={{ padding: '10px 16px', backgroundColor: '#ef4444', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: '600', fontSize: '14px' }}>
            Log Out
        </button>
    </div>
</header>

            <h2>Upcoming Events</h2>
            
            {loading ? (
                <p>Loading concerts...</p>
            ) : events.length === 0 ? (
                <p>No events available right now.</p>
            ) : (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '20px' }}>
                    {events.map(event => {
                        const isSoldOut = event.availableTickets === 0;
                        const isProcessing = processingEvents[event.id];

                        return (
                            <div key={event.id} style={{ border: '1px solid #ddd', borderRadius: '8px', padding: '20px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)', backgroundColor: '#fff', transition: 'all 0.3s' }}>
                                <h3 style={{ marginTop: 0, color: '#0056b3' }}>{event.name}</h3>
                                <p style={{ color: '#555', fontSize: '14px' }}>{event.description}</p>
                                
                                <div style={{ margin: '15px 0', padding: '10px', backgroundColor: '#f8f9fa', borderRadius: '5px' }}>
                                    <p style={{ margin: '5px 0' }}><strong>Date:</strong> {new Date(event.eventDate).toLocaleDateString()}</p>
                                    <p style={{ margin: '5px 0' }}><strong>Price:</strong> ${event.ticketPrice}</p>
                                    
                                    {/* LIVE TICKET COUNTER */}
                                    <p style={{ margin: '5px 0', color: event.availableTickets < 50 ? 'red' : 'green', fontWeight: 'bold', transition: 'color 0.3s' }}>
                                        Tickets Remaining: <span style={{ fontSize: '1.2em' }}>{event.availableTickets}</span>
                                    </p>
                                </div>

                                {/* Dropdown and Button Row */}
                                <div style={{ display: 'flex', gap: '10px', marginTop: '15px' }}>
                                    <select 
                                        value={quantities[event.id] || 1}
                                        onChange={(e) => handleQuantityChange(event.id, e.target.value)}
                                        disabled={isSoldOut || isProcessing}
                                        style={{ padding: '10px', borderRadius: '5px', border: '1px solid #ccc', flex: '1', cursor: isSoldOut ? 'not-allowed' : 'pointer' }}
                                    >
                                        {/* Generate options 1 through 10, capped at available tickets */}
                                        {[...Array(10)].map((_, i) => {
                                            const num = i + 1;
                                            if (num > event.availableTickets) return null;
                                            return <option key={num} value={num}>{num} Ticket{num > 1 ? 's' : ''}</option>;
                                        })}
                                    </select>

                                    <button 
                                        onClick={() => handleBuyTicket(event.id)}
                                        disabled={isSoldOut || isProcessing}
                                        style={{ 
                                            flex: '2', padding: '12px', 
                                            backgroundColor: isSoldOut ? '#ccc' : (isProcessing ? '#6c757d' : '#28a745'), 
                                            color: 'white', border: 'none', borderRadius: '5px', 
                                            fontSize: '16px', fontWeight: 'bold',
                                            cursor: (isSoldOut || isProcessing) ? 'not-allowed' : 'pointer',
                                            transition: 'background-color 0.2s'
                                        }}
                                    >
                                        {isProcessing ? 'Processing...' : (isSoldOut ? 'SOLD OUT' : 'Buy Now')}
                                    </button>
                                </div>

                                {/* Status Messages */}
                                {messages[event.id] && (
                                    <p style={{ marginTop: '15px', fontSize: '14px', color: messages[event.id].includes('Error') ? 'red' : 'green', textAlign: 'center', fontWeight: 'bold', backgroundColor: messages[event.id].includes('Error') ? '#f8d7da' : '#d4edda', padding: '10px', borderRadius: '5px' }}>
                                        {messages[event.id]}
                                    </p>
                                )}
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
}