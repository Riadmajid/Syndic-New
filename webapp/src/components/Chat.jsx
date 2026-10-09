import { useState, useEffect } from 'react';
import { db } from '../firebase';
import { collection, onSnapshot, query, orderBy, setDoc, doc } from 'firebase/firestore';

export default function Chat({ user, userData }) {
  const [messages, setMessages] = useState([]);

  useEffect(() => {
    const q = query(collection(db, 'syndic_chat'), orderBy('id', 'asc'));
    const unsub = onSnapshot(q, (snapshot) => {
      const msgs = snapshot.docs.map(doc => ({ firebaseId: doc.id, ...doc.data() }));
      setMessages(msgs);
    });
    return () => unsub();
  }, []);

  const sendMessage = async (e) => {
    e.preventDefault();
    const msgText = e.target.msg.value;
    if (!msgText) return;
    
    const id = Date.now();
    const newMsg = {
      id,
      name: userData?.name || 'مجهول',
      msg: msgText,
      time: new Date().toLocaleTimeString('ar-MA', { hour: '2-digit', minute: '2-digit' }),
      likes: 0,
      dislikes: 0,
      replies: [],
      isPrivate: false,
      senderUid: user.uid
    };

    await setDoc(doc(db, 'syndic_chat', id.toString()), newMsg);
    e.target.reset();
  };

  return (
    <div className="card chat-card">
      <h2>المحادثات والإعلانات 💬</h2>
      
      <div className="chat-container">
        {messages.length === 0 ? <p>لا توجد رسائل حالياً.</p> : (
          messages.map(m => (
            <div key={m.id} className={`chat-message ${m.senderUid === user.uid ? 'my-message' : ''}`}>
              <div className="msg-header">
                <strong>{m.name}</strong>
                <span className="msg-time">{m.time}</span>
              </div>
              <p className="msg-body">{m.msg}</p>
            </div>
          ))
        )}
      </div>

      <form onSubmit={sendMessage} className="chat-form">
        <input name="msg" type="text" placeholder="اكتب رسالتك هنا..." required />
        <button type="submit" className="submit-btn">إرسال</button>
      </form>
    </div>
  );
}
