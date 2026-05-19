import { initializeApp } from 'firebase/app';
import { getMessaging, getToken, onMessage } from 'firebase/messaging';

const firebaseConfig = {
    apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
    authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
    projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
    storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,
    messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
    appId: import.meta.env.VITE_FIREBASE_APP_ID,
};

const isFirebaseConfigured = Object.values(firebaseConfig).every(Boolean);
const app = isFirebaseConfigured ? initializeApp(firebaseConfig) : null;
let messaging: ReturnType<typeof getMessaging> | null = null;

if (app && typeof window !== 'undefined' && 'serviceWorker' in navigator) {
    try {
        messaging = getMessaging(app);
    } catch (error) {
        console.warn('Firebase Messaging initialization failed:', error);
    }
}

export const requestFCMToken = async (): Promise<string | null> => {
    if (!messaging) {
        console.warn('Firebase Messaging is not configured or unavailable.');
        return null;
    }

    if (!('Notification' in window)) {
        console.warn('This browser does not support notifications.');
        return null;
    }

    try {
        const permission = await Notification.requestPermission();
        if (permission !== 'granted') {
            console.warn('Notification permission denied.');
            return null;
        }

        const vapidKey = import.meta.env.VITE_FIREBASE_VAPID_KEY;
        if (!vapidKey) {
            console.warn('VITE_FIREBASE_VAPID_KEY is not configured.');
            return null;
        }

        return await getToken(messaging, { vapidKey });
    } catch (error) {
        console.error('Error getting FCM token:', error);
        return null;
    }
};

export const onMessageListener = () =>
    new Promise((resolve, reject) => {
        if (!messaging) {
            reject(new Error('Firebase Messaging is not configured or unavailable.'));
            return;
        }

        onMessage(messaging, (payload) => {
            resolve(payload);
        });
    });

export { messaging };
