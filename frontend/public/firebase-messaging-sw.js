// Copy this file locally and fill in your own Firebase web config if you use FCM.
// Do not commit real API keys or project identifiers unless they are intentionally public and restricted in Firebase.
importScripts('https://www.gstatic.com/firebasejs/10.7.1/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/10.7.1/firebase-messaging-compat.js');

const firebaseConfig = {
    apiKey: 'YOUR_FIREBASE_API_KEY',
    authDomain: 'YOUR_FIREBASE_AUTH_DOMAIN',
    projectId: 'YOUR_FIREBASE_PROJECT_ID',
    storageBucket: 'YOUR_FIREBASE_STORAGE_BUCKET',
    messagingSenderId: 'YOUR_FIREBASE_MESSAGING_SENDER_ID',
    appId: 'YOUR_FIREBASE_APP_ID',
};

if (!firebaseConfig.apiKey.startsWith('YOUR_')) {
    firebase.initializeApp(firebaseConfig);
    const messaging = firebase.messaging();

    messaging.onBackgroundMessage((payload) => {
        const notificationTitle = payload.notification?.title || 'New Notification';
        const notificationOptions = {
            body: payload.notification?.body || '',
            icon: '/icon.png',
            badge: '/badge.png',
        };

        self.registration.showNotification(notificationTitle, notificationOptions);
    });
}
