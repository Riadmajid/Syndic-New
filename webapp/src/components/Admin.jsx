import { useState, useEffect } from 'react';
import { db } from '../firebase';
import { collection, onSnapshot, doc, updateDoc, getDoc, setDoc } from 'firebase/firestore';

export default function Admin() {
  const [users, setUsers] = useState([]);
  const [appData, setAppData] = useState(null);

  useEffect(() => {
    const unsubUsers = onSnapshot(collection(db, 'users'), (snapshot) => {
      setUsers(snapshot.docs.map(d => d.data()));
    });
    const unsubData = onSnapshot(doc(db, 'syndic_data', 'zaineb4'), (docSnap) => {
      if (docSnap.exists()) setAppData(docSnap.data());
    });
    return () => { unsubUsers(); unsubData(); };
  }, []);

  const approveUser = async (u) => {
    // Approve in users collection
    await updateDoc(doc(db, 'users', u.uid), { approved: true });
    
    // Auto-register to buildings/apartments in syndic_data
    if (appData) {
      let buildings = [...(appData.buildings || [])];
      let apartments = [...(appData.apartments || [])];

      let building = buildings.find(b => b.name.trim().toLowerCase() === u.building.trim().toLowerCase());
      if (!building) {
        building = { id: Date.now(), name: u.building.trim() };
        buildings.push(building);
      }

      let apt = apartments.find(a => a.buildingId === building.id && a.name.trim().toLowerCase() === u.apartment.trim().toLowerCase());
      if (!apt) {
        apt = { id: Date.now() + 1, buildingId: building.id, name: u.apartment.trim(), residentName: u.name, residentPhone: u.phone };
        apartments.push(apt);
      } else {
        apt.residentName = u.name;
        apt.residentPhone = u.phone;
      }

      await updateDoc(doc(db, 'syndic_data', 'zaineb4'), { buildings, apartments });
    }
  };

  const pendingUsers = users.filter(u => !u.approved);

  return (
    <div className="card admin-card">
      <h2>لوحة الإدارة ⚙️</h2>
      
      <h3>طلبات الانضمام المعلقة ({pendingUsers.length})</h3>
      <div className="list-container">
        {pendingUsers.length === 0 ? <p>لا توجد طلبات معلقة.</p> : (
          pendingUsers.map(u => (
            <div key={u.uid} className="list-item">
              <div className="item-details">
                <strong>{u.name}</strong>
                <span>{u.building} - شقة {u.apartment}</span>
              </div>
              <button onClick={() => approveUser(u)} className="submit-btn" style={{marginTop: 0, padding: '8px 16px'}}>قبول</button>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
