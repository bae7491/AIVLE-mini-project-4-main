'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import useMyInfo from './useMyInfo';

export default function MyInfoPage() {
  const router = useRouter();

  // 페이지 접근 시 AccessToken 검사
  useEffect(() => {
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
      // 전역 토스트 사용
      window.dispatchEvent(
        new CustomEvent("show-toast", {
          detail: {
            msg: "로그아웃된 상태입니다.",
            type: "danger",
          },
        })
      );

      router.replace('/');
      return;
    }

  }, [router]);

  const {
    userId,
    userName,
    originalName,
    apiKey,
    pw,
    pwCheck,
    setUserName,
    setApiKey,
    setPw,
    setPwCheck,
    handleUpdate
  } = useMyInfo();
  
  const showToastMsg = (msg, type = 'danger') => {
    window.dispatchEvent(
      new CustomEvent("show-toast", {
        detail: { msg, type },
      })
    );
  };

  const onSubmit = async (e) => {
    e.preventDefault();

    if (!userName || userName.trim() === '') {
      showToastMsg('이름을 입력해주세요.', 'danger');
      return;
    }

    const ok = await handleUpdate();
    if (ok) {
      showToastMsg("회원정보가 성공적으로 수정되었습니다.", "success");
    } else {
      showToastMsg("회원정보 수정에 실패했습니다.", "danger");
    }
  };

  return (
    <div className="page">
      <div className="card">
        <h2 className="card-title">회원정보 수정</h2>

        <form className="form" onSubmit={onSubmit}>
          <label>
            아이디
            <input type="text" value={userId} readOnly style={{ backgroundColor: '#f3f3f3' }} />
          </label>

          <label>
            이름
            <input type="text" value={userName} onChange={(e) => setUserName(e.target.value)} />
          </label>

          <label>
            API Key
            <input type="text" value={apiKey} onChange={(e) => setApiKey(e.target.value)} />
          </label>

          <label>
            변경할 PW
            <input type="password" value={pw} onChange={(e) => setPw(e.target.value)} />
          </label>

          <label>
            PW 확인
            <input type="password" value={pwCheck} onChange={(e) => setPwCheck(e.target.value)} />
          </label>

          <div className="btn-row--center">
            <button className="sub-btn" type="submit">수정하기</button>
          </div>
        </form>
      </div>
    </div>
  );
}
