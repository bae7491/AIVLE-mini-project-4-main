'use client';

import { useState, useEffect } from 'react';
import { loginRequest } from './login';

export default function LoginPage() {
  const [id, setId] = useState('');
  const [pw, setPw] = useState('');

  // 회원가입 성공 메시지 → 토스트 표시
  useEffect(() => {
    const msg = sessionStorage.getItem('signupSuccessMsg');
    if (msg) {
      window.dispatchEvent(
        new CustomEvent("show-toast", {
          detail: { msg, type: "success" }
        })
      );

      sessionStorage.removeItem('signupSuccessMsg');
    }
  }, []);

  const handleLogin = async (e) => {
    e.preventDefault();

    // 입력값 검증
    if (!id.trim() || !pw.trim()) {
      window.dispatchEvent(
        new CustomEvent("show-toast", {
          detail: { msg: "ID와 비밀번호를 모두 입력해주세요.", type: "danger" }
        })
      );
      return;
    }

    try {
      const { result, accessToken } = await loginRequest(id, pw);

      // 로그인 성공
      if (result.status === 'success') {
        if (accessToken) {
          localStorage.setItem('accessToken', accessToken);
        }

        // 메인 페이지에서 토스트를 보이게 하기 위해 sessionStorage에 저장
        sessionStorage.setItem(
          "toast",
          JSON.stringify({ msg: "로그인 성공!", type: "success" })
        );

        window.location.href = '/';
        return;
      }

      // 로그인 실패 → 즉시 토스트 표시
      window.dispatchEvent(
        new CustomEvent("show-toast", {
          detail: { msg: result.message || "로그인 실패", type: "danger" }
        })
      );

    } catch (error) {
      console.error(error);

      if (error.response) {
        const { status, data } = error.response;

        window.dispatchEvent(
          new CustomEvent("show-toast", {
            detail: {
              msg: data?.message || `오류 발생 (코드: ${status})`,
              type: "danger"
            }
          })
        );

        return;
      }

      window.dispatchEvent(
        new CustomEvent("show-toast", {
          detail: {
            msg: "서버와 통신 중 오류가 발생했습니다.",
            type: "danger"
          }
        })
      );
    }
  };

  return (
    <div className="page">
      <div className="card">
        <h2 className="card-title">로그인</h2>

        <form className="form" onSubmit={handleLogin}>
          <label>
            ID
            <input
              type="text"
              value={id}
              onChange={(e) => setId(e.target.value)}
            />
          </label>

          <label>
            PW
            <input
              type="password"
              value={pw}
              onChange={(e) => setPw(e.target.value)}
            />
          </label>

          <div className="btn-row--center">
            <button className="sub-btn" type="submit">
              로그인
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
