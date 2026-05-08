import React, { useState, useRef, useEffect } from 'react';
import { MessageSquare, X, Send } from 'lucide-react';

const ChatbotWidget = () => {
    const [isOpen, setIsOpen] = useState(false);
    const [messages, setMessages] = useState([
        { text: "Hi! I'm the HMS Assistant. How can I help you today?", isBot: true }
    ]);
    const [input, setInput] = useState('');
    const messagesEndRef = useRef(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    useEffect(() => {
        if (isOpen) scrollToBottom();
    }, [messages, isOpen]);

    const handleSend = (e) => {
        e.preventDefault();
        if (!input.trim()) return;

        // Add user message
        const newMessages = [...messages, { text: input, isBot: false }];
        setMessages(newMessages);
        setInput('');

        // Simulate bot response (simplified FAQ logic)
        setTimeout(() => {
            let response = "I'm still learning! Please contact the front desk at +1 800-123-4567 for specific inquiries.";
            const lowerInput = input.toLowerCase();

            if (lowerInput.includes('book') || lowerInput.includes('appointment')) {
                response = "You can book an appointment by navigating to 'Find a Doctor' in your Patient Dashboard.";
            } else if (lowerInput.includes('prescription')) {
                response = "Your prescriptions are available in the 'My Prescriptions' tab. You can download the PDF there.";
            } else if (lowerInput.includes('password') || lowerInput.includes('login')) {
                response = "If you forgot your password, use the 'Forgot Password' link on the login page.";
            } else if (lowerInput.includes('hello') || lowerInput.includes('hi')) {
                response = "Hello! How can I assist you tracking an appointment or doctor?";
            }

            setMessages(prev => [...prev, { text: response, isBot: true }]);
        }, 600);
    };

    return (
        <>
            <button
                onClick={() => setIsOpen(true)}
                style={{
                    position: 'fixed', bottom: '2rem', right: '2rem', zIndex: 9999,
                    width: '60px', height: '60px', borderRadius: '50%',
                    backgroundColor: 'var(--primary)', color: 'white',
                    border: 'none', cursor: 'pointer', display: isOpen ? 'none' : 'flex',
                    alignItems: 'center', justifyContent: 'center',
                    boxShadow: '0 10px 25px -5px rgba(2, 132, 199, 0.5)',
                    transition: 'transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)'
                }}
                onMouseEnter={e => e.currentTarget.style.transform = 'scale(1.1)'}
                onMouseLeave={e => e.currentTarget.style.transform = 'scale(1)'}
            >
                <MessageSquare size={28} />
            </button>

            {isOpen && (
                <div style={{
                    position: 'fixed', bottom: '2rem', right: '2rem', zIndex: 9999,
                    width: '350px', height: '500px', backgroundColor: 'var(--surface)',
                    borderRadius: 'var(--radius-lg)', boxShadow: 'var(--shadow-lg)',
                    display: 'flex', flexDirection: 'column', overflow: 'hidden',
                    animation: 'entrance 0.3s ease-out forwards',
                    border: '1px solid var(--border)'
                }}>
                    {/* Header */}
                    <div style={{
                        backgroundColor: 'var(--primary)', color: 'white', padding: '1rem',
                        display: 'flex', justifyContent: 'space-between', alignItems: 'center'
                    }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 600 }}>
                            <MessageSquare size={20} /> HMS Assistant
                        </div>
                        <button onClick={() => setIsOpen(false)} style={{ background: 'none', border: 'none', color: 'white', cursor: 'pointer', display: 'flex' }}>
                            <X size={20} />
                        </button>
                    </div>

                    {/* Chat Area */}
                    <div style={{ flex: 1, padding: '1rem', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '1rem', backgroundColor: '#f8fafc' }}>
                        {messages.map((msg, idx) => (
                            <div key={idx} style={{
                                alignSelf: msg.isBot ? 'flex-start' : 'flex-end',
                                maxWidth: '85%',
                                backgroundColor: msg.isBot ? 'white' : 'var(--primary)',
                                color: msg.isBot ? 'var(--text-main)' : 'white',
                                padding: '0.75rem 1rem',
                                borderRadius: msg.isBot ? '1rem 1rem 1rem 0' : '1rem 1rem 0 1rem',
                                border: msg.isBot ? '1px solid var(--border)' : 'none',
                                fontSize: '0.9rem', lineHeight: 1.4,
                                boxShadow: 'var(--shadow-sm)'
                            }}>
                                {msg.text}
                            </div>
                        ))}
                        <div ref={messagesEndRef} />
                    </div>

                    {/* Input Area */}
                    <form onSubmit={handleSend} style={{
                        padding: '1rem', borderTop: '1px solid var(--border)', display: 'flex', gap: '0.5rem', background: 'var(--surface)'
                    }}>
                        <input
                            type="text"
                            value={input}
                            onChange={e => setInput(e.target.value)}
                            placeholder="Type your message..."
                            style={{
                                flex: 1, padding: '0.75rem 1rem', borderRadius: '2rem',
                                border: '1px solid var(--border)', outline: 'none', fontSize: '0.9rem'
                            }}
                        />
                        <button type="submit" disabled={!input.trim()} style={{
                            width: '40px', height: '40px', borderRadius: '50%',
                            backgroundColor: input.trim() ? 'var(--primary)' : '#cbd5e1',
                            color: 'white', border: 'none', display: 'flex', alignItems: 'center', justifyContent: 'center',
                            cursor: input.trim() ? 'pointer' : 'default', transition: 'background-color 0.2s'
                        }}>
                            <Send size={18} style={{ marginLeft: '-2px' }} />
                        </button>
                    </form>
                </div>
            )}
        </>
    );
};

export default ChatbotWidget;
