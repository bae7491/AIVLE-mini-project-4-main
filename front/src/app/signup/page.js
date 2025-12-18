'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';

export default function SignupPage() {
  const [id, setId] = useState('');
  const [pw, setPw] = useState('');
  const [pwCheck, setPwCheck] = useState('');
  const [name, setName] = useState('');
  const [apiKey, setApiKey] = useState('');

  // Bootstrap Alert 상태 (실패용)
  const [alertMsg, setAlertMsg] = useState('');
  const [alertType, setAlertType] = useState('');
  const [showAlert, setShowAlert] = useState(false);

  const router = useRouter();

  const showBootstrapAlert = (msg, type = 'danger') => {
    setAlertMsg(msg);
    setAlertType(type);
    setShowAlert(true);

    setTimeout(() => setShowAlert(false), 3000);
  };

  const handleSignup = async (e) => {
    e.preventDefault();

    const trimmedId = id.trim();
    const trimmedName = name.trim();
    const trimmedApiKey = apiKey.trim();

    if (!trimmedId || !pw.trim() || !trimmedName) {
      showBootstrapAlert('아이디, 비밀번호, 이름을 모두 입력해주세요.');
      return;
    }

    if (pw !== pwCheck) {
      showBootstrapAlert('비밀번호가 일치하지 않습니다.');
      return;
    }

    try {
      const headers = { 'Content-Type': 'application/json' };

      if (trimmedApiKey) {
        headers['API-KEY'] = trimmedApiKey;
      }

      const res = await fetch('http://localhost:8080/api/auth/signup', {
        method: 'POST',
        headers,
        body: JSON.stringify({
          id: trimmedId,
          pw,
          name: trimmedName,
        }),
      });

      const result = await res.json();

      // 성공 시: 회원가입 페이지에서는 Alert를 띄우지 않음
      if (res.ok && result.status === 'success') {
        sessionStorage.setItem(
          'signupSuccessMsg',
          result.message || '회원가입 성공!'
        );

        router.push('/login');
        return;
      }

      // 실패 시에만 Alert 표시
      showBootstrapAlert(result.message || '회원가입 실패');

    } catch (error) {
      console.error('회원가입 요청 오류:', error);
      showBootstrapAlert('서버와 통신 중 오류가 발생했습니다.');
    }
  };

  return (
    <div className="page">
      <div className="card">
        <h1 className="card-title">회원가입</h1>

        {/* 실패 메시지용 Bootstrap Alert */}
        {showAlert && (
          <div className={`alert alert-${alertType}`} role="alert">
            {alertMsg}
          </div>
        )}

        <form className="form" onSubmit={handleSignup}>
          <label>
            아이디
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

          <label>
            PW 확인
            <input
              type="password"
              value={pwCheck}
              onChange={(e) => setPwCheck(e.target.value)}
            />
          </label>

          <label>
            이름
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
          </label>

          <label>
            API Key (선택)
            <input
              type="text"
              value={apiKey}
              onChange={(e) => setApiKey(e.target.value)}
              placeholder="API Key를 입력하세요."
            />
          </label>

          <button
            type="submit"
            className="sub-btn"
            style={{ marginTop: '20px', width: '100%' }}
          >
            가입하기
          </button>
        </form>
      </div>
    </div>
  );
}
