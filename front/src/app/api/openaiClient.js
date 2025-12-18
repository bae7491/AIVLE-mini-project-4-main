// app/api/openaiClient.js

// 사용자 API Key 조회 (JWT → 개인 API Key)
export async function fetchUserApiKey() {
    const accessToken = localStorage.getItem("accessToken");
    if (!accessToken) return null;

    try {
        const res = await fetch("http://localhost:8080/api/auth/user-info", {
            method: "GET",
            headers: { Authorization: `Bearer ${accessToken}` }
        });

        if (!res.ok) {
            console.error("API Key 조회 실패:", res.status);
            return null;
        }

        const apiKey = res.headers.get("API-KEY");
        if (!apiKey) {
            alert("⚠️ 등록된 API Key가 없습니다.");
            return null;
        }

        return apiKey;
    } catch (err) {
        console.error("API KEY 조회 오류:", err);
        return null;
    }
}


// --------------------------------------------------------
// OpenAI 이미지 생성 전용 클라이언트 함수
// --------------------------------------------------------
export async function generateCoverImage(postData) {
    const apiKey = await fetchUserApiKey();
    if (!apiKey) return null;

    // Prompt 생성
    const categoryPrompt = postData.categoryName || postData.categoryId || "기본 카테고리";

    const prompt =
        `제목: ${postData.title}\n` +
        `설명: ${postData.description}\n` +
        `위 내용을 기반으로 ${categoryPrompt} 카테고리에 어울리는 단일 책 표지 이미지를 생성.\n` +
        `빈 공간 없이 깔끔한 하드커버 스타일로 표현.`;

    try {
        const response = await fetch("https://api.openai.com/v1/images/generations", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${apiKey}`,
            },
            body: JSON.stringify({
                model: "dall-e-3",
                prompt,
                size: "1024x1792"
            }),
        });

        const result = await response.json();

        if (result.error) {
            console.error("OpenAI Error:", result.error);
            alert("이미지 생성 실패: " + result.error.message);
            return null;
        }

        return result.data?.[0]?.url || null;

    } catch (err) {
        console.error("이미지 생성 요청 실패:", err);
        alert("이미지 생성 요청 중 오류가 발생했습니다.");
        return null;
    }
}
