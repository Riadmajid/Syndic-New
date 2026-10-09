import { useState, useEffect } from 'react';
import { db } from '../firebase';
import { doc, getDoc, onSnapshot, setDoc, updateDoc } from 'firebase/firestore';

export default function Expenses() {
  const [appData, setAppData] = useState(null);

  useEffect(() => {
    const unsub = onSnapshot(doc(db, 'syndic_data', 'zaineb4'), (docSnap) => {
      if (docSnap.exists()) {
        setAppData(docSnap.data());
      }
    });
    return () => unsub();
  }, []);

  const addExpense = async (e) => {
    e.preventDefault();
    const desc = e.target.desc.value;
    const amount = parseFloat(e.target.amount.value);
    if (!desc || isNaN(amount)) return;
    
    const newExpense = {
      id: Date.now(),
      desc,
      amount,
      date: new Date().toLocaleDateString('en-GB'),
      hasReceipt: false
    };

    const newExpenses = [...(appData?.expenses || []), newExpense];
    await setDoc(doc(db, 'syndic_data', 'zaineb4'), { expenses: newExpenses }, { merge: true });
    e.target.reset();
  };

  const expenses = appData?.expenses || [];
  const totalExpenses = expenses.reduce((sum, e) => sum + e.amount, 0);

  return (
    <div className="card expenses-card">
      <h2>سجل المصاريف 🧾</h2>
      <div className="total-box">
        <strong>إجمالي المصاريف:</strong> {totalExpenses} درهم
      </div>
      
      <form onSubmit={addExpense} className="add-expense-form">
        <input name="desc" type="text" placeholder="وصف المصروف" required />
        <input name="amount" type="number" step="0.01" placeholder="المبلغ (درهم)" required />
        <button type="submit" className="submit-btn">إضافة مصروف</button>
      </form>

      <div className="list-container">
        {expenses.length === 0 ? <p>لا توجد مصاريف حالياً.</p> : (
          expenses.map(exp => (
            <div key={exp.id} className="list-item">
              <div className="item-details">
                <strong>{exp.desc}</strong>
                <span>{exp.date}</span>
              </div>
              <strong className="item-amount">-{exp.amount} درهم</strong>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
