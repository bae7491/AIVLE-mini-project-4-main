'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';

import { fetchUserInfo, updateUserInfo, deleteUser } from './myInfoApi';

export default function useMyInfo() {
  const [userId, setUserId] = useState('');
  const [userName, setUserName] = useState('');
  const [originalName, setOriginalName] = useState('');
  const [apiKey, setApiKey] = useState('');
  const [pw, setPw] = useState('');
  const [pwCheck, setPwCheck] = useState('');

  const router = useRouter();

  // ==========================
  // 1) 유저 정보 로딩
  // ==========================
  useEffect(() => {
    const load = async () => {
      try {
        const res = await fetchUserInfo();

        setUserId(res.data.id);
        setUserName(res.data.name);
        setOriginalName(res.data.name);

        const headerApiKey = res.headers['api-key'];
        if (headerApiKey) setApiKey(headerApiKey);

      } catch (err) {
        console.error(err);
        if (err.response?.status === 401) {
          alert('로그인이 만료되었습니다.');
          router.push('/login');
        }
      }
    };

    load();
  }, [router]);

  // ==========================
  // 2) 회원정보 수정
  // ==========================
  const handleUpdate = async () => {
    const trimmedName = userName.trim();
    const trimmedPw = pw.trim();
    const trimmedPwCheck = pwCheck.trim();

    if (!trimmedName && !trimmedPw && !trimmedPwCheck && !apiKey.trim()) {
      alert('변경할 내용을 입력해주세요.');
      return false;
    }

    if (trimmedPw || trimmedPwCheck) {
      if (trimmedPw !== trimmedPwCheck) {
        alert('비밀번호가 일치하지 않습니다.');
        return false;
      }
    }

    const body = {};
    if (trimmedName) body.name = trimmedName;
    if (trimmedPw) body.pw = trimmedPw;

    const res = await updateUserInfo(body, apiKey);

    if (res.data.status === 'success') {
      if (body.name) {
        sessionStorage.setItem('userName', body.name);
        setOriginalName(body.name);
      }
      setPw('');
      setPwCheck('');
      return true;
    }

    return false;
  };

  return {
    userId,
    userName,
    apiKey,
    pw,
    pwCheck,
    setUserName,
    setApiKey,
    setPw,
    setPwCheck,
    handleUpdate,
  };
}
