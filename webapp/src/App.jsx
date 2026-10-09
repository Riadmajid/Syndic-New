import { useState, useEffect } from 'react';
import { auth, db } from './firebase.js';
import { signInWithEmailAndPassword, createUserWithEmailAndPassword, onAuthStateChanged, signOut } from 'firebase/auth';
import { doc, setDoc, getDoc } from 'firebase/firestore';
import './index.css';

import Expenses from './components/Expenses';
import Chat from './components/Chat';
import Admin from './components/Admin';

function App() {
  const [user, setUser] = useState(null);
  const [userData, setUserData] = useState(null);
  const [activeTab, setActiveTab] = useState('dashboard'); // dashboard, expenses, chat, admin
  
  // Auth Form State
  const [isRegisterMode, setIsRegisterMode] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [building, setBuilding] = useState('');
  const [apartment, setApartment] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, async (currentUser) => {
      setUser(currentUser);
      if (currentUser) {
        // Fetch user data from Firestore
        const docRef = doc(db, 'users', currentUser.uid);
        const docSnap = await getDoc(docRef);
        if (docSnap.exists()) {
          setUserData(docSnap.data());
        }
      } else {
        setUserData(null);
      }
    });
    return () => unsubscribe();
  }, []);

  const handleAuth = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);
    setSuccess(null);

    try {
      if (isRegisterMode) {
        if (!name || !phone || !building || !apartment) throw new Error("يرجى ملء جميع الحقول.");
        const userCredential = await createUserWithEmailAndPassword(auth, email, password);
        const newUser = userCredential.user;
        
        await setDoc(doc(db, 'users', newUser.uid), {
          uid: newUser.uid,
          email,
          name,
          phone,
          building,
          apartment,
          role: 'resident',
          approved: false
        });
        setSuccess("تم إنشاء الحساب بنجاح! يرجى انتظار الموافقة.");
      } else {
        await signInWithEmailAndPassword(auth, email, password);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleLogout = () => signOut(auth);

  if (user) {
    if (userData && !userData.approved) {
      return (
        <div className="dashboard-container">
          <div className="dashboard-card">
            <h1>في انتظار الموافقة ⏳</h1>
            <p className="welcome-text">مرحباً بك {userData.name}، حسابك قيد المراجعة من قبل الإدارة.</p>
            <button onClick={handleLogout} className="logout-btn" style={{marginTop: '20px'}}>تسجيل الخروج</button>
          </div>
        </div>
      );
    }

    return (
      <div className="app-layout">
        <aside className="sidebar">
          <div className="sidebar-header">
            <div className="logo-small">B</div>
            <h2>Bellouzou</h2>
          </div>
          <nav className="sidebar-nav">
            <button className={activeTab === 'dashboard' ? 'active' : ''} onClick={() => setActiveTab('dashboard')}>🏠 الرئيسية</button>
            <button className={activeTab === 'expenses' ? 'active' : ''} onClick={() => setActiveTab('expenses')}>🧾 المصاريف</button>
            <button className={activeTab === 'chat' ? 'active' : ''} onClick={() => setActiveTab('chat')}>💬 المحادثات</button>
            {userData?.role === 'admin' && (
              <button className={activeTab === 'admin' ? 'active' : ''} onClick={() => setActiveTab('admin')}>⚙️ لوحة الإدارة</button>
            )}
          </nav>
          <div className="sidebar-footer">
            <p className="user-name">{userData?.name || user.email}</p>
            <button onClick={handleLogout} className="logout-text-btn">تسجيل الخروج</button>
          </div>
        </aside>

        <main className="main-content">
          <header className="topbar">
            <h1>{
              activeTab === 'dashboard' ? 'الرئيسية' : 
              activeTab === 'expenses' ? 'المصاريف' : 
              activeTab === 'chat' ? 'تواصل السنديك' : 'لوحة الإدارة'
            }</h1>
          </header>
          
          <div className="content-area">
            {activeTab === 'dashboard' && (
              <div className="card">
                <h2>أهلاً بك، {userData?.name} 👋</h2>
                <p>مرحباً بك في لوحة تحكم Bellouzou عبر الويب. بياناتك متزامنة تماماً مع تطبيق الأندرويد.</p>
                <div className="info-grid">
                  <div className="info-box"><strong>العمارة:</strong> {userData?.building}</div>
                  <div className="info-box"><strong>الشقة:</strong> {userData?.apartment}</div>
                  <div className="info-box"><strong>الصلاحية:</strong> {userData?.role === 'admin' ? 'مدير' : 'مقيم'}</div>
                </div>
              </div>
            )}
            
            {activeTab === 'expenses' && <Expenses />}
            {activeTab === 'chat' && <Chat user={user} userData={userData} />}
            {activeTab === 'admin' && <Admin />}
          </div>
        </main>
      </div>
    );
  }

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="logo-container">
          <div className="logo">B</div>
        </div>
        <h1 className="brand-name">Bellouzou</h1>
        
        <div className="tab-row">
          <button className={`tab ${!isRegisterMode ? 'active' : ''}`} onClick={() => { setIsRegisterMode(false); setError(null); setSuccess(null); }}>
            تسجيل الدخول
          </button>
          <button className={`tab ${isRegisterMode ? 'active' : ''}`} onClick={() => { setIsRegisterMode(true); setError(null); setSuccess(null); }}>
            حساب جديد
          </button>
        </div>

        <form onSubmit={handleAuth} className="auth-form">
          {isRegisterMode && (
            <>
              <input type="text" placeholder="الاسم الكامل" value={name} onChange={e => setName(e.target.value)} required />
              <input type="tel" placeholder="رقم الهاتف" value={phone} onChange={e => setPhone(e.target.value)} required />
              <input type="text" placeholder="العمارة" value={building} onChange={e => setBuilding(e.target.value)} required />
              <input type="text" placeholder="الشقة" value={apartment} onChange={e => setApartment(e.target.value)} required />
            </>
          )}

          <input type="email" placeholder="البريد الإلكتروني" value={email} onChange={e => setEmail(e.target.value)} required />
          <input type="password" placeholder="كلمة المرور" value={password} onChange={e => setPassword(e.target.value)} required />

          {error && <p className="error-message">{error}</p>}
          {success && <p className="success-message">{success}</p>}

          <button type="submit" className="submit-btn" disabled={isLoading}>
            {isLoading ? 'جاري المعالجة...' : (isRegisterMode ? 'إنشاء حساب' : 'تسجيل الدخول')}
          </button>
        </form>
      </div>
    </div>
  );
}

export default App;

