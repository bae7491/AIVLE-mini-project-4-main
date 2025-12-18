"use client";

import { useEffect, useState } from "react";
import Toast from "./Toast";

export default function ToastContainer() {
  const [toast, setToast] = useState({ show: false });

  useEffect(() => {
    // ============================
    // 1) sessionStorage 토스트 처리
    // ============================
    const saved = sessionStorage.getItem("toast");
    if (saved) {
      const { msg, type } = JSON.parse(saved);
      setToast({ show: true, type, message: msg });

      setTimeout(() => setToast({ show: false }), 3000);

      // 사용 후 제거
      sessionStorage.removeItem("toast");
    }

    // ============================
    // 2) 이벤트 기반 토스트(show-toast)
    // ============================
    const handler = (e) => {
      const { msg, type } = e.detail;
      setToast({ show: true, type, message: msg });

      setTimeout(() => setToast({ show: false }), 3000);
    };

    window.addEventListener("show-toast", handler);
    return () => window.removeEventListener("show-toast", handler);
  }, []);

  return (
    <Toast
      show={toast.show}
      type={toast.type}
      message={toast.message}
    />
  );
}
