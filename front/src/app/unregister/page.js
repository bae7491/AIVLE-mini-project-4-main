'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import api from '@/app/api/apiClient';
import Toast from '@/app/components/Toast';
import ConfirmModal from '@/app/components/ConfirmModal';

export default function UnregisterPage() {
  const [pw, setPw] = useState('');
  const router = useRouter();

  // 페이지 내부에서만 쓰는 Toast (비밀번호 미입력 등)
  const [toastMsg, setToastMsg] = useState('');
  const [toastType, setToastType] = useState('danger');
  const [showToast, setShowToast] = useState(false);

  const showToastBox = (msg, type = 'danger') => {
    setToastMsg(msg);
    setToastType(type);
    setShowToast(true);
    setTimeout(() => setShowToast(false), 3000);
  };

  // Confirm 모달 상태
  const [showConfirm, setShowConfirm] = useState(false);
  const [confirmResolver, setConfirmResolver] = useState(null);

  const showConfirmModal = () =>
    new Promise((resolve) => {
      setShowConfirm(true);
      setConfirmResolver(() => resolve);
    });

  const handleConfirm = () => {
    if (confirmResolver) confirmResolver(true);
    setShowConfirm(false);
  };

  const handleCancel = () => {
    if (confirmResolver) confirmResolver(false);
    setShowConfirm(false);
  };

  // 페이지 접근 시 AccessToken 확인
  useEffect(() => {
    const token = localStorage.getItem("accessToken");

    if (!token) {
      // 전역 ToastContainer에 바로 이벤트 쏘기
      window.dispatchEvent(
        new CustomEvent("show-toast", {
          detail: {
            msg: "로그인이 필요한 페이지입니다.",
            type: "danger",
          },
        })
      );

      // 화면만 메인으로 교체 (레이아웃/토스트는 그대로 유지)
      router.replace("/");
    }
  }, [router]);

  // 회원탈퇴 처리
  const handleUnregister = async (e) => {
    e.preventDefault();

    const trimmedPw = pw.trim();
    if (!trimmedPw) {
      showToastBox('⚠️ 비밀번호를 입력해주세요.');
      return;
    }

    const ok = await showConfirmModal();
    if (!ok) return;

    try {
      const res = await api.post('/auth/delete', { pw: trimmedPw });

      if (res.data.status === 'success') {
        // 전역 토스트로 성공 메시지
        window.dispatchEvent(
          new CustomEvent("show-toast", {
            detail: {
              msg: "회원탈퇴가 완료되었습니다.",
              type: "success",
            },
          })
        );

        // 토큰 및 기타 클라이언트 상태 제거
        localStorage.clear();
        sessionStorage.clear();

        // 메인 화면으로 이동 (토스트는 계속 보임)
        router.push('/');
        return;
      }

      showToastBox(res.data.message ?? '회원탈퇴 실패');

    } catch (error) {
      console.error(error);
      showToastBox('서버와 통신 중 오류가 발생했습니다.');
    }
  };

  return (
    <div className="page">

      {/* 이 페이지 안에서만 쓰는 Toast UI */}
      <div style={{ position: 'fixed', top: '20px', right: '20px', zIndex: 9999 }}>
        <Toast show={showToast} type={toastType} message={toastMsg} />
      </div>

      <ConfirmModal
        show={showConfirm}
        title="⚠️ 회원탈퇴 확인"
        message="정말로 회원탈퇴 하시겠습니까? 되돌릴 수 없습니다."
        onConfirm={handleConfirm}
        onCancel={handleCancel}
      />

      <div className="card">
        <h1 className="card-title" style={{ color: 'red' }}>회원탈퇴</h1>

        <form className="form" onSubmit={handleUnregister}>
          <label>
            비밀번호 입력
            <input
              type="password"
              value={pw}
              onChange={(e) => setPw(e.target.value)}
              placeholder="비밀번호를 입력하세요"
            />
          </label>

          <button
            type="submit"
            className="sub-btn"
            style={{
              marginTop: '20px',
              width: '100%',
              color: 'white',
              backgroundColor: 'red',
            }}
          >
            회원탈퇴
          </button>
        </form>
      </div>
    </div>
  );
}
