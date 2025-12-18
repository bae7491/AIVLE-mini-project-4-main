'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import api from '../api/apiClient';

export default function LogoutPage() {
  const router = useRouter();

  useEffect(() => {
    const doLogout = async () => {
      const token = localStorage.getItem("accessToken");

      // 1) 로그인 안 된 상태에서 /logout 접근한 경우
      if (!token) {

        // 전역 토스트 즉시 출력
        window.dispatchEvent(
          new CustomEvent("show-toast", {
            detail: {
              msg: "로그아웃된 상태입니다.",
              type: "danger",
            },
          })
        );

        // 레이아웃이 유지되므로 토스트가 사라지지 않음
        router.replace("/");
        return;
      }

      // 2) 정상 로그아웃 처리
      try {
        await api.post('/auth/logout');
      } catch (err) {
        console.error("로그아웃 요청 중 오류:", err);
      } finally {
        localStorage.removeItem("accessToken");
        sessionStorage.clear();

        // 전역 토스트 즉시 출력
        window.dispatchEvent(
          new CustomEvent("show-toast", {
            detail: {
              msg: "로그아웃 성공!",
              type: "success",
            },
          })
        );

        router.replace('/');
        router.refresh();
      }
    };

    doLogout();
  }, [router]);

  return (
    <div className="page">
      <div className="card">
        <h2 className="card-title">로그아웃 중...</h2>
        <p>잠시만 기다려주세요.</p>
      </div>
    </div>
  );
}
