import { useRef, useState, useCallback } from 'react';
import SockJS from 'sockjs-client';
import { Client, type IMessage, type StompSubscription } from '@stomp/stompjs';

const BASE_URL = import.meta.env.VITE_API_URL || '';
const SOCKET_URL = `${BASE_URL}/ws-stomp`;

export interface UseStompReturn {
    connected: boolean;
    connect: (onConnectedCallback?: () => void) => void;
    disconnect: () => void;
    subscribe: (destination: string, callback: (payload: any) => void) => StompSubscription | null;
    publish: (destination: string, body: any) => void;
}

export const useStomp = (): UseStompReturn => {
    const [connected, setConnected] = useState(false);
    const clientRef = useRef<Client | null>(null);

    const connect = useCallback((onConnectedCallback?: () => void) => {
        if (clientRef.current?.active) return;

        let token = localStorage.getItem('accessToken');
        if (token && token.startsWith('"') && token.endsWith('"')) {
            token = token.slice(1, -1);
        }

        const client = new Client({
            webSocketFactory: () => new SockJS(SOCKET_URL),
            connectHeaders: {
                Authorization: token ? `Bearer ${token}` : '',
            },
            onConnect: () => {
                console.log('STOMP Connected');
                setConnected(true);
                if (onConnectedCallback) onConnectedCallback();
            },
            onDisconnect: () => {
                console.log('STOMP Disconnected');
                setConnected(false);
            },
            onStompError: (frame) => {
                console.error('STOMP Error:', frame.headers['message']);
                console.error('STOMP Details:', frame.body);
            },
            // 자동 재연결 설정
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        client.activate();
        clientRef.current = client;
    }, []);

    const disconnect = useCallback(() => {
        if (clientRef.current) {
            clientRef.current.deactivate();
            clientRef.current = null;
            setConnected(false);
        }
    }, []);

    const subscribe = useCallback((destination: string, callback: (payload: any) => void): StompSubscription | null => {
        if (!clientRef.current?.connected) {
            console.warn('STOMP NOT connected, cannot subscribe to', destination);
            return null;
        }

        return clientRef.current.subscribe(destination, (message: IMessage) => {
            try {
                const payload = JSON.parse(message.body);
                callback(payload);
            } catch (e) {
                console.error('Failed to parse STOMP message body', e);
                callback(message.body);
            }
        });
    }, []);

    const publish = useCallback((destination: string, body: any) => {
        if (!clientRef.current?.connected) {
            console.warn('STOMP NOT connected, cannot publish to', destination);
            return;
        }

        clientRef.current.publish({
            destination,
            body: JSON.stringify(body),
        });
    }, []);

    return { connected, connect, disconnect, subscribe, publish };
};
