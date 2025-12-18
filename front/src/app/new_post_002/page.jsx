"use client";

import React, { useState, useEffect } from "react";

function Page() {
    const [imageUrl, setImageUrl] = useState("");

    const [postData, setPostData] = useState({
        title: "",
        description: "",
        content: "",
        categoryId: "",
    });

    // ========================== JWT로 개인 API Key 가져오기  ==========================
    const getUserApiKey = async () => {
        const accessToken = localStorage.getItem("accessToken");

        if (!accessToken) {
            alert("로그인이 필요합니다.");
            return null;
        }

        try {
            const url = "http://localhost:8080/api/auth/user-info";

            const res = await fetch(url, {
                method: "GET",
                headers: {
                    "Authorization": `Bearer ${accessToken}`,
                },
            });

            if (!res.ok) {
                // 서버가 4xx 또는 5xx 오류를 반환했을 때의 처리
                const errorData = await res.json();
                throw new Error(`API Key 조회 실패: ${errorData.message || res.statusText}`);
            }

            //  API Key 추출 로직
            const userApiKey = res.headers.get("API-KEY");

            if (userApiKey) {
                return userApiKey;
            } else {
                // Body가 아니라 헤더에 API Key가 없는 경우
                throw new Error("서버 응답 헤더에서 'API-KEY' 필드를 찾을 수 없습니다. (API Key가 등록되지 않았을 수 있습니다.)");
            }

        } catch (err) {
            console.error("API Key 조회 중 에러:", err);
            // 최종 사용자에게 표시될 알림
            alert(`API Key를 가져오지 못했습니다. 오류: ${err.message}`);
            return null;
        }
    };

    // ========================== DALL·E 이미지 생성 ==========================
    const imageGenerate = async (initialData = null) => {
        const currentData = initialData || postData;

        if (!currentData || !currentData.title) {
            alert("유효한 게시물 데이터가 없습니다.");
            return;
        }

        const apiKey = await getUserApiKey();
        if (!apiKey) return;

        // 🚨 수정: categoryId 대신 categoryName을 사용하도록 변경
        // categoryName이 없으면 postData에 저장된 categoryId를 임시로 사용
        const categoryPrompt = currentData.categoryName || currentData.categoryId;

        // DALL·E 프롬프트 구성
        const prompt = `제목: ${currentData.title}, 내용 설명: ${currentData.description},  앞의 내용을 기반으로 카테고리의 책 표지 이미지를 생성. 
                                표지 이미지는 ${categoryPrompt}에 맞게. 책하나의 메인 표지로 사용할 수 있는 하나의 이미지를 빈칸없이 생성`;
        

        try {
            setImageUrl("");
            alert("AI 이미지 생성 중입니다. 잠시 기다려주세요...");

            const res = await fetch("https://api.openai.com/v1/images/generations", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    // 사용자 API Key 사용
                    "Authorization": `Bearer ${apiKey}`,
                },
                body: JSON.stringify({
                    model: "dall-e-3",
                    prompt,
                    size: "1024x1024",
                    quality: "hd",
                }),
            });

            const result = await res.json();
            const url = result.data?.[0]?.url;

            if (!url) {
                console.error("OpenAI Error:", result.error);
                alert(`이미지 URL을 받지 못했습니다. OpenAI 오류: ${result.error?.message || '알 수 없는 오류'}`);
                return;
            }

            setImageUrl(url);
            alert("이미지 생성이 완료되었습니다.");

        } catch (err) {
            console.error("네트워크 또는 DALL·E API 오류:", err);
            alert("이미지 생성 중 오류 발생. API Key가 유효한지 확인하세요.");
        }
    }

    // ========================== 이미지 자동 생성 ==========================
    useEffect(() => {
        const fetchPostAndGenerate = async () => {
            const tempPostData = localStorage.getItem("temp_post_data");

            if (tempPostData) {
                const data = JSON.parse(tempPostData);
                setPostData(data); // State는 화면 렌더링을 위해 업데이트 (비동기)

                console.log("임시 데이터 로드 완료. 이미지 생성 시작:", data.title);

                imageGenerate(data);
            } else {
                console.error("게시물 정보를 가져오지 못했습니다.");
                alert("게시물 정보를 찾을 수 없습니다. 작성 페이지로 돌아가 다시 시도해 주세요.");
                // window.close();
            }
        };

        fetchPostAndGenerate();

        // 부모 창 메시지 수신 로직
        const handleMessage = (event) => {
            if (event.data && event.data.imageUrl) {
                setImageUrl(event.data.imageUrl);
            }
        };
        window.addEventListener("message", handleMessage);
        return () => window.removeEventListener("message", handleMessage);
    }, []);


    // ==================== 결정 버튼 ===============

    const handleDecision = () => {
        if (imageUrl && window.opener) {
            // 부모 창(Page.js)에 이미지 URL 전달
            window.opener.postMessage({imageUrl: imageUrl}, "*");

            window.close(); // 메시지 전송 후 창을 닫습니다.
        } else {
            alert("생성된 이미지가 없습니다.");
        }
    }

    // ==================== Style 및 UI 코드 ====================

    const containerStyle = {
        maxWidth: '75%',
        width: '100%',
        minHeight: 'auto',
        margin: '0 auto',
        border: '1px solid black',
        padding: '10px',
        backgroundColor : 'white'
    };

    // 미리보기 이미지 나오는 곳
    const previewImageStyle = {
        border : '1px solid black',
        minHeight: '400px',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
    };

    // 버튼 나누기
    const buttonContainerStyle = {
        display: 'flex',
        justifyContent: 'space-between',
    };

    // 버튼 모양
    const buttonStyle = {
        margin: '10px',
        border: '1px solid black',
        borderRadius: '4px',
        backgroundColor: 'white',
        color: 'black',
        paddingLeft: '10px',
        paddingRight: '10px',
    };

    // ========================== UI ==========================
    return (
        <div style={containerStyle}>
            <div style={previewImageStyle}>
                {imageUrl ? (
                    <img src={imageUrl} style={{ width: "100%", height: "auto" }} alt="AI Generated Cover" />
                ) : (
                    <div style={{ padding: '20px', textAlign: 'center', color: 'black' }}>
                        이미지 생성 중... (잠시만 기다려주세요)
                    </div>
                )}
            </div>

            <div style={buttonContainerStyle}>
                <button style={buttonStyle} onClick={() => imageGenerate()}>
                    재생성
                </button>
                <button style={buttonStyle} onClick={handleDecision} disabled={!imageUrl}>
                    결정
                </button>
            </div>
        </div>
    );

}

export default Page;