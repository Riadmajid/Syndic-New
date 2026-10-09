import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';
import { getFirestore } from 'firebase/firestore';

const firebaseConfig = {
  apiKey: "AIzaSyArFiSUvAMx5fCi0ks6szW3wj6bPcwd6SU",
  authDomain: "syndic-54327.firebaseapp.com",
  projectId: "syndic-54327",
  storageBucket: "syndic-54327.firebasestorage.app",
  messagingSenderId: "221199418649",
  appId: "1:221199418649:web:temp_web_app_id_if_needed"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getFirestore(app);
